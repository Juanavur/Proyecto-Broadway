package persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector y escritor de JSON mínimo, sin librerías externas.
 * Un objeto JSON se representa como Map&lt;String, Object&gt;, un arreglo como List&lt;Object&gt;,
 * y los valores como String, Double, Boolean o null.
 */
public final class Json {

    private Json() {
    }

    // ------------------------------------------------------------------ escribir

    /** Convierte mapas, listas, textos, números y booleanos en JSON con sangría. */
    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb, 0);
        return sb.append('\n').toString();
    }

    private static void escribir(Object valor, StringBuilder sb, int nivel) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String) {
            texto((String) valor, sb);
        } else if (valor instanceof Boolean) {
            sb.append(valor);
        } else if (valor instanceof Number) {
            numero(((Number) valor).doubleValue(), sb);
        } else if (valor instanceof Map) {
            Map<?, ?> mapa = (Map<?, ?>) valor;
            if (mapa.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<?, ?> e : mapa.entrySet()) {
                sangria(sb, nivel + 1);
                texto(String.valueOf(e.getKey()), sb);
                sb.append(": ");
                escribir(e.getValue(), sb, nivel + 1);
                sb.append(++i < mapa.size() ? ",\n" : "\n");
            }
            sangria(sb, nivel);
            sb.append('}');
        } else if (valor instanceof List) {
            List<?> lista = (List<?>) valor;
            if (lista.isEmpty()) {
                sb.append("[]");
                return;
            }
            boolean simple = lista.stream().noneMatch(v -> v instanceof Map || v instanceof List);
            if (simple) {
                sb.append('[');
                for (int i = 0; i < lista.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    escribir(lista.get(i), sb, nivel);
                }
                sb.append(']');
                return;
            }
            sb.append("[\n");
            for (int i = 0; i < lista.size(); i++) {
                sangria(sb, nivel + 1);
                escribir(lista.get(i), sb, nivel + 1);
                sb.append(i + 1 < lista.size() ? ",\n" : "\n");
            }
            sangria(sb, nivel);
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipo no soportado en JSON: " + valor.getClass().getSimpleName());
        }
    }

    private static void numero(double d, StringBuilder sb) {
        if (!Double.isFinite(d)) {
            throw new IllegalArgumentException("Número no válido para JSON: " + d);
        }
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            sb.append((long) d);
        } else {
            sb.append(d);
        }
    }

    private static void texto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
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

    private static void sangria(StringBuilder sb, int nivel) {
        for (int i = 0; i < nivel; i++) {
            sb.append("  ");
        }
    }

    // ------------------------------------------------------------------ leer

    /** Lee un texto JSON. Lanza IllegalArgumentException con la posición si está mal formado. */
    public static Object leer(String json) {
        Lector lector = new Lector(json);
        Object valor = lector.valor();
        lector.espacios();
        if (lector.pos != json.length()) {
            throw lector.error("Sobra texto después del JSON");
        }
        return valor;
    }

    private static final class Lector {

        private final String s;
        private int pos;

        Lector(String s) {
            this.s = s;
        }

        Object valor() {
            espacios();
            if (pos >= s.length()) {
                throw error("El JSON terminó antes de tiempo");
            }
            char c = s.charAt(pos);
            switch (c) {
                case '{': return objeto();
                case '[': return arreglo();
                case '"': return texto();
                case 't': return palabra("true", Boolean.TRUE);
                case 'f': return palabra("false", Boolean.FALSE);
                case 'n': return palabra("null", null);
                default:
                    if (c == '-' || Character.isDigit(c)) {
                        return numero();
                    }
                    throw error("Carácter inesperado '" + c + "'");
            }
        }

        Map<String, Object> objeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            pos++;
            espacios();
            if (siguiente('}')) {
                return mapa;
            }
            do {
                espacios();
                if (pos >= s.length() || s.charAt(pos) != '"') {
                    throw error("Se esperaba el nombre de un campo");
                }
                String clave = texto();
                espacios();
                if (!siguiente(':')) {
                    throw error("Se esperaba ':'");
                }
                mapa.put(clave, valor());
                espacios();
            } while (siguiente(','));
            if (!siguiente('}')) {
                throw error("Se esperaba '}'");
            }
            return mapa;
        }

        List<Object> arreglo() {
            List<Object> lista = new ArrayList<>();
            pos++;
            espacios();
            if (siguiente(']')) {
                return lista;
            }
            do {
                lista.add(valor());
                espacios();
            } while (siguiente(','));
            if (!siguiente(']')) {
                throw error("Se esperaba ']'");
            }
            return lista;
        }

        String texto() {
            StringBuilder sb = new StringBuilder();
            pos++;
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (pos >= s.length()) {
                    break;
                }
                char e = s.charAt(pos++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > s.length()) {
                            throw error("Escape \\u incompleto");
                        }
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: throw error("Escape no válido \\" + e);
                }
            }
            throw error("Texto sin cerrar");
        }

        Double numero() {
            int inicio = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            try {
                return Double.valueOf(s.substring(inicio, pos));
            } catch (NumberFormatException e) {
                throw error("Número no válido");
            }
        }

        Object palabra(String palabra, Object valor) {
            if (!s.startsWith(palabra, pos)) {
                throw error("Se esperaba " + palabra);
            }
            pos += palabra.length();
            return valor;
        }

        boolean siguiente(char c) {
            if (pos < s.length() && s.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        void espacios() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        IllegalArgumentException error(String mensaje) {
            return new IllegalArgumentException(mensaje + " (posición " + pos + ")");
        }
    }
}
