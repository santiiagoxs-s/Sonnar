package Correo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * ==========================================================================
 * ENVÍO DE CORREO — API HTTP DE RESEND
 *
 * VitalBoost corre en Railway, y los planes Free/Trial/Hobby de Railway
 * bloquean las conexiones SMTP salientes (puertos 25, 465, 587) para
 * prevenir spam/abuso — ver https://docs.railway.com/networking/outbound-networking.
 * Por eso el envío por SMTP crudo se quedaba colgado hasta hacer timeout.
 *
 * Esta versión usa la API HTTP de Resend (https://api.resend.com/emails),
 * que viaja por HTTPS normal (puerto 443) y no depende de ningún puerto
 * bloqueado. Solo usa java.net.http.HttpClient, incluido en el JDK desde
 * Java 11 (el proyecto compila con Java 17), así que no hay que agregar
 * ninguna librería nueva a web/WEB-INF/lib ni tocar el Dockerfile.
 *
 * CONFIGURACIÓN (variables de entorno; en Railway se agregan en la pestaña
 * "Variables" del servicio, igual que MYSQLHOST etc.):
 *
 *   RESEND_API_KEY    API key generada en el dashboard de Resend (re_...)
 *   MAIL_FROM         correo remitente en tu dominio verificado
 *                      (ej: verificacion@vitalboost.space)
 *   MAIL_FROM_NOMBRE  nombre visible del remitente (por defecto "VitalBoost")
 *
 * Si RESEND_API_KEY no está configurada (por ejemplo mientras se desarrolla
 * en local sin credenciales a mano), esta clase NO lanza error: simplemente
 * deja constancia en consola de que el correo no se envió, para que el
 * registro nunca se rompa por falta de configuración de correo.
 * ==========================================================================
 */
public class EnvioCorreo {

    private static final String RESEND_API_KEY = obtenerVariable("RESEND_API_KEY", null);
    private static final String REMITENTE = obtenerVariable("MAIL_FROM", null);
    private static final String REMITENTE_NOMBRE = obtenerVariable("MAIL_FROM_NOMBRE", "VitalBoost");

    private static final String RESEND_URL = "https://api.resend.com/emails";

    private static final HttpClient CLIENTE_HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static String obtenerVariable(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    /**
     * @return true si el correo se configuró (API key y remitente presentes).
     */
    public static boolean estaConfigurado() {
        return RESEND_API_KEY != null && REMITENTE != null;
    }

    // ======================================================================
    // CORREO DE VERIFICACIÓN DE CUENTA
    // ======================================================================
    public static boolean enviarVerificacion(String destinatario, String nombrePaciente, String urlVerificacion) {

        String asunto = "Verifica tu correo en VitalBoost";

        // Ícono de check dentro del círculo verde (mismo lenguaje visual que
        // .exito-icono de registroExitoso.css: "acción completada / positiva").
        String iconoSvg = "<svg width=\"30\" height=\"30\" viewBox=\"0 0 24 24\" fill=\"none\" "
                + "stroke=\"#ffffff\" stroke-width=\"3\" stroke-linecap=\"round\" stroke-linejoin=\"round\">"
                + "<path d=\"M20 6 9 17l-5-5\"/></svg>";

        String cuerpoHtml = plantillaCorreo(
                iconoSvg,
                "#17c993", "#0ea472",
                "Confirma tu correo",
                nombrePaciente,
                "Gracias por registrarte en VitalBoost. Tu cuenta y tu código QR de emergencia "
                        + "todav\u00eda no se han creado: confirma que este correo es tuyo haciendo clic "
                        + "en el siguiente bot\u00f3n para activarlos.",
                "Verificar mi correo",
                urlVerificacion,
                "Este enlace vence en 24 horas. Si no confirmas tu correo antes de que venza, "
                        + "tendr\u00e1s que registrarte de nuevo."
        );

        return enviar(destinatario, asunto, cuerpoHtml);
    }

    // ======================================================================
    // CORREO DE RECUPERACIÓN DE CONTRASEÑA
    // ======================================================================
    public static boolean enviarRecuperacion(String destinatario, String nombreUsuario, String urlRecuperacion) {

        String asunto = "Restablece tu contraseña en VitalBoost";

        // Ícono de candado dentro del círculo azul (mismo candado que ya se
        // usa en olvidePassword.jsp), para que se lea como "seguridad de tu
        // cuenta" y no como un logro/confirmación.
        String iconoSvg = "<svg width=\"28\" height=\"28\" viewBox=\"0 0 24 24\" fill=\"none\" "
                + "stroke=\"#ffffff\" stroke-width=\"2.4\" stroke-linecap=\"round\" stroke-linejoin=\"round\">"
                + "<rect x=\"3\" y=\"11\" width=\"18\" height=\"10\" rx=\"2\"/>"
                + "<path d=\"M7 11V7a5 5 0 0 1 10 0v4\"/></svg>";

        String cuerpoHtml = plantillaCorreo(
                iconoSvg,
                "#1d3fd6", "#2f5bff",
                "Restablece tu contraseña",
                nombreUsuario,
                "Recibimos una solicitud para restablecer la contrase\u00f1a de tu cuenta en VitalBoost. "
                        + "Haz clic en el siguiente bot\u00f3n para elegir una contrase\u00f1a nueva.",
                "Restablecer contraseña",
                urlRecuperacion,
                "Este enlace vence en 1 hora. Si t\u00fa no solicitaste este cambio, puedes ignorar "
                        + "este correo: tu contrase\u00f1a actual seguir\u00e1 funcionando con normalidad."
        );

        return enviar(destinatario, asunto, cuerpoHtml);
    }

    // ======================================================================
    // PLANTILLA COMPARTIDA DE CORREO
    //
    // Ambos correos (verificación y recuperación) comparten el mismo
    // esqueleto visual: encabezado degradado con el logo de VitalBoost,
    // tarjeta blanca con ícono circular temático, botón de acción y pie de
    // página. Se arma con <table> y estilos en línea (no clases, no <style>
    // en el <head>) porque así es como hay que maquetar correos para que se
    // vean bien en Gmail, Outlook y clientes de correo en general, que no
    // soportan CSS moderno de forma confiable.
    // ======================================================================
    private static String plantillaCorreo(String iconoSvg, String colorIconoDesde, String colorIconoHasta,
            String titulo, String nombre, String mensaje, String textoBoton, String urlBoton, String notaFooter) {

        String nombreSeguro = escaparHtml(nombre);
        String tituloSeguro = escaparHtml(titulo);
        String textoBotonSeguro = escaparHtml(textoBoton);
        String notaSeguro = escaparHtml(notaFooter);

        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#eef2fb;padding:40px 16px;font-family:Arial,Helvetica,sans-serif;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:480px;width:100%;background-color:#ffffff;border-radius:20px;"
                + "overflow:hidden;box-shadow:0 10px 30px rgba(19,36,92,0.10);\">"

                // ---------------- ENCABEZADO CON LOGO ----------------
                + "<tr><td align=\"center\" "
                + "style=\"background-color:#13245c;"
                + "background-image:linear-gradient(135deg,#13245c,#0a1230);padding:26px 32px;\">"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\"><tr>"
                + "<td style=\"padding-right:10px;\">"
                + "<svg width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#ffffff\" "
                + "stroke-width=\"2.2\" stroke-linecap=\"round\" stroke-linejoin=\"round\">"
                + "<path d=\"M20.8 6.6c-1.8-3.4-6.2-4.2-8.8-1.6l-.9.9-.9-.9C7.6 2.4 3.2 3.2 1.4 6.6c-1.5 2.9-.7 6.4 1.9 9l7.7 7.6 7.7-7.6c2.6-2.6 3.4-6.1 1.9-9z\"/>"
                + "<path d=\"M6 12h3l1.5-3 2 6 1.5-3H18\"/></svg>"
                + "</td>"
                + "<td style=\"font-family:Arial,Helvetica,sans-serif;font-size:19px;font-weight:bold;"
                + "color:#ffffff;letter-spacing:.2px;\">Vital<span style=\"font-weight:900;\">Boost</span></td>"
                + "</tr></table>"
                + "</td></tr>"

                // ---------------- CUERPO ----------------
                + "<tr><td style=\"padding:40px 36px 8px;text-align:center;\">"

                // Ícono circular temático
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" "
                + "style=\"margin:0 auto 22px;\"><tr><td width=\"64\" height=\"64\" align=\"center\" valign=\"middle\" "
                + "style=\"width:64px;height:64px;border-radius:50%;background-color:" + colorIconoDesde + ";"
                + "background-image:linear-gradient(135deg," + colorIconoDesde + "," + colorIconoHasta + ");\">"
                + iconoSvg
                + "</td></tr></table>"

                + "<h1 style=\"margin:0 0 14px;font-family:Arial,Helvetica,sans-serif;font-size:22px;"
                + "font-weight:800;color:#16203f;\">" + tituloSeguro + "</h1>"

                + "<p style=\"margin:0 0 6px;font-family:Arial,Helvetica,sans-serif;font-size:15px;"
                + "line-height:1.6;color:#5a6584;text-align:left;\">Hola, <strong style=\"color:#16203f;\">"
                + nombreSeguro + "</strong>.</p>"

                + "<p style=\"margin:0 0 28px;font-family:Arial,Helvetica,sans-serif;font-size:15px;"
                + "line-height:1.6;color:#5a6584;text-align:left;\">" + mensaje + "</p>"

                // Botón de acción
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" "
                + "style=\"margin:0 auto 28px;\"><tr><td align=\"center\" "
                + "style=\"border-radius:12px;background-color:#2f5bff;"
                + "background-image:linear-gradient(135deg,#1d3fd6,#2f5bff);"
                + "box-shadow:0 10px 22px rgba(47,91,255,0.30);\">"
                + "<a href=\"" + urlBoton + "\" target=\"_blank\" "
                + "style=\"display:inline-block;padding:15px 30px;font-family:Arial,Helvetica,sans-serif;"
                + "font-size:15px;font-weight:bold;color:#ffffff;text-decoration:none;\">"
                + textoBotonSeguro + "</a></td></tr></table>"

                + "<p style=\"margin:0 0 4px;font-family:Arial,Helvetica,sans-serif;font-size:12.5px;"
                + "line-height:1.6;color:#8b93ad;text-align:left;\">Si el bot\u00f3n no funciona, copia y "
                + "pega este enlace en tu navegador:</p>"
                + "<p style=\"margin:0 0 26px;font-family:Arial,Helvetica,sans-serif;font-size:12px;"
                + "line-height:1.5;text-align:left;word-break:break-all;\">"
                + "<a href=\"" + urlBoton + "\" style=\"color:#2f5bff;text-decoration:underline;\">"
                + urlBoton + "</a></p>"

                // Nota de aviso (vencimiento / seguridad)
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"background-color:#f2f6ff;border-radius:12px;margin:0 0 6px;\">"
                + "<tr><td style=\"padding:14px 16px;font-family:Arial,Helvetica,sans-serif;font-size:12.5px;"
                + "line-height:1.55;color:#5a6584;text-align:left;\">" + notaSeguro + "</td></tr></table>"

                + "</td></tr>"

                // ---------------- PIE DE PÁGINA ----------------
                + "<tr><td align=\"center\" style=\"padding:22px 36px 28px;\">"
                + "<p style=\"margin:0;font-family:Arial,Helvetica,sans-serif;font-size:11.5px;"
                + "color:#9aa3bd;text-align:center;\">"
                + "VitalBoost &middot; Historial m\u00e9dico y c\u00f3digo QR de emergencia<br>"
                + "Si no esperabas este correo, puedes ignorarlo con confianza.</p>"
                + "</td></tr>"

                + "</table></td></tr></table>";
    }

    private static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ======================================================================
    // ENVÍO GENÉRICO VÍA API HTTP DE RESEND
    // ======================================================================
    private static boolean enviar(String destinatario, String asunto, String cuerpoHtml) {

        if (!estaConfigurado()) {
            System.out.println("--- CORREO NO CONFIGURADO (faltan RESEND_API_KEY/MAIL_FROM) ---");
            System.out.println("No se envió el correo a " + destinatario + ". Asunto: " + asunto);
            return false;
        }

        String remitenteCompleto = REMITENTE_NOMBRE + " <" + REMITENTE + ">";

        String jsonBody = "{"
                + "\"from\":\"" + escaparJson(remitenteCompleto) + "\","
                + "\"to\":[\"" + escaparJson(destinatario) + "\"],"
                + "\"subject\":\"" + escaparJson(asunto) + "\","
                + "\"html\":\"" + escaparJson(cuerpoHtml) + "\""
                + "}";

        try {
            HttpRequest peticion = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + RESEND_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> respuesta = CLIENTE_HTTP.send(peticion, HttpResponse.BodyHandlers.ofString());

            int codigo = respuesta.statusCode();
            if (codigo >= 200 && codigo < 300) {
                return true;
            }

            System.err.println("--- ERROR ENVIANDO CORREO A " + destinatario + " (Resend respondió " + codigo + ") ---");
            System.err.println(respuesta.body());
            return false;

        } catch (IOException | InterruptedException e) {
            System.err.println("--- ERROR ENVIANDO CORREO A " + destinatario + " ---");
            e.printStackTrace();
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    // Escapa comillas, barras invertidas y saltos de línea para poder meter
    // el texto dentro de un string JSON sin romper el formato.
    private static String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder resultado = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"':
                    resultado.append("\\\"");
                    break;
                case '\\':
                    resultado.append("\\\\");
                    break;
                case '\n':
                    resultado.append("\\n");
                    break;
                case '\r':
                    resultado.append("\\r");
                    break;
                case '\t':
                    resultado.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        resultado.append(String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
            }
        }
        return resultado.toString();
    }
}
