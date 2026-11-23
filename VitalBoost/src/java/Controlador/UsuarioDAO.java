package Controlador;

import Conexion.Conexion;
import Modelo.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    Conexion cn = new Conexion();

    // =========================
    // INSERTAR / AGREGAR
    // =========================
    // NOTA SOBRE VERIFICACIÓN DE CORREO:
    // Existen dos formas de crear un usuario y cada una decide de forma
    // distinta si el correo queda verificado desde el inicio:
    //
    //  - insertar(u): usada por el panel de administrador (Servletregistrarusuario)
    //    para crear personal (médicos, otros admins, etc), y también por
    //    ServletVerificarEmail cuando un paciente confirma su correo (en ese
    //    momento la cuenta nace directamente verificada=1, ya que el dato
    //    que se estaba confirmando era justamente el correo).
    //
    //  - insertarConVerificacion(u, token): se deja disponible por si se
    //    necesita crear una cuenta ya verificada=0 con token propio, pero
    //    el registro público (ServletRegistro) ya NO la usa: mientras el
    //    paciente no confirma su correo, sus datos solo existen en
    //    Registros_pendientes (ver RegistroPendienteDAO), no en Usuarios.
    //
    // Ambas reutilizan el mismo INSERT interno para no duplicar SQL.
    public int insertar(Usuario u) {
        return insertarInterno(u, true, null);
    }

    public int insertarConVerificacion(Usuario u, String token) {
        return insertarInterno(u, false, token);
    }

    private int insertarInterno(Usuario u, boolean verificado, String token) {
        String sql = "{call sp_usuario_insertar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        int r = 0;

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, u.getId_rol());
            cs.setInt(2, u.getId_tipo_documento());
            cs.setInt(3, u.getId_tipo_sangre());
            cs.setString(4, u.getNumero_documento());
            cs.setString(5, u.getNombre_completo());
            cs.setString(6, u.getEmail());
            cs.setString(7, u.getPassword());
            cs.setString(8, u.getFecha_nacimiento());
            cs.setString(9, u.getAlergias_conocidas());
            cs.setString(10, u.getEps());
            cs.setString(11, u.getMedicamentos_actuales());
            cs.setInt(12, verificado ? 1 : 0);
            cs.setString(13, token);

            r = cs.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    // =========================
    // VERIFICACIÓN DE CORREO
    // =========================

    // Busca por el token exacto, sin importar si ya venció (para poder
    // distinguir "token inválido" de "token vencido" en el servlet).
    public Usuario buscarPorTokenVerificacion(String token) {
        Usuario u = null;
        String sql = "{call sp_usuario_buscar_por_token_verificacion(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, token);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    u = mapearUsuario(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    public boolean marcarEmailVerificado(int idUsuario) {
        String sql = "{call sp_usuario_marcar_email_verificado(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Genera un nuevo token (24h) para reenviar el correo de verificación,
    // por ejemplo cuando el anterior venció o el correo no llegó.
    public boolean actualizarTokenVerificacion(int idUsuario, String token) {
        String sql = "{call sp_usuario_actualizar_token_verificacion(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setString(2, token);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // RECUPERACIÓN DE CONTRASEÑA
    // =========================

    // Genera un token nuevo (1 hora de validez) para el usuario dueño de ese
    // correo. Solo aplica a cuentas activas: si el correo no corresponde a
    // ningún usuario activo, no actualiza nada y devuelve false (así el
    // servlet sabe si debe enviar el correo o no, sin tener que exponer esa
    // decisión al usuario final).
    public boolean generarTokenRecuperacion(int idUsuario, String token) {
        String sql = "{call sp_usuario_generar_token_recuperacion(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setString(2, token);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Busca por el token exacto, sin importar si ya venció (para poder
    // distinguir "token inválido" de "token vencido" en el servlet).
    public Usuario buscarPorTokenRecuperacion(String token) {
        Usuario u = null;
        String sql = "{call sp_usuario_buscar_por_token_recuperacion(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, token);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    u = mapearUsuario(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    // Guarda la nueva contraseña y consume el token (para que el enlace del
    // correo no se pueda volver a usar una vez restablecida la contraseña).
    public boolean actualizarPasswordConToken(int idUsuario, String nuevaPassword) {
        String sql = "{call sp_usuario_actualizar_password_con_token(?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, idUsuario);
            cs.setString(2, nuevaPassword);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // LISTAR TODOS
    // =========================
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "{call sp_usuario_listar()}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    public Usuario buscarPorId(int id) {
        Usuario u = null;
        String sql = "{call sp_usuario_buscar_por_id(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    u = mapearUsuario(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    // =========================
    // BUSCAR POR DOCUMENTO (Requerido por ServletLogin)
    // =========================
    public Usuario buscarPorDocumento(String documento) {
        Usuario u = null;
        String sql = "{call sp_usuario_buscar_por_documento(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, documento);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    u = mapearUsuario(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    // =========================
    // BUSCAR POR EMAIL (Requerido por ServletLogin)
    // =========================
    public Usuario buscarPorEmail(String email) {
        Usuario u = null;
        String sql = "{call sp_usuario_buscar_por_email(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, email);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    u = mapearUsuario(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return u;
    }

    // =========================
    // MAPEO COMÚN FILA -> OBJETO
    // =========================
    private Usuario mapearUsuario(ResultSet rs) throws java.sql.SQLException {
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("id_usuario"));
        u.setId_rol(rs.getInt("id_rol"));
        u.setId_tipo_documento(rs.getInt("id_tipo_documento"));
        u.setId_tipo_sangre(rs.getInt("id_tipo_sangre"));
        u.setNumero_documento(rs.getString("numero_documento"));
        u.setNombre_completo(rs.getString("nombre_completo"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFecha_nacimiento(rs.getString("fecha_nacimiento"));
        u.setAlergias_conocidas(rs.getString("alergias_conocidas"));
        u.setEps(rs.getString("eps"));
        u.setMedicamentos_actuales(rs.getString("medicamentos_actuales"));
        u.setNombreRol(rs.getString("nombre_rol"));
        u.setNombreTipoSangre(rs.getString("nombre_tipo_sangre"));
        u.setNombreTipoDocumento(rs.getString("descripcion_tipo_documento"));
        u.setActivo(rs.getInt("activo"));
        u.setEmail_verificado(rs.getInt("email_verificado"));
        u.setToken_verificacion(rs.getString("token_verificacion"));
        u.setToken_expira(rs.getTimestamp("token_expira"));
        u.setToken_recuperacion(rs.getString("token_recuperacion"));
        u.setToken_recuperacion_expira(rs.getTimestamp("token_recuperacion_expira"));
        return u;
    }

    // =========================
    // ACTUALIZAR
    // =========================
    public boolean actualizar(Usuario u) {
        String sql = "{call sp_usuario_actualizar(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, u.getId_usuario());
            cs.setInt(2, u.getId_rol());
            cs.setInt(3, u.getId_tipo_documento());
            cs.setInt(4, u.getId_tipo_sangre());
            cs.setString(5, u.getNumero_documento());
            cs.setString(6, u.getNombre_completo());
            cs.setString(7, u.getEmail());
            cs.setString(8, u.getPassword());
            cs.setString(9, u.getFecha_nacimiento());
            cs.setString(10, u.getAlergias_conocidas());
            cs.setString(11, u.getEps());
            cs.setString(12, u.getMedicamentos_actuales());

            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DESACTIVAR (BORRADO LÓGICO)
    // =========================
    public boolean desactivar(int id) {
        String sql = "{call sp_usuario_desactivar(?)}";

        try (Connection con = cn.getConnection();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setInt(1, id);
            return cs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // ACTIVAR
    // =========================
    public boolean activar(int id) {
        String sql = "{call sp_usuario_activar(?)}";

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