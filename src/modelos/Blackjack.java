package modelos;

import java.util.List;
import java.util.Random;

/**
 * Blackjack. El resultado de una ronda se codifica como un número:
 * PIERDE = 0, GANA = 1, BLACKJACK = 2 (natural) y EMPATE = 3.
 */
public class Blackjack extends Juego {

    public static final int PIERDE = 0;
    public static final int GANA = 1;
    public static final int BLACKJACK = 2;
    public static final int EMPATE = 3;

    private final Random azar;

    public Blackjack(Random azar) {
        super("Blackjack");
        this.azar = azar;
    }

    /** Blackjack natural: paga 3 a 2. Con 10.000 apostados gana 15.000. */
    @Override
    public double calcularPremio(double monto) {
        return monto * 1.5;
    }

    @Override
    public double calcularPremio(double monto, String tipo) {
        return "Natural".equals(tipo) ? calcularPremio(monto) : monto;
    }

    @Override
    public double liquidarApuesta(Apuesta apuesta, int resultado) {
        double monto = apuesta.getMonto();
        switch (resultado) {
            case BLACKJACK: return monto + calcularPremio(monto, "Natural");
            case GANA: return monto + apuesta.calcularPago(this);
            case EMPATE: return monto;
            default: return 0;
        }
    }

    /** Saca una carta de 1 (As) a 13 (Rey). */
    public int nuevaCarta() {
        return azar.nextInt(13) + 1;
    }

    /** Valor de una mano: figuras valen 10 y el As vale 11 o 1, lo que más convenga. */
    public static int valorMano(List<Integer> cartas) {
        int total = 0;
        int ases = 0;
        for (int c : cartas) {
            if (c == 1) {
                ases++;
                total += 11;
            } else {
                total += Math.min(c, 10);
            }
        }
        while (total > 21 && ases > 0) {
            total -= 10;
            ases--;
        }
        return total;
    }

    @Override
    public String[] getTiposApuesta() {
        return new String[] {"Mano"};
    }
}
