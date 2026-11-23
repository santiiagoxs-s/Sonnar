package Controlador;

import Conexion.Conexion;
import Modelo.ContactoEmergencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactoEmergenciaDAO {

    Conexion cn = new Conexion();

    Connection con;
    CallableStatement cs;
    ResultSet rs;

    // LISTAR
    public List<ContactoEmergencia> listar() {

        List<ContactoEmergencia> lista = new ArrayList<>();

        String sql = "{call sp_contacto_listar()}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);
            rs = cs.executeQuery();

            while (rs.next()) {

                ContactoEmergencia c = new ContactoEmergencia();

                c.setId_contacto(rs.getInt("id_contacto"));
                c.setId_usuario(rs.getInt("id_usuario"));
                c.setNombre_contacto(rs.getString("nombre_contacto"));
                c.setParentesco(rs.getString("parentesco"));
                c.setTelefono(rs.getString("telefono"));
                c.setNombreUsuario(rs.getString("nombre_usuario"));

                lista.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error listar contactos: " + e.getMessage());
        } finally {
            cerrar();
        }

        return lista;
    }

    // LISTAR POR USUARIO (para mostrar en la hoja de vida médica)
    public List<ContactoEmergencia> listarPorUsuario(int idUsuario) {

        List<ContactoEmergencia> lista = new ArrayList<>();

        String sql = "{call sp_contacto_listar_por_usuario(?)}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);
            cs.setInt(1, idUsuario);
            rs = cs.executeQuery();

            while (rs.next()) {

                ContactoEmergencia c = new ContactoEmergencia();

                c.setId_contacto(rs.getInt("id_contacto"));
                c.setId_usuario(rs.getInt("id_usuario"));
                c.setNombre_contacto(rs.getString("nombre_contacto"));
                c.setParentesco(rs.getString("parentesco"));
                c.setTelefono(rs.getString("telefono"));

                lista.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error listar contactos por usuario: " + e.getMessage());
        } finally {
            cerrar();
        }

        return lista;
    }

    // INSERTAR
    public int agregar(ContactoEmergencia c) {

        String sql = "{call sp_contacto_agregar(?,?,?,?)}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);

            cs.setInt(1, c.getId_usuario());
            cs.setString(2, c.getNombre_contacto());
            cs.setString(3, c.getParentesco());
            cs.setString(4, c.getTelefono());

            return cs.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error agregar contacto: " + e.getMessage());
            return 0;
        } finally {
            cerrar();
        }
    }

    // ACTUALIZAR
    public int actualizar(ContactoEmergencia c) {

        String sql = "{call sp_contacto_actualizar(?,?,?,?,?)}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);

            cs.setInt(1, c.getId_contacto());
            cs.setInt(2, c.getId_usuario());
            cs.setString(3, c.getNombre_contacto());
            cs.setString(4, c.getParentesco());
            cs.setString(5, c.getTelefono());

            return cs.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error actualizar contacto: " + e.getMessage());
            return 0;
        } finally {
            cerrar();
        }
    }

    // ELIMINAR
    public void eliminar(int id) {

        String sql = "{call sp_contacto_eliminar(?)}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);
            cs.setInt(1, id);
            cs.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error eliminar contacto: " + e.getMessage());
        } finally {
            cerrar();
        }
    }

    // CERRAR RECURSOS
    private void cerrar() {
        try {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.out.println("Error cerrando recursos: " + e.getMessage());
        }
    }

    public ContactoEmergencia obtenerPorId(int id) {

        ContactoEmergencia c = null;

        String sql = "{call sp_contacto_obtener_por_id(?)}";

        try {
            con = cn.getConnection();
            cs = con.prepareCall(sql);
            cs.setInt(1, id);
            rs = cs.executeQuery();

            if (rs.next()) {

                c = new ContactoEmergencia();

                c.setId_contacto(rs.getInt("id_contacto"));
                c.setId_usuario(rs.getInt("id_usuario"));
                c.setNombre_contacto(rs.getString("nombre_contacto"));
                c.setParentesco(rs.getString("parentesco"));
                c.setTelefono(rs.getString("telefono"));
            }

        } catch (Exception e) {
            System.out.println("Error obtener contacto: " + e.getMessage());
        } finally {
            cerrar();
        }

        return c;
    }
}