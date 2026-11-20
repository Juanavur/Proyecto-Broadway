package ui;

import modelos.Administrador;
import modelos.Casino;
import modelos.Crupier;
import modelos.TecnicoMaquina;
import modelos.Trabajador;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de un empleado después de iniciar sesión. El menú de la izquierda solo muestra
 * las secciones que su cargo puede usar (Trabajador.puedeRealizar).
 */
public class PanelTrabajo extends JPanel {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final List<Seccion> secciones = new ArrayList<>();
    private final List<BotonMenu> botones = new ArrayList<>();

    public PanelTrabajo(Casino casino, Trabajador usuario, Runnable cerrarSesion) {
        super(new BorderLayout());
        setBackground(Tema.FONDO);
        add(barraSuperior(usuario, cerrarSesion), BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Tema.PANEL);
        menu.setBorder(Tema.margen(18, 12, 18, 12));
        menu.setPreferredSize(new Dimension(220, 10));
        JLabel tituloMenu = Tema.etiqueta("MENÚ", Tema.negrita(12), Tema.TEXTO_SUAVE);
        tituloMenu.setBorder(Tema.margen(0, 8, 10, 0));
        menu.add(tituloMenu);

        if (usuario.puedeRealizar(Trabajador.REGISTRAR_EMPLEADO)) {
            agregar(menu, "Empleados", new SeccionEmpleados(casino, (Administrador) usuario));
        }
        if (usuario.puedeRealizar(Trabajador.CREAR_MESA)) {
            agregar(menu, "Mesas", new SeccionMesas(casino, (Administrador) usuario));
        }
        if (usuario.puedeRealizar(Trabajador.REGISTRAR_CLIENTE) || usuario.puedeRealizar(Trabajador.VER_REPORTES)) {
            agregar(menu, "Clientes", new SeccionClientes(casino, usuario));
        }
        if (usuario.puedeRealizar(Trabajador.MANEJAR_CAJA)) {
            agregar(menu, "Caja", new SeccionCaja(casino));
        }
        if (usuario.puedeRealizar(Trabajador.REGISTRAR_APUESTA)) {
            agregar(menu, "Mesas de juego", new SeccionMesaJuego(casino, (Crupier) usuario));
            agregar(menu, "Sala de máquinas", new SeccionTragamonedas(casino));
        }
        if (usuario.puedeRealizar(Trabajador.REGISTRAR_MAQUINA)) {
            agregar(menu, "Máquinas", new SeccionMaquinas(casino, (TecnicoMaquina) usuario));
        }
        menu.add(javax.swing.Box.createVerticalGlue());
        add(menu, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        if (!botones.isEmpty()) {
            mostrar(0);
        }
    }

    private void agregar(JPanel menu, String nombre, Seccion seccion) {
        int indice = secciones.size();
        secciones.add(seccion);
        contenido.add(seccion, nombre);
        BotonMenu b = new BotonMenu(nombre);
        b.addActionListener(e -> mostrar(indice));
        botones.add(b);
        menu.add(b);
        menu.add(javax.swing.Box.createVerticalStrut(6));
    }

    private void mostrar(int indice) {
        for (int i = 0; i < botones.size(); i++) {
            botones.get(i).activo = i == indice;
            botones.get(i).repaint();
        }
        secciones.get(indice).refrescar();
        tarjetas.show(contenido, botones.get(indice).getText());
    }

    private JComponent barraSuperior(Trabajador usuario, Runnable cerrarSesion) {
        JPanel barra = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(0x3A0E16), getWidth(), 0, Tema.VINO));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Tema.ORO);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
                g.dispose();
            }
        };
        barra.setBorder(Tema.margen(10, 18, 13, 18));
        PanelLogin.Logo logo = new PanelLogin.Logo();
        logo.setPreferredSize(new Dimension(170, 58));
        barra.add(logo, BorderLayout.WEST);

        String cargo = usuario.getCargo();
        JLabel quien = Tema.etiqueta("<html><div style='text-align:right'><b>" + usuario.getNombre()
                + "</b><br><span style='color:#F2DC8F'>" + cargo + "</span></div></html>", Tema.texto(15), Tema.CREMA);
        BotonCasino salir = new BotonCasino("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion.run());
        JPanel derecha = Componentes.fila(quien, salir);
        derecha.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 8));
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    /** Botón del menú lateral; el activo se resalta en dorado. */
    private static class BotonMenu extends JButton {

        boolean activo;

        BotonMenu(String texto) {
            super(texto);
            setFont(Tema.negrita(15));
            setForeground(Tema.CREMA);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(Tema.margen(10, 14, 10, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            if (activo) {
                g.setColor(new Color(0xD4AF37));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                setForeground(new Color(0x2B1D00));
            } else {
                if (getModel().isRollover()) {
                    g.setColor(Tema.PANEL_CLARO);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                }
                setForeground(Tema.CREMA);
                g.setColor(Tema.BORDE);
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
}
