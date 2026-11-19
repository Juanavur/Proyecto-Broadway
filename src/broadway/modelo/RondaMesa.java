package broadway.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Una ronda de juego en una mesa: reúne las apuestas y las paga cuando sale el resultado. */
public class RondaMesa {

    private final Juego juego;
    private final List<Apuesta> apuestas = new ArrayList<>();
    private boolean liquidada;
    private int resultado = -1;

    public RondaMesa(Juego juego) {
        this.juego = juego;
    }

    public void agregarApuesta(Apuesta a) {
        if (liquidada) {
            throw new IllegalStateException("La ronda ya terminó.");
        }
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
