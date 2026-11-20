package modelos;

import servicios.Formato;

import java.time.LocalDate;
import java.time.Period;

/**
 * Cliente registrado del casino: tiene un saldo de fichas, lo que ha apostado y perdido,
 * y un nivel de fidelidad que sube según el total apostado (RF-01, RF-06, RF-10).
 */
public class Cliente extends Persona {

    /** Edad mínima para registrarse: es la misma para todos los clientes. */
    public static int edadMinima = 18;

    private double saldoFichas;
    private double totalApostado;
    private double perdidasPeriodo;
    private NivelFidelidad nivel = NivelFidelidad.BRONCE;

    public Cliente(String documento, String nombre, LocalDate fechaNacimiento) {
        super(documento, nombre, fechaNacimiento);
        if (!esMayorDeEdad(fechaNacimiento)) {
            throw new IllegalArgumentException("No se registran menores de " + edadMinima + " años.");
        }
    }

    public static boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        return fechaNacimiento != null
                && Period.between(fechaNacimiento, LocalDate.now()).getYears() >= edadMinima;
    }

    public void comprarFichas(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        saldoFichas += monto;
    }

    /** Descuenta la apuesta del saldo y la suma al total apostado; puede subir el nivel. */
    public void apostar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("La apuesta debe ser mayor que 0.");
        }
        if (monto > saldoFichas) {
            throw new IllegalArgumentException("Saldo insuficiente: tiene " + Formato.dinero(saldoFichas) + " en fichas.");
        }
        saldoFichas -= monto;
        totalApostado += monto;
        perdidasPeriodo += monto;
        NivelFidelidad nuevo = NivelFidelidad.segunTotalApostado(totalApostado);
        if (nuevo.ordinal() > nivel.ordinal()) {
            nivel = nuevo; // el nivel nunca baja
        }
    }

    /** Paga un premio (apuesta devuelta + ganancia). Lo que se recupera deja de contar como pérdida. */
    public void cobrarPremio(double pago) {
        if (pago <= 0) {
            return;
        }
        saldoFichas += pago;
        perdidasPeriodo = Math.max(0, perdidasPeriodo - pago);
    }

    /** Saca fichas del saldo para cambiarlas por dinero en la caja. */
    public void retirarFichas(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0.");
        }
        if (monto > saldoFichas) {
            throw new IllegalArgumentException("No puede cambiar más fichas de las que tiene.");
        }
        saldoFichas -= monto;
    }

    /** Cashback de las pérdidas del periodo según el nivel; se abona en fichas y el periodo vuelve a cero. */
    public double reclamarCashback() {
        double cashback = NivelFidelidad.calcularCashback(nivel.name(), perdidasPeriodo);
        saldoFichas += cashback;
        perdidasPeriodo = 0;
        return cashback;
    }

    public double getSaldoFichas() {
        return saldoFichas;
    }

    public double getTotalApostado() {
        return totalApostado;
    }

    public double getPerdidasPeriodo() {
        return perdidasPeriodo;
    }

    public NivelFidelidad getNivel() {
        return nivel;
    }

    /**
     * Nivel de fidelidad del cliente. Está anidado porque solo tiene sentido para un cliente.
     * Los porcentajes de cashback son estáticos: son reglas del programa, iguales para todos.
     */
    public enum NivelFidelidad {
        BRONCE("Bronce", 0),
        PLATA("Plata", 2_000_000),
        ORO("Oro", 10_000_000),
        VIP("VIP", 50_000_000);

        private static final double cashbackBronce = 0.0;
        private static final double cashbackPlata = 0.02;
        private static final double cashbackOro = 0.05;
        private static final double cashbackVip = 0.08;

        private final String nombre;
        private final double apuestaParaLlegar;

        NivelFidelidad(String nombre, double apuestaParaLlegar) {
            this.nombre = nombre;
            this.apuestaParaLlegar = apuestaParaLlegar;
        }

        /** Ejemplo: calcularCashback("ORO", 100000) = 5000. */
        public static double calcularCashback(String nivel, double perdida) {
            if (perdida <= 0) {
                return 0;
            }
            switch (nivel.toUpperCase()) {
                case "PLATA": return perdida * cashbackPlata;
                case "ORO": return perdida * cashbackOro;
                case "VIP": return perdida * cashbackVip;
                default: return perdida * cashbackBronce;
            }
        }

        static NivelFidelidad segunTotalApostado(double total) {
            NivelFidelidad resultado = BRONCE;
            for (NivelFidelidad n : values()) {
                if (total >= n.apuestaParaLlegar) {
                    resultado = n;
                }
            }
            return resultado;
        }

        public double getPorcentaje() {
            return calcularCashback(name(), 100);
        }

        public double getApuestaParaLlegar() {
            return apuestaParaLlegar;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
