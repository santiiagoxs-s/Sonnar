package Utilidades;

import Modelo.Usuario;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

// ==================================================================
// SESIONES DE LA APP MÓVIL (TOKEN EN MEMORIA)
//
// La web usa HttpSession (cookie de JSESSIONID), pero eso no es lo
// ideal para una app Flutter nativa. Por eso el login desde el
// celular genera un TOKEN aleatorio que el cliente guarda y reenvía
// en cada petición (como header o parámetro "token"); este token
// identifica al usuario logueado sin depender de cookies.
//
// Es intencionalmente simple (un mapa en memoria con expiración) para
// no depender de librerías externas (JWT, etc.) que el proyecto no
// tiene instaladas. Si el servidor se reinicia, los tokens activos se
// pierden y la app simplemente vuelve a pedir login, igual que pasaría
// si expirara una sesión de HttpSession normal.
// ==================================================================
public final class SesionMovilStore {

    // Duración de la sesión móvil: 12 horas de inactividad.
    private static final long DURACION_MS = 12L * 60 * 60 * 1000;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, Entrada> TOKENS = new ConcurrentHashMap<>();

    private SesionMovilStore() {
    }

    private static class Entrada {
        final Usuario usuario;
        volatile long expiraEn;

        Entrada(Usuario usuario, long expiraEn) {
            this.usuario = usuario;
            this.expiraEn = expiraEn;
        }
    }

    // Crea un token nuevo para el usuario que acaba de iniciar sesión.
    public static String crearToken(Usuario usuario) {
        limpiarExpirados();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TOKENS.put(token, new Entrada(usuario, System.currentTimeMillis() + DURACION_MS));
        return token;
    }

    // Devuelve el usuario asociado a un token válido, o null si no existe,
    // venció, o el token viene vacío. Renueva la expiración (sesión
    // "deslizante": mientras la app se siga usando, no se cierra sola).
    public static Usuario obtenerUsuario(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Entrada entrada = TOKENS.get(token);
        if (entrada == null) {
            return null;
        }
        if (entrada.expiraEn < System.currentTimeMillis()) {
            TOKENS.remove(token);
            return null;
        }
        entrada.expiraEn = System.currentTimeMillis() + DURACION_MS;
        return entrada.usuario;
    }

    // Reemplaza los datos del usuario guardados bajo un token (por ejemplo
    // después de recargarlo desde la base de datos con info actualizada).
    public static void actualizarUsuario(String token, Usuario usuario) {
        Entrada entrada = TOKENS.get(token);
        if (entrada != null) {
            TOKENS.put(token, new Entrada(usuario, entrada.expiraEn));
        }
    }

    public static void invalidar(String token) {
        if (token != null) {
            TOKENS.remove(token);
        }
    }

    private static void limpiarExpirados() {
        long ahora = System.currentTimeMillis();
        TOKENS.entrySet().removeIf(e -> e.getValue().expiraEn < ahora);
    }
}
