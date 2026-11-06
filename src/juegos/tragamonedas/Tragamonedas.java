package broadway.juegos.tragamonedas;

import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Máquina tragamonedas clásica de 3 carretes, 3 filas visibles y 5 líneas de pago.
 *
 * Como en una máquina real, cada carrete es una tira de símbolos con distinta
 * cantidad de cada uno (los valiosos aparecen menos). El retorno teórico al
 * jugador (RTP) con esta configuración es de aproximadamente 94,4 %.
 */
public class Tragamonedas extends Juego {

    public static final int CARRETES = 3;
    public static final int FILAS = 3;
    public static final int MAX_LINEAS = 5;
    public static final long[] VALORES_LINEA = {1_000, 2_000, 5_000, 10_000, 20_000};

    /** Filas que recorre cada línea en los carretes 1, 2 y 3 (0 = arriba, 1 = centro, 2 = abajo). */
    public static final int[][] LINEAS = {
            {1, 1, 1},  // línea 1: centro
            {0, 0, 0},  // línea 2: arriba
            {2, 2, 2},  // línea 3: abajo
            {0, 1, 2},  // línea 4: diagonal descendente
            {2, 1, 0}   // línea 5: diagonal ascendente
    };

    private final List<List<Simbolo>> carretes = new ArrayList<>();
    private final Random azar;

    public Tragamonedas(Random azar) {
        super("Tragamonedas", 1_000, 100_000);
        this.azar = azar;
        carretes.add(crearCarrete(new int[]{7, 6, 5, 4, 4, 3, 2, 1}, 11));
        carretes.add(crearCarrete(new int[]{6, 6, 6, 5, 3, 3, 2, 1}, 23));
        carretes.add(crearCarrete(new int[]{4, 6, 6, 5, 4, 3, 2, 2}, 37));
    }

    /**
     * Arma la tira de un carrete con la cantidad indicada de cada símbolo
     * (en el orden de {@link Simbolo#values()}) y la mezcla con una semilla fija,
     * para que el carrete sea siempre el mismo, como una máquina física.
     */
    private static List<Simbolo> crearCarrete(int[] cantidades, long semilla) {
        List<Simbolo> tira = new ArrayList<>();
        Simbolo[] simbolos = Simbolo.values();
        for (int i = 0; i < simbolos.length; i++) {
            for (int c = 0; c < cantidades[i]; c++) {
                tira.add(simbolos[i]);
            }
        }
        Collections.shuffle(tira, new Random(semilla));
        return Collections.unmodifiableList(tira);
    }

    public ResultadoGiro girar(Jugador jugador, long valorPorLinea, int lineasActivas) {
        if (lineasActivas < 1 || lineasActivas > MAX_LINEAS) {
            throw new OperacionInvalidaException("Puedes jugar de 1 a " + MAX_LINEAS + " líneas.");
        }
        if (valorPorLinea <= 0) {
            throw new OperacionInvalidaException("El valor por línea debe ser mayor que 0.");
        }
        long apuestaTotal = valorPorLinea * lineasActivas;
        validarApuesta(jugador, apuestaTotal);
        jugador.apostar(apuestaTotal);

        int[] paradas = new int[CARRETES];
        Simbolo[][] ventana = new Simbolo[FILAS][CARRETES];
        for (int c = 0; c < CARRETES; c++) {
            List<Simbolo> tira = carretes.get(c);
            paradas[c] = azar.nextInt(tira.size());
            for (int f = 0; f < FILAS; f++) {
                ventana[f][c] = tira.get(Math.floorMod(paradas[c] + f - 1, tira.size()));
            }
        }

        List<LineaPremiada> premiadas = new ArrayList<>();
        long premioTotal = 0;
        for (int l = 0; l < lineasActivas; l++) {
            Simbolo a = ventana[LINEAS[l][0]][0];
            Simbolo b = ventana[LINEAS[l][1]][1];
            Simbolo c = ventana[LINEAS[l][2]][2];
            int multiplicador = calcularMultiplicador(a, b, c);
            if (multiplicador > 0) {
                long premio = multiplicador * valorPorLinea;
                premioTotal += premio;
                premiadas.add(new LineaPremiada(l + 1, describir(a, b, c), multiplicador, premio));
            }
        }
        ResultadoRonda ronda = liquidar(jugador, apuestaTotal, premioTotal);
        return new ResultadoGiro(ventana, paradas, premiadas, ronda);
    }

    /** Tabla de pagos: tres iguales, dos cerezas a la izquierda o una cereza en el primer carrete. */
    public static int calcularMultiplicador(Simbolo a, Simbolo b, Simbolo c) {
        if (a == b && b == c) {
            return a.getPremioTres();
        }
        if (a == Simbolo.CEREZA && b == Simbolo.CEREZA) {
            return Simbolo.PREMIO_DOS_CEREZAS;
        }
        if (a == Simbolo.CEREZA) {
            return Simbolo.PREMIO_UNA_CEREZA;
        }
        return 0;
    }

    private static String describir(Simbolo a, Simbolo b, Simbolo c) {
        if (a == b && b == c) {
            return "3 × " + a.getNombre();
        }
        if (a == Simbolo.CEREZA && b == Simbolo.CEREZA) {
            return "2 × Cereza";
        }
        return "1 × Cereza";
    }

    /** Retorno teórico al jugador calculado exactamente a partir de las tiras de los carretes. */
    public double calcularRtpTeorico() {
        Map<Simbolo, Integer>[] conteos = contarSimbolos();
        double n = 1.0;
        for (List<Simbolo> tira : carretes) {
            n *= tira.size();
        }
        double esperado = 0;
        for (Simbolo a : Simbolo.values()) {
            for (Simbolo b : Simbolo.values()) {
                for (Simbolo c : Simbolo.values()) {
                    double casos = (double) conteos[0].get(a) * conteos[1].get(b) * conteos[2].get(c);
                    esperado += casos / n * calcularMultiplicador(a, b, c);
                }
            }
        }
        return esperado;
    }

    @SuppressWarnings("unchecked")
    private Map<Simbolo, Integer>[] contarSimbolos() {
        Map<Simbolo, Integer>[] conteos = new Map[CARRETES];
        for (int c = 0; c < CARRETES; c++) {
            conteos[c] = new EnumMap<>(Simbolo.class);
            for (Simbolo s : Simbolo.values()) {
                conteos[c].put(s, 0);
            }
            for (Simbolo s : carretes.get(c)) {
                conteos[c].merge(s, 1, Integer::sum);
            }
        }
        return conteos;
    }

    /** Tira completa de un carrete (para dibujar la animación). */
    public List<Simbolo> getCarrete(int indice) {
        return carretes.get(indice);
    }

    @Override
    public String getDescripcion() {
        return "3 carretes, 5 líneas de pago y un premio mayor de 500 veces la línea.";
    }

    @Override
    public String getReglas() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRAGAMONEDAS — 3 carretes, 3 filas, hasta 5 líneas\n\n");
        sb.append("1. Escoge el valor por línea y cuántas líneas quieres jugar (1 a 5).\n");
        sb.append("   La apuesta total es valor por línea × líneas.\n");
        sb.append("2. Las líneas son: 1 centro, 2 arriba, 3 abajo, 4 y 5 las diagonales.\n");
        sb.append("3. Solo pagan las líneas activas, y cada línea paga solo su mejor combinación.\n\n");
        sb.append("TABLA DE PAGOS (veces la apuesta de la línea)\n");
        Simbolo[] s = Simbolo.values();
        for (int i = s.length - 1; i >= 0; i--) {
            sb.append(String.format("   3 × %-9s %5d%n", s[i].getNombre(), s[i].getPremioTres()));
        }
        sb.append(String.format("   2 × Cereza (carretes 1 y 2) %d%n", Simbolo.PREMIO_DOS_CEREZAS));
        sb.append(String.format("   1 × Cereza (carrete 1)      %d%n", Simbolo.PREMIO_UNA_CEREZA));
        sb.append("\nApuesta mínima ").append(Formato.pesos(getApuestaMinima()))
                .append(", máxima ").append(Formato.pesos(getApuestaMaxima())).append(" por giro.\n");
        sb.append(String.format("Retorno teórico al jugador: %.1f %%.", calcularRtpTeorico() * 100));
        return sb.toString();
    }
}
