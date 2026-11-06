package broadway.modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Utilidades para mostrar cantidades de dinero y fichas en formato colombiano ($1.250.000). */
public final class Formato {

    private static final DecimalFormat MILES;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        MILES = new DecimalFormat("#,##0", simbolos);
    }

    private Formato() {
    }

    public static String pesos(long valor) {
        String signo = valor < 0 ? "-" : "";
        return signo + "$" + MILES.format(Math.abs(valor));
    }

    public static String numero(long valor) {
        return MILES.format(valor);
    }

    /** Cuota decimal con dos decimales, por ejemplo 3,45. */
    public static String cuota(double cuota) {
        return String.format("%.2f", cuota).replace('.', ',');
    }
}
