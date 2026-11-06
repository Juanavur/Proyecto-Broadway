package broadway.cartas;

/** Valores de las cartas, del 2 al As. El rango sirve para comparar en póker (As = 14). */
public enum Valor {
    DOS(2, "2"), TRES(3, "3"), CUATRO(4, "4"), CINCO(5, "5"), SEIS(6, "6"), SIETE(7, "7"),
    OCHO(8, "8"), NUEVE(9, "9"), DIEZ(10, "10"), JOTA(11, "J"), REINA(12, "Q"), REY(13, "K"), AS(14, "A");

    private final int rango;
    private final String simbolo;

    Valor(int rango, String simbolo) {
        this.rango = rango;
        this.simbolo = simbolo;
    }

    /** Rango para póker: 2..14 (el As es la más alta). */
    public int getRango() {
        return rango;
    }

    public String getSimbolo() {
        return simbolo;
    }

    /** Puntos en blackjack: las figuras valen 10 y el As vale 11 (la mano lo baja a 1 si hace falta). */
    public int getPuntosBlackjack() {
        if (this == AS) {
            return 11;
        }
        return Math.min(rango, 10);
    }
}
