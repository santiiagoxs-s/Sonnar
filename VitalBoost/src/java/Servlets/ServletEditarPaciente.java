package Servlets;

import Controlador.UsuarioDAO;
import Controlador.ContactoEmergenciaDAO;
import Modelo.Usuario;
import Modelo.ContactoEmergencia;
import Utilidades.PasswordUtil;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// ==================================================================
// EDICIÓN DE DATOS DEL PACIENTE (DESDE SU PROPIO PANEL)
//
// Permite editar los datos del usuario que tiene la sesión abierta:
// nombre, correo, fecha de nacimiento, tipo de sangre, alergias y su
// contacto de emergencia principal. El número de documento y el rol
// NO se tocan aquí.
//
// También permite cambiar la contraseña desde el mismo formulario,
// pero solo si el paciente confirma su contraseña actual (misma
// política de complejidad usada en el registro).
// ==================================================================
@WebServlet(name = "ServletEditarPaciente", urlPatterns = {"/EditarPaciente"})
public class ServletEditarPaciente extends HttpServlet {

    private final UsuarioDAO udao = new UsuarioDAO();
    private final ContactoEmergenciaDAO cdao = new ContactoEmergenciaDAO();

    // Misma política de contraseña que ServletRegistro: 8-20 caracteres,
    // con mayúscula, minúscula, número y carácter especial.
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 20;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{" + PASSWORD_MIN + "," + PASSWORD_MAX + "}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Usuario sesionUsuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (sesionUsuario == null) {
            response.sendRedirect(request.getContextPath() + "/ServletLogin");
            return;
        }

        String nombre = request.getParameter("txtNombre");
        String email = request.getParameter("txtEmail");
        String fecha = request.getParameter("txtFecha");
        String idSangre = request.getParameter("txtIdSangre");
        String alergias = request.getParameter("txtAlergias");
        String eps = request.getParameter("txtEps");
        String medicamentos = request.getParameter("txtMedicamentos");
        String nombreContacto = request.getParameter("txtNombreContacto");
        String parentescoContacto = request.getParameter("txtParentescoContacto");
        String telefonoContacto = request.getParameter("txtTelefonoContacto");

        String passwordActual = request.getParameter("txtPasswordActual");
        String passwordNueva = request.getParameter("txtPasswordNueva");
        String passwordConfirmar = request.getParameter("txtPasswordConfirmar");

        if (nombre == null || nombre.isBlank()
                || email == null || email.isBlank()
                || fecha == null || fecha.isBlank()
                || idSangre == null || idSangre.isBlank()) {

            session.setAttribute("flashError", "Completa nombre, correo, fecha de nacimiento y tipo de sangre.");
            response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
            return;
        }

        // Se parte del registro completo en BD para no perder datos que
        // este formulario no edita (número de documento, rol...) y para
        // tener la contraseña real y actual del paciente, no la de sesión.
        Usuario paciente = udao.buscarPorId(sesionUsuario.getId_usuario());
        if (paciente == null) {
            paciente = sesionUsuario;
        }

        // -------- Cambio de contraseña (opcional) --------
        // Solo se procesa si el paciente diligenció alguno de los 3 campos.
        // Si escribe la contraseña actual mal, o la nueva no cumple la
        // política, se cancela TODO el guardado (no solo el de la clave)
        // para que el error quede claro y no haya cambios a medias.
        boolean quiereCambiarPassword =
                (passwordActual != null && !passwordActual.isBlank())
                || (passwordNueva != null && !passwordNueva.isBlank())
                || (passwordConfirmar != null && !passwordConfirmar.isBlank());

        if (quiereCambiarPassword) {

            if (passwordActual == null || passwordActual.isBlank()) {
                session.setAttribute("flashError", "Ingresa tu contraseña actual para poder cambiarla.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            if (!PasswordUtil.verificar(passwordActual, paciente.getPassword())) {
                session.setAttribute("flashError", "La contraseña actual ingresada no es correcta.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            if (passwordNueva == null || passwordNueva.isBlank()) {
                session.setAttribute("flashError", "Ingresa la nueva contraseña.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            if (!passwordNueva.equals(passwordConfirmar)) {
                session.setAttribute("flashError", "La confirmación no coincide con la nueva contraseña.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            if (!PASSWORD_PATTERN.matcher(passwordNueva).matches()) {
                session.setAttribute("flashError",
                        "La nueva contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX
                        + " caracteres, con mayúscula, minúscula, número y carácter especial.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            if (passwordActual.equals(passwordNueva)) {
                session.setAttribute("flashError", "La nueva contraseña debe ser diferente a la actual.");
                response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
                return;
            }

            paciente.setPassword(PasswordUtil.hash(passwordNueva));
        }

        paciente.setNombre_completo(nombre.trim());
        paciente.setEmail(email.trim());
        paciente.setFecha_nacimiento(fecha.trim());
        paciente.setAlergias_conocidas(alergias != null ? alergias.trim() : "");
        paciente.setEps(eps != null ? eps.trim() : "");
        paciente.setMedicamentos_actuales(medicamentos != null ? medicamentos.trim() : "");

        try {
            paciente.setId_tipo_sangre(Integer.parseInt(idSangre));
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Selecciona un tipo de sangre válido.");
            response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
            return;
        }

        boolean actualizado = udao.actualizar(paciente);

        // -------- Contacto de emergencia (actualiza el primero o lo crea) --------
        if (nombreContacto != null && !nombreContacto.isBlank()
                && telefonoContacto != null && !telefonoContacto.isBlank()) {

            List<ContactoEmergencia> contactos = cdao.listarPorUsuario(paciente.getId_usuario());

            ContactoEmergencia contacto = !contactos.isEmpty() ? contactos.get(0) : new ContactoEmergencia();
            contacto.setId_usuario(paciente.getId_usuario());
            contacto.setNombre_contacto(nombreContacto.trim());
            contacto.setParentesco(parentescoContacto != null ? parentescoContacto.trim() : "");
            contacto.setTelefono(telefonoContacto.trim());

            if (!contactos.isEmpty()) {
                cdao.actualizar(contacto);
            } else {
                cdao.agregar(contacto);
            }
        }

        if (actualizado) {
            session.setAttribute("usuarioLogueado", paciente);
            session.setAttribute("flashSuccess", quiereCambiarPassword
                    ? "Tus datos y tu contraseña se actualizaron correctamente."
                    : "Tus datos se actualizaron correctamente.");
        } else {
            session.setAttribute("flashError", "No se pudieron guardar los cambios. Intenta de nuevo.");
        }

        response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/DashboardPaciente");
    }
}
