package broadway.cartas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Una o varias barajas francesas de 52 cartas mezcladas juntas
 * (en blackjack se usa un "zapato" de 6 barajas).
 */
public class Baraja {

    private final int numeroDeBarajas;
    private final Random azar;
    private final List<Carta> cartas = new ArrayList<>();

    public Baraja(int numeroDeBarajas, Random azar) {
        if (numeroDeBarajas < 1) {
            throw new IllegalArgumentException("Debe haber al menos una baraja.");
        }
        this.numeroDeBarajas = numeroDeBarajas;
        this.azar = azar;
        rearmarYBarajar();
    }

    /** Vuelve a juntar todas las cartas y las mezcla. */
    public final void rearmarYBarajar() {
        cartas.clear();
        for (int i = 0; i < numeroDeBarajas; i++) {
            for (Palo palo : Palo.values()) {
                for (Valor valor : Valor.values()) {
                    cartas.add(new Carta(valor, palo));
                }
            }
        }
        Collections.shuffle(cartas, azar);
    }

    public Carta robar() {
        if (cartas.isEmpty()) {
            rearmarYBarajar();
        }
        return cartas.remove(cartas.size() - 1);
    }

    public int getCartasRestantes() {
        return cartas.size();
    }

    public int getTotalCartas() {
        return numeroDeBarajas * 52;
    }

    /** Fracción de cartas que quedan (1.0 recién barajada). */
    public double getFraccionRestante() {
        return (double) cartas.size() / getTotalCartas();
    }
}
