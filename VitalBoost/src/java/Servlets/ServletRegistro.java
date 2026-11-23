package Servlets;

import Controlador.Tipo_DocumentoDAO;
import Controlador.Tipo_sangreDAO;
import Controlador.UsuarioDAO;
import Controlador.RolesDAO;
import Controlador.RegistroPendienteDAO;
import Correo.EnvioCorreo;

import Modelo.Roles;
import Modelo.RegistroPendiente;
import Utilidades.PasswordUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ServletRegistro", urlPatterns = {"/ServletRegistro"})
public class ServletRegistro extends HttpServlet {

    UsuarioDAO udao = new UsuarioDAO();
    RolesDAO rdao = new RolesDAO();
    RegistroPendienteDAO pdao = new RegistroPendienteDAO();

    // =========================
    // LÍMITES Y PATRONES DE VALIDACIÓN
    // =========================
    // Correo: formato válido y máximo 100 caracteres (evita registros con
    // datos basura o cadenas exageradamente largas).
    private static final int EMAIL_MAX = 100;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Contraseña: entre 8 y 20 caracteres, con al menos una mayúscula,
    // una minúscula, un número y un carácter especial.
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 20;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{" + PASSWORD_MIN + "," + PASSWORD_MAX + "}$");

    // Número de documento: solo dígitos, entre 6 y 15 caracteres.
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^\\d{6,15}$");

    // Teléfono de contacto de emergencia: solo dígitos, entre 7 y 15 caracteres.
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{7,15}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {

            // =========================
            // RECIBIR DATOS
            // =========================

            String nombre = request.getParameter("txtNombre");
            String email = request.getParameter("txtEmail");
            String password = request.getParameter("txtPassword");
            String fecha = request.getParameter("txtFecha");
            String alergias = request.getParameter("txtAlergias");
            String eps = request.getParameter("txtEps");
            String medicamentos = request.getParameter("txtMedicamentos");
            String numeroDocumento = request.getParameter("txtNumeroDocumento");

            String tipoDoc = request.getParameter("txtIdTipoDoc");
            String tipoSangre = request.getParameter("txtIdSangre");

            String nombreContacto = request.getParameter("txtNombreContacto");
            String parentescoContacto = request.getParameter("txtParentescoContacto");
            String telefonoContacto = request.getParameter("txtTelefonoContacto");

            // El registro público siempre corresponde al rol Paciente.
            // El único rol que inicia sesión con credenciales es el Administrador.
            // Se busca el rol por NOMBRE porque los roles se administran
            // libremente desde el panel de admin y su id puede variar.
            Roles rolPaciente = rdao.obtenerPorNombre("Paciente");

            if (rolPaciente == null) {

                request.setAttribute("error",
                        "No existe un rol 'Paciente' configurado en el sistema. "
                        + "Contacta al administrador.");

                request.setAttribute("listaDocumentos",
                        new Tipo_DocumentoDAO().listar());

                request.setAttribute("listaSangres",
                        new Tipo_sangreDAO().listar());

                request.getRequestDispatcher("/vistas/registro.jsp")
                        .forward(request, response);

                return;
            }

            boolean hayError = false;

            // =========================
            // VALIDAR NOMBRE
            // =========================

            if (nombre == null || nombre.isBlank()) {

                request.setAttribute("errorNombre",
                        "El nombre es obligatorio.");

                hayError = true;
            }

            // =========================
            // VALIDAR EMAIL
            // =========================

            if (email == null || email.isBlank()) {

                request.setAttribute("errorEmail",
                        "El correo es obligatorio.");

                hayError = true;

            } else if (email.length() > EMAIL_MAX) {

                request.setAttribute("errorEmail",
                        "El correo no puede superar los " + EMAIL_MAX + " caracteres.");

                hayError = true;

            } else if (!EMAIL_PATTERN.matcher(email).matches()) {

                request.setAttribute("errorEmail",
                        "Ingresa un correo electrónico válido.");

                hayError = true;
            }

            // =========================
            // VALIDAR PASSWORD
            // =========================

            if (password == null || password.isBlank()) {

                request.setAttribute("errorPassword",
                        "La contraseña es obligatoria.");

                hayError = true;

            } else if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {

                request.setAttribute("errorPassword",
                        "La contraseña debe tener entre " + PASSWORD_MIN + " y " + PASSWORD_MAX + " caracteres.");

                hayError = true;

            } else if (!PASSWORD_PATTERN.matcher(password).matches()) {

                request.setAttribute("errorPassword",
                        "La contraseña debe incluir mayúscula, minúscula, número y carácter especial.");

                hayError = true;
            }

            // =========================
            // VALIDAR DOCUMENTO
            // =========================

            if (numeroDocumento == null || numeroDocumento.isBlank()) {

                request.setAttribute("errorDocumento",
                        "El documento es obligatorio.");

                hayError = true;

            } else if (!DOCUMENTO_PATTERN.matcher(numeroDocumento).matches()) {

                request.setAttribute("errorDocumento",
                        "El documento debe contener solo números (entre 6 y 15 dígitos).");

                hayError = true;
            }

            // =========================
            // VALIDAR FECHA
            // =========================

            if (fecha == null || fecha.isBlank()) {

                request.setAttribute("errorFecha",
                        "La fecha es obligatoria.");

                hayError = true;
            }

            // =========================
            // VALIDAR TIPO DOC
            // =========================

            if (tipoDoc == null || tipoDoc.isBlank()) {

                request.setAttribute("errorTipoDoc",
                        "Seleccione un tipo de documento.");

                hayError = true;
            }

            // =========================
            // VALIDAR SANGRE
            // =========================

            if (tipoSangre == null || tipoSangre.isBlank()) {

                request.setAttribute("errorSangre",
                        "Seleccione un tipo de sangre.");

                hayError = true;
            }

            // =========================
            // VALIDAR CONTACTO DE EMERGENCIA
            // =========================

            if (nombreContacto == null || nombreContacto.isBlank()) {

                request.setAttribute("errorNombreContacto",
                        "El nombre del contacto de emergencia es obligatorio.");

                hayError = true;
            }

            if (telefonoContacto == null || telefonoContacto.isBlank()) {

                request.setAttribute("errorTelefonoContacto",
                        "El número de emergencia es obligatorio.");

                hayError = true;

            } else if (!TELEFONO_PATTERN.matcher(telefonoContacto).matches()) {

                request.setAttribute("errorTelefonoContacto",
                        "El número de emergencia debe contener solo números (entre 7 y 15 dígitos).");

                hayError = true;
            }

            // =========================
            // VALIDAR DUPLICADOS (cuenta ya creada o registro pendiente
            // de verificación con el mismo correo/documento)
            // =========================

            if (!hayError) {

                if (email != null && !email.isBlank()) {

                    if (udao.buscarPorEmail(email) != null) {

                        request.setAttribute("errorEmail",
                                "Ese correo ya está registrado. Inicia sesión o recupera tu cuenta.");

                        hayError = true;

                    } else {

                        RegistroPendiente pendienteEmail = pdao.buscarPorEmail(email);

                        if (pendienteEmail != null) {

                            if (pendienteEmail.getToken_expira() != null
                                    && pendienteEmail.getToken_expira()
                                            .before(new java.sql.Timestamp(System.currentTimeMillis()))) {

                                // El registro pendiente anterior venció: se descarta
                                // para permitir que la persona vuelva a intentarlo.
                                pdao.eliminar(pendienteEmail.getId_pendiente());

                            } else {

                                request.setAttribute("errorEmail",
                                        "Ya enviamos un correo de verificación a esta dirección. "
                                        + "Revisa tu bandeja de entrada o solicita un reenvío desde el "
                                        + "inicio de sesión.");

                                hayError = true;
                            }
                        }
                    }
                }

                if (!hayError && numeroDocumento != null && !numeroDocumento.isBlank()) {

                    if (udao.buscarPorDocumento(numeroDocumento) != null) {

                        request.setAttribute("errorDocumento",
                                "Ya existe una cuenta registrada con ese número de documento.");

                        hayError = true;

                    } else {

                        RegistroPendiente pendienteDoc = pdao.buscarPorDocumento(numeroDocumento);

                        if (pendienteDoc != null) {

                            if (pendienteDoc.getToken_expira() != null
                                    && pendienteDoc.getToken_expira()
                                            .before(new java.sql.Timestamp(System.currentTimeMillis()))) {

                                pdao.eliminar(pendienteDoc.getId_pendiente());

                            } else {

                                request.setAttribute("errorDocumento",
                                        "Ya hay un registro pendiente de verificación con ese documento.");

                                hayError = true;
                            }
                        }
                    }
                }
            }

            // =========================
            // SI HAY ERRORES
            // =========================

            if (hayError) {

                request.setAttribute("listaDocumentos",
                        new Tipo_DocumentoDAO().listar());

                request.setAttribute("listaSangres",
                        new Tipo_sangreDAO().listar());

                request.getRequestDispatcher("/vistas/registro.jsp")
                        .forward(request, response);

                return;
            }

            // =========================
            // CREAR OBJETO (registro pendiente, todavía NO es una cuenta)
            //
            // La cuenta (Usuario) y su contacto de emergencia se crean
            // recién cuando el paciente confirma el correo, ver
            // ServletVerificarEmail. Así, mientras no verifique, no existe
            // ningún dato suyo en Usuarios ni un QR de emergencia activo.
            // =========================

            RegistroPendiente rp = new RegistroPendiente();

            rp.setNombre_completo(nombre);
            rp.setEmail(email);
            // Se guarda el hash, nunca la contraseña en texto plano, ni
            // siquiera en la tabla de registros pendientes.
            rp.setPassword(PasswordUtil.hash(password));
            rp.setFecha_nacimiento(fecha);
            rp.setAlergias_conocidas(alergias);
            rp.setEps(eps);
            rp.setMedicamentos_actuales(medicamentos);
            rp.setNumero_documento(numeroDocumento);

            rp.setId_tipo_documento(Integer.parseInt(tipoDoc));
            rp.setId_tipo_sangre(Integer.parseInt(tipoSangre));
            rp.setId_rol(rolPaciente.getId_rol());

            rp.setNombre_contacto(nombreContacto);
            rp.setParentesco_contacto(parentescoContacto);
            rp.setTelefono_contacto(telefonoContacto);

            // =========================
            // INSERTAR (con token de verificación de correo)
            // =========================

            String tokenVerificacion = UUID.randomUUID().toString().replace("-", "");

            int r = pdao.insertar(rp, tokenVerificacion);

            if (r > 0) {

                // ==================================================
                // ENVIAR CORREO DE VERIFICACIÓN
                //
                // Se manda en un hilo aparte para que el registro NUNCA
                // quede esperando al servidor de correo (aunque ya tiene
                // timeout, un SMTP lento no debe demorar la respuesta al
                // usuario). No bloquea el registro si el envío falla: el
                // paciente puede pedir un reenvío desde el login. La
                // cuenta definitiva (y su QR de emergencia) solo se crean
                // cuando se hace clic en el enlace de este correo.
                // ==================================================

                String urlBase = request.getScheme() + "://"
                        + request.getServerName()
                        + ":" + request.getServerPort()
                        + request.getContextPath();

                String urlVerificacion = urlBase + "/VerificarEmail?token=" + tokenVerificacion;

                final String emailFinal = email;
                final String nombreFinal = nombre;
                new Thread(() -> {
                    try {
                        EnvioCorreo.enviarVerificacion(emailFinal, nombreFinal, urlVerificacion);
                    } catch (Exception correoEx) {
                        correoEx.printStackTrace();
                    }
                }).start();

                request.setAttribute("nombrePaciente", nombre);
                request.setAttribute("emailPaciente", email);

                request.getRequestDispatcher("/vistas/registroExitoso.jsp")
                        .forward(request, response);

            } else {

                request.setAttribute("error",
                        "No se pudo registrar el usuario.");

                request.setAttribute("listaDocumentos",
                        new Tipo_DocumentoDAO().listar());

                request.setAttribute("listaSangres",
                        new Tipo_sangreDAO().listar());

                request.getRequestDispatcher("/vistas/registro.jsp")
                        .forward(request, response);
            }

        } catch (Exception e) {

            e.printStackTrace();

            request.setAttribute("error", e.getMessage());

            request.setAttribute("listaDocumentos",
                    new Tipo_DocumentoDAO().listar());

            request.setAttribute("listaSangres",
                    new Tipo_sangreDAO().listar());

            request.getRequestDispatcher("/vistas/registro.jsp")
                    .forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("listaDocumentos",
                new Tipo_DocumentoDAO().listar());

        request.setAttribute("listaSangres",
                new Tipo_sangreDAO().listar());

        request.getRequestDispatcher("/vistas/registro.jsp")
                .forward(request, response);
    }
}