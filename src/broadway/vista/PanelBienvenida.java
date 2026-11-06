package broadway.vista;

import broadway.modelo.Formato;
import broadway.modelo.Jugador;
import broadway.modelo.NivelFidelidad;
import broadway.modelo.OperacionInvalidaException;
import broadway.persistencia.GestorArchivos;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

/** Registro del jugador: datos personales, nivel de fidelidad y dinero con el que llega. */
public class PanelBienvenida extends PanelFieltro {

    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final long[] DINERO = {500_000, 1_000_000, 2_000_000, 5_000_000, 10_000_000};

    private final VentanaPrincipal ventana;
    private final JTextField nombre = campo();
    private final JTextField documento = campo();
    private final JComboBox<Integer> dia = new JComboBox<>();
    private final JComboBox<String> mes = new JComboBox<>(MESES);
    private final JComboBox<Integer> anio = new JComboBox<>();
    private final JComboBox<String> dinero = new JComboBox<>();
    private final TarjetaNivel[] tarjetas = new TarjetaNivel[NivelFidelidad.values().length];
    private final JLabel error = Tema.etiqueta(" ", Tema.negrita(13), Tema.ERROR);
    private NivelFidelidad nivelElegido = NivelFidelidad.BRONCE;

    public PanelBienvenida(VentanaPrincipal ventana) {
        super(new GridBagLayout());
        this.ventana = ventana;

        for (int d = 1; d <= 31; d++) {
            dia.addItem(d);
        }
        int actual = LocalDate.now().getYear();
        for (int a = actual; a >= actual - 100; a--) {
            anio.addItem(a);
        }
        anio.setSelectedItem(actual - 25);
        for (long d : DINERO) {
            dinero.addItem(Formato.pesos(d));
        }
        dinero.setSelectedIndex(1);
        for (JComboBox<?> c : new JComboBox<?>[]{dia, mes, anio, dinero}) {
            c.setFont(Tema.texto(14));
        }

        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(new Color(7, 26, 19, 235));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g.setColor(Tema.ORO_OSCURO);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(Tema.margen(22, 34, 22, 34));

        JLabel titulo = Tema.etiqueta("CASINO BROADWAY", Tema.titulo(40), Tema.ORO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = Tema.etiqueta("Regístrate en la entrada para pasar a la sala de juegos", Tema.texto(15), Tema.TEXTO_SUAVE);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(titulo);
        tarjeta.add(sub);
        tarjeta.add(Box.createVerticalStrut(14));

        JComponent continuar = crearContinuar();
        if (continuar != null) {
            continuar.setAlignmentX(Component.CENTER_ALIGNMENT);
            tarjeta.add(continuar);
            tarjeta.add(Box.createVerticalStrut(12));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        fila(form, c, 0, "Nombre completo", nombre);
        fila(form, c, 1, "Documento (solo números)", documento);
        JPanel fecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        fecha.setOpaque(false);
        fecha.add(dia);
        fecha.add(mes);
        fecha.add(anio);
        fila(form, c, 2, "Fecha de nacimiento", fecha);
        fila(form, c, 3, "Dinero con el que llegas", dinero);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(form);
        tarjeta.add(Box.createVerticalStrut(10));

        JLabel tn = Tema.etiqueta("Escoge tu nivel de fidelidad", Tema.negrita(15), Tema.ORO_CLARO);
        tn.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(tn);
        JLabel tn2 = Tema.etiqueta("Recibirás ese porcentaje de las fichas que pierdas como cashback.", Tema.texto(13), Tema.TEXTO_SUAVE);
        tn2.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(tn2);
        tarjeta.add(Box.createVerticalStrut(8));
        JPanel niveles = new JPanel(new GridLayout(1, 4, 12, 0));
        niveles.setOpaque(false);
        for (NivelFidelidad n : NivelFidelidad.values()) {
            TarjetaNivel t = new TarjetaNivel(n);
            tarjetas[n.ordinal()] = t;
            niveles.add(t);
        }
        niveles.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(niveles);
        tarjeta.add(Box.createVerticalStrut(12));

        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(error);
        tarjeta.add(Box.createVerticalStrut(8));
        BotonCasino entrar = new BotonCasino("Entrar al casino", BotonCasino.Estilo.PRIMARIO);
        entrar.setFont(Tema.negrita(17));
        entrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        entrar.addActionListener(e -> registrar());
        tarjeta.add(entrar);
        JLabel aviso = Tema.etiqueta("Solo mayores de 18 años. Juego virtual: no se usa dinero real.", Tema.texto(12), Tema.TEXTO_SUAVE);
        aviso.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(aviso);

        add(tarjeta);
        seleccionarNivel(NivelFidelidad.BRONCE);
    }

    private JComponent crearContinuar() {
        GestorArchivos archivos = ventana.getCasino().getArchivos();
        if (!archivos.existePartida()) {
            return null;
        }
        try {
            Jugador guardado = archivos.cargar();
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
            p.setOpaque(false);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE), Tema.margen(6, 10, 6, 10)));
            p.add(Tema.etiqueta("Partida guardada: " + guardado.getNombre() + " (" + guardado.getNivel().getNombre()
                    + ") — fichas " + Formato.pesos(guardado.getFichas()) + ", efectivo " + Formato.pesos(guardado.getDinero()),
                    Tema.texto(14), Tema.CREMA));
            BotonCasino seguir = new BotonCasino("Continuar", BotonCasino.Estilo.PRIMARIO);
            seguir.addActionListener(e -> ventana.entrar(guardado, false));
            p.add(seguir);
            return p;
        } catch (IOException ex) {
            return Tema.etiqueta("Había una partida guardada pero no se pudo leer; puedes empezar una nueva.", Tema.texto(13), Tema.ERROR);
        }
    }

    private void registrar() {
        try {
            LocalDate fecha = LocalDate.of((Integer) anio.getSelectedItem(), mes.getSelectedIndex() + 1,
                    (Integer) dia.getSelectedItem());
            Jugador j = new Jugador(nombre.getText(), documento.getText().trim(), fecha, nivelElegido,
                    DINERO[dinero.getSelectedIndex()]);
            if (ventana.getCasino().getArchivos().existePartida()
                    && !Dialogos.confirmar(this, "Nueva partida", "Ya hay una partida guardada. Si empiezas una nueva, la anterior se reemplaza.", "Empezar de nuevo")) {
                return;
            }
            error.setText(" ");
            ventana.entrar(j, true);
        } catch (DateTimeException ex) {
            error.setText("Esa fecha no existe. Revisa el día y el mes.");
        } catch (OperacionInvalidaException ex) {
            error.setText(ex.getMessage());
        }
    }

    private void seleccionarNivel(NivelFidelidad n) {
        nivelElegido = n;
        for (TarjetaNivel t : tarjetas) {
            t.repaint();
        }
    }

    private static JTextField campo() {
        JTextField t = new JTextField(22);
        t.setFont(Tema.texto(15));
        t.setBackground(Tema.PANEL_CLARO);
        t.setForeground(Tema.CREMA);
        t.setCaretColor(Tema.ORO_CLARO);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE), Tema.margen(6, 8, 6, 8)));
        return t;
    }

    private static void fila(JPanel form, GridBagConstraints c, int y, String texto, JComponent campo) {
        c.gridy = y;
        c.gridx = 0;
        c.weightx = 0;
        form.add(Tema.etiqueta(texto, Tema.texto(14), Tema.CREMA), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(campo, c);
    }

    /** Tarjeta seleccionable de un nivel de fidelidad. */
    private class TarjetaNivel extends JComponent {
        private final NivelFidelidad nivel;

        TarjetaNivel(NivelFidelidad nivel) {
            this.nivel = nivel;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    seleccionarNivel(nivel);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(132, 92);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            boolean sel = nivel == nivelElegido;
            Color c = Tema.colorNivel(nivel);
            int w = getWidth() - 2;
            int h = getHeight() - 2;
            g.setColor(sel ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 60) : Tema.PANEL);
            g.fillRoundRect(1, 1, w, h, 18, 18);
            g.setColor(sel ? c : Tema.BORDE);
            g.setStroke(new java.awt.BasicStroke(sel ? 3f : 1f));
            g.drawRoundRect(1, 1, w, h, 18, 18);
            g.setColor(c);
            g.setFont(Tema.titulo(19));
            Dibujo.centrar(g, nivel.getNombre(), getWidth() / 2f, h * 0.30f);
            g.setFont(Tema.negrita(28));
            Dibujo.centrar(g, nivel.getPorcentajeCashback() + "%", getWidth() / 2f, h * 0.62f);
            g.setFont(Tema.texto(11));
            g.setColor(Tema.TEXTO_SUAVE);
            Dibujo.centrar(g, "de cashback", getWidth() / 2f, h * 0.86f);
            g.dispose();
        }
    }
}
