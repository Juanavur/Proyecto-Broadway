package modelos;

import java.time.LocalDate;

/**
 * Empleado del casino. Cada cargo (subclase) decide qué acciones puede realizar (RF-02, RF-03).
 */
public abstract class Trabajador extends Persona {

    public static final String CREAR_MESA = "CREAR_MESA";
    public static final String REGISTRAR_EMPLEADO = "REGISTRAR_EMPLEADO";
    public static final String REGISTRAR_CLIENTE = "REGISTRAR_CLIENTE";
    public static final String VENDER_FICHAS = "VENDER_FICHAS";
    public static final String MANEJAR_CAJA = "MANEJAR_CAJA";
    public static final String REGISTRAR_APUESTA = "REGISTRAR_APUESTA";
    public static final String CAMBIAR_ESTADO_MAQUINA = "CAMBIAR_ESTADO_MAQUINA";
    public static final String REGISTRAR_MAQUINA = "REGISTRAR_MAQUINA";
    public static final String VER_REPORTES = "VER_REPORTES";

    private final String id;
    private final String usuario;
    private final String contraseña;

    protected Trabajador(String documento, String nombre, LocalDate fechaNacimiento,
                         String id, String usuario, String contraseña) {
        super(documento, nombre, fechaNacimiento);
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (contraseña == null || contraseña.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres.");
        }
        this.id = id;
        this.usuario = usuario.trim();
        this.contraseña = contraseña;
    }

    /** Devuelve el cargo si el usuario y la contraseña coinciden, o null si no. */
    public String autenticacion(String usuario, String contraseña) {
        if (this.usuario.equalsIgnoreCase(usuario) && this.contraseña.equals(contraseña)) {
            return getCargo();
        }
        return null;
    }

    public abstract boolean puedeRealizar(String accion);

    public abstract String getCargo();

    /** Lanza un error si el cargo no permite la acción. */
    protected void exigir(String accion) {
        if (!puedeRealizar(accion)) {
            throw new IllegalStateException("Un " + getCargo().toLowerCase() + " no puede hacer esto.");
        }
    }

    public String getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }
}
