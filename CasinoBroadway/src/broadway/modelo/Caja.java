package broadway.modelo;

/**
 * La caja del casino: cambia efectivo por fichas y fichas por efectivo
 * y entrega el cashback acumulado. La tasa es 1 peso = 1 ficha.
 *
 * Las fichas físicas tienen denominaciones, así que solo se venden y compran
 * múltiplos de la ficha más pequeña ($1.000).
 */
public class Caja {

    public static final long FICHA_MINIMA = 1_000;
    public static final long[] DENOMINACIONES = {1_000, 5_000, 10_000, 25_000, 50_000, 100_000, 500_000};

    public void venderFichas(Jugador jugador, long monto) {
        validarDenominacion(monto);
        jugador.comprarFichas(monto);
    }

    public void comprarFichas(Jugador jugador, long monto) {
        validarDenominacion(monto);
        jugador.venderFichas(monto);
    }

    /** Cambia por efectivo todas las fichas que sean múltiplo de $1.000 (lo que sobre queda en fichas). */
    public long cambiarTodo(Jugador jugador) {
        long monto = jugador.getFichas() / FICHA_MINIMA * FICHA_MINIMA;
        if (monto <= 0) {
            throw new OperacionInvalidaException("No tienes fichas suficientes para cambiar (mínimo "
                    + Formato.pesos(FICHA_MINIMA) + ").");
        }
        jugador.venderFichas(monto);
        return monto;
    }

    public long entregarCashback(Jugador jugador) {
        return jugador.reclamarCashback();
    }

    private void validarDenominacion(long monto) {
        if (monto <= 0) {
            throw new OperacionInvalidaException("El monto debe ser mayor que 0.");
        }
        if (monto % FICHA_MINIMA != 0) {
            throw new OperacionInvalidaException("El monto debe ser múltiplo de " + Formato.pesos(FICHA_MINIMA)
                    + ", que es la ficha más pequeña.");
        }
    }
}
