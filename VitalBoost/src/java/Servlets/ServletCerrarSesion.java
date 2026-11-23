package Servlets; //

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ServletCerrarSesion", urlPatterns = {"/ServletCerrarSesion"})
public class ServletCerrarSesion extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Obtener la sesión actual si existe
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 2. Invalidar la sesión para borrar todos los datos (como usuarioLogueado)
            session.invalidate(); 
        }
        
        // 3. Redirigir al login después de limpiar la sesión
        response.sendRedirect(request.getContextPath() + "/ServletLogin");
    }

    // NOTA DE SEGURIDAD: ya no se sobreescribe doGet.
    // Cerrar sesión cambia el estado del servidor (invalida la sesión),
    // por lo tanto solo debe poder dispararse por POST. Un GET a esta
    // ruta ahora responde 405 (comportamiento por defecto de HttpServlet).

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}