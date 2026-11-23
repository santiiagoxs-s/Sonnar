package Servlets;

import Controlador.ContactoEmergenciaDAO;
import Controlador.UsuarioDAO;
import Modelo.ContactoEmergencia;
import Modelo.Usuario;
import Utilidades.JsonUtil;
import Utilidades.SesionMovilStore;
import Utilidades.PasswordUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// ==================================================================
// API PARA LA APP MÓVIL (FLUTTER)
//
// Este servlet NO usa HttpSession (eso obligaría a manejar cookies en
// Flutter, incómodo en una app nativa). En su lugar, el login entrega
// un TOKEN (ver Utilidades.SesionMovilStore) que la app guarda y
// reenvía en cada petición posterior como parámetro "token".
//
// Todo responde en JSON, construido con Utilidades.JsonUtil, así que
// agregar una acción nueva en el futuro (por ejemplo "misContactos" o
// "editarPerfil") es tan fácil como sumar un nuevo "case" aquí abajo:
// no hay nada hardcodeado sobre las acciones existentes.
//
// ACCIONES DISPONIBLES (parámetro "accion", por GET o POST):
//
//   POST accion=login          -> email, password
//                                  Devuelve token + datos del usuario.
//
//   GET  accion=qr             -> token
//                                  Devuelve la URL de la hoja de vida
//                                  y la URL de la imagen del QR, listas
//                                  para que la app las use directamente
//                                  (ej. cargar el QR con Image.network).
//
//   GET  accion=hojaVida       -> doc
//                                  Acceso PÚBLICO (igual que al
//                                  escanear el QR físico): trae los
//                                  datos médicos de emergencia de un
//                                  paciente por su número de documento.
//
//   GET/POST accion=validarSesion -> token
//                                  Confirma si el token sigue activo y
//                                  devuelve los datos actualizados del
//                                  usuario (útil para el splash/auto-
//                                  login de la app).
//
//   POST accion=cerrarSesion   -> token
//                                  Invalida el token en el servidor.
// ==================================================================
@WebServlet(name = "ServletApiMovil", urlPatterns = {"/ApiMovil"})
public class ServletApiMovil extends HttpServlet {

    private final UsuarioDAO udao = new UsuarioDAO();
    private final ContactoEmergenciaDAO cdao = new ContactoEmergenciaDAO();

    private static final int CAMPO_MAX = 100;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        procesar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        procesar(request, response);
    }

    // Permite que apps Flutter Web (o pruebas desde el navegador) hagan
    // la petición previa "preflight" de CORS sin que el servidor la rechace.
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        aplicarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void procesar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        aplicarCORS(response);

        String accion = request.getParameter("accion");

        try {
            if (accion == null || accion.isBlank()) {
                responder(response, HttpServletResponse.SC_BAD_REQUEST,
                        JsonUtil.mapa("success", false, "mensaje",
                                "Falta el parámetro 'accion'. Acciones válidas: login, qr, hojaVida, validarSesion, cerrarSesion."));
                return;
            }

            switch (accion) {
                case "login":
                    accionLogin(request, response);
                    break;
                case "qr":
                    accionQr(request, response);
                    break;
                case "hojaVida":
                    accionHojaVida(request, response);
                    break;
                case "validarSesion":
                    accionValidarSesion(request, response);
                    break;
                case "cerrarSesion":
                    accionCerrarSesion(request, response);
                    break;
                default:
                    responder(response, HttpServletResponse.SC_BAD_REQUEST,
                            JsonUtil.mapa("success", false, "mensaje", "Acción no reconocida: " + accion));
            }
        } catch (Exception e) {
            e.printStackTrace();
            responder(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonUtil.mapa("success", false, "mensaje", "Error interno del servidor."));
        }
    }

    // ==================================================================
    // LOGIN
    // ==================================================================
    private void accionLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank() || email.length() > CAMPO_MAX) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    JsonUtil.mapa("success", false, "mensaje", "El correo es obligatorio."));
            return;
        }
        if (password == null || password.isBlank() || password.length() > CAMPO_MAX) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    JsonUtil.mapa("success", false, "mensaje", "La contraseña es obligatoria."));
            return;
        }

        Usuario u = udao.buscarPorEmail(email);

        // Mismo mensaje genérico tanto si el correo no existe como si la
        // contraseña es incorrecta, para no revelar cuáles correos están
        // registrados en el sistema (buena práctica de seguridad).
        if (u == null || !PasswordUtil.verificar(password, u.getPassword())) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                    JsonUtil.mapa("success", false, "mensaje", "Correo o contraseña incorrectos."));
            return;
        }

        if (u.getActivo() == 0) {
            responder(response, HttpServletResponse.SC_FORBIDDEN,
                    JsonUtil.mapa("success", false, "mensaje", "Este usuario está inactivo. Contacta al administrador."));
            return;
        }

        if (!u.isEmailVerificado()) {
            responder(response, HttpServletResponse.SC_FORBIDDEN,
                    JsonUtil.mapa("success", false, "mensaje", "Aún no verificas tu correo. Revisa tu bandeja de entrada."));
            return;
        }

        // Si es paciente, se recarga por documento para traer todos los
        // datos completos (igual que hace ServletLogin con la sesión web).
        Usuario usuarioCompleto = u;
        if (u.getNumero_documento() != null && !u.getNumero_documento().isBlank()) {
            Usuario porDocumento = udao.buscarPorDocumento(u.getNumero_documento());
            if (porDocumento != null) {
                usuarioCompleto = porDocumento;
            }
        }

        String token = SesionMovilStore.crearToken(usuarioCompleto);

        Map<String, Object> datos = JsonUtil.mapa(
                "success", true,
                "mensaje", "Inicio de sesión correcto.",
                "token", token,
                "usuario", usuarioAJson(usuarioCompleto)
        );
        responder(response, HttpServletResponse.SC_OK, datos);
    }

    // ==================================================================
    // QR DE LA HOJA DE VIDA (usuario ya logueado en la app)
    // ==================================================================
    private void accionQr(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario sesion = usuarioDesdeToken(request, response);
        if (sesion == null) {
            return; // usuarioDesdeToken ya respondió el error
        }

        // Se recarga desde la base de datos por si el usuario acaba de
        // editar su perfil, así el QR y el link siempre quedan al día
        // (comportamiento "dinámico" igual que en ServletDashboardPaciente).
        Usuario actual = udao.buscarPorId(sesion.getId_usuario());
        if (actual == null) {
            responder(response, HttpServletResponse.SC_NOT_FOUND,
                    JsonUtil.mapa("success", false, "mensaje", "El usuario ya no existe."));
            return;
        }
        if (actual.getActivo() == 0) {
            responder(response, HttpServletResponse.SC_FORBIDDEN,
                    JsonUtil.mapa("success", false, "mensaje", "Este usuario se encuentra inactivo."));
            return;
        }
        if (actual.getNumero_documento() == null || actual.getNumero_documento().isBlank()) {
            responder(response, HttpServletResponse.SC_CONFLICT,
                    JsonUtil.mapa("success", false, "mensaje", "El usuario no tiene número de documento registrado."));
            return;
        }

        SesionMovilStore.actualizarUsuario(request.getParameter("token"), actual);

        // -------- Enlace y código QR de la hoja de vida de emergencia --------
        // Construido de forma 100% dinámica a partir de la petición actual
        // (esquema, host, puerto y contexto), igual que en la web: así
        // funciona sin cambios tanto en local como en el servidor real,
        // sin necesidad de hardcodear ningún dominio.
        String urlBase = request.getScheme() + "://"
                + request.getServerName()
                + ":" + request.getServerPort()
                + request.getContextPath();

        String urlHojaVida = urlBase + "/HojaVidaPublica?doc="
                + URLEncoder.encode(actual.getNumero_documento(), StandardCharsets.UTF_8);

        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data="
                + URLEncoder.encode(urlHojaVida, StandardCharsets.UTF_8);

        Map<String, Object> datos = JsonUtil.mapa(
                "success", true,
                "urlHojaVida", urlHojaVida,
                "qrImageUrl", qrImageUrl,
                "usuario", usuarioAJson(actual)
        );
        responder(response, HttpServletResponse.SC_OK, datos);
    }

    // ==================================================================
    // HOJA DE VIDA PÚBLICA (lo que se ve al escanear el QR, sin login)
    // ==================================================================
    private void accionHojaVida(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String doc = request.getParameter("doc");

        if (doc == null || doc.isBlank()) {
            responder(response, HttpServletResponse.SC_BAD_REQUEST,
                    JsonUtil.mapa("success", false, "mensaje", "Código QR inválido: no se indicó ningún documento."));
            return;
        }

        Usuario paciente = udao.buscarPorDocumento(doc);

        if (paciente == null) {
            responder(response, HttpServletResponse.SC_NOT_FOUND,
                    JsonUtil.mapa("success", false, "mensaje", "No se encontró ningún paciente registrado con este código."));
            return;
        }

        if (paciente.getActivo() == 0) {
            responder(response, HttpServletResponse.SC_FORBIDDEN,
                    JsonUtil.mapa("success", false, "mensaje", "Este perfil se encuentra inactivo. Contacta al administrador de VitalBoost."));
            return;
        }

        List<ContactoEmergencia> contactos = cdao.listarPorUsuario(paciente.getId_usuario());
        List<Object> contactosJson = new ArrayList<>();
        for (ContactoEmergencia c : contactos) {
            contactosJson.add(contactoAJson(c));
        }

        Map<String, Object> datos = JsonUtil.mapa(
                "success", true,
                "paciente", usuarioAJson(paciente),
                "contactosEmergencia", contactosJson
        );
        responder(response, HttpServletResponse.SC_OK, datos);
    }

    // ==================================================================
    // VALIDAR SESIÓN (auto-login / splash de la app)
    // ==================================================================
    private void accionValidarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario sesion = usuarioDesdeToken(request, response);
        if (sesion == null) {
            return;
        }

        Usuario actual = udao.buscarPorId(sesion.getId_usuario());
        if (actual == null || actual.getActivo() == 0) {
            SesionMovilStore.invalidar(request.getParameter("token"));
            responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                    JsonUtil.mapa("success", false, "mensaje", "La sesión ya no es válida."));
            return;
        }

        SesionMovilStore.actualizarUsuario(request.getParameter("token"), actual);

        responder(response, HttpServletResponse.SC_OK,
                JsonUtil.mapa("success", true, "usuario", usuarioAJson(actual)));
    }

    // ==================================================================
    // CERRAR SESIÓN
    // ==================================================================
    private void accionCerrarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SesionMovilStore.invalidar(request.getParameter("token"));
        responder(response, HttpServletResponse.SC_OK,
                JsonUtil.mapa("success", true, "mensaje", "Sesión cerrada."));
    }

    // ==================================================================
    // AYUDANTES
    // ==================================================================

    // Busca y valida el token en cualquier acción protegida. Si no es
    // válido, ya deja la respuesta de error escrita y devuelve null.
    private Usuario usuarioDesdeToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getParameter("token");
        Usuario usuario = SesionMovilStore.obtenerUsuario(token);
        if (usuario == null) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                    JsonUtil.mapa("success", false, "mensaje", "Sesión inválida o expirada. Inicia sesión de nuevo."));
            return null;
        }
        return usuario;
    }

    // Convierte un Usuario a un Map "dinámico" para el JSON de salida.
    // OJO: nunca se incluye la contraseña ni datos de verificación de
    // correo (token_verificacion), esos son internos del servidor.
    private Map<String, Object> usuarioAJson(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId_usuario());
        m.put("documento", u.getNumero_documento());
        m.put("nombreCompleto", u.getNombre_completo());
        m.put("email", u.getEmail());
        m.put("rol", u.getNombreRol());
        m.put("tipoDocumento", u.getNombreTipoDocumento());
        m.put("tipoSangre", u.getNombreTipoSangre());
        m.put("fechaNacimiento", u.getFecha_nacimiento());
        m.put("alergiasConocidas", u.getAlergias_conocidas());
        m.put("eps", u.getEps());
        m.put("medicamentosActuales", u.getMedicamentos_actuales());
        m.put("activo", u.getActivo() == 1);
        m.put("emailVerificado", u.isEmailVerificado());
        return m;
    }

    private Map<String, Object> contactoAJson(ContactoEmergencia c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId_contacto());
        m.put("nombre", c.getNombre_contacto());
        m.put("parentesco", c.getParentesco());
        m.put("telefono", c.getTelefono());
        return m;
    }

    private void responder(HttpServletResponse response, int status, Map<String, Object> datos) throws IOException {
        response.setStatus(status);
        response.getWriter().write(JsonUtil.toJson(datos));
    }

    // CORS abierto: necesario porque una app Flutter (móvil o web) hace
    // la petición desde un origen distinto al del servidor. Solo se
    // exponen aquí endpoints de solo lectura o protegidos por token, así
    // que no hay cookies de sesión de por medio que proteger.
    private void aplicarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
