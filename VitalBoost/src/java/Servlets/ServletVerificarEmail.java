package Servlets;

import Controlador.UsuarioDAO;
import Controlador.ContactoEmergenciaDAO;
import Controlador.RegistroPendienteDAO;
import Correo.EnvioCorreo;
import Modelo.Usuario;
import Modelo.ContactoEmergencia;
import Modelo.RegistroPendiente;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ==========================================================================
 * VERIFICACIÓN DE CORREO
 *
 * GET  /VerificarEmail?token=...   -> valida el enlace que llegó al correo.
 *      Si el token es válido y no ha vencido, es AQUÍ donde recién se crea
 *      la cuenta definitiva (Usuario + ContactoEmergencia) a partir de los
 *      datos guardados en Registros_pendientes. Antes de este punto la
 *      cuenta no existe: no hay fila en Usuarios ni QR de emergencia activo.
 *
 * POST /VerificarEmail (accion=Reenviar, txtEmail=...) -> genera un token
 *      nuevo (24h) y reenvía el correo, para cuando el anterior venció o
 *      nunca llegó. Se usa desde login.jsp y desde la propia vista de
 *      verificación cuando el enlace ya expiró.
 * ==========================================================================
 */
@WebServlet(name = "ServletVerificarEmail", urlPatterns = {"/VerificarEmail"})
public class ServletVerificarEmail extends HttpServlet {

    private final UsuarioDAO udao = new UsuarioDAO();
    private final ContactoEmergenciaDAO cdao = new ContactoEmergenciaDAO();
    private final RegistroPendienteDAO pdao = new RegistroPendienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            request.setAttribute("estado", "invalido");
            request.getRequestDispatcher("/vistas/verificarEmail.jsp").forward(request, response);
            return;
        }

        RegistroPendiente rp = pdao.buscarPorToken(token);

        if (rp == null) {
            // El token no existe: o ya se usó (la cuenta ya se creó) o es inválido.
            request.setAttribute("estado", "invalido");

        } else if (rp.getToken_expira() != null
                && rp.getToken_expira().before(new java.sql.Timestamp(System.currentTimeMillis()))) {

            request.setAttribute("estado", "expirado");
            request.setAttribute("emailExpirado", rp.getEmail());

        } else {

            // ==================================================
            // CREAR LA CUENTA DEFINITIVA
            // ==================================================
            Usuario u = new Usuario();
            u.setNombre_completo(rp.getNombre_completo());
            u.setEmail(rp.getEmail());
            // rp.getPassword() ya viene hasheado con BCrypt desde
            // ServletRegistro (nunca se guardó en texto plano).
            u.setPassword(rp.getPassword());
            u.setFecha_nacimiento(rp.getFecha_nacimiento());
            u.setAlergias_conocidas(rp.getAlergias_conocidas());
            u.setEps(rp.getEps());
            u.setMedicamentos_actuales(rp.getMedicamentos_actuales());
            u.setNumero_documento(rp.getNumero_documento());
            u.setId_tipo_documento(rp.getId_tipo_documento());
            u.setId_tipo_sangre(rp.getId_tipo_sangre());
            u.setId_rol(rp.getId_rol());

            // insertar() (no insertarConVerificacion) porque en este punto
            // el correo YA se está confirmando: la cuenta nace verificada.
            int creado = udao.insertar(u);

            if (creado > 0) {

                // Se busca el usuario recién creado por su documento (único)
                // para obtener su id_usuario, ya que el INSERT no retorna
                // la llave generada.
                Usuario pacienteCreado = udao.buscarPorDocumento(rp.getNumero_documento());

                if (pacienteCreado != null) {
                    ContactoEmergencia contacto = new ContactoEmergencia();
                    contacto.setId_usuario(pacienteCreado.getId_usuario());
                    contacto.setNombre_contacto(rp.getNombre_contacto());
                    contacto.setParentesco(rp.getParentesco_contacto());
                    contacto.setTelefono(rp.getTelefono_contacto());
                    cdao.agregar(contacto);
                }

                // El registro pendiente ya cumplió su función.
                pdao.eliminar(rp.getId_pendiente());

                // ==================================================
                // INICIAR SESIÓN AUTOMÁTICAMENTE Y LLEVAR AL PANEL
                //
                // El paciente ya demostró que el correo es suyo al hacer
                // clic en el enlace, así que no tiene sentido pedirle que
                // además inicie sesión a mano: se le abre la sesión de una
                // vez y se le manda directo a su panel (que ya muestra su
                // QR de emergencia, ver ServletDashboardPaciente).
                // ==================================================
                if (pacienteCreado != null) {
                    HttpSession session = request.getSession();
                    session.setAttribute("usuarioLogueado", pacienteCreado);
                    response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                    return;
                }

                request.setAttribute("estado", "exito");
                request.setAttribute("nombrePaciente", rp.getNombre_completo());

            } else {
                request.setAttribute("estado", "invalido");
                request.setAttribute("errorReenvio",
                        "No pudimos activar tu cuenta. Intenta registrarte de nuevo.");
            }
        }

        request.getRequestDispatcher("/vistas/verificarEmail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if (!"Reenviar".equalsIgnoreCase(accion)) {
            response.sendRedirect(request.getContextPath() + "/VerificarEmail");
            return;
        }

        String email = request.getParameter("txtEmail");

        if (email == null || email.isBlank()) {
            request.setAttribute("estado", "reenvioError");
            request.setAttribute("errorReenvio", "Ingresa tu correo para reenviar la verificación.");
            request.getRequestDispatcher("/vistas/verificarEmail.jsp").forward(request, response);
            return;
        }

        RegistroPendiente rp = pdao.buscarPorEmail(email);

        if (rp == null) {

            Usuario existente = udao.buscarPorEmail(email);

            if (existente != null && existente.isEmailVerificado()) {
                request.setAttribute("estado", "reenvioError");
                request.setAttribute("errorReenvio", "Ese correo ya está verificado, puedes iniciar sesión.");
            } else {
                request.setAttribute("estado", "reenvioError");
                request.setAttribute("errorReenvio", "No existe ningún registro pendiente con ese correo.");
            }

        } else {
            String nuevoToken = UUID.randomUUID().toString().replace("-", "");
            pdao.actualizarToken(rp.getId_pendiente(), nuevoToken);

            String urlBase = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath();
            String urlVerificacion = urlBase + "/VerificarEmail?token=" + nuevoToken;

            boolean enviado = false;
            try {
                enviado = EnvioCorreo.enviarVerificacion(rp.getEmail(), rp.getNombre_completo(), urlVerificacion);
            } catch (Exception e) {
                e.printStackTrace();
            }

            request.setAttribute("estado", enviado ? "reenvioExito" : "reenvioError");
            if (!enviado) {
                request.setAttribute("errorReenvio",
                        "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
            }
        }

        request.getRequestDispatcher("/vistas/verificarEmail.jsp").forward(request, response);
    }
}
