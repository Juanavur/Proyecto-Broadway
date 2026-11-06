package broadway.juegos.ruleta;

import broadway.juegos.Apuesta;
import broadway.modelo.Formato;
import broadway.modelo.OperacionInvalidaException;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Una apuesta sobre el tapete. Se crea con los métodos de fábrica
 * (pleno, semipleno, calle...), que validan que los números formen
 * una apuesta real del tapete; por eso el constructor es privado.
 */
public class ApuestaRuleta implements Apuesta<Integer> {

    private final TipoApuestaRuleta tipo;
    private final Set<Integer> numeros;
    private long monto;

    private ApuestaRuleta(TipoApuestaRuleta tipo, Set<Integer> numeros, long monto) {
        if (monto <= 0) {
            throw new OperacionInvalidaException("El monto de la apuesta debe ser mayor que 0.");
        }
        this.tipo = tipo;
        this.numeros = Collections.unmodifiableSet(new TreeSet<>(numeros));
        this.monto = monto;
    }

    // ---------------------------------------------------------------- fábricas

    public static ApuestaRuleta pleno(int numero, long monto) {
        validarNumero(numero, 0);
        return new ApuestaRuleta(TipoApuestaRuleta.PLENO, Set.of(numero), monto);
    }

    /** Dos números vecinos en el tapete (incluido el 0 con 1, 2 o 3). */
    public static ApuestaRuleta semipleno(int a, int b, long monto) {
        validarNumero(a, 0);
        validarNumero(b, 0);
        int menor = Math.min(a, b);
        int mayor = Math.max(a, b);
        boolean conCero = menor == 0 && mayor >= 1 && mayor <= 3;
        boolean misma = menor > 0 && mayor - menor == 1 && calleDe(menor) == calleDe(mayor);
        boolean contiguas = menor > 0 && mayor - menor == 3;
        if (!conCero && !misma && !contiguas) {
            throw new OperacionInvalidaException(menor + " y " + mayor + " no están uno al lado del otro en el tapete.");
        }
        return new ApuestaRuleta(TipoApuestaRuleta.SEMIPLENO, Set.of(menor, mayor), monto);
    }

    /** Los tres números de una calle: 1-2-3, 4-5-6, ..., 34-35-36 (calle 1 a 12). */
    public static ApuestaRuleta calle(int calle, long monto) {
        if (calle < 1 || calle > 12) {
            throw new OperacionInvalidaException("La calle debe estar entre 1 y 12.");
        }
        int base = 3 * calle - 2;
        return new ApuestaRuleta(TipoApuestaRuleta.CALLE, Set.of(base, base + 1, base + 2), monto);
    }

    /** Cuatro números que se tocan en una esquina; se indica el menor de ellos. */
    public static ApuestaRuleta cuadro(int menor, long monto) {
        if (menor < 1 || menor > 32 || menor % 3 == 0) {
            throw new OperacionInvalidaException("No hay un cuadro que empiece en " + menor + ".");
        }
        return new ApuestaRuleta(TipoApuestaRuleta.CUADRO, Set.of(menor, menor + 1, menor + 3, menor + 4), monto);
    }

    /** Seis números de dos calles seguidas; se indica la primera calle (1 a 11). */
    public static ApuestaRuleta seisena(int primeraCalle, long monto) {
        if (primeraCalle < 1 || primeraCalle > 11) {
            throw new OperacionInvalidaException("La seisena debe empezar entre la calle 1 y la 11.");
        }
        Set<Integer> nums = new TreeSet<>();
        int base = 3 * primeraCalle - 2;
        for (int i = 0; i < 6; i++) {
            nums.add(base + i);
        }
        return new ApuestaRuleta(TipoApuestaRuleta.SEISENA, nums, monto);
    }

    public static ApuestaRuleta docena(int docena, long monto) {
        if (docena < 1 || docena > 3) {
            throw new OperacionInvalidaException("La docena debe ser 1, 2 o 3.");
        }
        Set<Integer> nums = new TreeSet<>();
        for (int n = 12 * (docena - 1) + 1; n <= 12 * docena; n++) {
            nums.add(n);
        }
        return new ApuestaRuleta(TipoApuestaRuleta.DOCENA, nums, monto);
    }

    /** Columna 1 = 1, 4, 7...; columna 2 = 2, 5, 8...; columna 3 = 3, 6, 9... */
    public static ApuestaRuleta columna(int columna, long monto) {
        if (columna < 1 || columna > 3) {
            throw new OperacionInvalidaException("La columna debe ser 1, 2 o 3.");
        }
        Set<Integer> nums = new TreeSet<>();
        for (int n = columna; n <= 36; n += 3) {
            nums.add(n);
        }
        return new ApuestaRuleta(TipoApuestaRuleta.COLUMNA, nums, monto);
    }

    /** Apuestas sencillas: rojo, negro, par, impar, falta o pasa. */
    public static ApuestaRuleta sencilla(TipoApuestaRuleta tipo, long monto) {
        Set<Integer> nums = new TreeSet<>();
        for (int n = 1; n <= 36; n++) {
            boolean entra;
            switch (tipo) {
                case ROJO: entra = Ruleta.esRojo(n); break;
                case NEGRO: entra = !Ruleta.esRojo(n); break;
                case PAR: entra = n % 2 == 0; break;
                case IMPAR: entra = n % 2 == 1; break;
                case FALTA: entra = n <= 18; break;
                case PASA: entra = n >= 19; break;
                default:
                    throw new IllegalArgumentException(tipo + " no es una apuesta sencilla.");
            }
            if (entra) {
                nums.add(n);
            }
        }
        return new ApuestaRuleta(tipo, nums, monto);
    }

    // ---------------------------------------------------------------- lógica

    private static void validarNumero(int n, int minimo) {
        if (n < minimo || n > 36) {
            throw new OperacionInvalidaException("El número " + n + " no está en la ruleta.");
        }
    }

    /** Calle (1-12) en la que está un número del 1 al 36. */
    public static int calleDe(int numero) {
        return (numero - 1) / 3 + 1;
    }

    @Override
    public boolean gana(Integer numero) {
        return numeros.contains(numero);
    }

    /** Lo que recibe el jugador si sale ese número: apuesta × pago más la apuesta. 0 si pierde. */
    @Override
    public long calcularPremio(Integer numero) {
        return gana(numero) ? monto * (tipo.getPago() + 1) : 0;
    }

    /** Dos apuestas están en la misma casilla si son del mismo tipo y cubren los mismos números. */
    public boolean mismaPosicion(ApuestaRuleta otra) {
        return tipo == otra.tipo && numeros.equals(otra.numeros);
    }

    void aumentar(long extra) {
        monto += extra;
    }

    ApuestaRuleta copia() {
        return new ApuestaRuleta(tipo, numeros, monto);
    }

    public TipoApuestaRuleta getTipo() {
        return tipo;
    }

    public Set<Integer> getNumeros() {
        return numeros;
    }

    @Override
    public long getMonto() {
        return monto;
    }

    @Override
    public String getDescripcion() {
        switch (tipo) {
            case PLENO:
            case SEMIPLENO:
            case CUADRO:
                return tipo.getNombre() + " " + numeros.stream().map(String::valueOf).collect(Collectors.joining("-"));
            case CALLE:
            case SEISENA:
                int primero = numeros.iterator().next();
                int ultimo = new TreeSet<>(numeros).last();
                return tipo.getNombre() + " " + primero + " a " + ultimo;
            case DOCENA:
                return (numeros.iterator().next() / 12 + 1) + "ª docena";
            case COLUMNA:
                return "Columna " + numeros.iterator().next();
            default:
                return tipo.getNombre();
        }
    }

    @Override
    public String toString() {
        return getDescripcion() + " — " + Formato.pesos(monto) + " (paga " + tipo.getPago() + ":1)";
    }
}
