package servicios;

import modelos.Administrador;
import modelos.Blackjack;
import modelos.Cajero;
import modelos.Casino;
import modelos.Cliente;
import modelos.Crupier;
import modelos.MaquinaTragaMoneda;
import modelos.Mesa;
import modelos.Ruleta;
import modelos.TecnicoMaquina;

import java.time.LocalDate;
import java.util.Random;

/**
 * Crea el casino con datos de ejemplo para poder probarlo de una vez:
 * un empleado de cada cargo, algunos clientes, dos mesas y tres máquinas.
 */
public final class DatosIniciales {

    private DatosIniciales() {
    }

    public static Casino crear() {
        Random azar = new Random();
        Casino casino = new Casino("Casino Broadway");
        Ruleta ruleta = new Ruleta(azar);
        Blackjack blackjack = new Blackjack(azar);
        casino.registrarJuego(ruleta);
        casino.registrarJuego(blackjack);

        Administrador admin = new Administrador("1010", "Laura Gómez", LocalDate.of(1985, 3, 12),
                "E1", "admin", "admin123", casino);
        casino.registrarEmpleado(admin);
        Cajero cajero = (Cajero) admin.definirCargoEmpleado("Cajero", "1020", "Andrés Pérez",
                LocalDate.of(1995, 7, 2), "cajero", "cajero123");
        admin.registrarEmpleados(cajero);
        Crupier crupier = (Crupier) admin.definirCargoEmpleado("Crupier", "1030", "Sofía Ruiz",
                LocalDate.of(1998, 11, 20), "crupier", "crupier123");
        admin.registrarEmpleados(crupier);
        TecnicoMaquina tecnico = (TecnicoMaquina) admin.definirCargoEmpleado("Técnico de máquinas", "1040",
                "Mateo Rojas", LocalDate.of(1990, 1, 5), "tecnico", "tecnico123");
        admin.registrarEmpleados(tecnico);

        Mesa mesaRuleta = admin.crearMesa("RUL-01", ruleta, 5_000, 500_000, 7, crupier);
        Mesa mesaBlackjack = admin.crearMesa("BJ-01", blackjack, 10_000, 1_000_000, 5, crupier);
        admin.registrarMesaDisponible(mesaRuleta);
        admin.registrarMesaDisponible(mesaBlackjack);
        mesaRuleta.abrir();
        mesaBlackjack.abrir();

        tecnico.registrarMaquinasTragaMonedas("MAQ-01", 2_000);
        tecnico.registrarMaquinasTragaMonedas("MAQ-02", 5_000);
        MaquinaTragaMoneda vip = tecnico.registrarMaquinasTragaMonedas("MAQ-VIP", 20_000);
        tecnico.ponerEnMantenimiento(vip);

        casino.getCaja().abrirTurno(2_000_000);
        cajero.registrarClientes("52301234", "Valentina Torres", LocalDate.of(1997, 4, 18));
        cajero.registrarClientes("80123456", "Carlos Méndez", LocalDate.of(1979, 9, 30));
        cajero.registrarClientes("1001234567", "Juliana Ríos", LocalDate.of(2003, 2, 14));
        for (Cliente c : casino.getClientes()) {
            cajero.venderFichas(c, 300_000);
        }
        return casino;
    }
}
