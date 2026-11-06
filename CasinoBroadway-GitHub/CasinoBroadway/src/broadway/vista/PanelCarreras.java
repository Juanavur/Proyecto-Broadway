package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.ServicioSonido;
import broadway.sonido.Sonidos;
import broadway.juegos.carreras.ApuestaCarrera;
import broadway.juegos.carreras.Caballo;
import broadway.juegos.carreras.CarreraCaballos;
import broadway.juegos.carreras.ResultadoCarrera;
import broadway.juegos.carreras.SimulacionCarrera;
import broadway.juegos.carreras.TipoApuestaCarrera;
import broadway.modelo.Formato;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.List;

/** Hipódromo: pista animada, cuotas y apuestas. */
public class PanelCarreras extends PanelJuego {

    static final Color[] COLORES = {
            new Color(0xD32F2F), new Color(0xF5F5F5), new Color(0x1E5BC6), new Color(0xFBC02D),
            new Color(0x2E7D32), new Color(0x212121), new Color(0xF57C00), new Color(0xEC6FA8)};
    private static final long[] FICHAS = {1_000, 5_000, 10_000, 25_000, 100_000};
    private static final int MS_POR_PASO = 55;

    private final CarreraCaballos carreras;
    private final Pista pista = new Pista();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"#", "Caballo", "Forma", "Ganador", "Colocado"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JRadioButton rGanador = radio("Ganador", true);
    private final JRadioButton rColocado = radio("Colocado (top 3)", false);
    private final JRadioButton rExacta = radio("Exacta", false);
    private final JComboBox<String> segundo = new JComboBox<>();
    private final JLabel montoLbl = Tema.etiqueta("", Tema.negrita(16), Tema.ORO_CLARO);
    private final JLabel cuotaLbl = Tema.etiqueta(" ", Tema.texto(13), Tema.CREMA);
    private final DefaultListModel<String> listaApuestas = new DefaultListModel<>();
    private final JLabel totalLbl = Tema.etiqueta("", Tema.negrita(14), Tema.CREMA);
    private final JLabel mensaje = Tema.etiqueta(" ", Tema.negrita(16), Tema.CREMA);
    private final BotonCasino apostar = new BotonCasino("Apostar", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino largar = new BotonCasino("¡LARGADA!", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino cancelar = new BotonCasino("Cancelar apuestas");
    private final BotonCasino siguiente = new BotonCasino("Siguiente carrera", BotonCasino.Estilo.PRIMARIO);

    private long monto = 10_000;
    private ResultadoCarrera resultado;
    private Timer animacion;
    private long inicioAnimacion;
    private double pasoActual;
    private ServicioSonido.Reproduccion galope;
    private static final int ESPERA_LARGADA_MS = 2300;

    public PanelCarreras(VentanaPrincipal ventana) {
        super(ventana, ventana.getCasino().getCarreras());
        this.carreras = ventana.getCasino().getCarreras();

        PanelFieltro arriba = new PanelFieltro(new BorderLayout());
        arriba.setBorder(Tema.margen(8, 12, 8, 12));
        arriba.add(pista, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(14, 0));
        abajo.setBackground(Tema.PANEL);
        abajo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE), Tema.margen(8, 14, 8, 14)));

        // Tabla de caballos
        tabla.setRowHeight(27);
        tabla.setFont(Tema.texto(14));
        tabla.getTableHeader().setFont(Tema.negrita(13));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        int[] anchos = {38, 170, 90, 80, 90};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setFont(Tema.negrita(14));
                l.setBackground(COLORES[r]);
                l.setForeground(r == 1 || r == 3 ? Color.BLACK : Color.WHITE);
                return l;
            }
        });
        tabla.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(Tema.ORO);
                return l;
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarCuota());
        JPanel izq = new JPanel(new BorderLayout(0, 4));
        izq.setOpaque(false);
        izq.add(Tema.etiqueta("Caballos y cuotas (toca uno para apostar)", Tema.negrita(14), Tema.ORO_CLARO), BorderLayout.NORTH);
        izq.add(Dialogos.scroll(tabla), BorderLayout.CENTER);
        izq.setPreferredSize(new Dimension(480, 10));
        abajo.add(izq, BorderLayout.WEST);

        // Armado de la apuesta
        JPanel medio = new JPanel();
        medio.setOpaque(false);
        medio.setLayout(new BoxLayout(medio, BoxLayout.Y_AXIS));
        JLabel t = Tema.etiqueta("Tu apuesta", Tema.negrita(14), Tema.ORO_CLARO);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(t);
        ButtonGroup grupo = new ButtonGroup();
        JPanel tipos = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tipos.setOpaque(false);
        for (JRadioButton r : new JRadioButton[]{rGanador, rColocado, rExacta}) {
            grupo.add(r);
            tipos.add(r);
            r.addActionListener(e -> actualizarCuota());
        }
        tipos.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(tipos);
        JPanel exacta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        exacta.setOpaque(false);
        exacta.add(Tema.etiqueta("Segundo en la exacta:", Tema.texto(13), Tema.TEXTO_SUAVE));
        segundo.setFont(Tema.texto(13));
        segundo.addActionListener(e -> actualizarCuota());
        exacta.add(segundo);
        exacta.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(exacta);
        SelectorFichas selector = new SelectorFichas(FICHAS, 18);
        selector.setAlClic(v -> {
            monto = v;
            actualizarCuota();
        });
        JPanel filaMonto = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaMonto.setOpaque(false);
        filaMonto.add(selector);
        BotonCasino doble = new BotonCasino("×2");
        doble.addActionListener(e -> {
            monto = Math.min(carreras.getApuestaMaxima(), monto * 2);
            actualizarCuota();
        });
        filaMonto.add(doble);
        filaMonto.add(montoLbl);
        filaMonto.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(filaMonto);
        cuotaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(cuotaLbl);
        apostar.addActionListener(e -> apostar());
        JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        filaBoton.setOpaque(false);
        filaBoton.add(apostar);
        filaBoton.setAlignmentX(Component.LEFT_ALIGNMENT);
        medio.add(filaBoton);
        abajo.add(medio, BorderLayout.CENTER);

        // Apuestas hechas y largada
        JPanel der = new JPanel(new BorderLayout(0, 4));
        der.setOpaque(false);
        der.setPreferredSize(new Dimension(330, 10));
        der.add(Tema.etiqueta("Apuestas para esta carrera", Tema.negrita(14), Tema.ORO_CLARO), BorderLayout.NORTH);
        JList<String> lista = new JList<>(listaApuestas);
        lista.setFont(Tema.texto(13));
        der.add(Dialogos.scroll(lista), BorderLayout.CENTER);
        JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
        botones.setOpaque(false);
        botones.add(totalLbl);
        botones.add(cancelar);
        largar.setFont(Tema.negrita(18));
        largar.addActionListener(e -> largar());
        cancelar.addActionListener(e -> {
            carreras.cancelarApuestas(jugador());
            ventana.actualizarSaldo();
            actualizarApuestas();
        });
        siguiente.addActionListener(e -> siguienteCarrera());
        botones.add(siguiente);
        botones.add(largar);
        der.add(botones, BorderLayout.SOUTH);
        abajo.add(der, BorderLayout.EAST);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(arriba, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBackground(Tema.PANEL);
        mensaje.setHorizontalAlignment(JLabel.CENTER);
        mensaje.setBorder(Tema.margen(6, 0, 0, 0));
        sur.add(mensaje, BorderLayout.NORTH);
        sur.add(abajo, BorderLayout.CENTER);
        abajo.setPreferredSize(new Dimension(10, 300));
        contenedor.add(sur, BorderLayout.SOUTH);
        add(contenedor, BorderLayout.CENTER);

        cargarCaballos();
        actualizarApuestas();
    }

    private static JRadioButton radio(String texto, boolean sel) {
        JRadioButton r = new JRadioButton(texto, sel);
        r.setOpaque(false);
        r.setForeground(Tema.CREMA);
        r.setFont(Tema.texto(14));
        r.setFocusPainted(false);
        return r;
    }

    private void cargarCaballos() {
        modelo.setRowCount(0);
        segundo.removeAllItems();
        for (Caballo c : carreras.getCaballos()) {
            int i = c.getNumero() - 1;
            modelo.addRow(new Object[]{c.getNumero(), c.getNombre(), estrellas(c.getForma()),
                    Formato.cuota(carreras.getCuotaGanador(i)), Formato.cuota(carreras.getCuotaColocado(i))});
            segundo.addItem(c.getNumero() + ". " + c.getNombre());
        }
        tabla.setRowSelectionInterval(0, 0);
        segundo.setSelectedIndex(1);
        resultado = null;
        pasoActual = 0;
        mensaje.setText("Carrera " + carreras.getNumeroCarrera() + " · 1.000 metros. Haz tus apuestas.");
        mensaje.setForeground(Tema.CREMA);
        pista.repaint();
    }

    private static String estrellas(int n) {
        return "★★★★★".substring(0, n) + "☆☆☆☆☆".substring(0, 5 - n);
    }

    private int caballoElegido() {
        int fila = tabla.getSelectedRow();
        return Math.max(0, fila);
    }

    private void actualizarCuota() {
        montoLbl.setText(Formato.pesos(monto));
        int c = caballoElegido();
        double cuota;
        String que;
        if (rExacta.isSelected()) {
            int s = segundo.getSelectedIndex();
            if (s < 0 || s == c) {
                cuotaLbl.setText("En la exacta escoge un segundo caballo distinto.");
                return;
            }
            cuota = carreras.getCuotaExacta(c, s);
            que = "Exacta " + (c + 1) + "-" + (s + 1);
        } else if (rColocado.isSelected()) {
            cuota = carreras.getCuotaColocado(c);
            que = "Colocado #" + (c + 1);
        } else {
            cuota = carreras.getCuotaGanador(c);
            que = "Ganador #" + (c + 1);
        }
        cuotaLbl.setText(que + " · cuota " + Formato.cuota(cuota) + " · si gana cobras " + Formato.pesos(ApuestaCarrera.calcularPremio(monto, cuota)));
    }

    private void apostar() {
        if (resultado != null || corriendo()) {
            ventana.aviso("Espera la siguiente carrera para apostar.", Tema.ERROR);
            return;
        }
        int c = caballoElegido();
        boolean ok;
        if (rExacta.isSelected()) {
            int s = segundo.getSelectedIndex();
            ok = intentar(() -> carreras.apostarExacta(jugador(), c, s, monto));
        } else if (rColocado.isSelected()) {
            ok = intentar(() -> carreras.apostarColocado(jugador(), c, monto));
        } else {
            ok = intentar(() -> carreras.apostarGanador(jugador(), c, monto));
        }
        if (ok) {
            Sonidos.reproducir(Efecto.FICHA);
            ventana.actualizarSaldo();
            actualizarApuestas();
        }
    }

    private void actualizarApuestas() {
        listaApuestas.clear();
        for (ApuestaCarrera a : carreras.getApuestas()) {
            listaApuestas.addElement(a.toString());
        }
        totalLbl.setText("Total: " + Formato.pesos(carreras.getTotalApostado()));
        boolean hay = !carreras.getApuestas().isEmpty();
        boolean libre = !corriendo() && resultado == null;
        largar.setEnabled(hay && libre);
        cancelar.setEnabled(hay && libre);
        apostar.setEnabled(libre);
        siguiente.setEnabled(resultado != null && !corriendo());
        actualizarCuota();
    }

    private boolean corriendo() {
        return animacion != null && animacion.isRunning();
    }

    private void largar() {
        final ResultadoCarrera[] r = new ResultadoCarrera[1];
        if (!intentar(() -> r[0] = carreras.correr(jugador()))) {
            return;
        }
        resultado = r[0];
        mensaje.setText("¡Suena la corneta! Los caballos entran a los partidores...");
        mensaje.setForeground(Tema.ORO_CLARO);
        Sonidos.reproducir(Efecto.CORNETA);
        inicioAnimacion = System.currentTimeMillis() + ESPERA_LARGADA_MS;
        animacion = new Timer(16, e -> {
            long ahora = System.currentTimeMillis();
            if (ahora < inicioAnimacion) {
                return; // todavía suena la corneta
            }
            if (galope == null) {
                galope = Sonidos.bucle(Efecto.GALOPE);
                mensaje.setText("¡Y arrancan!");
            }
            pasoActual = (ahora - inicioAnimacion) / (double) MS_POR_PASO;
            if (pasoActual >= resultado.getPosiciones().size() - 1) {
                pasoActual = resultado.getPosiciones().size() - 1;
                ((Timer) e.getSource()).stop();
                terminarCarrera();
            }
            pista.repaint();
        });
        animacion.start();
        actualizarApuestas();
        for (ApuestaCarrera a : resultado.getApuestas()) {
            listaApuestas.addElement(a.toString());
        }
    }

    private void terminarCarrera() {
        if (galope != null) {
            galope.detener();
            galope = null;
        }
        Sonidos.reproducir(Efecto.MULTITUD);
        if (resultado.getRonda().getNeto() > 0) {
            Sonidos.reproducir(Efecto.GANAR, 700);
            Sonidos.reproducir(Efecto.FICHAS, 1200);
        }
        int[] orden = resultado.getOrdenLlegada();
        List<Caballo> cab = carreras.getCaballos();
        StringBuilder sb = new StringBuilder("Llegada: 1º " + cab.get(orden[0]).getNombre() + " (#" + (orden[0] + 1) + "), 2º #"
                + (orden[1] + 1) + ", 3º #" + (orden[2] + 1) + ".   " + textoNeto(resultado.getRonda()) + ".");
        mensaje.setText(sb.toString());
        long neto = resultado.getRonda().getNeto();
        mensaje.setForeground(neto > 0 ? Tema.EXITO : neto == 0 ? Tema.ORO_CLARO : Tema.ERROR);
        listaApuestas.clear();
        for (ApuestaCarrera a : resultado.getApuestas()) {
            long premio = a.calcularPremio(orden);
            listaApuestas.addElement((premio > 0 ? "✔ " : "✘ ") + a.getDescripcion() + " — " + Formato.pesos(a.getMonto())
                    + (premio > 0 ? " → cobra " + Formato.pesos(premio) : ""));
        }
        rondaTerminada(resultado.getRonda());
        siguiente.setEnabled(true);
        largar.setEnabled(false);
        cancelar.setEnabled(false);
        totalLbl.setText("Pagado: " + Formato.pesos(resultado.getRonda().getPagado()));
    }

    private void siguienteCarrera() {
        carreras.prepararCarrera();
        cargarCaballos();
        actualizarApuestas();
    }

    @Override
    public boolean alSalir() {
        if (corriendo()) {
            ventana.aviso("Espera a que termine la carrera.", Tema.ERROR);
            return false;
        }
        if (!carreras.getApuestas().isEmpty()) {
            carreras.cancelarApuestas(jugador());
            ventana.actualizarSaldo();
            actualizarApuestas();
            ventana.aviso("Se cancelaron tus apuestas y se te devolvieron las fichas.", Tema.ORO_CLARO);
        }
        return true;
    }

    @Override
    public void prepararCierre() {
        if (animacion != null) {
            animacion.stop();
        }
        if (galope != null) {
            galope.detener();
        }
        if (jugador() != null && !carreras.getApuestas().isEmpty()) {
            carreras.cancelarApuestas(jugador());
        }
    }

    @Override
    public void alEntrar() {
        actualizarApuestas();
    }

    @Override
    public void dibujarIcono(Graphics2D g, float x, float y, float t) {
        g.setColor(new Color(0xB07A3A));
        g.fillRoundRect(Math.round(x), Math.round(y + t * 0.62f), Math.round(t), Math.round(t * 0.22f), 10, 10);
        dibujarCaballo(g, x + t * 0.5f, y + t * 0.58f, t * 0.9f, COLORES[0], 1, 0.3);
    }

    /** Dibuja un caballo con su jinete. (cx, cy) es el centro del cuerpo; {@code fase} anima las patas. */
    static void dibujarCaballo(Graphics2D g, float cx, float cy, float tam, Color seda, int numero, double fase) {
        float s = tam / 100f;
        Color pelo = new Color(0x7B4A26);
        Color oscuro = new Color(0x4A2A12);
        g.setStroke(new BasicStroke(5 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // patas
        double a = Math.sin(fase * Math.PI * 2);
        double b = Math.sin(fase * Math.PI * 2 + Math.PI);
        g.setColor(oscuro);
        pata(g, cx - 20 * s, cy + 8 * s, a, s);
        pata(g, cx + 20 * s, cy + 8 * s, b, s);
        g.setColor(pelo);
        pata(g, cx - 14 * s, cy + 8 * s, b, s);
        pata(g, cx + 26 * s, cy + 8 * s, a, s);
        // cola
        g.setColor(oscuro);
        GeneralPath cola = new GeneralPath();
        cola.moveTo(cx - 30 * s, cy - 6 * s);
        cola.quadTo(cx - 48 * s, cy - 2 * s, cx - 44 * s, cy + 16 * s);
        g.draw(cola);
        // cuerpo
        g.setColor(pelo);
        g.fill(new Ellipse2D.Float(cx - 34 * s, cy - 14 * s, 68 * s, 28 * s));
        // cuello y cabeza
        GeneralPath cuello = new GeneralPath();
        cuello.moveTo(cx + 20 * s, cy - 8 * s);
        cuello.lineTo(cx + 38 * s, cy - 32 * s);
        cuello.lineTo(cx + 54 * s, cy - 26 * s);
        cuello.lineTo(cx + 50 * s, cy - 18 * s);
        cuello.lineTo(cx + 38 * s, cy - 18 * s);
        cuello.lineTo(cx + 32 * s, cy + 2 * s);
        cuello.closePath();
        g.fill(cuello);
        g.setColor(oscuro);
        g.fill(new Ellipse2D.Float(cx + 36 * s, cy - 38 * s, 6 * s, 10 * s));
        // jinete
        g.setColor(seda);
        g.fill(new Ellipse2D.Float(cx - 6 * s, cy - 36 * s, 22 * s, 26 * s));
        g.setColor(new Color(0x111111));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Float(cx - 6 * s, cy - 36 * s, 22 * s, 26 * s));
        g.setColor(seda.darker());
        g.fill(new Ellipse2D.Float(cx + 4 * s, cy - 50 * s, 13 * s, 13 * s));
        // número en la manta
        g.setColor(seda);
        g.fillRoundRect(Math.round(cx - 20 * s), Math.round(cy - 10 * s), Math.round(24 * s), Math.round(18 * s), 4, 4);
        g.setColor(seda.getRed() + seda.getGreen() + seda.getBlue() > 500 ? Color.BLACK : Color.WHITE);
        g.setFont(Tema.negrita(Math.max(8, 13 * s)));
        Dibujo.centrar(g, String.valueOf(numero), cx - 8 * s, cy - 1 * s);
        g.setStroke(new BasicStroke(1f));
    }

    private static void pata(Graphics2D g, float x, float y, double angulo, float s) {
        float dx = (float) (Math.sin(angulo * 0.6) * 12 * s);
        g.drawLine(Math.round(x), Math.round(y), Math.round(x + dx), Math.round(y + 24 * s));
    }

    /** Pista con los ocho carriles. */
    private class Pista extends JComponent {
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int n = CarreraCaballos.NUMERO_CABALLOS;
            float carril = (h - 30) / (float) n;
            float tam = Math.min(carril * 1.25f, 70);
            float salida = tam * 1.15f + 8;
            float meta = w - 60;
            // césped y tierra
            g.setPaint(new GradientPaint(0, 0, new Color(0x3E8E41), 0, h, new Color(0x2C6B2F)));
            g.fillRect(0, 0, w, h);
            for (int i = 0; i < n; i++) {
                float y = 15 + i * carril;
                g.setColor(i % 2 == 0 ? new Color(0xB98A55) : new Color(0xAD7F4C));
                g.fillRect(0, (int) y, w, (int) Math.ceil(carril));
                g.setColor(new Color(255, 255, 255, 60));
                g.drawLine(0, (int) y, w, (int) y);
            }
            // marcas de distancia
            g.setFont(Tema.texto(11));
            for (int m = 250; m < 1000; m += 250) {
                float x = salida + (meta - salida) * m / 1000f;
                g.setColor(new Color(255, 255, 255, 70));
                g.drawLine((int) x, 15, (int) x, (int) (15 + n * carril));
                g.setColor(new Color(255, 255, 255, 170));
                g.drawString(m + " m", x + 3, 12);
            }
            // meta a cuadros
            int cuadro = 8;
            for (int yy = 15; yy < 15 + n * carril; yy += cuadro) {
                for (int k = 0; k < 2; k++) {
                    g.setColor(((yy / cuadro) + k) % 2 == 0 ? Color.WHITE : Color.BLACK);
                    g.fillRect((int) meta + k * cuadro, yy, cuadro, cuadro);
                }
            }
            g.setColor(Color.WHITE);
            g.setFont(Tema.negrita(12));
            g.drawString("META", meta - 6, 12);
            // caballos
            List<Caballo> cab = carreras.getCaballos();
            double[] posiciones = new double[n];
            if (resultado != null) {
                List<double[]> pos = resultado.getPosiciones();
                int k = (int) Math.floor(pasoActual);
                double f = pasoActual - k;
                double[] a = pos.get(Math.min(k, pos.size() - 1));
                double[] b = pos.get(Math.min(k + 1, pos.size() - 1));
                for (int i = 0; i < n; i++) {
                    posiciones[i] = a[i] + (b[i] - a[i]) * f;
                }
            }
            for (int i = 0; i < n; i++) {
                float y = 15 + (i + 0.62f) * carril;
                float x = salida + (float) (posiciones[i] / SimulacionCarrera.DISTANCIA) * (meta - salida);
                double fase = resultado == null || posiciones[i] >= SimulacionCarrera.DISTANCIA ? 0.25 : pasoActual / 3.0 + i * 0.13;
                dibujarCaballo(g, x - tam * 0.54f, y, tam, COLORES[i], i + 1, fase); // el hocico toca la posición
                if (resultado == null) {
                    g.setColor(new Color(0, 0, 0, 140));
                    g.setFont(Tema.negrita(12));
                    String nombre = cab.get(i).getNombre();
                    g.drawString(nombre, salida + 12, y + 4);
                }
            }
            // posiciones en vivo
            if (resultado != null) {
                Integer[] orden = new Integer[n];
                for (int i = 0; i < n; i++) {
                    orden[i] = i;
                }
                boolean terminada = !corriendo();
                if (terminada) {
                    int[] o = resultado.getOrdenLlegada();
                    for (int i = 0; i < n; i++) {
                        orden[i] = o[i];
                    }
                } else {
                    java.util.Arrays.sort(orden, (x, y) -> Double.compare(posiciones[y], posiciones[x]));
                }
                int bw = 34 * 3 + 16;
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRoundRect(10, 18, bw, 36, 12, 12);
                for (int i = 0; i < 3; i++) {
                    int c = orden[i];
                    int bx = 18 + i * 34;
                    g.setColor(COLORES[c]);
                    g.fillRoundRect(bx, 24, 28, 24, 6, 6);
                    g.setColor(c == 1 || c == 3 ? Color.BLACK : Color.WHITE);
                    g.setFont(Tema.negrita(14));
                    Dibujo.centrar(g, String.valueOf(c + 1), bx + 14, 36);
                }
                if (terminada) {
                    String txt = "Ganador: #" + (orden[0] + 1) + " " + cab.get(orden[0]).getNombre();
                    g.setFont(Tema.titulo(26));
                    int tw = g.getFontMetrics().stringWidth(txt) + 40;
                    g.setColor(new Color(0, 0, 0, 170));
                    g.fillRoundRect((w - tw) / 2, h / 2 - 28, tw, 56, 20, 20);
                    g.setColor(Tema.ORO_CLARO);
                    Dibujo.centrar(g, txt, w / 2f, h / 2f);
                }
            }
            g.dispose();
        }
    }
}
