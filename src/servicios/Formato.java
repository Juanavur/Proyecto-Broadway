package servicios;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formato de dinero en pesos colombianos ($1.250.000). */
public final class Formato {

    private static final DecimalFormat PESOS =
            new DecimalFormat("$#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));

    private Formato() {
    }

    public static String dinero(double valor) {
        return PESOS.format(valor);
    }

    public static String chips(double amount) {
        return dinero(amount).replace("$", "");
    }

    public static double parseAmount(String text) {
        String normalized = text.replace("$", "").replace(".", "").trim()
                .replaceFirst(",(?=\\d{1,2}$)", ".").replace(",", "");
        try {
            double amount = Double.parseDouble(normalized);
            if (Double.isFinite(amount)) {
                return amount;
            }
        } catch (NumberFormatException invalid) {
            // Use the same message for invalid numbers and non-finite values.
        }
        throw new IllegalArgumentException("Escriba un monto válido, por ejemplo 50.000,50.");
    }
}
