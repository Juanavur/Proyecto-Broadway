package broadway.juegos.carreras;

/**
 * Un caballo de la carrera. La velocidad y la resistencia no se le muestran
 * al jugador directamente: se reflejan en su forma (estrellas) y en las cuotas.
 */
public class Caballo {

    private final int numero;
    private final String nombre;
    private final double velocidad;
    private final double resistencia;
    private int forma = 3;

    public Caballo(int numero, String nombre, double velocidad, double resistencia) {
        this.numero = numero;
        this.nombre = nombre;
        this.velocidad = velocidad;
        this.resistencia = resistencia;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    double getVelocidad() {
        return velocidad;
    }

    double getResistencia() {
        return resistencia;
    }

    /** Forma de 1 a 5 estrellas: resume qué tan probable es que gane. */
    public int getForma() {
        return forma;
    }

    void setForma(int forma) {
        this.forma = forma;
    }

    @Override
    public String toString() {
        return numero + ". " + nombre;
    }
}
