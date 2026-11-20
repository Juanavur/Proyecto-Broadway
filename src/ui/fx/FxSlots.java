package ui.fx;

import enums.Efecto;
import interfaces.ServicioSonido;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import modelos.MaquinaTragaMoneda;
import servicios.Formato;
import servicios.sonido.Sonidos;

final class FxSlots extends FxCustomerGame {
    private final ComboBox<MaquinaTragaMoneda> machines = new ComboBox<>();
    private final Button spin = FxUi.button("GIRAR", "primary", this::spin);
    private final Label stake = FxUi.label("", "total");
    private final FxArt.Picture cabinet = new FxArt.Picture(620, 540, this::paintCabinet);
    private final double[] reels = {0, 2, 4};
    private boolean spinning;
    private boolean win;

    FxSlots(FxPlayContext view) {
        super(view, "Tragamonedas", "Tres carretes y una línea de premio.",
                "Escoge una máquina. El giro cuesta el precio marcado. Tres símbolos iguales en la línea central pagan diez veces el giro.", null);
        headerControls.getChildren().add(0, machines);
        machines.setOnAction(event -> refreshStake());
        VBox payTable = FxUi.panel(FxUi.label("Tabla de premios", "title"), FxUi.label("Tres iguales en la línea central", "hint"));
        for (int symbol = 0; symbol < FxArt.SYMBOLS.length; symbol++) {
            final int current = symbol;
            FxArt.Picture item = new FxArt.Picture(130, 42, (g, w, h) -> {
                for (int column = 0; column < 3; column++) FxArt.symbol(g, current, 22 + column * 42, h / 2, 30);
            });
            payTable.getChildren().add(FxUi.row(item, FxUi.label("×10", "total")));
        }
        HBox body = FxUi.row(cabinet, payTable);
        body.setPadding(new Insets(15, 18, 15, 18));
        HBox.setHgrow(cabinet, Priority.ALWAYS);
        cabinet.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        body.setFillHeight(true);
        setCenter(body);
        footer(FxUi.row(FxUi.spacer(), stake, spin));
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && !machines.isShowing()) {
                spin();
                event.consume();
            }
        });
        refreshStake();
    }

    @Override void enter() {
        MaquinaTragaMoneda previous = machines.getValue();
        machines.getItems().setAll(view.casino().getMaquinas().stream().filter(MaquinaTragaMoneda::estaDisponible).toList());
        machines.setValue(machines.getItems().contains(previous) ? previous : machines.getItems().stream().findFirst().orElse(null));
        message.setText(machines.getValue() == null ? "No hay tragamonedas en servicio." : "Escoge una máquina y gira.");
        refreshStake();
    }

    private void refreshStake() {
        MaquinaTragaMoneda machine = machines.getValue();
        stake.setText(machine == null ? "" : "Costo del giro: " + Formato.dinero(machine.getCostoPorGiro()));
        spin.setDisable(machine == null || spinning);
        machines.setDisable(spinning);
    }

    private void spin() {
        MaquinaTragaMoneda machine = machines.getValue();
        if (machine == null || spinning) return;
        view.attempt(() -> animate(machine.jugarGiro(view.customer())));
    }

    private void animate(MaquinaTragaMoneda.RondaMaquina result) {
        int[] end = result.getCarretes();
        double[] start = reels.clone();
        boolean[] stopped = new boolean[end.length];
        spinning = true;
        win = false;
        message.setText("Girando…");
        refreshStake();
        ServicioSonido.Reproduccion sound = Sonidos.bucle(Efecto.RODILLOS);
        new AnimationTimer() {
            private long initial;
            @Override public void handle(long now) {
                if (initial == 0) initial = now;
                for (int index = 0; index < end.length; index++) {
                    double progress = Math.min(1, (now - initial) / (1_050_000_000.0 + index * 410_000_000));
                    double destination = end[index] - (3 + index) * FxArt.SYMBOLS.length;
                    reels[index] = destination + (start[index] - destination) * Math.pow(1 - progress, 3);
                    if (progress == 1 && !stopped[index]) {
                        stopped[index] = true;
                        Sonidos.reproducir(Efecto.RODILLO_PARA);
                    }
                }
                cabinet.redraw();
                if (stopped[end.length - 1]) {
                    stop();
                    sound.detener();
                    for (int index = 0; index < end.length; index++) reels[index] = end[index];
                    spinning = false;
                    win = result.getPremio() > 0;
                    message.setText(win ? "¡Tres iguales! Ganaste " + Formato.dinero(result.getPremio()) : "Sin premio esta vez.");
                    if (win) Sonidos.reproducir(Efecto.GANAR);
                    refreshStake();
                    view.refresh();
                    cabinet.redraw();
                }
            }
        }.start();
    }

    private void paintCabinet(GraphicsContext g, double width, double height) {
        double cell = Math.max(12, Math.min((width - 125) / 3, (height - 130) / 3));
        double total = cell * 3 + 24;
        double left = (width - total) / 2;
        double top = (height - cell * 3) / 2 + 19;
        g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#8e2433")), new Stop(1, Color.web("#3d0a12"))));
        g.fillRoundRect(left - 48, top - 67, total + 96, cell * 3 + 101, 38, 38);
        g.setStroke(FxArt.GOLD);
        g.setLineWidth(4);
        g.strokeRoundRect(left - 48, top - 67, total + 96, cell * 3 + 101, 38, 38);
        FxArt.text(g, "★ BROADWAY 777 ★", width / 2, top - 32, Math.min(33, cell * .29), FxArt.CREAM);
        for (int reel = 0; reel < reels.length; reel++) {
            double x = left + reel * (cell + 12);
            g.save();
            g.beginPath();
            g.rect(x, top, cell, cell * 3);
            g.closePath();
            g.clip();
            g.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#c9c1ac")), new Stop(.5, Color.WHITE), new Stop(1, Color.web("#c9c1ac"))));
            g.fillRect(x, top, cell, cell * 3);
            int floor = (int) Math.floor(reels[reel]);
            for (int row = floor - 2; row <= floor + 2; row++) FxArt.symbol(g,
                    Math.floorMod(row, FxArt.SYMBOLS.length), x + cell / 2, top + (row - reels[reel] + 1.5) * cell, cell * .77);
            g.restore();
            g.setStroke(Color.web("#3a2a10"));
            g.setLineWidth(3);
            g.strokeRoundRect(x, top, cell, cell * 3, 14, 14);
        }
        g.setFill(FxArt.GOLD);
        g.fillOval(left - 27, top + cell * 1.5 - 7, 14, 14);
        g.fillOval(left + total + 13, top + cell * 1.5 - 7, 14, 14);
        if (win) {
            g.setStroke(FxArt.CREAM);
            g.setLineWidth(4);
            g.strokeRoundRect(left - 6, top + cell, total + 12, cell, 14, 14);
        }
    }

    @Override Node icon() {
        return new FxArt.Picture(128, 100, (g, width, height) -> {
            FxArt.symbol(g, 0, width * .25, height * .57, 44);
            FxArt.symbol(g, 5, width * .5, height * .38, 52);
            FxArt.symbol(g, 4, width * .77, height * .54, 45);
        });
    }

    @Override boolean leave() {
        return waitForRound(spinning, "Espera a que se detengan los carretes.");
    }
}
