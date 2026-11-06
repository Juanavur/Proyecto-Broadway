package broadway.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El cliente que juega en el casino.
 *
 * Su efectivo y sus fichas son privados y NO tienen setters: solo cambian con
 * operaciones que validan las reglas (comprar, vender, apostar, cobrar). Así es
 * imposible que otra clase deje un saldo negativo o salte una regla.
 */
public class Jugador implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int EDAD_MINIMA = 18;
    private static final int MAX_MOVIMIENTOS = 300;

    private final String nombre;
    private final String documento;
    private final LocalDate fechaNacimiento;
    private final NivelFidelidad nivel;

    private long dinero;
    private long fichas;
    private long cashbackPendiente;
    private long cashbackTotalRecibido;
    private long totalApostado;
    private long totalPerdido;
    private long totalGanado;
    private final List<Movimiento> historial = new ArrayList<>();

    public Jugador(String nombre, String documento, LocalDate fechaNacimiento,
                   NivelFidelidad nivel, long dineroInicial) {
        if (nombre == null || nombre.isBlank()) {
            throw new OperacionInvalidaException("El nombre no puede estar vacío.");
        }
        if (documento == null || !documento.matches("\\d{6,10}")) {
            throw new OperacionInvalidaException("El documento debe tener entre 6 y 10 dígitos, sin puntos ni letras.");
        }
        if (fechaNacimiento == null) {
            throw new OperacionInvalidaException("Debes indicar la fecha de nacimiento.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new OperacionInvalidaException("La fecha de nacimiento no puede estar en el futuro.");
        }
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < EDAD_MINIMA) {
            throw new OperacionInvalidaException("Solo pueden jugar mayores de " + EDAD_MINIMA + " años.");
        }
        if (nivel == null) {
            throw new OperacionInvalidaException("Debes escoger un nivel de fidelidad.");
        }
        if (dineroInicial <= 0) {
            throw new OperacionInvalidaException("El dinero inicial debe ser mayor que 0.");
        }
        this.nombre = nombre.trim();
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.nivel = nivel;
        this.dinero = dineroInicial;
    }

    // ------------------------------------------------------------------ caja

    /** Cambia efectivo por fichas (1 peso = 1 ficha). La caja valida las denominaciones. */
    void comprarFichas(long monto) {
        validarPositivo(monto);
        if (monto > dinero) {
            throw new OperacionInvalidaException("No tienes suficiente efectivo. Tienes " + Formato.pesos(dinero) + ".");
        }
        dinero -= monto;
        fichas += monto;
        registrar(new Movimiento(TipoMovimiento.COMPRA_FICHAS,
                "Compró " + Formato.pesos(monto) + " en fichas", -monto, monto));
    }

    /** Cambia fichas por efectivo (1 ficha = 1 peso). */
    void venderFichas(long monto) {
        validarPositivo(monto);
        if (monto > fichas) {
            throw new OperacionInvalidaException("No tienes suficientes fichas. Tienes " + Formato.pesos(fichas) + ".");
        }
        fichas -= monto;
        dinero += monto;
        registrar(new Movimiento(TipoMovimiento.VENTA_FICHAS,
                "Cambió " + Formato.pesos(monto) + " en fichas por efectivo", monto, -monto));
    }

    /** Pasa el cashback acumulado a las fichas del jugador. Devuelve cuánto se acreditó. */
    long reclamarCashback() {
        if (cashbackPendiente <= 0) {
            throw new OperacionInvalidaException("No tienes cashback pendiente por reclamar.");
        }
        long monto = cashbackPendiente;
        cashbackPendiente = 0;
        cashbackTotalRecibido += monto;
        fichas += monto;
        registrar(new Movimiento(TipoMovimiento.CASHBACK,
                "Cashback " + nivel.getNombre() + " (" + nivel.getPorcentajeCashback() + "%)", 0, monto));
        return monto;
    }

    // ----------------------------------------------------------------- juego

    /** Retira fichas del jugador para ponerlas en juego. */
    public void apostar(long monto) {
        validarPositivo(monto);
        if (monto > fichas) {
            throw new OperacionInvalidaException("No tienes fichas suficientes para apostar " + Formato.pesos(monto)
                    + ". Tienes " + Formato.pesos(fichas) + ".");
        }
        fichas -= monto;
    }

    /** Devuelve fichas al jugador (premios o apuestas devueltas). */
    public void cobrar(long monto) {
        if (monto < 0) {
            throw new OperacionInvalidaException("Un premio no puede ser negativo.");
        }
        fichas += monto;
    }

    /**
     * Registra el resultado de una ronda ya cobrada y calcula el cashback.
     *
     * @param apostado total de fichas que el jugador puso en juego durante la ronda
     * @param pagado   total de fichas que recibió de vuelta (incluye la apuesta si ganó o empató)
     * @return cashback generado por la ronda (0 si no perdió)
     */
    public long registrarRonda(String juego, long apostado, long pagado) {
        totalApostado += apostado;
        long neto = pagado - apostado;
        long cashback = 0;
        if (neto < 0) {
            long perdida = -neto;
            totalPerdido += perdida;
            cashback = nivel.calcularCashback(perdida);
            cashbackPendiente += cashback;
        } else {
            totalGanado += neto;
        }
        String detalle = juego + ": apostó " + Formato.pesos(apostado) + ", cobró " + Formato.pesos(pagado)
                + (cashback > 0 ? " (cashback " + Formato.pesos(cashback) + ")" : "");
        registrar(new Movimiento(TipoMovimiento.JUEGO, detalle, 0, neto));
        return cashback;
    }

    // ------------------------------------------------------------- utilidades

    private void validarPositivo(long monto) {
        if (monto <= 0) {
            throw new OperacionInvalidaException("El monto debe ser mayor que 0.");
        }
    }

    private void registrar(Movimiento movimiento) {
        historial.add(movimiento);
        if (historial.size() > MAX_MOVIMIENTOS) {
            historial.remove(0);
        }
    }

    public int getEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public NivelFidelidad getNivel() {
        return nivel;
    }

    public long getDinero() {
        return dinero;
    }

    public long getFichas() {
        return fichas;
    }

    public long getCashbackPendiente() {
        return cashbackPendiente;
    }

    public long getCashbackTotalRecibido() {
        return cashbackTotalRecibido;
    }

    public long getTotalApostado() {
        return totalApostado;
    }

    public long getTotalPerdido() {
        return totalPerdido;
    }

    public long getTotalGanado() {
        return totalGanado;
    }

    /** Historial de solo lectura, del más antiguo al más reciente. */
    public List<Movimiento> getHistorial() {
        return Collections.unmodifiableList(historial);
    }
}
