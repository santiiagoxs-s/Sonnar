package Servlets;

import Controlador.UsuarioDAO;
import Controlador.RolesDAO;
import Controlador.Tipo_DocumentoDAO;
import Controlador.Tipo_sangreDAO;
import Modelo.Usuario;
import Utilidades.PasswordUtil;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "Servletregistrarusuario", urlPatterns = {"/Servletregistrarusuario"})
public class Servletregistrarusuario extends HttpServlet {

    private final UsuarioDAO dao = new UsuarioDAO();
    private final RolesDAO rolesDao = new RolesDAO();
    private final Tipo_DocumentoDAO tipoDocumentoDao = new Tipo_DocumentoDAO();
    private final Tipo_sangreDAO tipoSangreDao = new Tipo_sangreDAO();

    // NOTA DE SEGURIDAD: por GET solo se permite listar (lectura segura).
    // Editar y eliminar cambian datos/estado y ahora solo se aceptan por POST.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("lista", dao.listar());
        request.setAttribute("listaRoles", rolesDao.listar());
        request.setAttribute("listaTiposDocumento", tipoDocumentoDao.listar());
        request.setAttribute("listaTiposSangre", tipoSangreDao.listar());
        request.getRequestDispatcher("/vistas/registrarusuario.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("editar".equals(accion)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                Usuario u = dao.buscarPorId(id);
                request.setAttribute("usuarioEdit", u);
            } catch (Exception e) {
                e.printStackTrace();
            }
            request.setAttribute("lista", dao.listar());
            request.setAttribute("listaRoles", rolesDao.listar());
            request.setAttribute("listaTiposDocumento", tipoDocumentoDao.listar());
            request.setAttribute("listaTiposSangre", tipoSangreDao.listar());
            request.getRequestDispatcher("/vistas/registrarusuario.jsp").forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                dao.desactivar(id);
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=desactivado");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=error");
            }
            return;
        }

        if ("activar".equals(accion)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                dao.activar(id);
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=activado");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=error");
            }
            return;
        }

        try {
            Usuario u = new Usuario();
            u.setId_rol(Integer.parseInt(request.getParameter("txtIdRol")));
            u.setId_tipo_documento(Integer.parseInt(request.getParameter("txtIdTipoDocumento")));
            u.setId_tipo_sangre(Integer.parseInt(request.getParameter("txtIdTipoSangre")));
            u.setNumero_documento(request.getParameter("txtNumeroDocumento"));
            u.setNombre_completo(request.getParameter("txtNombre"));
            u.setEmail(request.getParameter("txtEmail"));
            String passwordIngresada = request.getParameter("txtPassword");
            u.setFecha_nacimiento(request.getParameter("txtFechaNacimiento"));
            u.setAlergias_conocidas(request.getParameter("txtAlergias"));
            u.setEps(request.getParameter("txtEps"));
            u.setMedicamentos_actuales(request.getParameter("txtMedicamentos"));

            if ("actualizar".equals(accion)) {
                u.setId_usuario(Integer.parseInt(request.getParameter("id")));

                if (passwordIngresada == null || passwordIngresada.isBlank()) {
                    // No se escribió una contraseña nueva: se conserva el
                    // hash que ya existe en la base de datos.
                    Usuario existente = dao.buscarPorId(u.getId_usuario());
                    u.setPassword(existente != null ? existente.getPassword() : null);
                } else {
                    u.setPassword(PasswordUtil.hash(passwordIngresada));
                }

                dao.actualizar(u);
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=actualizado");
            } else {
                u.setPassword(PasswordUtil.hash(passwordIngresada));
                dao.insertar(u);
                response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=guardado");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/Servletregistrarusuario?msg=error");
        }
    }
}