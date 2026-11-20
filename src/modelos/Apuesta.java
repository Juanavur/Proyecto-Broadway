package modelos;

/** Apuesta de un cliente en una ronda de mesa (RF-08). */
public class Apuesta {

    private final String tipo;
    private final double monto;
    private final Cliente cliente;

    public Apuesta(String tipo, double monto, Cliente cliente) {
        this.tipo = tipo;
        this.monto = monto;
        this.cliente = cliente;
    }

    /** Lo que gana esta apuesta si acierta: depende del juego (polimorfismo). */
    public double calcularPago(Juego juego) {
        return juego.calcularPremio(monto, tipo);
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public Cliente getCliente() {
        return cliente;
    }
}
