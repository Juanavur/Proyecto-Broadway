package servicios.simulacion;

import modelos.simulacion.juegos.Juego;
import modelos.simulacion.juegos.blackjack.Blackjack;
import modelos.simulacion.juegos.carreras.CarreraCaballos;
import modelos.simulacion.juegos.poker.PokerTexasHoldem;
import modelos.simulacion.juegos.ruleta.Ruleta;
import modelos.simulacion.juegos.tragamonedas.Tragamonedas;
import modelos.simulacion.Caja;
import modelos.simulacion.Jugador;
import persistencia.GestorArchivos;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * El Casino Broadway: reúne la caja, los juegos y al jugador de la sesión.
 * Solo registra y entrega los objetos; las reglas viven en cada juego.
 */
public class Casino {

    public static final String NOMBRE = "Casino Broadway";

    private final Random azar = new Random();
    private final Caja caja = new Caja();
    private final Tragamonedas tragamonedas = new Tragamonedas(azar);
    private final Blackjack blackjack = new Blackjack(azar);
    private final PokerTexasHoldem poker = new PokerTexasHoldem(azar);
    private final CarreraCaballos carreras = new CarreraCaballos(azar);
    private final Ruleta ruleta = new Ruleta(azar);
    private final GestorArchivos archivos = new GestorArchivos();
    private Jugador jugador;

    /** Todos los juegos, tratados de forma polimórfica como {@link Juego}. */
    public List<Juego> getJuegos() {
        return List.of(tragamonedas, blackjack, poker, carreras, ruleta);
    }

    public void guardar() throws IOException {
        if (jugador != null) {
            archivos.guardar(jugador);
        }
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Caja getCaja() {
        return caja;
    }

    public Tragamonedas getTragamonedas() {
        return tragamonedas;
    }

    public Blackjack getBlackjack() {
        return blackjack;
    }

    public PokerTexasHoldem getPoker() {
        return poker;
    }

    public CarreraCaballos getCarreras() {
        return carreras;
    }

    public Ruleta getRuleta() {
        return ruleta;
    }

    public GestorArchivos getArchivos() {
        return archivos;
    }
}
