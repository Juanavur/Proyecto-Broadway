package broadway.cartas;

import java.util.Objects;

/** Una carta de la baraja. Es inmutable. */
public final class Carta {

    private final Valor valor;
    private final Palo palo;

    public Carta(Valor valor, Palo palo) {
        this.valor = Objects.requireNonNull(valor);
        this.palo = Objects.requireNonNull(palo);
    }

    public Valor getValor() {
        return valor;
    }

    public Palo getPalo() {
        return palo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Carta)) {
            return false;
        }
        Carta otra = (Carta) o;
        return valor == otra.valor && palo == otra.palo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, palo);
    }

    @Override
    public String toString() {
        return valor.getSimbolo() + palo.getSimbolo();
    }
}
