package broadway.persistencia;

import broadway.modelo.Jugador;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Guarda y carga la partida del jugador en un archivo, para que el efectivo,
 * las fichas, el cashback y el historial sobrevivan al cierre de la aplicación.
 *
 * Es la única clase que sabe de archivos: si mañana se cambia el formato,
 * solo se modifica esta clase.
 */
public class GestorArchivos {

    private final Path archivo;

    public GestorArchivos() {
        this(Paths.get("datos", "partida.dat"));
    }

    public GestorArchivos(Path archivo) {
        this.archivo = archivo;
    }

    public boolean existePartida() {
        return Files.isRegularFile(archivo);
    }

    public void guardar(Jugador jugador) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        // Se escribe primero en un archivo temporal para no dañar la partida si algo falla a mitad.
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temporal);
             ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(jugador);
        }
        Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
    }

    public Jugador cargar() throws IOException {
        try (InputStream in = Files.newInputStream(archivo);
             ObjectInputStream ois = new ObjectInputStream(in)) {
            return (Jugador) ois.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("El archivo de partida no es válido.", e);
        }
    }

    public void borrar() throws IOException {
        Files.deleteIfExists(archivo);
    }

    public Path getArchivo() {
        return archivo;
    }
}
