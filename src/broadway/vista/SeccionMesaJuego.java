package broadway.vista;

import broadway.modelo.Apuesta;
import broadway.modelo.Blackjack;
import broadway.modelo.Casino;
import broadway.modelo.Cliente;
import broadway.modelo.Crupier;
import broadway.modelo.Formato;
import broadway.modelo.Mesa;
import broadway.modelo.RondaMesa;
import broadway.modelo.Ruleta;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Mesa del crupier: registra apuestas en la ronda y la liquida cuando sale el resultado (RF-08).
 * Según el juego de la mesa se ve la rueda de la ruleta o la mesa de blackjack.
 */
public class SeccionMesaJuego extends Seccion {

    private final Casino casino;
    private final Crupier crupier;
    private final JComboBox<Mesa> mesas = Componentes.combo();
    private final JComboBox<Cliente> clientes = Componentes.combo();
    private final JLabel infoMesa = Componentes.nota(" ");
    private final JLabel saldo = Tema.etiqueta(" ", Tema.negrita(15), Tema.CREMA);
    private final CardLayout tarjetasCentro = new CardLayout();
    private final CardLayout tarjetasControles = new CardLayout();
    private final JPanel centro = new JPanel(tarjetasCentro);
    private final JPanel controles = new JPanel(tarjetasControles);

    // Ruleta
    private final RuedaRuleta rueda = new RuedaRuleta();
    private final JComboBox<String> tipo = Componentes.combo();
    private final JSpinner numero = new JSpinner(new SpinnerNumberModel(17, 0, 36, 1));
    private final JTextField montoRuleta = Componentes.campo(9);
    private final DefaultTableModel apuestas = Componentes.modelo("Cliente", "Apuesta", "Monto", "Paga");
    private final BotonCasino girar = new BotonCasino("Girar ruleta", BotonCasino.Estilo.PRIMARIO);

    // Blackjack
    private final MesaBlackjack mesaBj = new MesaBlackjack();
    private final JTextField montoBj = Componentes.campo(9);
    private final BotonCasino repartir = new BotonCasino("Repartir", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino pedir = new BotonCasino("Pedir carta");
    private final BotonCasino plantarse = new BotonCasino("Plantarse");
    private final List<Integer> manoCasa = new ArrayList<>();
    private final List<Integer> manoJugador = new ArrayList<>();
    private boolean manoEnCurso;
    private boolean casaOculta;

    public SeccionMesaJuego(Casino casino, Crupier crupier) {
        this.casino = casino;
        this.crupier = crupier;
        JPanel cabecera = Componentes.bloque(6);
        cabecera.add(Componentes.titulo("Mesas de juego"), BorderLayout.WEST);
        JPanel elegir = Componentes.fila(Tema.etiqueta("Mesa", Tema.texto(14), Tema.CREMA), mesas, infoMesa);
        cabecera.add(elegir, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        centro.setOpaque(false);
        JPanel envolturaRueda = Componentes.bloque(0);
        envolturaRueda.add(rueda, BorderLayout.CENTER);
        centro.add(envolturaRueda, "Ruleta");
        centro.add(mesaBj, "Blackjack");
        JPanel vacio = Componentes.bloque(0);
        vacio.add(Componentes.nota("No hay mesas abiertas. El administrador puede abrirlas."), BorderLayout.CENTER);
        centro.add(vacio, "Nada");
        add(centro, BorderLayout.CENTER);

        controles.setOpaque(false);
        controles.add(controlesRuleta(), "Ruleta");
        controles.add(controlesBlackjack(), "Blackjack");
        controles.add(Componentes.bloque(0), "Nada");
        JPanel lado = Componentes.bloque(12);
        JPanel cliente = Componentes.tarjeta(new GridLayout(3, 1, 0, 6));
        cliente.add(Componentes.subtitulo("Cliente"));
        cliente.add(clientes);
        cliente.add(saldo);
        lado.add(cliente, BorderLayout.NORTH);
        lado.add(controles, BorderLayout.CENTER);
        lado.setPreferredSize(new java.awt.Dimension(380, 10));
        add(lado, BorderLayout.EAST);

        mesas.addActionListener(e -> mostrarMesa());
        clientes.addActionListener(e -> actualizarSaldo());
        refrescar();
    }

    private JPanel controlesRuleta() {
        for (String t : new Ruleta(new java.util.Random()).getTiposApuesta()) {
            tipo.addItem(t);
        }
        tipo.addActionListener(e -> numero.setEnabled("Pleno".equals(tipo.getSelectedItem())));
        montoRuleta.setText("10.000");
        BotonCasino apostar = new BotonCasino("Registrar apuesta");
        apostar.addActionListener(e -> intentar(() -> {
            String t = (String) tipo.getSelectedItem();
            if ("Pleno".equals(t)) {
                t = "Pleno " + numero.getValue();
            }
            crupier.registrarApuestas(Componentes.monto(montoRuleta.getText()), mesa(), cliente(), t);
        }));
        girar.addActionListener(e -> girarRuleta());
        JPanel p = Componentes.tarjeta(new BorderLayout(0, 10));
        p.add(Componentes.subtitulo("Apuestas de la ronda"), BorderLayout.NORTH);
        JPanel arriba = Componentes.bloque(8);
        arriba.add(Componentes.formulario("Tipo", tipo, "Número (pleno)", numero, "Monto", montoRuleta), BorderLayout.NORTH);
        arriba.add(apostar, BorderLayout.SOUTH);
        JPanel medio = Componentes.bloque(8);
        medio.add(arriba, BorderLayout.NORTH);
        medio.add(Componentes.scroll(Componentes.tabla(apuestas)), BorderLayout.CENTER);
        p.add(medio, BorderLayout.CENTER);
        p.add(girar, BorderLayout.SOUTH);
        return p;
    }

    private void girarRuleta() {
        Mesa mesa = mesa();
        if (mesa == null || rueda.girando()) {
            return;
        }
        RondaMesa ronda = mesa.rondaActual();
        if (ronda.getApuestas().isEmpty()) {
            Dialogos.error(this, "Registre al menos una apuesta antes de girar.");
            return;
        }
        Ruleta ruleta = (Ruleta) mesa.getJuegoAsignado();
        int n = ruleta.girar();
        girar.setEnabled(false);
        rueda.girarHasta(n, () -> {
            StringBuilder detalle = new StringBuilder();
            for (Apuesta a : ronda.getApuestas()) {
                double pago = ruleta.liquidarApuesta(a, n);
                detalle.append(a.getCliente().getNombre()).append(" · ").append(a.getTipo()).append(": ")
                        .append(pago > 0 ? "gana " + Formato.dinero(pago) : "pierde " + Formato.dinero(a.getMonto()))
                        .append('\n');
            }
            int ganadoras = ronda.liquidar(n);
            girar.setEnabled(true);
            refrescar();
            String color = n == 0 ? "verde" : (Ruleta.esRojo(n) ? "rojo" : "negro");
            Dialogos.mensaje(this, "Salió el " + n + " " + color,
                    ganadoras + " apuesta(s) ganadora(s).\n\n" + detalle);
        });
    }

    private JPanel controlesBlackjack() {
        montoBj.setText("20.000");
        repartir.addActionListener(e -> intentar(this::repartir));
        pedir.addActionListener(e -> intentar(() -> {
            Blackjack bj = (Blackjack) mesa().getJuegoAsignado();
            manoJugador.add(bj.nuevaCarta());
            mesaBj.cartaJugador(manoJugador.get(manoJugador.size() - 1));
            if (Blackjack.valorMano(manoJugador) > 21) {
                terminarMano(Blackjack.PIERDE, "Se pasó de 21.");
            } else {
                textos("¿Pide otra o se planta?");
            }
        }));
        plantarse.addActionListener(e -> intentar(this::jugarCasa));
        JPanel p = Componentes.tarjeta(new BorderLayout(0, 10));
        p.add(Componentes.subtitulo("Mano de blackjack"), BorderLayout.NORTH);
        JPanel medio = Componentes.bloque(8);
        medio.add(Componentes.formulario("Apuesta", montoBj), BorderLayout.NORTH);
        medio.add(Componentes.nota("<html>Blackjack natural paga 3 a 2, ganar paga 1 a 1 y el empate devuelve la apuesta."
                + " El crupier pide hasta 17.</html>"), BorderLayout.CENTER);
        p.add(medio, BorderLayout.CENTER);
        JPanel botones = new JPanel(new GridLayout(3, 1, 0, 6));
        botones.setOpaque(false);
        botones.add(repartir);
        botones.add(pedir);
        botones.add(plantarse);
        p.add(botones, BorderLayout.SOUTH);
        return p;
    }

    private void repartir() {
        if (manoEnCurso) {
            return;
        }
        Mesa mesa = mesa();
        crupier.registrarApuestas(Componentes.monto(montoBj.getText()), mesa, cliente(), "Mano");
        Blackjack bj = (Blackjack) mesa.getJuegoAsignado();
        manoCasa.clear();
        manoJugador.clear();
        mesaBj.limpiar();
        manoEnCurso = true;
        for (int i = 0; i < 2; i++) {
            manoJugador.add(bj.nuevaCarta());
            mesaBj.cartaJugador(manoJugador.get(i));
            manoCasa.add(bj.nuevaCarta());
            mesaBj.cartaCasa(manoCasa.get(i));
        }
        mesaBj.setOcultarSegunda(true);
        casaOculta = true;
        if (Blackjack.valorMano(manoJugador) == 21) {
            mesaBj.setOcultarSegunda(false);
            if (Blackjack.valorMano(manoCasa) == 21) {
                terminarMano(Blackjack.EMPATE, "Los dos tienen blackjack: empate.");
            } else {
                terminarMano(Blackjack.BLACKJACK, "¡Blackjack natural!");
            }
            return;
        }
        textos("¿Pide carta o se planta?");
    }

    private void jugarCasa() {
        if (!manoEnCurso) {
            return;
        }
        Blackjack bj = (Blackjack) mesa().getJuegoAsignado();
        mesaBj.setOcultarSegunda(false);
        while (Blackjack.valorMano(manoCasa) < 17) {
            manoCasa.add(bj.nuevaCarta());
            mesaBj.cartaCasa(manoCasa.get(manoCasa.size() - 1));
        }
        int casa = Blackjack.valorMano(manoCasa);
        int jugador = Blackjack.valorMano(manoJugador);
        if (casa > 21 || jugador > casa) {
            terminarMano(Blackjack.GANA, casa > 21 ? "El crupier se pasó: gana el cliente." : "Gana el cliente.");
        } else if (jugador == casa) {
            terminarMano(Blackjack.EMPATE, "Empate: se devuelve la apuesta.");
        } else {
            terminarMano(Blackjack.PIERDE, "Gana la casa.");
        }
    }

    private void terminarMano(int resultado, String texto) {
        mesaBj.setOcultarSegunda(false);
        casaOculta = false;
        RondaMesa ronda = mesa().rondaActual();
        Apuesta apuesta = ronda.getApuestas().get(0);
        double pago = mesa().getJuegoAsignado().liquidarApuesta(apuesta, resultado);
        ronda.liquidar(resultado);
        manoEnCurso = false;
        textos(texto + (pago > 0 ? "  Recibe " + Formato.dinero(pago) + "." : ""));
        refrescar();
    }

    private void textos(String mensaje) {
        String casa = manoCasa.isEmpty() ? "" : (casaOculta ? "· ?" : "· " + Blackjack.valorMano(manoCasa));
        mesaBj.setTextos(casa, manoJugador.isEmpty() ? "" : "· " + Blackjack.valorMano(manoJugador), mensaje);
        botonesBj();
    }

    private void botonesBj() {
        repartir.setEnabled(!manoEnCurso);
        pedir.setEnabled(manoEnCurso);
        plantarse.setEnabled(manoEnCurso);
    }

    private Mesa mesa() {
        Mesa m = (Mesa) mesas.getSelectedItem();
        if (m == null) {
            throw new IllegalStateException("No hay una mesa abierta.");
        }
        return m;
    }

    private Cliente cliente() {
        Cliente c = (Cliente) clientes.getSelectedItem();
        if (c == null) {
            throw new IllegalArgumentException("No hay clientes registrados.");
        }
        return c;
    }

    private void mostrarMesa() {
        Mesa m = (Mesa) mesas.getSelectedItem();
        String carta = m == null ? "Nada" : m.getJuegoAsignado().getNombreJuego();
        tarjetasCentro.show(centro, carta);
        tarjetasControles.show(controles, carta);
        infoMesa.setText(m == null ? " " : "   Apuestas de " + Formato.dinero(m.getApuestaMinima()) + " a "
                + Formato.dinero(m.getApuestaMaxima()) + " · hasta " + m.getJugadoresMaximos() + " jugadores · crupier "
                + m.getCrupierAsignado().getNombre());
        actualizarApuestas();
    }

    private void actualizarSaldo() {
        Cliente c = (Cliente) clientes.getSelectedItem();
        saldo.setText(c == null ? " " : "Fichas: " + Formato.dinero(c.getSaldoFichas()) + " · Nivel " + c.getNivel());
    }

    private void actualizarApuestas() {
        apuestas.setRowCount(0);
        Mesa m = (Mesa) mesas.getSelectedItem();
        if (m == null || !(m.getJuegoAsignado() instanceof Ruleta)) {
            return;
        }
        for (Apuesta a : m.rondaActual().getApuestas()) {
            apuestas.addRow(new Object[]{a.getCliente().getNombre(), a.getTipo(), Formato.dinero(a.getMonto()),
                    "+" + Formato.dinero(a.calcularPago(m.getJuegoAsignado()))});
        }
    }

    @Override
    public void refrescar() {
        if (manoEnCurso || rueda.girando()) {
            actualizarSaldo();
            actualizarApuestas();
            return;
        }
        Object mesaElegida = mesas.getSelectedItem();
        Object clienteElegido = clientes.getSelectedItem();
        mesas.removeAllItems();
        for (Mesa m : casino.getMesas()) {
            if (m.estaDisponible()) {
                mesas.addItem(m);
            }
        }
        if (mesaElegida != null) {
            mesas.setSelectedItem(mesaElegida);
        }
        clientes.removeAllItems();
        for (Cliente c : casino.getClientes()) {
            clientes.addItem(c);
        }
        if (clienteElegido != null) {
            clientes.setSelectedItem(clienteElegido);
        }
        mostrarMesa();
        actualizarSaldo();
        botonesBj();
    }
}
