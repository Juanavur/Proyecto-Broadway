package broadway.modelo;

import java.util.Random;

/** Ruleta europea (un solo cero). El resultado de una ronda es el número que sale, de 0 a 36. */
public class Ruleta extends Juego {

    private static final int[] ROJOS = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
    private static final String[] TIPOS = {"Pleno", "Rojo", "Negro", "Par", "Impar", "Falta (1-18)", "Pasa (19-36)",
            "Docena 1", "Docena 2", "Docena 3"};

    private final Random azar;

    public Ruleta(Random azar) {
        super("Ruleta");
        this.azar = azar;
    }

    /** Pleno: paga 35 a 1. Con 10.000 apostados gana 350.000. */
    @Override
    public double calcularPremio(double monto) {
        return monto * 35;
    }

    @Override
    public double calcularPremio(double monto, String tipo) {
        if (tipo.startsWith("Pleno")) {
            return calcularPremio(monto);
        }
        if (tipo.startsWith("Docena")) {
            return monto * 2;
        }
        return monto; // apuestas sencillas: 1 a 1
    }

    @Override
    public double liquidarApuesta(Apuesta apuesta, int numero) {
        return gana(apuesta.getTipo(), numero) ? apuesta.getMonto() + apuesta.calcularPago(this) : 0;
    }

    /** Tipo "Pleno 17" gana si sale el 17; las sencillas pierden con el 0. */
    public boolean gana(String tipo, int n) {
        if (tipo.startsWith("Pleno")) {
            return Integer.parseInt(tipo.substring(6).trim()) == n;
        }
        if (n == 0) {
            return false;
        }
        switch (tipo) {
            case "Rojo": return esRojo(n);
            case "Negro": return !esRojo(n);
            case "Par": return n % 2 == 0;
            case "Impar": return n % 2 == 1;
            case "Falta (1-18)": return n <= 18;
            case "Pasa (19-36)": return n >= 19;
            case "Docena 1": return n <= 12;
            case "Docena 2": return n >= 13 && n <= 24;
            case "Docena 3": return n >= 25;
            default: return false;
        }
    }

    public int girar() {
        return azar.nextInt(37);
    }

    public static boolean esRojo(int n) {
        for (int r : ROJOS) {
            if (r == n) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getTiposApuesta() {
        return TIPOS.clone();
    }
}
