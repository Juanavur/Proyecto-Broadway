import servicios.DatosIniciales;
import ui.Tema;
import ui.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class Main {

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (Exception ignorado) {
                // Si falla, se usa el aspecto por defecto.
            }
            Tema.aplicarGlobal();
            new VentanaPrincipal(DatosIniciales.crear()).setVisible(true);
        });
    }
}
