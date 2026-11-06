package broadway.juegos;

import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import java.util.List;

/**
 * Clase base de todos los juegos del casino.
 *
 * Todos los juegos comparten estado (nombre y límites de apuesta) y la forma de
 * cerrar una ronda (cobrar el premio, registrar el resultado y calcular el cashback),
 * pero cada uno tiene sus propias reglas. Por eso es abstracta: no existe un
 * "juego" genérico, solo Blackjack, Ruleta, etc.
 */
public abstract class Juego {

    private final String nombre;
    private final long apuestaMinima;
    private final long apuestaMaxima;

    protected Juego(String nombre, long apuestaMinima, long apuestaMaxima) {
        if (apuestaMinima <= 0 || apuestaMinima > apuestaMaxima) {
            throw new IllegalArgumentException("La apuesta mínima debe ser positiva y menor que la máxima.");
        }
        this.nombre = nombre;
        this.apuestaMinima = apuestaMinima;
        this.apuestaMaxima = apuestaMaxima;
    }

    /** Frase corta que se muestra en el lobby. */
    public abstract String getDescripcion();

    /** Reglas completas del juego, tal como se le explican al jugador. */
    public abstract String getReglas();

    /** Comprueba que una apuesta respete los límites de la mesa y las fichas del jugador. */
    public void validarApuesta(Jugador jugador, long monto) {
        if (monto < apuestaMinima) {
            throw new OperacionInvalidaException("La apuesta mínima en " + nombre + " es " + Formato.pesos(apuestaMinima) + ".");
        }
        if (monto > apuestaMaxima) {
            throw new OperacionInvalidaException("La apuesta máxima en " + nombre + " es " + Formato.pesos(apuestaMaxima) + ".");
        }
        if (monto > jugador.getFichas()) {
            throw new OperacionInvalidaException("No tienes fichas suficientes. Tienes " + Formato.pesos(jugador.getFichas())
                    + ". Puedes comprar más en la caja.");
        }
    }

    /**
     * Cierra una ronda: entrega el premio, la registra en el historial del jugador
     * y calcula el cashback según su nivel de fidelidad.
     *
     * @param apostado total de fichas que el jugador puso en la ronda (ya descontadas)
     * @param pagado   total que se le devuelve, incluida la apuesta cuando gana o empata
     */
    protected ResultadoRonda liquidar(Jugador jugador, long apostado, long pagado) {
        if (pagado > 0) {
            jugador.cobrar(pagado);
        }
        long cashback = jugador.registrarRonda(nombre, apostado, pagado);
        return new ResultadoRonda(nombre, apostado, pagado, cashback);
    }

    /**
     * Paga una lista de apuestas con el resultado de la jugada y cierra la ronda.
     * Funciona igual para la ruleta y para las carreras: cada apuesta sabe
     * calcular su propio premio (polimorfismo a través de {@link Apuesta}).
     *
     * @param ganadoras lista donde se dejan las apuestas que cobraron
     */
    protected <R, A extends Apuesta<R>> ResultadoRonda liquidarApuestas(Jugador jugador, List<A> apuestas,
                                                                     R resultado, List<A> ganadoras) {
        long pagado = 0;
        for (A apuesta : apuestas) {
            long premio = apuesta.calcularPremio(resultado);
            if (premio > 0) {
                pagado += premio;
                ganadoras.add(apuesta);
            }
        }
        return liquidar(jugador, Apuesta.sumarMontos(apuestas), pagado);
    }

    public String getNombre() {
        return nombre;
    }

    public long getApuestaMinima() {
        return apuestaMinima;
    }

    public long getApuestaMaxima() {
        return apuestaMaxima;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
