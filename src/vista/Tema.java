package broadway.vista;

import broadway.modelo.NivelFidelidad;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Colores, fuentes y estilos comunes de la interfaz. */
public final class Tema {

    public static final Color FONDO = new Color(0x0B1F17);
    public static final Color PANEL = new Color(0x12332A);
    public static final Color PANEL_CLARO = new Color(0x1A4536);
    public static final Color BORDE = new Color(0x2C5E4B);
    public static final Color FIELTRO = new Color(0x0F6B43);
    public static final Color FIELTRO_OSCURO = new Color(0x08402A);
    public static final Color ORO = new Color(0xD4AF37);
    public static final Color ORO_CLARO = new Color(0xF2DC8F);
    public static final Color ORO_OSCURO = new Color(0x8C6D1A);
    public static final Color VINO = new Color(0x7A1F2B);
    public static final Color VINO_CLARO = new Color(0xA33445);
    public static final Color CREMA = new Color(0xF6F0E1);
    public static final Color TEXTO_SUAVE = new Color(0xA9BFB4);
    public static final Color ROJO_RULETA = new Color(0xC0272D);
    public static final Color NEGRO_RULETA = new Color(0x1B1B1B);
    public static final Color VERDE_CERO = new Color(0x13834F);
    public static final Color EXITO = new Color(0x4CD08A);
    public static final Color ERROR = new Color(0xFF7A7A);

    private static final String SERIF = "Serif";
    private static final String SANS = "SansSerif";

    private Tema() {
    }

    public static Font titulo(float tamano) {
        return new Font(SERIF, Font.BOLD, Math.round(tamano));
    }

    public static Font texto(float tamano) {
        return new Font(SANS, Font.PLAIN, Math.round(tamano));
    }

    public static Font negrita(float tamano) {
        return new Font(SANS, Font.BOLD, Math.round(tamano));
    }

    public static Font mono(float tamano) {
        return new Font(Font.MONOSPACED, Font.PLAIN, Math.round(tamano));
    }

    public static Color colorNivel(NivelFidelidad nivel) {
        switch (nivel) {
            case BRONCE: return new Color(0xCD7F32);
            case PLATA: return new Color(0xC9CED6);
            case ORO: return new Color(0xFFD24A);
            default: return new Color(0x8FE3FF);
        }
    }

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    public static Border margen(int arriba, int izquierda, int abajo, int derecha) {
        return BorderFactory.createEmptyBorder(arriba, izquierda, abajo, derecha);
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Colores por defecto de los componentes estándar de Swing (diálogos, tablas, listas...). */
    public static void aplicarGlobal() {
        UIManager.put("Panel.background", FONDO);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", CREMA);
        UIManager.put("Label.foreground", CREMA);
        UIManager.put("ToolTip.background", PANEL_CLARO);
        UIManager.put("ToolTip.foreground", CREMA);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(ORO_OSCURO));
        UIManager.put("ScrollBar.thumb", BORDE);
        UIManager.put("ScrollBar.track", PANEL);
        UIManager.put("ComboBox.background", PANEL_CLARO);
        UIManager.put("ComboBox.foreground", CREMA);
        UIManager.put("ComboBox.selectionBackground", ORO_OSCURO);
        UIManager.put("ComboBox.selectionForeground", CREMA);
        UIManager.put("TextField.background", PANEL_CLARO);
        UIManager.put("TextField.foreground", CREMA);
        UIManager.put("TextField.caretForeground", ORO_CLARO);
        UIManager.put("FormattedTextField.background", PANEL_CLARO);
        UIManager.put("FormattedTextField.foreground", CREMA);
        UIManager.put("Spinner.background", PANEL_CLARO);
        UIManager.put("RadioButton.background", PANEL);
        UIManager.put("RadioButton.foreground", CREMA);
        UIManager.put("Table.background", PANEL);
        UIManager.put("Table.foreground", CREMA);
        UIManager.put("Table.gridColor", BORDE);
        UIManager.put("Table.selectionBackground", ORO_OSCURO);
        UIManager.put("Table.selectionForeground", CREMA);
        UIManager.put("TableHeader.background", PANEL_CLARO);
        UIManager.put("TableHeader.foreground", ORO_CLARO);
        UIManager.put("Viewport.background", PANEL);
        UIManager.put("ScrollPane.background", PANEL);
        UIManager.put("TextArea.background", PANEL);
        UIManager.put("TextArea.foreground", CREMA);
        UIManager.put("List.background", PANEL);
        UIManager.put("List.foreground", CREMA);
    }
}
