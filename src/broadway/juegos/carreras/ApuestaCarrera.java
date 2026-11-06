package broadway.juegos.carreras;

import broadway.juegos.Apuesta;
import broadway.modelo.Formato;

/** Una apuesta de carrera con la cuota que se ofrecía en el momento de apostar. */
public class ApuestaCarrera implements Apuesta<int[]> {

    private final TipoApuestaCarrera tipo;
    private final int caballo;
    /** Solo para exacta: el caballo que debe llegar segundo. */
    private final int segundo;
    private final long monto;
    private final double cuota;

    ApuestaCarrera(TipoApuestaCarrera tipo, int caballo, int segundo, long monto, double cuota) {
        this.tipo = tipo;
        this.caballo = caballo;
        this.segundo = segundo;
        this.monto = monto;
        this.cuota = cuota;
    }

    /** ¿Gana con este orden de llegada? (índices de caballos, 0 = primero). */
    @Override
    public boolean gana(int[] orden) {
        switch (tipo) {
            case GANADOR:
                return orden[0] == caballo;
            case COLOCADO:
                return orden[0] == caballo || orden[1] == caballo || orden[2] == caballo;
            case EXACTA:
                return orden[0] == caballo && orden[1] == segundo;
            default:
                return false;
        }
    }

    /** Premio total (incluye lo apostado): monto × cuota. */
    @Override
    public long calcularPremio(int[] orden) {
        return gana(orden) ? premioSiGana() : 0;
    }

    /** monto × cuota calculado con enteros (la cuota tiene dos decimales) para evitar errores de redondeo. */
    public long premioSiGana() {
        return calcularPremio(monto, cuota);
    }

    public static long calcularPremio(long monto, double cuota) {
        return monto * Math.round(cuota * 100) / 100;
    }

    public TipoApuestaCarrera getTipo() {
        return tipo;
    }

    public int getCaballo() {
        return caballo;
    }

    public int getSegundo() {
        return segundo;
    }

    @Override
    public long getMonto() {
        return monto;
    }

    public double getCuota() {
        return cuota;
    }

    @Override
    public String getDescripcion() {
        if (tipo == TipoApuestaCarrera.EXACTA) {
            return "Exacta " + (caballo + 1) + "-" + (segundo + 1);
        }
        return tipo.getNombre() + " #" + (caballo + 1);
    }

    @Override
    public String toString() {
        return getDescripcion() + " — " + Formato.pesos(monto) + " a cuota " + Formato.cuota(cuota);
    }
}
