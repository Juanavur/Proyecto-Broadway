package broadway.juegos.blackjack;

import broadway.cartas.Carta;
import broadway.cartas.Valor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Una mano de blackjack: sus cartas, su apuesta y en qué estado quedó. */
public class ManoBlackjack {

    private final List<Carta> cartas = new ArrayList<>();
    private long apuesta;
    private final boolean vieneDeDivision;
    private final boolean divisionDeAses;
    private boolean doblada;
    private boolean rendida;
    private boolean terminada;
    private String resultado = "";

    public ManoBlackjack(long apuesta, boolean vieneDeDivision, boolean divisionDeAses) {
        this.apuesta = apuesta;
        this.vieneDeDivision = vieneDeDivision;
        this.divisionDeAses = divisionDeAses;
    }

    void agregar(Carta carta) {
        cartas.add(carta);
    }

    /** Quita la segunda carta (se usa al dividir un par). */
    Carta quitarSegunda() {
        return cartas.remove(1);
    }

    /** Valor de la mano contando cada As como 11 mientras no se pase de 21. */
    public int getValor() {
        int total = 0;
        int ases = 0;
        for (Carta c : cartas) {
            total += c.getValor().getPuntosBlackjack();
            if (c.getValor() == Valor.AS) {
                ases++;
            }
        }
        while (total > 21 && ases > 0) {
            total -= 10;
            ases--;
        }
        return total;
    }

    /** Una mano es "blanda" si tiene un As que todavía vale 11. */
    public boolean esBlanda() {
        int total = 0;
        int ases = 0;
        for (Carta c : cartas) {
            total += c.getValor().getPuntosBlackjack();
            if (c.getValor() == Valor.AS) {
                ases++;
            }
        }
        while (total > 21 && ases > 0) {
            total -= 10;
            ases--;
        }
        return ases > 0;
    }

    /** Blackjack natural: As + carta de 10 con las dos primeras cartas, y no después de dividir. */
    public boolean esBlackjack() {
        return cartas.size() == 2 && getValor() == 21 && !vieneDeDivision;
    }

    public boolean sePaso() {
        return getValor() > 21;
    }

    /** Se puede dividir con dos cartas del mismo valor de blackjack (por ejemplo J y K). */
    public boolean esPar() {
        return cartas.size() == 2
                && cartas.get(0).getValor().getPuntosBlackjack() == cartas.get(1).getValor().getPuntosBlackjack();
    }

    public String getDescripcionValor() {
        if (esBlackjack()) {
            return "Blackjack";
        }
        int valor = getValor();
        if (esBlanda() && valor < 21) {
            return (valor - 10) + " / " + valor;
        }
        return String.valueOf(valor);
    }

    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }

    public long getApuesta() {
        return apuesta;
    }

    void duplicarApuesta() {
        apuesta *= 2;
        doblada = true;
    }

    public boolean isVieneDeDivision() {
        return vieneDeDivision;
    }

    public boolean isDivisionDeAses() {
        return divisionDeAses;
    }

    public boolean isDoblada() {
        return doblada;
    }

    public boolean isRendida() {
        return rendida;
    }

    void rendir() {
        rendida = true;
        terminada = true;
    }

    public boolean isTerminada() {
        return terminada;
    }

    void terminar() {
        terminada = true;
    }

    public String getResultado() {
        return resultado;
    }

    void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
