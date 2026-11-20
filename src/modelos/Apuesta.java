package modelos;

/** Apuesta de un cliente en una ronda de mesa (RF-08). */
public class Apuesta {

    private final String tipo;
    private final double monto;
    private final Cliente cliente;

    public Apuesta(String tipo, double monto, Cliente cliente) {
        if (tipo == null || tipo.isBlank() || cliente == null) {
            throw new IllegalArgumentException("La apuesta requiere un cliente y un tipo.");
        }
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("La apuesta debe ser mayor que 0.");
        }
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
