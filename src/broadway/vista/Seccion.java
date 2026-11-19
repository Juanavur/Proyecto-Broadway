package broadway.vista;

import java.awt.BorderLayout;

/**
 * Pantalla de trabajo de un cargo (clientes, caja, mesas...). Cada sección sabe
 * volver a leer los datos del casino cuando se muestra.
 */
public abstract class Seccion extends PanelFieltro {

    protected Seccion() {
        super(new BorderLayout(16, 16));
        setBorder(Tema.margen(22, 26, 22, 26));
    }

    /** Vuelve a cargar tablas y textos desde el modelo. */
    public abstract void refrescar();

    /** Ejecuta una acción del modelo y muestra el error si algo no se cumple. */
    protected void intentar(Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            Dialogos.error(this, e.getMessage());
        }
        refrescar();
    }
}
