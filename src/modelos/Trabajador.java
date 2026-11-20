package modelos;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;

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
    /** Nunca se guarda en texto plano: es "sha256$sal$hash" (RNF-01). */
    private String contraseña;

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
        this.contraseña = cifrar(contraseña);
    }

    /** Devuelve el cargo si el usuario y la contraseña coinciden, o null si no. */
    public String autenticacion(String usuario, String contraseña) {
        if (this.usuario.equalsIgnoreCase(usuario) && verificar(contraseña, this.contraseña)) {
            return getCargo();
        }
        return null;
    }

    /** Cifra una contraseña con SHA-256 y una sal aleatoria. */
    static String cifrar(String contraseña) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return "sha256$" + Base64.getEncoder().encodeToString(sal) + "$" + hash(sal, contraseña);
    }

    private static boolean verificar(String contraseña, String cifrada) {
        if (contraseña == null || cifrada == null) {
            return false;
        }
        String[] partes = cifrada.split("\\$");
        if (partes.length != 3 || !partes[0].equals("sha256")) {
            return false;
        }
        byte[] sal = Base64.getDecoder().decode(partes[1]);
        return MessageDigest.isEqual(hash(sal, contraseña).getBytes(StandardCharsets.US_ASCII),
                partes[2].getBytes(StandardCharsets.US_ASCII));
    }

    private static String hash(byte[] sal, String contraseña) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(sal);
            return Base64.getEncoder().encodeToString(sha.digest(contraseña.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    /** La contraseña ya cifrada, para guardarla en archivo. */
    public String getContraseñaCifrada() {
        return contraseña;
    }

    /**
     * Vuelve a crear un empleado guardado en archivo (RF-11) con su contraseña ya cifrada.
     * El cargo es el mismo texto que devuelve getCargo().
     */
    public static Trabajador restaurar(String cargo, String documento, String nombre, LocalDate nacimiento,
                                       String id, String usuario, String contraseñaCifrada, Casino casino) {
        String temporal = "restaurando";
        Trabajador t;
        switch (cargo) {
            case "Administrador": t = new Administrador(documento, nombre, nacimiento, id, usuario, temporal, casino); break;
            case "Cajero": t = new Cajero(documento, nombre, nacimiento, id, usuario, temporal, casino); break;
            case "Crupier": t = new Crupier(documento, nombre, nacimiento, id, usuario, temporal); break;
            case "Técnico de máquinas": t = new TecnicoMaquina(documento, nombre, nacimiento, id, usuario, temporal, casino); break;
            default: throw new IllegalArgumentException("Cargo desconocido en el archivo: " + cargo);
        }
        if (contraseñaCifrada == null || !contraseñaCifrada.startsWith("sha256$")) {
            throw new IllegalArgumentException("La contraseña de " + usuario + " no está cifrada en el archivo.");
        }
        t.contraseña = contraseñaCifrada;
        return t;
    }

    public abstract Boolean puedeRealizar(String accion);

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
