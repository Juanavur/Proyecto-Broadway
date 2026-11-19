package broadway.simulacion;

import broadway.simulacion.modelo.Jugador;
import broadway.simulacion.modelo.NivelFidelidad;

import java.time.LocalDate;
import java.util.Random;

/**
 * Crea un cliente al azar para la simulación: nombre, documento, fecha de nacimiento
 * (siempre mayor de edad), nivel de fidelidad y dinero en efectivo.
 */
public final class ClienteAleatorio {

    private static final String[] NOMBRES = {"Valentina", "Santiago", "Mariana", "Sebastián", "Daniela", "Alejandro",
            "Camila", "Mateo", "Isabella", "Samuel", "Laura", "Nicolás", "Sofía", "Andrés", "Gabriela", "Felipe",
            "Juliana", "Tomás", "Natalia", "David", "Paula", "Juan Pablo", "Manuela", "Esteban", "Luisa", "Carlos"};
    private static final String[] APELLIDOS = {"Rodríguez", "Gómez", "González", "Martínez", "García", "López",
            "Hernández", "Sánchez", "Ramírez", "Pérez", "Díaz", "Torres", "Rojas", "Vargas", "Moreno", "Castro",
            "Jiménez", "Ortiz", "Ruiz", "Suárez", "Herrera", "Medina", "Restrepo", "Cárdenas", "Ospina", "Quintero"};

    private ClienteAleatorio() {
    }

    public static Jugador generar(Random azar) {
        String nombre = NOMBRES[azar.nextInt(NOMBRES.length)] + " " + APELLIDOS[azar.nextInt(APELLIDOS.length)]
                + " " + APELLIDOS[azar.nextInt(APELLIDOS.length)];
        // Cédula colombiana: 8 a 10 dígitos, sin empezar en 0.
        int digitos = 8 + azar.nextInt(3);
        StringBuilder documento = new StringBuilder().append(1 + azar.nextInt(9));
        for (int i = 1; i < digitos; i++) {
            documento.append(azar.nextInt(10));
        }
        LocalDate nacimiento = LocalDate.now().minusYears(18 + azar.nextInt(58)).minusDays(azar.nextInt(365));
        NivelFidelidad[] niveles = NivelFidelidad.values();
        NivelFidelidad nivel = niveles[azar.nextInt(niveles.length)];
        // Entre 200.000 y 5.000.000, en múltiplos de 50.000.
        long dinero = 50_000L * (4 + azar.nextInt(97));
        return new Jugador(nombre, documento.toString(), nacimiento, nivel, dinero);
    }
}
