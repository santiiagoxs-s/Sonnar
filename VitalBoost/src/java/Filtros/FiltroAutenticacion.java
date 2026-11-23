package Filtros;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// ==================================================================
// FILTRO DE AUTENTICACIÓN Y CONTROL DE CACHÉ DEL NAVEGADOR
//
// Resuelve dos problemas relacionados:
//
//  1) SEGURIDAD DE SESIÓN: cualquier vista o servlet que necesite un
//     usuario logueado se protege en un solo lugar, en vez de repetir
//     "if (session.getAttribute(...) == null)" en cada servlet.
//
//  2) BOTÓN ATRÁS/ADELANTE DEL NAVEGADOR: por defecto el navegador
//     guarda en caché las páginas ya visitadas (dashboard, perfil,
//     etc.). Si un usuario cierra sesión y luego presiona "atrás" o
//     "adelante", el navegador puede mostrar la página protegida
//     desde su caché SIN volver a pedirla al servidor, dando la
//     falsa sensación de que sigue logueado. Por eso este filtro
//     manda en cada respuesta encabezados "no-cache" que obligan al
//     navegador a pedir SIEMPRE la página de nuevo al servidor; y el
//     servidor, al no encontrar sesión activa, redirige al login.
// ==================================================================
@WebFilter(filterName = "FiltroAutenticacion", urlPatterns = {"/*"})
public class FiltroAutenticacion implements Filter {

    // ==========================================================
    // RUTAS PÚBLICAS: no requieren sesión iniciada
    // ==========================================================
    private static final String[] RUTAS_PUBLICAS = {
        "/",
        "/index.jsp",
        "/ServletRegistro",
        "/HojaVidaPublica",
        "/VerificarEmail",
        "/RecuperarPassword",
        "/vistas/login.jsp",
        "/vistas/registro.jsp",
        "/vistas/registroExitoso.jsp",
        "/vistas/terminosCondiciones.jsp",
        "/vistas/olvidePassword.jsp",
        "/vistas/restablecerPassword.jsp",
        // API de la app móvil (Flutter): no usa HttpSession, hace su
        // propia validación por token en ServletApiMovil (ver
        // Utilidades.SesionMovilStore), así que este filtro no debe
        // exigirle una sesión de navegador.
        "/ApiMovil"
    };

    // ==========================================================
    // PREFIJOS PÚBLICOS: recursos estáticos (css, js, imágenes...)
    // ==========================================================
    private static final String[] PREFIJOS_PUBLICOS = {
        "/css/", "/js/", "/includes/", "/img/", "/images/"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // ------------------------------------------------------
        // 1) ANTI-CACHÉ: se aplica a TODA respuesta (pública o
        //    protegida) para que atrás/adelante siempre repregunte
        //    al servidor en vez de reutilizar una copia guardada.
        // ------------------------------------------------------
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI().substring(contextPath.length());
        if (uri.isEmpty()) {
            uri = "/";
        }

        // ------------------------------------------------------
        // 2) RUTAS PÚBLICAS: siguen sin pedir sesión
        // ------------------------------------------------------
        if (esRutaPublica(uri)) {
            chain.doFilter(req, res);
            return;
        }

        // ------------------------------------------------------
        // 3) CASO ESPECIAL /ServletLogin:
        //    mostrar el formulario, iniciar sesión ("Ingresar") y
        //    cerrar sesión ("Salir") son públicos. Las acciones de
        //    búsqueda de paciente por huella ("BuscarPaciente",
        //    "ConfirmarHuella") son parte del panel del médico y sí
        //    requieren que ya exista una sesión iniciada.
        // ------------------------------------------------------
        if (uri.equals("/ServletLogin")) {
            String accion = request.getParameter("accion");
            boolean accionPublica = accion == null
                    || accion.equalsIgnoreCase("Ingresar")
                    || accion.equalsIgnoreCase("Salir");

            if (accionPublica) {
                chain.doFilter(req, res);
                return;
            }
        }

        // ------------------------------------------------------
        // 4) TODO LO DEMÁS REQUIERE SESIÓN INICIADA
        // ------------------------------------------------------
        HttpSession session = request.getSession(false);
        Object usuarioLogueado = (session != null) ? session.getAttribute("usuarioLogueado") : null;

        if (usuarioLogueado == null) {
            response.sendRedirect(contextPath + "/ServletLogin");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean esRutaPublica(String uri) {
        for (String ruta : RUTAS_PUBLICAS) {
            if (uri.equals(ruta)) {
                return true;
            }
        }
        for (String prefijo : PREFIJOS_PUBLICOS) {
            if (uri.startsWith(prefijo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No se requiere inicialización
    }

    @Override
    public void destroy() {
        // No se requiere limpieza
    }
}
