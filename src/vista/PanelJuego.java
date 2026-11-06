package broadway.vista;

import broadway.juegos.Juego;
import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.OperacionInvalidaException;
import broadway.modelo.ResultadoRonda;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Base de las pantallas de juego: encabezado con el nombre, la descripción
 * y el botón de reglas, más utilidades comunes para mostrar resultados.
 */
public abstract class PanelJuego extends JPanel {

    protected final VentanaPrincipal ventana;
    private final Juego juego;

    protected PanelJuego(VentanaPrincipal ventana, Juego juego) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.juego = juego;
        setBackground(Tema.FONDO);
        add(crearEncabezado(), BorderLayout.NORTH);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE), Tema.margen(8, 18, 8, 18)));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.etiqueta(juego.getNombre(), Tema.titulo(24), Tema.ORO_CLARO));
        textos.add(Tema.etiqueta(juego.getDescripcion(), Tema.texto(13), Tema.TEXTO_SUAVE));
        p.add(textos, BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botones.setOpaque(false);
        JLabel limites = Tema.etiqueta(textoLimites(), Tema.texto(12), Tema.TEXTO_SUAVE);
        botones.add(limites);
        BotonCasino reglas = new BotonCasino("Reglas");
        reglas.addActionListener(e -> Dialogos.textoLargo(this, "Reglas — " + juego.getNombre(), juego.getReglas()));
        botones.add(reglas);
        p.add(botones, BorderLayout.EAST);
        return p;
    }

    /**
     * Texto de límites que se muestra en el encabezado (cada juego puede cambiarlo).
     * Se llama desde el constructor, así que solo debe usar {@link #getJuego()} o constantes.
     */
    protected String textoLimites() {
        return "Apuesta " + Formato.pesos(juego.getApuestaMinima()) + " a " + Formato.pesos(juego.getApuestaMaxima());
    }

    protected Jugador jugador() {
        return ventana.getCasino().getJugador();
    }

    /** Dibuja el ícono del juego para su tarjeta en el lobby, dentro de un cuadrado de lado {@code t}. */
    public abstract void dibujarIcono(java.awt.Graphics2D g, float x, float y, float t);

    /** Se llama cada vez que el jugador entra a esta pantalla. */
    public void alEntrar() {
    }

    /**
     * Se llama antes de salir de la pantalla. Devuelve false para impedirlo
     * (por ejemplo, si hay una animación o una mano a medias).
     */
    public boolean alSalir() {
        return true;
    }

    /** Se llama al cerrar la aplicación: deja el juego en un estado seguro (sin fichas perdidas). */
    public void prepararCierre() {
    }

    /** Actualiza saldos, avisa el cashback y guarda la partida al terminar una ronda. */
    protected void rondaTerminada(ResultadoRonda r) {
        ventana.actualizarSaldo();
        if (r != null && r.getCashback() > 0) {
            ventana.aviso("Cashback " + jugador().getNivel().getNombre() + ": +" + Formato.pesos(r.getCashback())
                    + " (" + jugador().getNivel().getPorcentajeCashback() + "% de lo perdido). Reclámalo en la caja.", Tema.EXITO);
        }
        ventana.guardarEnSilencio();
    }

    /** Ejecuta una acción del juego mostrando el error al jugador si viola una regla. */
    protected boolean intentar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (OperacionInvalidaException ex) {
            ventana.aviso(ex.getMessage(), Tema.ERROR);
            return false;
        }
    }

    protected static String textoNeto(ResultadoRonda r) {
        long neto = r.getNeto();
        if (neto > 0) {
            return "Ganaste " + Formato.pesos(neto);
        }
        if (neto == 0) {
            return "Recuperas tu apuesta";
        }
        return "Perdiste " + Formato.pesos(-neto);
    }

    public Juego getJuego() {
        return juego;
    }
}
