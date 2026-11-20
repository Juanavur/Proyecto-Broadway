package modelos;

import interfaces.Operable;

import java.util.ArrayList;
import java.util.List;

/** Mesa de juego con sus límites de apuesta, su capacidad y su crupier (RF-04). */
public class Mesa implements Operable {

    private final String codigoMesa;
    private final Juego juegoAsignado;
    private final double apuestaMinima;
    private final double apuestaMaxima;
    private final int jugadoresMaximos;
    private Crupier crupierAsignado;
    private boolean abierta;
    private final List<RondaMesa> rondas = new ArrayList<>();

    public Mesa(String codigoMesa, Juego juegoAsignado, double apuestaMinima, double apuestaMaxima,
                int jugadoresMaximos) {
        if (codigoMesa == null || codigoMesa.isBlank()) {
            throw new IllegalArgumentException("El código de la mesa es obligatorio.");
        }
        if (juegoAsignado == null) {
            throw new IllegalArgumentException("La mesa necesita un juego registrado.");
        }
        if (!Double.isFinite(apuestaMinima) || !Double.isFinite(apuestaMaxima)
                || apuestaMinima <= 0 || apuestaMinima >= apuestaMaxima) {
            throw new IllegalArgumentException("La apuesta mínima debe ser mayor que 0 y menor que la máxima.");
        }
        if (jugadoresMaximos < 1 || jugadoresMaximos > 7) {
            throw new IllegalArgumentException("La capacidad debe estar entre 1 y 7 jugadores.");
        }
        this.codigoMesa = codigoMesa.trim().toUpperCase();
        this.juegoAsignado = juegoAsignado;
        this.apuestaMinima = apuestaMinima;
        this.apuestaMaxima = apuestaMaxima;
        this.jugadoresMaximos = jugadoresMaximos;
    }

    public boolean validarMontoApuesta(double monto) {
        return monto >= apuestaMinima && monto <= apuestaMaxima;
    }

    public void asignarCrupier(Crupier crupier) {
        if (crupier == null) {
            throw new IllegalArgumentException("Solo se puede asignar un empleado con cargo Crupier.");
        }
        this.crupierAsignado = crupier;
    }

    public RondaMesa nuevaRonda() {
        if (!rondas.isEmpty()) {
            RondaMesa anterior = rondas.get(rondas.size() - 1);
            if (!anterior.estaLiquidada() && !anterior.getApuestas().isEmpty()) {
                throw new IllegalStateException("Primero hay que liquidar la ronda actual.");
            }
        }
        RondaMesa ronda = new RondaMesa(juegoAsignado, jugadoresMaximos);
        rondas.add(ronda);
        return ronda;
    }

    /** La ronda en curso; si no hay o la última ya se pagó, abre una nueva. */
    public RondaMesa rondaActual() {
        if (rondas.isEmpty() || rondas.get(rondas.size() - 1).estaLiquidada()) {
            return nuevaRonda();
        }
        return rondas.get(rondas.size() - 1);
    }

    @Override
    public boolean estaDisponible() {
        return abierta && crupierAsignado != null;
    }

    @Override
    public void abrir() {
        abierta = true;
    }

    @Override
    public void cerrar() {
        abierta = false;
    }

    public String getCodigoMesa() {
        return codigoMesa;
    }

    public Juego getJuegoAsignado() {
        return juegoAsignado;
    }

    public double getApuestaMinima() {
        return apuestaMinima;
    }

    public double getApuestaMaxima() {
        return apuestaMaxima;
    }

    public int getJugadoresMaximos() {
        return jugadoresMaximos;
    }

    public Crupier getCrupierAsignado() {
        return crupierAsignado;
    }

    public int getRondasJugadas() {
        int n = 0;
        for (RondaMesa r : rondas) {
            if (r.estaLiquidada()) {
                n++;
            }
        }
        return n;
    }

    @Override
    public String toString() {
        return codigoMesa + " · " + juegoAsignado.getNombreJuego();
    }
}
