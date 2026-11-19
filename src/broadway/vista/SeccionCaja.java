package broadway.vista;

import broadway.modelo.Caja;
import broadway.modelo.Casino;
import broadway.modelo.Formato;
import broadway.modelo.MovimientoCaja;
import broadway.modelo.TurnoCaja;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** Turnos de caja: abrir con una base, ver los movimientos y cerrar con el arqueo (RF-07). */
public class SeccionCaja extends Seccion {

    private final Caja caja;
    private final DefaultTableModel modelo = Componentes.modelo("Fecha", "Tipo", "Monto");
    private final JLabel estado = Tema.etiqueta("", Tema.titulo(22), Tema.CREMA);
    private final JLabel base = valor();
    private final JLabel ventas = valor();
    private final JLabel canjes = valor();
    private final JLabel esperado = valor();
    private final JTextField campoBase = Componentes.campo(10);
    private final JTextField campoContado = Componentes.campo(10);
    private final BotonCasino abrir = new BotonCasino("Abrir turno", BotonCasino.Estilo.PRIMARIO);
    private final BotonCasino cerrar = new BotonCasino("Cerrar turno", BotonCasino.Estilo.PELIGRO);

    public SeccionCaja(Casino casino) {
        this.caja = casino.getCaja();
        JPanel cabecera = Componentes.bloque(4);
        cabecera.add(Componentes.titulo("Caja"), BorderLayout.NORTH);
        cabecera.add(Componentes.nota("Cada venta y canje queda registrado en el turno. Al cerrar se compara el balance"
                + " (ventas - canjes) con el efectivo contado."), BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);

        JPanel resumen = Componentes.tarjeta(new GridLayout(5, 2, 10, 8));
        resumen.add(Componentes.subtitulo("Turno"));
        resumen.add(estado);
        resumen.add(etiqueta("Base inicial"));
        resumen.add(base);
        resumen.add(etiqueta("Ventas de fichas"));
        resumen.add(ventas);
        resumen.add(etiqueta("Canjes de fichas"));
        resumen.add(canjes);
        resumen.add(etiqueta("Saldo esperado en caja"));
        resumen.add(esperado);

        campoBase.setText("2.000.000");
        abrir.addActionListener(e -> intentar(() -> caja.abrirTurno(Componentes.monto(campoBase.getText()))));
        cerrar.addActionListener(e -> intentar(() -> {
            String resultado = caja.cerrarTurno(Componentes.monto(campoContado.getText()));
            String[] partes = resultado.split(" · ", 2);
            if (partes[0].equals("DESCUADRE")) {
                Dialogos.error(this, "DESCUADRE\n\n" + partes[1]);
            } else {
                Dialogos.mensaje(this, "Caja cuadrada", partes[1]);
            }
            campoContado.setText("");
        }));
        JPanel acciones = Componentes.tarjeta(new BorderLayout(0, 10));
        acciones.add(Componentes.subtitulo("Abrir y cerrar"), BorderLayout.NORTH);
        acciones.add(Componentes.formulario("Base para abrir", campoBase,
                "Efectivo del turno contado (sin la base)", campoContado), BorderLayout.CENTER);
        acciones.add(Componentes.fila(abrir, cerrar), BorderLayout.SOUTH);

        JPanel izquierda = Componentes.bloque(14);
        izquierda.add(resumen, BorderLayout.NORTH);
        JPanel envoltura = Componentes.bloque(0);
        envoltura.add(acciones, BorderLayout.NORTH);
        izquierda.add(envoltura, BorderLayout.CENTER);
        add(izquierda, BorderLayout.WEST);

        JPanel movimientos = Componentes.tarjeta(new BorderLayout(0, 10));
        movimientos.add(Componentes.subtitulo("Movimientos del turno"), BorderLayout.NORTH);
        movimientos.add(Componentes.scroll(Componentes.tabla(modelo)), BorderLayout.CENTER);
        add(movimientos, BorderLayout.CENTER);
        refrescar();
    }

    private static JLabel valor() {
        return Tema.etiqueta("-", Tema.negrita(17), Tema.CREMA);
    }

    private static JLabel etiqueta(String texto) {
        return Tema.etiqueta(texto, Tema.texto(14), Tema.TEXTO_SUAVE);
    }

    @Override
    public void refrescar() {
        TurnoCaja turno = caja.turnoActual();
        modelo.setRowCount(0);
        abrir.setEnabled(turno == null);
        cerrar.setEnabled(turno != null);
        if (turno == null) {
            estado.setText("Cerrada");
            estado.setForeground(Tema.ERROR);
            base.setText("-");
            ventas.setText("-");
            canjes.setText("-");
            esperado.setText("-");
            return;
        }
        estado.setText("Abierto · N.º " + turno.getId());
        estado.setForeground(Tema.EXITO);
        double v = 0;
        double c = 0;
        for (MovimientoCaja m : turno.getMovimientos()) {
            modelo.addRow(new Object[]{m.getFecha(), m.getTipo(), Formato.dinero(m.getMonto())});
            if (m.getTipo().equals(MovimientoCaja.VENTA)) {
                v += m.getMonto();
            } else {
                c += m.getMonto();
            }
        }
        base.setText(Formato.dinero(turno.getSaldoInicial()));
        ventas.setText(Formato.dinero(v));
        canjes.setText(Formato.dinero(c));
        esperado.setText(Formato.dinero(turno.calcularSaldoEsperado()));
    }
}
