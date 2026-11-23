package Servlets;

import Controlador.RolesDAO;
import Modelo.Roles;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ServletregistrarRol", urlPatterns = {"/ServletregistrarRol"})
public class ServletregistrarRol extends HttpServlet {

    RolesDAO dao = new RolesDAO();

    // NOTA DE SEGURIDAD: por GET solo se permite listar (lectura segura).
    // Editar y eliminar cambian datos/estado y ahora solo se aceptan por POST.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("listaRoles", dao.listar());

        request.getRequestDispatcher("/vistas/registrarRol.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        // EDITAR (CARGAR DATOS EN FORMULARIO)
        if ("editar".equals(accion)) {

            int id = Integer.parseInt(request.getParameter("id"));

            Roles rol = dao.obtenerPorId(id);

            request.setAttribute("rolEditar", rol);
            request.setAttribute("listaRoles", dao.listar());

            request.getRequestDispatcher("/vistas/registrarRol.jsp")
                    .forward(request, response);
            return;
        }

        // ELIMINAR
        if ("eliminar".equals(accion)) {

            try {
                int id = Integer.parseInt(request.getParameter("id"));
                int resultado = dao.eliminar(id);

                if (resultado == -1) {
                    // Hay usuarios usando este rol: no se puede eliminar
                    request.setAttribute("mensajeError",
                            "No se puede eliminar este rol porque hay usuarios asignados a él. "
                            + "Reasigna esos usuarios a otro rol antes de eliminarlo.");
                } else if (resultado == -2) {
                    request.setAttribute("mensajeError",
                            "Ocurrió un error al intentar eliminar el rol. Intenta nuevamente.");
                } else if (resultado == 0) {
                    request.setAttribute("mensajeError",
                            "El rol que intentas eliminar ya no existe.");
                } else {
                    request.setAttribute("mensajeExito", "Rol eliminado correctamente.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("mensajeError",
                        "Ocurrió un error inesperado al intentar eliminar el rol.");
            }

            request.setAttribute("listaRoles", dao.listar());

            request.getRequestDispatcher("/vistas/registrarRol.jsp")
                    .forward(request, response);
            return;
        }

        Roles r = new Roles();

        r.setNombre_rol(request.getParameter("txtNombreRol"));
        r.setDescripcion(request.getParameter("txtDescripcion"));

        // GUARDAR
        if ("guardar".equals(accion)) {
            dao.agregar(r);
        }

        // ACTUALIZAR
        else if ("actualizar".equals(accion)) {

            r.setId_rol(Integer.parseInt(request.getParameter("idRol")));
            dao.actualizar(r);
        }

        response.sendRedirect(request.getContextPath()
                + "/ServletregistrarRol?accion=listar");
    }
}