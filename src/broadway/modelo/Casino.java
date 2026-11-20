package broadway.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Punto central del sistema: guarda clientes, empleados, mesas, máquinas, juegos y la caja. */
public class Casino {

    /** Límite de mesas del casino: es un dato del casino, no de cada mesa. */
    public static int totalMesas = 50;

    private final String nombre;
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Trabajador> empleados = new ArrayList<>();
    private final List<Mesa> mesas = new ArrayList<>();
    private final List<MaquinaTragaMoneda> maquinas = new ArrayList<>();
    private final List<Juego> juegos = new ArrayList<>();
    private final Caja caja = new Caja();

    public Casino(String nombre) {
        this.nombre = nombre;
    }

    /** RF-01: no se permiten dos clientes con el mismo documento. */
    public boolean registrarCliente(Cliente cliente) {
        if (buscarClientePorDocumento(cliente.getDocumento()) != null) {
            return false;
        }
        clientes.add(cliente);
        return true;
    }

    /** RF-02: el usuario es único. */
    public boolean registrarEmpleado(Trabajador empleado) {
        for (Trabajador t : empleados) {
            if (t.getUsuario().equalsIgnoreCase(empleado.getUsuario())) {
                return false;
            }
        }
        empleados.add(empleado);
        return true;
    }

    public Cliente buscarClientePorDocumento(String documento) {
        for (Cliente c : clientes) {
            if (c.getDocumento().equals(documento.trim())) {
                return c;
            }
        }
        return null;
    }

    /** Busca al empleado que corresponde al usuario y la contraseña; null si no coincide ninguno. */
    public Trabajador iniciarSesion(String usuario, String contraseña) {
        for (Trabajador t : empleados) {
            if (t.autenticacion(usuario, contraseña) != null) {
                return t;
            }
        }
        return null;
    }

    /** RF-05: no se permiten dos juegos con el mismo nombre. */
    public boolean registrarJuego(Juego juego) {
        for (Juego j : juegos) {
            if (j.getNombreJuego().equalsIgnoreCase(juego.getNombreJuego())) {
                return false;
            }
        }
        juegos.add(juego);
        return true;
    }

    /** RF-04: código único, juego registrado y como máximo totalMesas mesas. */
    public boolean agregarMesa(Mesa mesa) {
        if (mesas.size() >= totalMesas) {
            throw new IllegalStateException("El casino ya tiene el máximo de " + totalMesas + " mesas.");
        }
        if (!juegos.contains(mesa.getJuegoAsignado())) {
            throw new IllegalArgumentException("Una mesa solo puede usar un juego registrado.");
        }
        for (Mesa m : mesas) {
            if (m.getCodigoMesa().equals(mesa.getCodigoMesa())) {
                return false;
            }
        }
        mesas.add(mesa);
        return true;
    }

    public boolean agregarMaquina(MaquinaTragaMoneda maquina) {
        for (MaquinaTragaMoneda m : maquinas) {
            if (m.getCodigoMaquina().equals(maquina.getCodigoMaquina())) {
                return false;
            }
        }
        maquinas.add(maquina);
        return true;
    }

    public List<Crupier> getCrupieres() {
        List<Crupier> lista = new ArrayList<>();
        for (Trabajador t : empleados) {
            if (t instanceof Crupier) {
                lista.add((Crupier) t);
            }
        }
        return lista;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(clientes);
    }

    public List<Trabajador> getEmpleados() {
        return Collections.unmodifiableList(empleados);
    }

    public List<Mesa> getMesas() {
        return Collections.unmodifiableList(mesas);
    }

    public List<MaquinaTragaMoneda> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    public List<Juego> getJuegos() {
        return Collections.unmodifiableList(juegos);
    }

    public Caja getCaja() {
        return caja;
    }
}
