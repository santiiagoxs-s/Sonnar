package Utilidades;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ==================================================================
// UTILIDAD JSON GENÉRICA Y DINÁMICA
//
// El proyecto no trae ninguna librería JSON (Gson/Jackson) entre sus
// dependencias (solo JSTL y el driver de MySQL), así que en vez de
// forzar una dependencia nueva que pueda romper el build/despliegue,
// esta clase arma el JSON a mano de forma segura (escapando texto
// correctamente) y sirve para CUALQUIER endpoint nuevo que se quiera
// agregar más adelante, no solo para el login o el QR.
//
// Uso típico:
//
//   Map<String, Object> json = new LinkedHashMap<>();
//   json.put("success", true);
//   json.put("mensaje", "Login correcto");
//   json.put("usuario", datosUsuario); // otro Map, o una List, etc.
//   JsonUtil.escribir(response, json);
// ==================================================================
public final class JsonUtil {

    private JsonUtil() {
    }

    // Punto de entrada más común: recibe un Map "dinámico" (clave -> valor)
    // y lo convierte en un objeto JSON completo.
    public static String toJson(Map<String, Object> datos) {
        StringBuilder sb = new StringBuilder();
        escribirValor(sb, datos);
        return sb.toString();
    }

    // Permite construir un Map en una sola línea desde pares clave/valor,
    // por ejemplo: mapa("success", true, "token", token)
    public static Map<String, Object> mapa(Object... paresClaveValor) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < paresClaveValor.length - 1; i += 2) {
            m.put(String.valueOf(paresClaveValor[i]), paresClaveValor[i + 1]);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static void escribirValor(StringBuilder sb, Object valor) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof Map) {
            escribirObjeto(sb, (Map<String, Object>) valor);
        } else if (valor instanceof List) {
            escribirArreglo(sb, (List<Object>) valor);
        } else if (valor instanceof Object[]) {
            escribirArreglo(sb, List.of((Object[]) valor));
        } else if (valor instanceof Number || valor instanceof Boolean) {
            sb.append(valor.toString());
        } else {
            // Cualquier otra cosa (String, enums, fechas, etc.) se trata como texto
            escribirTexto(sb, valor.toString());
        }
    }

    private static void escribirObjeto(StringBuilder sb, Map<String, Object> mapa) {
        sb.append('{');
        boolean primero = true;
        for (Map.Entry<String, Object> entrada : mapa.entrySet()) {
            if (!primero) {
                sb.append(',');
            }
            primero = false;
            escribirTexto(sb, entrada.getKey());
            sb.append(':');
            escribirValor(sb, entrada.getValue());
        }
        sb.append('}');
    }

    private static void escribirArreglo(StringBuilder sb, List<Object> lista) {
        sb.append('[');
        boolean primero = true;
        for (Object item : lista) {
            if (!primero) {
                sb.append(',');
            }
            primero = false;
            escribirValor(sb, item);
        }
        sb.append(']');
    }

    // Escapa el texto según la especificación de JSON para que nunca se
    // rompa la respuesta, sin importar qué escriba el usuario en un campo
    // (comillas, saltos de línea, acentos, emojis, etc.)
    private static void escribirTexto(StringBuilder sb, String texto) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
