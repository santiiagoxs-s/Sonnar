package Controlador;

import Conexion.Conexion;
import Modelo.RegistroPendiente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class RegistroPendienteDAO {

    Conexion cn = new Conexion();

    // =========================
    // INSERTAR
    // =========================
    public int insertar(RegistroPendiente rp, String token) {
        String sql = "{call sp_pendiente_insertar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        int r = 0;
        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, rp.getId_rol());
            cs.setInt(2, rp.getId_tipo_documento());
            cs.setInt(3, rp.getId_tipo_sangre());
            cs.setString(4, rp.getNumero_documento());
            cs.setString(5, rp.getNombre_completo());
            cs.setString(6, rp.getEmail());
            cs.setString(7, rp.getPassword());
            cs.setString(8, rp.getFecha_nacimiento());
            cs.setString(9, rp.getAlergias_conocidas());
            cs.setString(10, rp.getEps());
            cs.setString(11, rp.getMedicamentos_actuales());
            cs.setString(12, rp.getNombre_contacto());
            cs.setString(13, rp.getParentesco_contacto());
            cs.setString(14, rp.getTelefono_contacto());
            cs.setString(15, token);

            r = cs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    // =========================
    // BUSCAR POR TOKEN (sin importar si venció, igual que en UsuarioDAO,
    // para poder distinguir "token inválido" de "token vencido")
    // =========================
    public RegistroPendiente buscarPorToken(String token) {
        String sql = "{call sp_pendiente_buscar_por_token(?)}";
        return buscarUno(sql, token);
    }

    // =========================
    // BUSCAR POR EMAIL (para evitar registros duplicados y para reenviar)
    // =========================
    public RegistroPendiente buscarPorEmail(String email) {
        String sql = "{call sp_pendiente_buscar_por_email(?)}";
        return buscarUno(sql, email);
    }

    // =========================
    // BUSCAR POR DOCUMENTO (para evitar registros duplicados)
    // =========================
    public RegistroPendiente buscarPorDocumento(String documento) {
        String sql = "{call sp_pendiente_buscar_por_documento(?)}";
        return buscarUno(sql, documento);
    }

    private RegistroPendiente buscarUno(String sql, String parametro) {
        RegistroPendiente rp = null;
        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, parametro);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    rp = mapear(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }

    // =========================
    // ACTUALIZAR TOKEN (para reenviar el correo de verificación)
    // =========================
    public boolean actualizarToken(int idPendiente, String nuevoToken) {
        String sql = "{call sp_pendiente_actualizar_token(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idPendiente);
            cs.setString(2, nuevoToken);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ELIMINAR (tras crear la cuenta definitiva, o al descartar uno vencido)
    // =========================
    public boolean eliminar(int idPendiente) {
        String sql = "{call sp_pendiente_eliminar(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idPendiente);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // MAPEO COMÚN FILA -> OBJETO
    // =========================
    private RegistroPendiente mapear(ResultSet rs) throws java.sql.SQLException {
        RegistroPendiente rp = new RegistroPendiente();
        rp.setId_pendiente(rs.getInt("id_pendiente"));
        rp.setId_rol(rs.getInt("id_rol"));
        rp.setId_tipo_documento(rs.getInt("id_tipo_documento"));
        rp.setId_tipo_sangre(rs.getInt("id_tipo_sangre"));
        rp.setNumero_documento(rs.getString("numero_documento"));
        rp.setNombre_completo(rs.getString("nombre_completo"));
        rp.setEmail(rs.getString("email"));
        rp.setPassword(rs.getString("password"));
        rp.setFecha_nacimiento(rs.getString("fecha_nacimiento"));
        rp.setAlergias_conocidas(rs.getString("alergias_conocidas"));
        rp.setEps(rs.getString("eps"));
        rp.setMedicamentos_actuales(rs.getString("medicamentos_actuales"));
        rp.setNombre_contacto(rs.getString("nombre_contacto"));
        rp.setParentesco_contacto(rs.getString("parentesco_contacto"));
        rp.setTelefono_contacto(rs.getString("telefono_contacto"));
        rp.setToken_verificacion(rs.getString("token_verificacion"));
        rp.setToken_expira(rs.getTimestamp("token_expira"));
        return rp;
    }
}