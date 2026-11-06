package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.Casino;
import broadway.juegos.Juego;
import broadway.modelo.Jugador;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Ventana del casino: cambia entre la portada, la bienvenida, el lobby, la caja y los juegos. */
public class VentanaPrincipal extends JFrame {

    public static final String PORTADA = "portada";
    public static final String BIENVENIDA = "bienvenida";
    public static final String LOBBY = "lobby";
    public static final String CAJA = "caja";

    private final Casino casino;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final BarraSuperior barra;
    private final PanelLobby lobby;
    private final PanelCaja caja;
    private final Map<String, PanelJuego> juegos = new LinkedHashMap<>();
    private final Aviso aviso = new Aviso();
    private final Timer temporizadorAviso;
    private String pantallaActual = PORTADA;

    public VentanaPrincipal(Casino casino) {
        super(Casino.NOMBRE);
        this.casino = casino;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrar();
            }
        });

        barra = new BarraSuperior(this::irAlLobby, () -> mostrar(CAJA));
        barra.setVisible(false);

        registrarJuego(new PanelTragamonedas(this));
        registrarJuego(new PanelBlackjack(this));
        registrarJuego(new PanelPoker(this));
        registrarJuego(new PanelCarreras(this));
        registrarJuego(new PanelRuleta(this));

        PanelBienvenida bienvenida = new PanelBienvenida(this);
        contenido.add(new PanelPortada(() -> {
            mostrar(BIENVENIDA);
            javax.swing.SwingUtilities.invokeLater(() -> enfocarPrimerCampo(bienvenida));
        }), PORTADA);
        contenido.add(bienvenida, BIENVENIDA);
        lobby = new PanelLobby(this);
        contenido.add(lobby, LOBBY);
        caja = new PanelCaja(this);
        contenido.add(caja, CAJA);
        for (Map.Entry<String, PanelJuego> e : juegos.entrySet()) {
            contenido.add(e.getValue(), e.getKey());
        }

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(barra, BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        getLayeredPane().add(aviso, JLayeredPane.POPUP_LAYER);
        aviso.setVisible(false);
        temporizadorAviso = new Timer(3800, e -> aviso.setVisible(false));
        temporizadorAviso.setRepeats(false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ubicarAviso();
            }
        });

        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int ancho = Math.min(1440, pantalla.width - 20);
        int alto = Math.min(900, pantalla.height - 20);
        setSize(ancho, alto);
        setMinimumSize(new Dimension(Math.min(1100, ancho), Math.min(680, alto)));
        setLocationRelativeTo(null);
        if (pantalla.width <= 1366 || pantalla.height <= 768) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    /** Pone el cursor en el primer campo de texto de la pantalla (el nombre del jugador). */
    private static boolean enfocarPrimerCampo(java.awt.Container c) {
        for (java.awt.Component hijo : c.getComponents()) {
            if (hijo instanceof javax.swing.JTextField) {
                hijo.requestFocusInWindow();
                return true;
            }
            if (hijo instanceof java.awt.Container && enfocarPrimerCampo((java.awt.Container) hijo)) {
                return true;
            }
        }
        return false;
    }

    private void registrarJuego(PanelJuego panel) {
        juegos.put("juego-" + juegos.size(), panel);
    }

    // ============================================================ navegación

    /** Inicia la sesión de un jugador nuevo o recuperado. */
    public void entrar(Jugador jugador, boolean nuevo) {
        casino.setJugador(jugador);
        barra.setVisible(true);
        actualizarSaldo();
        guardarEnSilencio();
        if (nuevo) {
            mostrar(CAJA);
            aviso("Bienvenido, " + jugador.getNombre() + ". Compra fichas para empezar a jugar.", Tema.ORO_CLARO);
        } else {
            mostrar(LOBBY);
            aviso("Bienvenido de nuevo, " + jugador.getNombre() + ".", Tema.ORO_CLARO);
        }
    }

    public void irAlLobby() {
        mostrar(LOBBY);
    }

    public void irAJuego(PanelJuego panel) {
        for (Map.Entry<String, PanelJuego> e : juegos.entrySet()) {
            if (e.getValue() == panel) {
                mostrar(e.getKey());
                return;
            }
        }
    }

    public void mostrar(String clave) {
        if (clave.equals(pantallaActual)) {
            return;
        }
        PanelJuego saliente = juegos.get(pantallaActual);
        if (saliente != null && !saliente.alSalir()) {
            return;
        }
        pantallaActual = clave;
        cartas.show(contenido, clave);
        barra.setBotonesVisibles(!LOBBY.equals(clave), !CAJA.equals(clave));
        PanelJuego entrante = juegos.get(clave);
        if (entrante != null) {
            entrante.alEntrar();
        }
        if (LOBBY.equals(clave)) {
            lobby.actualizar();
        }
        if (CAJA.equals(clave)) {
            caja.actualizar();
        }
        actualizarSaldo();
    }

    public void actualizarSaldo() {
        barra.actualizar(casino.getJugador());
    }

    // ================================================================ avisos

    /** Muestra un aviso breve en la parte de abajo de la ventana. */
    public void aviso(String texto, Color color) {
        if (color == Tema.ERROR) {
            Sonidos.reproducir(Efecto.ERROR);
        }
        aviso.mostrar(texto, color);
        ubicarAviso();
        aviso.setVisible(true);
        temporizadorAviso.setInitialDelay(color == Tema.ERROR ? 4500 : 3800);
        temporizadorAviso.restart();
    }

    private void ubicarAviso() {
        Dimension d = aviso.getPreferredSize();
        int w = Math.min(d.width, getLayeredPane().getWidth() - 40);
        int x = (getLayeredPane().getWidth() - w) / 2;
        // Arriba, sobre la franja del encabezado, para no tapar los botones de juego.
        int y = (barra.isVisible() ? barra.getHeight() : 0) + 8;
        aviso.setBounds(x, y, w, d.height);
    }

    // ============================================================= guardado

    public void guardarEnSilencio() {
        try {
            casino.guardar();
        } catch (IOException ex) {
            aviso("No se pudo guardar la partida: " + ex.getMessage(), Tema.ERROR);
        }
    }

    public void cerrar() {
        if (casino.getJugador() != null) {
            for (PanelJuego p : juegos.values()) {
                p.prepararCierre();
            }
            try {
                casino.guardar();
            } catch (IOException ex) {
                Dialogos.error(this, "No se pudo guardar la partida: " + ex.getMessage());
            }
        }
        dispose();
        System.exit(0);
    }

    public Casino getCasino() {
        return casino;
    }

    public Map<String, PanelJuego> getPanelesJuego() {
        return juegos;
    }

    public PanelJuego getPanel(Juego juego) {
        for (PanelJuego p : juegos.values()) {
            if (p.getJuego() == juego) {
                return p;
            }
        }
        return null;
    }

    /** Recuadro flotante para los avisos. */
    private static class Aviso extends JComponent {
        private String texto = "";
        private Color color = Tema.CREMA;

        void mostrar(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Tema.negrita(15));
            return new Dimension(fm.stringWidth(texto) + 48, 46);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(new Color(8, 22, 16, 235));
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g.setColor(color);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            g.setFont(Tema.negrita(15));
            FontMetrics fm = g.getFontMetrics();
            String t = texto;
            while (fm.stringWidth(t) > getWidth() - 30 && t.length() > 4) {
                t = t.substring(0, t.length() - 4) + "…";
            }
            g.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
