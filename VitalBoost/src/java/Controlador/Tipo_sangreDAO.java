package Controlador;

import Conexion.Conexion;
import Modelo.Tipo_sangre;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Tipo_sangreDAO {

    Conexion cn = new Conexion();

    // =========================
    // LISTAR TODOS
    // =========================
    public List<Tipo_sangre> listar() {
        List<Tipo_sangre> lista = new ArrayList<>();
        String sql = "{call sp_tipo_sangre_listar()}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                Tipo_sangre t = new Tipo_sangre();
                t.setId_tipo_sangre(rs.getInt("id_tipo_sangre"));
                t.setNombre_tipo(rs.getString("nombre_tipo_sangre"));
                lista.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // =========================
    // INSERTAR
    // =========================
    public int insertar(Tipo_sangre t) {
        String sql = "{call sp_tipo_sangre_insertar(?)}";
        int r = 0;

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, t.getNombre_tipo());
            r = cs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    public Tipo_sangre buscarPorId(int id) {
        Tipo_sangre t = null;
        String sql = "{call sp_tipo_sangre_buscar_por_id(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    t = new Tipo_sangre();
                    t.setId_tipo_sangre(rs.getInt("id_tipo_sangre"));
                    t.setNombre_tipo(rs.getString("nombre_tipo_sangre"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    // =========================
    // ACTUALIZAR
    // =========================
    public boolean actualizar(Tipo_sangre t) {
        String sql = "{call sp_tipo_sangre_actualizar(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, t.getId_tipo_sangre());
            cs.setString(2, t.getNombre_tipo());

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
        String sql = "{call sp_tipo_sangre_eliminar(?)}";

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