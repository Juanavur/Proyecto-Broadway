package broadway.modelo;

/**
 * Resumen de una ronda ya liquidada: cuánto apostó el jugador, cuánto cobró
 * (incluida la apuesta devuelta) y cuánto cashback generó la ronda.
 */
public class ResultadoRonda {

    private final String juego;
    private final long apostado;
    private final long pagado;
    private final long cashback;

    public ResultadoRonda(String juego, long apostado, long pagado, long cashback) {
        this.juego = juego;
        this.apostado = apostado;
        this.pagado = pagado;
        this.cashback = cashback;
    }

    public String getJuego() {
        return juego;
    }

    public long getApostado() {
        return apostado;
    }

    public long getPagado() {
        return pagado;
    }

    public long getCashback() {
        return cashback;
    }

    /** Ganancia neta de la ronda: positiva si ganó, negativa si perdió. */
    public long getNeto() {
        return pagado - apostado;
    }
}
