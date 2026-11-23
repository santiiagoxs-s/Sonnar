package Modelo;

// ==========================================================================
// Datos de un registro público que todavía NO es una cuenta real.
// Se guarda temporalmente aquí (tabla Registros_pendientes) mientras el
// paciente confirma su correo; solo en ese momento se crea el Usuario y su
// ContactoEmergencia definitivos (ver ServletVerificarEmail).
// ==========================================================================
public class RegistroPendiente {

    private int id_pendiente;

    private int id_rol;
    private int id_tipo_documento;
    private int id_tipo_sangre;

    private String numero_documento;
    private String nombre_completo;
    private String email;
    private String password;
    private String fecha_nacimiento;
    private String alergias_conocidas;
    private String eps;
    private String medicamentos_actuales;

    private String nombre_contacto;
    private String parentesco_contacto;
    private String telefono_contacto;

    private String token_verificacion;
    private java.sql.Timestamp token_expira;

    public int getId_pendiente() {
        return id_pendiente;
    }

    public void setId_pendiente(int id_pendiente) {
        this.id_pendiente = id_pendiente;
    }

    public int getId_rol() {
        return id_rol;
    }

    public void setId_rol(int id_rol) {
        this.id_rol = id_rol;
    }

    public int getId_tipo_documento() {
        return id_tipo_documento;
    }

    public void setId_tipo_documento(int id_tipo_documento) {
        this.id_tipo_documento = id_tipo_documento;
    }

    public int getId_tipo_sangre() {
        return id_tipo_sangre;
    }

    public void setId_tipo_sangre(int id_tipo_sangre) {
        this.id_tipo_sangre = id_tipo_sangre;
    }

    public String getNumero_documento() {
        return numero_documento;
    }

    public void setNumero_documento(String numero_documento) {
        this.numero_documento = numero_documento;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setFecha_nacimiento(String fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public String getAlergias_conocidas() {
        return alergias_conocidas;
    }

    public void setAlergias_conocidas(String alergias_conocidas) {
        this.alergias_conocidas = alergias_conocidas;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public String getMedicamentos_actuales() {
        return medicamentos_actuales;
    }

    public void setMedicamentos_actuales(String medicamentos_actuales) {
        this.medicamentos_actuales = medicamentos_actuales;
    }

    public String getNombre_contacto() {
        return nombre_contacto;
    }

    public void setNombre_contacto(String nombre_contacto) {
        this.nombre_contacto = nombre_contacto;
    }

    public String getParentesco_contacto() {
        return parentesco_contacto;
    }

    public void setParentesco_contacto(String parentesco_contacto) {
        this.parentesco_contacto = parentesco_contacto;
    }

    public String getTelefono_contacto() {
        return telefono_contacto;
    }

    public void setTelefono_contacto(String telefono_contacto) {
        this.telefono_contacto = telefono_contacto;
    }

    public String getToken_verificacion() {
        return token_verificacion;
    }

    public void setToken_verificacion(String token_verificacion) {
        this.token_verificacion = token_verificacion;
    }

    public java.sql.Timestamp getToken_expira() {
        return token_expira;
    }

    public void setToken_expira(java.sql.Timestamp token_expira) {
        this.token_expira = token_expira;
    }
}
