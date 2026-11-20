package modelos;

import interfaces.Operable;
import servicios.Formato;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Máquina tragamonedas de tres carretes. Con tres símbolos iguales paga 10 veces el giro (RF-09). */
public class MaquinaTragaMoneda implements Operable {

    public static final int SIMBOLOS = 6;
    private static final Random AZAR = new Random();

    private final String codigoMaquina;
    private final double costoPorGiro;
    private boolean estado = true;
    private final List<RondaMaquina> jugadas = new ArrayList<>();

    public MaquinaTragaMoneda(String codigoMaquina, double costoPorGiro) {
        if (codigoMaquina == null || codigoMaquina.isBlank()) {
            throw new IllegalArgumentException("El código de la máquina es obligatorio.");
        }
        if (!Double.isFinite(costoPorGiro) || costoPorGiro <= 0) {
            throw new IllegalArgumentException("El valor por giro debe ser mayor que 0.");
        }
        this.codigoMaquina = codigoMaquina.trim().toUpperCase();
        this.costoPorGiro = costoPorGiro;
    }

    /** Cobra el giro al cliente, gira los tres carretes y paga si salen tres iguales. */
    public RondaMaquina jugarGiro(Cliente cliente) {
        if (!estaDisponible()) {
            throw new IllegalStateException("La máquina " + codigoMaquina + " está en mantenimiento.");
        }
        cliente.apostar(costoPorGiro);
        int[] carretes = {AZAR.nextInt(SIMBOLOS), AZAR.nextInt(SIMBOLOS), AZAR.nextInt(SIMBOLOS)};
        boolean tresIguales = carretes[0] == carretes[1] && carretes[1] == carretes[2];
        double premio = tresIguales ? costoPorGiro * 10 : 0;
        cliente.cobrarPremio(premio);
        RondaMaquina ronda = new RondaMaquina(costoPorGiro, premio, cliente, carretes);
        jugadas.add(ronda);
        return ronda;
    }

    /** Agrega un giro leído del archivo (RF-11), sin volver a cobrarlo ni pagarlo. */
    public void restaurarJugada(LocalDate hora, double costo, double premio, Cliente jugador, int[] carretes) {
        jugadas.add(new RondaMaquina(hora, costo, premio, jugador, carretes.clone()));
    }

    @Override
    public boolean estaDisponible() {
        return estado;
    }

    @Override
    public void abrir() {
        estado = true;
    }

    @Override
    public void cerrar() {
        estado = false;
    }

    public String getCodigoMaquina() {
        return codigoMaquina;
    }

    public double getCostoPorGiro() {
        return costoPorGiro;
    }

    public List<RondaMaquina> getJugadas() {
        return Collections.unmodifiableList(jugadas);
    }

    @Override
    public String toString() {
        return codigoMaquina + " · " + Formato.dinero(costoPorGiro) + " por giro";
    }

    /** Un giro de la máquina. Está anidada porque cada giro pertenece a una máquina concreta. */
    public static class RondaMaquina {

        private final LocalDate hora;
        private final double costo;
        private final double premio;
        private final Cliente jugador;
        private final int[] carretes;

        RondaMaquina(double costo, double premio, Cliente jugador, int[] carretes) {
            this(LocalDate.now(), costo, premio, jugador, carretes);
        }

        RondaMaquina(LocalDate hora, double costo, double premio, Cliente jugador, int[] carretes) {
            this.hora = hora;
            this.costo = costo;
            this.premio = premio;
            this.jugador = jugador;
            this.carretes = carretes;
        }

        public LocalDate getHora() {
            return hora;
        }

        public double getCosto() {
            return costo;
        }

        public double getPremio() {
            return premio;
        }

        public Cliente getJugador() {
            return jugador;
        }

        /** Símbolo que quedó en cada carrete (0 a SIMBOLOS - 1), para dibujarlos. */
        public int[] getCarretes() {
            return carretes.clone();
        }
    }
}
