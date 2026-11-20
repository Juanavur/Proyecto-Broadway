package broadway.modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formato de dinero en pesos colombianos ($1.250.000). */
public final class Formato {

    private static final DecimalFormat PESOS =
            new DecimalFormat("$#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));

    private Formato() {
    }

    public static String dinero(double valor) {
        return PESOS.format(Math.round(valor));
    }
}
