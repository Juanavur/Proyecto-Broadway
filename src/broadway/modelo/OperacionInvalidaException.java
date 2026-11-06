package broadway.modelo;

/**
 * Se lanza cuando una operación viola una regla del casino
 * (monto inválido, saldo insuficiente, apuesta fuera de los límites, etc.).
 * El mensaje siempre está escrito para mostrarse directamente al jugador.
 */
public class OperacionInvalidaException extends RuntimeException {

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
