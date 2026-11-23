package Controlador;

import Conexion.Conexion;
import Modelo.Tipo_Documento;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Tipo_DocumentoDAO {

    Conexion cn = new Conexion();

    // =========================
    // LISTAR TODOS
    // =========================
    public List<Tipo_Documento> listar() {
        List<Tipo_Documento> lista = new ArrayList<>();
        String sql = "{call sp_tipo_documento_listar()}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                Tipo_Documento td = new Tipo_Documento();
                td.setId_tipo_documento(rs.getInt("id_tipo_documento"));
                td.setDescripcion_tipo_documento(rs.getString("descripcion_tipo_documento"));
                lista.add(td);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // =========================
    // INSERTAR
    // =========================
    public int insertar(Tipo_Documento td) {
        String sql = "{call sp_tipo_documento_insertar(?)}";
        int r = 0;

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, td.getDescripcion_tipo_documento());
            r = cs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    public Tipo_Documento buscarPorId(int id) {
        Tipo_Documento td = null;
        String sql = "{call sp_tipo_documento_buscar_por_id(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    td = new Tipo_Documento();
                    td.setId_tipo_documento(rs.getInt("id_tipo_documento"));
                    td.setDescripcion_tipo_documento(rs.getString("descripcion_tipo_documento"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return td;
    }

    // =========================
    // ACTUALIZAR
    // =========================
    public boolean actualizar(Tipo_Documento td) {
        String sql = "{call sp_tipo_documento_actualizar(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, td.getId_tipo_documento());
            cs.setString(2, td.getDescripcion_tipo_documento());

            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ELIMINAR
    // =========================
    public boolean eliminar(int id) {
        String sql = "{call sp_tipo_documento_eliminar(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}