package Utilidades;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad centralizada para el manejo seguro de contraseñas.
 *
 * Nunca se debe guardar ni comparar una contraseña en texto plano.
 * Esta clase concentra toda la lógica de hashing (BCrypt) para que
 * los Servlets y DAOs no manipulen contraseñas directamente.
 */
public class PasswordUtil {

    // Factor de costo de BCrypt (10 es el valor por defecto recomendado;
    // subirlo hace el hash más lento y más resistente a fuerza bruta).
    private static final int COST_FACTOR = 12;

    private PasswordUtil() {
    }

    /**
     * Genera el hash BCrypt de una contraseña en texto plano.
     * Este es el valor que se debe guardar en la base de datos,
     * nunca la contraseña original.
     */
    public static String hash(String passwordPlano) {
        if (passwordPlano == null) {
            throw new IllegalArgumentException("La contraseña no puede ser null.");
        }
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(COST_FACTOR));
    }

    /**
     * Verifica que una contraseña en texto plano corresponda al hash
     * guardado en la base de datos. Se usa en login y en cualquier
     * punto donde se le pida al usuario su contraseña actual.
     */
    public static boolean verificar(String passwordPlano, String hashGuardado) {
        if (passwordPlano == null || hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(passwordPlano, hashGuardado);
        } catch (IllegalArgumentException e) {
            // El hash guardado no tiene formato BCrypt válido
            // (por ejemplo, una contraseña vieja aún en texto plano).
            return false;
        }
    }

    /**
     * Indica si un valor ya tiene formato de hash BCrypt
     * (útil para migrar datos viejos en texto plano sin re-hashear
     * algo que ya está hasheado).
     */
    public static boolean esHashBcrypt(String valor) {
        return valor != null && (valor.startsWith("$2a$")
                || valor.startsWith("$2b$")
                || valor.startsWith("$2y$"));
    }
}
