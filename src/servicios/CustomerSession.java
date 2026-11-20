package servicios;

import modelos.Cajero;
import modelos.Casino;
import modelos.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The cash brought by a new customer lives for the duration of the visit. */
public final class CustomerSession {

    private final Cajero teller;
    private final Cliente customer;
    private BigDecimal cash;

    public CustomerSession(Casino casino, String name, String document, LocalDate birthDate, double initialCash) {
        if (!Double.isFinite(initialCash) || initialCash < 0) {
            throw new IllegalArgumentException("El dinero inicial debe ser un monto válido y no negativo.");
        }
        teller = casino.getEmpleados().stream().filter(Cajero.class::isInstance).map(Cajero.class::cast)
                .findFirst().orElseThrow(() -> new IllegalStateException("No hay un cajero disponible."));
        if (!teller.registrarClientes(document, name, birthDate)) {
            throw new IllegalArgumentException("El documento ya está registrado. Escribe un documento nuevo.");
        }
        customer = casino.buscarClientePorDocumento(document);
        cash = BigDecimal.valueOf(initialCash);
    }

    public Cliente customer() {
        return customer;
    }

    public double cash() {
        return cash.doubleValue();
    }

    public void buyChips(double amount) {
        BigDecimal price = exchangeAmount(amount);
        if (price.compareTo(cash) > 0) {
            throw new IllegalArgumentException("Efectivo insuficiente. Tienes " + Formato.dinero(cash()) + ".");
        }
        teller.venderFichas(customer, amount);
        cash = cash.subtract(price);
    }

    public void redeemChips(double amount) {
        BigDecimal balance = cash.add(exchangeAmount(amount));
        if (!Double.isFinite(balance.doubleValue())) {
            throw new IllegalArgumentException("El efectivo resultante supera el monto permitido.");
        }
        teller.canjearFichas(customer, amount);
        cash = balance;
    }

    private static BigDecimal exchangeAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Escribe un monto mayor que cero.");
        }
        return BigDecimal.valueOf(amount);
    }
}
