package interfaces;

/**
 * Algo del casino que se puede poner en servicio y sacar de servicio.
 * La implementan las mesas y las máquinas tragamonedas: ninguna de las dos permite jugar si no está disponible.
 */
public interface Operable {

    boolean estaDisponible();

    void abrir();

    void cerrar();
}
