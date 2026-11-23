package Servlets;

import Controlador.UsuarioDAO;
import Correo.EnvioCorreo;
import Modelo.Usuario;
import Utilidades.PasswordUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ==========================================================================
 * RECUPERACIÓN DE CONTRASEÑA
 *
 * GET  /RecuperarPassword                 -> muestra el formulario para
 *      pedir el enlace (vistas/olvidePassword.jsp).
 *
 * GET  /RecuperarPassword?token=...       -> valida el enlace que llegó al
 *      correo y, si es válido y no ha vencido, muestra el formulario para
 *      elegir la contraseña nueva (vistas/restablecerPassword.jsp).
 *
 * POST /RecuperarPassword (accion=EnviarEnlace, txtEmail=...) -> genera un
 *      token (1 hora) y envía el correo con el botón "Restablecer
 *      contraseña".
 *
 * POST /RecuperarPassword (accion=Restablecer, token=..., txtPassword=...,
 *      txtConfirmarPassword=...) -> valida el token y la contraseña nueva,
 *      la guarda, consume el token y redirige al login.
 * ==========================================================================
 */
@WebServlet(name = "ServletRecuperarPassword", urlPatterns = {"/RecuperarPassword"})
public class ServletRecuperarPassword extends HttpServlet {

    // Misma política de contraseña que usa el registro (ServletRegistro),
    // para que una contraseña "restablecida" nunca sea más débil que una
    // recién creada.
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 20;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{" + PASSWORD_MIN + "," + PASSWORD_MAX + "}$");

    private final UsuarioDAO udao = new UsuarioDAO();

    // ======================================================================
    // GET
    // ======================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            request.getRequestDispatcher("/vistas/olvidePassword.jsp").forward(request, response);
            return;
        }

        Usuario u = udao.buscarPorTokenRecuperacion(token);

        if (u == null) {
            request.setAttribute("estado", "invalido");

        } else if (u.getToken_recuperacion_expira() != null
                && u.getToken_recuperacion_expira().before(new java.sql.Timestamp(System.currentTimeMillis()))) {

            request.setAttribute("estado", "expirado");

        } else {
            request.setAttribute("estado", "formulario");
            request.setAttribute("token", token);
        }

        request.getRequestDispatcher("/vistas/restablecerPassword.jsp").forward(request, response);
    }

    // ======================================================================
    // POST
    // ======================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("Restablecer".equalsIgnoreCase(accion)) {
            restablecer(request, response);
        } else {
            enviarEnlace(request, response);
        }
    }

    // ======================================================================
    // PASO 1: PEDIR EL ENLACE POR CORREO
    // ======================================================================
    private void enviarEnlace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("txtEmail");

        if (email == null || email.isBlank()) {
            request.setAttribute("errorEmail", "Ingresa tu correo electrónico.");
            request.getRequestDispatcher("/vistas/olvidePassword.jsp").forward(request, response);
            return;
        }

        Usuario u = udao.buscarPorEmail(email);

        // No se distingue en pantalla si el correo existe o no: así nadie
        // puede usar este formulario para descubrir qué correos están
        // registrados en VitalBoost. El envío real (o no envío) queda solo
        // en los logs del servidor.
        if (u != null && u.getActivo() == 1) {

            String token = UUID.randomUUID().toString().replace("-", "");
            boolean generado = udao.generarTokenRecuperacion(u.getId_usuario(), token);

            if (generado) {
                String urlBase = request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort() + request.getContextPath();
                String urlRecuperacion = urlBase + "/RecuperarPassword?token=" + token;

                try {
                    EnvioCorreo.enviarRecuperacion(u.getEmail(), u.getNombre_completo(), urlRecuperacion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        request.setAttribute("enlaceEnviado", true);
        request.getRequestDispatcher("/vistas/olvidePassword.jsp").forward(request, response);
    }

    // ======================================================================
    // PASO 2: GUARDAR LA CONTRASEÑA NUEVA
    // ======================================================================
    private void restablecer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String password = request.getParameter("txtPassword");
        String confirmar = request.getParameter("txtConfirmarPassword");

        Usuario u = (token == null || token.isBlank()) ? null : udao.buscarPorTokenRecuperacion(token);

        if (u == null) {
            request.setAttribute("estado", "invalido");
            request.getRequestDispatcher("/vistas/restablecerPassword.jsp").forward(request, response);
            return;
        }

        if (u.getToken_recuperacion_expira() != null
                && u.getToken_recuperacion_expira().before(new java.sql.Timestamp(System.currentTimeMillis()))) {

            request.setAttribute("estado", "expirado");
            request.getRequestDispatcher("/vistas/restablecerPassword.jsp").forward(request, response);
            return;
        }

        boolean hayError = false;

        if (password == null || password.isBlank()) {
            request.setAttribute("errorPassword", "La contraseña es obligatoria.");
            hayError = true;
        } else if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            request.setAttribute("errorPassword",
                    "La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres.");
            hayError = true;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            request.setAttribute("errorPassword",
                    "La contraseña debe incluir mayúscula, minúscula, número y carácter especial.");
            hayError = true;
        } else if (!password.equals(confirmar)) {
            request.setAttribute("errorConfirmar", "Las contraseñas no coinciden.");
            hayError = true;
        }

        if (hayError) {
            request.setAttribute("estado", "formulario");
            request.setAttribute("token", token);
            request.getRequestDispatcher("/vistas/restablecerPassword.jsp").forward(request, response);
            return;
        }

        boolean guardado = udao.actualizarPasswordConToken(u.getId_usuario(), PasswordUtil.hash(password));

        if (!guardado) {
            request.setAttribute("estado", "invalido");
            request.setAttribute("errorReenvio", "No pudimos actualizar tu contraseña. Intenta de nuevo.");
            request.getRequestDispatcher("/vistas/restablecerPassword.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/ServletLogin?restablecida=1");
    }
}
