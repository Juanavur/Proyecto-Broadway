package broadway.vista;

import broadway.modelo.Casino;
import broadway.modelo.Cliente;
import broadway.modelo.Formato;
import broadway.modelo.MaquinaTragaMoneda;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** Sala de máquinas: un cliente juega un giro en una máquina disponible (RF-09). */
public class SeccionTragamonedas extends Seccion {

    private final Casino casino;
    private final Carretes carretes = new Carretes();
    private final JComboBox<MaquinaTragaMoneda> maquinas = Componentes.combo();
    private final JComboBox<Cliente> clientes = Componentes.combo();
    private final JLabel saldo = Tema.etiqueta(" ", Tema.negrita(15), Tema.CREMA);
    private final JLabel resultado = Tema.etiqueta(" ", Tema.titulo(24), Tema.ORO_CLARO);
    private final BotonCasino girar = new BotonCasino("GIRAR", BotonCasino.Estilo.PRIMARIO);

    public SeccionTragamonedas(Casino casino) {
        this.casino = casino;
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Sala de máquinas"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("Cada giro descuenta su valor de las fichas del cliente. Tres símbolos iguales pagan 10 veces."),
                BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        JPanel centro = Componentes.bloque(18);
        carretes.setPreferredSize(new java.awt.Dimension(560, 380));
        centro.add(carretes, BorderLayout.NORTH);
        resultado.setHorizontalAlignment(JLabel.CENTER);
        resultado.setVerticalAlignment(JLabel.TOP);
        centro.add(resultado, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        girar.setFont(Tema.titulo(22));
        girar.addActionListener(e -> girar());
        JPanel lado = Componentes.tarjeta(new GridLayout(7, 1, 0, 8));
        lado.add(Componentes.subtitulo("Máquina"));
        lado.add(maquinas);
        lado.add(Componentes.subtitulo("Cliente"));
        lado.add(clientes);
        lado.add(saldo);
        lado.add(new JLabel());
        lado.add(girar);
        JPanel envoltura = Componentes.bloque(0);
        envoltura.add(lado, BorderLayout.NORTH);
        envoltura.setPreferredSize(new java.awt.Dimension(340, 10));
        add(envoltura, BorderLayout.EAST);
        clientes.addActionListener(e -> actualizarSaldo());
        refrescar();
    }

    private void girar() {
        if (carretes.girando()) {
            return;
        }
        try {
            MaquinaTragaMoneda m = (MaquinaTragaMoneda) maquinas.getSelectedItem();
            Cliente c = (Cliente) clientes.getSelectedItem();
            if (m == null || c == null) {
                throw new IllegalArgumentException("Escoja una máquina disponible y un cliente.");
            }
            MaquinaTragaMoneda.RondaMaquina ronda = m.jugarGiro(c);
            girar.setEnabled(false);
            resultado.setText(" ");
            actualizarSaldoAntes(c, ronda);
            carretes.girarHasta(ronda.getCarretes(), ronda.getPremio() > 0, () -> {
                resultado.setText(ronda.getPremio() > 0 ? "¡Tres iguales! Gana " + Formato.dinero(ronda.getPremio())
                        : "Sin premio esta vez");
                resultado.setForeground(ronda.getPremio() > 0 ? Tema.ORO_CLARO : Tema.TEXTO_SUAVE);
                girar.setEnabled(true);
                actualizarSaldo();
            });
        } catch (RuntimeException e) {
            Dialogos.error(this, e.getMessage());
        }
    }

    /** Mientras giran los carretes se muestra el saldo ya descontado el giro, sin el premio. */
    private void actualizarSaldoAntes(Cliente c, MaquinaTragaMoneda.RondaMaquina ronda) {
        saldo.setText("Fichas: " + Formato.dinero(c.getSaldoFichas() - ronda.getPremio()));
    }

    private void actualizarSaldo() {
        Cliente c = (Cliente) clientes.getSelectedItem();
        saldo.setText(c == null ? " " : "Fichas: " + Formato.dinero(c.getSaldoFichas()) + " · " + c.getNivel());
    }

    @Override
    public void refrescar() {
        Object m = maquinas.getSelectedItem();
        Object c = clientes.getSelectedItem();
        maquinas.removeAllItems();
        for (MaquinaTragaMoneda maquina : casino.getMaquinas()) {
            if (maquina.estaDisponible()) {
                maquinas.addItem(maquina);
            }
        }
        if (m != null) {
            maquinas.setSelectedItem(m);
        }
        clientes.removeAllItems();
        for (Cliente cliente : casino.getClientes()) {
            clientes.addItem(cliente);
        }
        if (c != null) {
            clientes.setSelectedItem(c);
        }
        actualizarSaldo();
    }
}
