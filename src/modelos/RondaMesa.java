package modelos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Una ronda de juego en una mesa: reúne las apuestas y las paga cuando sale el resultado. */
public class RondaMesa {

    private final Juego juego;
    private final int maxPlayers;
    private final List<Apuesta> apuestas = new ArrayList<>();
    private boolean liquidada;
    private int resultado = -1;

    RondaMesa(Juego juego, int maxPlayers) {
        this.juego = juego;
        this.maxPlayers = maxPlayers;
    }

    /** Rellena la ronda con lo que había en el archivo (RF-11). */
    void restaurar(boolean liquidada, int resultado, List<Apuesta> guardadas) {
        apuestas.addAll(guardadas);
        this.liquidada = liquidada;
        this.resultado = resultado;
    }

    public void agregarApuesta(Apuesta a) {
        if (liquidada) {
            throw new IllegalStateException("La ronda ya terminó.");
        }
        if (!juego.acceptsBetType(a.getTipo())) {
            throw new IllegalArgumentException("El tipo de apuesta no es válido para " + juego.getNombreJuego() + ".");
        }
        String document = a.getCliente().getDocumento();
        long otherPlayers = apuestas.stream()
                .map(bet -> bet.getCliente().getDocumento())
                .filter(other -> !other.equals(document))
                .distinct()
                .count();
        if (otherPlayers >= maxPlayers) {
            throw new IllegalStateException("La ronda ya alcanzó la capacidad de la mesa.");
        }
        a.getCliente().apostar(a.getMonto());
        apuestas.add(a);
    }

    /** Paga las apuestas ganadoras y devuelve cuántas ganaron. */
    public int liquidar(int resultado) {
        if (liquidada) {
            throw new IllegalStateException("La ronda ya fue liquidada.");
        }
        int ganadoras = 0;
        for (Apuesta a : apuestas) {
            double pago = juego.liquidarApuesta(a, resultado);
            if (pago > 0) {
                a.getCliente().cobrarPremio(pago);
                if (pago > a.getMonto()) {
                    ganadoras++;
                }
            }
        }
        this.resultado = resultado;
        liquidada = true;
        return ganadoras;
    }

    public List<Apuesta> getApuestas() {
        return Collections.unmodifiableList(apuestas);
    }

    public boolean estaLiquidada() {
        return liquidada;
    }

    public int getResultado() {
        return resultado;
    }
}
