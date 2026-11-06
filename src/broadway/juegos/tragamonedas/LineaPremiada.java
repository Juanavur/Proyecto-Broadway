package broadway.juegos.tragamonedas;

/** Una línea activa que obtuvo premio en un giro. */
public class LineaPremiada {

    private final int numeroLinea;
    private final String combinacion;
    private final int multiplicador;
    private final long premio;

    public LineaPremiada(int numeroLinea, String combinacion, int multiplicador, long premio) {
        this.numeroLinea = numeroLinea;
        this.combinacion = combinacion;
        this.multiplicador = multiplicador;
        this.premio = premio;
    }

    public int getNumeroLinea() {
        return numeroLinea;
    }

    public String getCombinacion() {
        return combinacion;
    }

    public int getMultiplicador() {
        return multiplicador;
    }

    public long getPremio() {
        return premio;
    }
}
