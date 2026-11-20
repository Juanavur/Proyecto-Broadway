package modelos;

/**
 * Juego de mesa del casino (RF-05). Cada juego sabe cuánto paga: el mismo llamado
 * calcularPremio(monto) da un resultado distinto en la ruleta y en el blackjack (polimorfismo).
 */
public abstract class Juego {

    private final String nombreJuego;

    protected Juego(String nombreJuego) {
        this.nombreJuego = nombreJuego;
    }

    /** Ganancia de la apuesta principal del juego (sin contar la apuesta devuelta). */
    public abstract double calcularPremio(double monto);

    /** Sobrecarga: ganancia según el tipo de apuesta. Por defecto, la apuesta principal. */
    public double calcularPremio(double monto, String tipo) {
        return calcularPremio(monto);
    }

    /** Fichas que se le devuelven al cliente (apuesta + ganancia) según el resultado de la ronda; 0 si pierde. */
    public abstract double liquidarApuesta(Apuesta apuesta, int resultado);

    /** Tipos de apuesta que acepta el juego, para mostrarlos en la interfaz. */
    public abstract String[] getTiposApuesta();

    public String getNombreJuego() {
        return nombreJuego;
    }

    @Override
    public String toString() {
        return nombreJuego;
    }
}
