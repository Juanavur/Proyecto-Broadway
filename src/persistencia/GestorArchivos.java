package persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Lee y escribe archivos de texto (RF-11, RNF-02).
 * Escribe primero en un archivo temporal y luego lo reemplaza, para que un corte
 * a mitad de la escritura nunca deje dañado el archivo anterior.
 */
public final class GestorArchivos {

    private GestorArchivos() {
    }

    /** Escribe el texto en UTF-8 de forma segura (temporal + reemplazo). */
    public static void escribirTexto(Path archivo, String contenido) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        Files.createDirectories(carpeta);
        Path temporal = Files.createTempFile(carpeta, archivo.getFileName().toString(), ".tmp");
        try {
            Files.write(temporal, contenido.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public static String leerTexto(Path archivo) throws IOException {
        return new String(Files.readAllBytes(archivo), StandardCharsets.UTF_8);
    }
}
