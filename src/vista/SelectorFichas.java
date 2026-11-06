package broadway.vista;

import broadway.sonido.Efecto;
import broadway.sonido.Sonidos;
import broadway.modelo.Formato;

import javax.swing.JComponent;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.LongConsumer;

/** Fila de fichas para escoger con qué denominación apostar. */
public class SelectorFichas extends JComponent {

    private final long[] valores;
    private int seleccion;
    private LongConsumer alClic;
    private final int radio;

    public SelectorFichas(long[] valores, int radio) {
        this.valores = valores;
        this.radio = radio;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = indiceEn(e.getX(), e.getY());
                if (i >= 0) {
                    seleccion = i;
                    Sonidos.reproducir(Efecto.FICHA);
                    repaint();
                    if (alClic != null) {
                        alClic.accept(valores[i]);
                    }
                }
            }
        });
        actualizarTooltip();
    }

    /** Acción adicional al hacer clic en una ficha (por ejemplo, sumarla a la apuesta). */
    public void setAlClic(LongConsumer alClic) {
        this.alClic = alClic;
    }

    public long getValor() {
        return valores[seleccion];
    }

    private void actualizarTooltip() {
        setToolTipText("Escoge el valor de la ficha");
    }

    private int indiceEn(int x, int y) {
        int paso = radio * 2 + 10;
        int i = (x - 4) / paso;
        if (i >= 0 && i < valores.length) {
            return i;
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = indiceEn(e.getX(), e.getY());
        return i >= 0 ? "Ficha de " + Formato.pesos(valores[i]) : null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(valores.length * (radio * 2 + 10) + 8, radio * 2 + 14);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        int paso = radio * 2 + 10;
        for (int i = 0; i < valores.length; i++) {
            float cx = 4 + i * paso + radio + 5;
            float cy = getHeight() / 2f - (i == seleccion ? 3 : 0);
            Dibujo.ficha(g, valores[i], cx, cy, radio, i == seleccion);
        }
        g.dispose();
    }
}
