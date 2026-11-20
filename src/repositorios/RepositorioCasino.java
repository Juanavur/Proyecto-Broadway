package repositorios;

import modelos.Apuesta;
import modelos.Blackjack;
import modelos.Casino;
import modelos.Cliente;
import modelos.Crupier;
import modelos.Juego;
import modelos.MaquinaTragaMoneda;
import modelos.Mesa;
import modelos.MovimientoCaja;
import modelos.RondaMesa;
import modelos.Ruleta;
import modelos.Trabajador;
import modelos.TurnoCaja;
import persistencia.GestorArchivos;
import persistencia.Json;
import servicios.DatosIniciales;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Guarda y recupera todo el casino en datos/casino.json (RF-11, RNF-02): clientes con sus fichas
 * y niveles, empleados (con la contraseña cifrada, RNF-01), juegos, mesas con sus rondas,
 * máquinas con sus giros y la caja con sus turnos y movimientos.
 * <p>
 * Si el archivo no existe se arranca con los datos de ejemplo. Si está dañado se aparta
 * como copia de respaldo y también se arranca con los datos de ejemplo.
 */
public final class RepositorioCasino {

    public static final Path ARCHIVO_POR_DEFECTO = Paths.get("datos", "casino.json");
    private static final int VERSION = 1;

    private final Path archivo;
    private String ultimoGuardado;
    private String aviso;

    public RepositorioCasino() {
        this(ARCHIVO_POR_DEFECTO);
    }

    public RepositorioCasino(Path archivo) {
        this.archivo = archivo;
    }

    /** Carga el casino guardado o, si no hay uno válido, crea el de ejemplo y lo guarda. */
    public Casino cargarOCrear() {
        aviso = null;
        if (Files.exists(archivo)) {
            try {
                String texto = GestorArchivos.leerTexto(archivo);
                Casino casino = desdeJson(Json.leer(texto));
                ultimoGuardado = Json.escribir(aJson(casino));
                return casino;
            } catch (IOException | RuntimeException e) {
                apartarArchivoDanado(e);
            }
        }
        Casino nuevo = DatosIniciales.crear();
        guardarSiCambio(nuevo);
        return nuevo;
    }

    /**
     * Guarda el casino solo si cambió desde el último guardado.
     *
     * @return true si escribió el archivo
     */
    public synchronized boolean guardarSiCambio(Casino casino) {
        try {
            String contenido = Json.escribir(aJson(casino));
            if (contenido.equals(ultimoGuardado)) {
                return false;
            }
            GestorArchivos.escribirTexto(archivo, contenido);
            ultimoGuardado = contenido;
            return true;
        } catch (IOException | RuntimeException e) {
            aviso = "No se pudo guardar el casino en " + archivo + ": " + e.getMessage();
            return false;
        }
    }

    /** Mensaje para mostrar si algo falló al cargar o guardar; null si todo salió bien. */
    public String getAviso() {
        return aviso;
    }

    public Path getArchivo() {
        return archivo;
    }

    private void apartarArchivoDanado(Exception causa) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path respaldo = archivo.resolveSibling(archivo.getFileName() + ".danado-" + fecha);
        try {
            Files.move(archivo, respaldo, StandardCopyOption.REPLACE_EXISTING);
            aviso = "El archivo guardado no se pudo leer (" + causa.getMessage() + "). Se guardó una copia en "
                    + respaldo.getFileName() + " y se cargaron los datos de ejemplo.";
        } catch (IOException e) {
            aviso = "El archivo guardado no se pudo leer y tampoco se pudo apartar: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------ Casino -> JSON

    static Map<String, Object> aJson(Casino casino) {
        Map<String, Object> raiz = new LinkedHashMap<>();
        raiz.put("version", VERSION);
        raiz.put("nombre", casino.getNombre());

        List<Object> juegos = new ArrayList<>();
        for (Juego j : casino.getJuegos()) {
            juegos.add(j.getNombreJuego());
        }
        raiz.put("juegos", juegos);

        List<Object> empleados = new ArrayList<>();
        for (Trabajador t : casino.getEmpleados()) {
            Map<String, Object> m = persona(t.getDocumento(), t.getNombre(), t.getFechaNacimiento());
            m.put("cargo", t.getCargo());
            m.put("id", t.getId());
            m.put("usuario", t.getUsuario());
            m.put("contrasena", t.getContraseñaCifrada());
            empleados.add(m);
        }
        raiz.put("empleados", empleados);

        List<Object> clientes = new ArrayList<>();
        for (Cliente c : casino.getClientes()) {
            Map<String, Object> m = persona(c.getDocumento(), c.getNombre(), c.getFechaNacimiento());
            m.put("saldoFichas", c.getSaldoFichas());
            m.put("totalApostado", c.getTotalApostado());
            m.put("perdidasPeriodo", c.getPerdidasPeriodo());
            m.put("nivel", c.getNivel().name());
            clientes.add(m);
        }
        raiz.put("clientes", clientes);

        List<Object> mesas = new ArrayList<>();
        for (Mesa mesa : casino.getMesas()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo", mesa.getCodigoMesa());
            m.put("juego", mesa.getJuegoAsignado().getNombreJuego());
            m.put("apuestaMinima", mesa.getApuestaMinima());
            m.put("apuestaMaxima", mesa.getApuestaMaxima());
            m.put("jugadoresMaximos", mesa.getJugadoresMaximos());
            m.put("crupier", mesa.getCrupierAsignado() == null ? null : mesa.getCrupierAsignado().getUsuario());
            m.put("abierta", mesa.estaAbierta());
            List<Object> rondas = new ArrayList<>();
            for (RondaMesa r : mesa.getRondas()) {
                Map<String, Object> rm = new LinkedHashMap<>();
                rm.put("liquidada", r.estaLiquidada());
                rm.put("resultado", r.getResultado());
                List<Object> apuestas = new ArrayList<>();
                for (Apuesta a : r.getApuestas()) {
                    Map<String, Object> am = new LinkedHashMap<>();
                    am.put("tipo", a.getTipo());
                    am.put("monto", a.getMonto());
                    am.put("cliente", a.getCliente().getDocumento());
                    apuestas.add(am);
                }
                rm.put("apuestas", apuestas);
                rondas.add(rm);
            }
            m.put("rondas", rondas);
            mesas.add(m);
        }
        raiz.put("mesas", mesas);

        List<Object> maquinas = new ArrayList<>();
        for (MaquinaTragaMoneda maq : casino.getMaquinas()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo", maq.getCodigoMaquina());
            m.put("costoPorGiro", maq.getCostoPorGiro());
            m.put("enServicio", maq.estaDisponible());
            List<Object> jugadas = new ArrayList<>();
            for (MaquinaTragaMoneda.RondaMaquina r : maq.getJugadas()) {
                Map<String, Object> jm = new LinkedHashMap<>();
                jm.put("fecha", r.getHora().toString());
                jm.put("costo", r.getCosto());
                jm.put("premio", r.getPremio());
                jm.put("cliente", r.getJugador() == null ? null : r.getJugador().getDocumento());
                List<Object> carretes = new ArrayList<>();
                for (int c : r.getCarretes()) {
                    carretes.add(c);
                }
                jm.put("carretes", carretes);
                jugadas.add(jm);
            }
            m.put("jugadas", jugadas);
            maquinas.add(m);
        }
        raiz.put("maquinas", maquinas);

        List<Object> turnos = new ArrayList<>();
        for (TurnoCaja t : casino.getCaja().getTurnos()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("saldoInicial", t.getSaldoInicial());
            m.put("apertura", t.getApertura().toString());
            m.put("cierre", t.getCierre() == null ? null : t.getCierre().toString());
            List<Object> movimientos = new ArrayList<>();
            for (MovimientoCaja mov : t.getMovimientos()) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("tipo", mov.getTipo());
                mm.put("monto", mov.getMonto());
                mm.put("fecha", mov.getFecha().toString());
                movimientos.add(mm);
            }
            m.put("movimientos", movimientos);
            turnos.add(m);
        }
        raiz.put("turnosCaja", turnos);
        return raiz;
    }

    private static Map<String, Object> persona(String documento, String nombre, LocalDate nacimiento) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("documento", documento);
        m.put("nombre", nombre);
        m.put("fechaNacimiento", nacimiento.toString());
        return m;
    }

    // ------------------------------------------------------------------ JSON -> Casino

    static Casino desdeJson(Object json) {
        Map<String, Object> raiz = mapa(json, "casino");
        int version = (int) numero(raiz, "version");
        if (version != VERSION) {
            throw new IllegalArgumentException("Versión de archivo no soportada: " + version);
        }
        Casino casino = new Casino(texto(raiz, "nombre"));
        Random azar = new Random();

        Map<String, Juego> juegos = new LinkedHashMap<>();
        for (Object o : lista(raiz, "juegos")) {
            String nombre = (String) o;
            Juego juego;
            switch (nombre) {
                case "Ruleta": juego = new Ruleta(azar); break;
                case "Blackjack": juego = new Blackjack(azar); break;
                default: throw new IllegalArgumentException("Juego desconocido en el archivo: " + nombre);
            }
            casino.registrarJuego(juego);
            juegos.put(nombre, juego);
        }

        Map<String, Trabajador> empleados = new LinkedHashMap<>();
        for (Object o : lista(raiz, "empleados")) {
            Map<String, Object> m = mapa(o, "empleado");
            Trabajador t = Trabajador.restaurar(texto(m, "cargo"), texto(m, "documento"), texto(m, "nombre"),
                    fecha(m, "fechaNacimiento"), texto(m, "id"), texto(m, "usuario"), texto(m, "contrasena"), casino);
            if (!casino.registrarEmpleado(t)) {
                throw new IllegalArgumentException("Usuario repetido en el archivo: " + t.getUsuario());
            }
            empleados.put(t.getUsuario(), t);
        }

        Map<String, Cliente> clientes = new LinkedHashMap<>();
        for (Object o : lista(raiz, "clientes")) {
            Map<String, Object> m = mapa(o, "cliente");
            Cliente c = Cliente.restaurar(texto(m, "documento"), texto(m, "nombre"), fecha(m, "fechaNacimiento"),
                    numero(m, "saldoFichas"), numero(m, "totalApostado"), numero(m, "perdidasPeriodo"),
                    Cliente.NivelFidelidad.valueOf(texto(m, "nivel")));
            if (!casino.registrarCliente(c)) {
                throw new IllegalArgumentException("Documento repetido en el archivo: " + c.getDocumento());
            }
            clientes.put(c.getDocumento(), c);
        }

        for (Object o : lista(raiz, "mesas")) {
            Map<String, Object> m = mapa(o, "mesa");
            Juego juego = juegos.get(texto(m, "juego"));
            Mesa mesa = new Mesa(texto(m, "codigo"), juego, numero(m, "apuestaMinima"), numero(m, "apuestaMaxima"),
                    (int) numero(m, "jugadoresMaximos"));
            Object usuarioCrupier = m.get("crupier");
            if (usuarioCrupier != null) {
                Trabajador t = empleados.get((String) usuarioCrupier);
                if (!(t instanceof Crupier)) {
                    throw new IllegalArgumentException("La mesa " + mesa.getCodigoMesa() + " apunta a un crupier que no existe.");
                }
                mesa.asignarCrupier((Crupier) t);
            }
            for (Object ro : lista(m, "rondas")) {
                Map<String, Object> rm = mapa(ro, "ronda");
                List<Apuesta> apuestas = new ArrayList<>();
                for (Object ao : lista(rm, "apuestas")) {
                    Map<String, Object> am = mapa(ao, "apuesta");
                    apuestas.add(new Apuesta(texto(am, "tipo"), numero(am, "monto"), cliente(clientes, texto(am, "cliente"))));
                }
                mesa.restaurarRonda(booleano(rm, "liquidada"), (int) numero(rm, "resultado"), apuestas);
            }
            if (!casino.agregarMesa(mesa)) {
                throw new IllegalArgumentException("Mesa repetida en el archivo: " + mesa.getCodigoMesa());
            }
            if (booleano(m, "abierta")) {
                mesa.abrir();
            }
        }

        for (Object o : lista(raiz, "maquinas")) {
            Map<String, Object> m = mapa(o, "máquina");
            MaquinaTragaMoneda maq = new MaquinaTragaMoneda(texto(m, "codigo"), numero(m, "costoPorGiro"));
            for (Object jo : lista(m, "jugadas")) {
                Map<String, Object> jm = mapa(jo, "jugada");
                List<Object> carretesJson = lista(jm, "carretes");
                int[] carretes = new int[carretesJson.size()];
                for (int i = 0; i < carretes.length; i++) {
                    carretes[i] = ((Double) carretesJson.get(i)).intValue();
                }
                Object doc = jm.get("cliente");
                maq.restaurarJugada(fecha(jm, "fecha"), numero(jm, "costo"), numero(jm, "premio"),
                        doc == null ? null : cliente(clientes, (String) doc), carretes);
            }
            if (!booleano(m, "enServicio")) {
                maq.cerrar();
            }
            if (!casino.agregarMaquina(maq)) {
                throw new IllegalArgumentException("Máquina repetida en el archivo: " + maq.getCodigoMaquina());
            }
        }

        for (Object o : lista(raiz, "turnosCaja")) {
            Map<String, Object> m = mapa(o, "turno de caja");
            List<MovimientoCaja> movimientos = new ArrayList<>();
            for (Object mo : lista(m, "movimientos")) {
                Map<String, Object> mm = mapa(mo, "movimiento");
                movimientos.add(new MovimientoCaja(texto(mm, "tipo"), numero(mm, "monto"), fecha(mm, "fecha")));
            }
            Object cierre = m.get("cierre");
            casino.getCaja().restaurarTurno((long) numero(m, "id"), numero(m, "saldoInicial"), fecha(m, "apertura"),
                    cierre == null ? null : LocalDate.parse((String) cierre), movimientos);
        }
        return casino;
    }

    private static Cliente cliente(Map<String, Cliente> clientes, String documento) {
        Cliente c = clientes.get(documento);
        if (c == null) {
            throw new IllegalArgumentException("Un registro apunta a un cliente que no existe: " + documento);
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object o, String que) {
        if (!(o instanceof Map)) {
            throw new IllegalArgumentException("Se esperaba un objeto para " + que);
        }
        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lista(Map<String, Object> m, String campo) {
        Object o = m.get(campo);
        if (!(o instanceof List)) {
            throw new IllegalArgumentException("Falta la lista \"" + campo + "\"");
        }
        return (List<Object>) o;
    }

    private static String texto(Map<String, Object> m, String campo) {
        Object o = m.get(campo);
        if (!(o instanceof String)) {
            throw new IllegalArgumentException("Falta el texto \"" + campo + "\"");
        }
        return (String) o;
    }

    private static double numero(Map<String, Object> m, String campo) {
        Object o = m.get(campo);
        if (!(o instanceof Double)) {
            throw new IllegalArgumentException("Falta el número \"" + campo + "\"");
        }
        return (Double) o;
    }

    private static boolean booleano(Map<String, Object> m, String campo) {
        Object o = m.get(campo);
        if (!(o instanceof Boolean)) {
            throw new IllegalArgumentException("Falta el valor sí/no \"" + campo + "\"");
        }
        return (Boolean) o;
    }

    private static LocalDate fecha(Map<String, Object> m, String campo) {
        return LocalDate.parse(texto(m, campo));
    }
}
