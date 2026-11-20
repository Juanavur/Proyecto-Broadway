package broadway.modelo;

import java.time.LocalDate;
import java.time.Period;

/** Datos comunes de cualquier persona del casino (clientes y trabajadores). */
public abstract class Persona {

    private final String documento;
    private final String nombre;
    private final LocalDate fechaNacimiento;

    protected Persona(String documento, String nombre, LocalDate fechaNacimiento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida.");
        }
        this.documento = documento.trim();
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getEdad() {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
