package ui.fx;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelos.Casino;
import modelos.Trabajador;
import servicios.DatosIniciales;
import servicios.sonido.Sonidos;

import java.util.Objects;

public final class BroadwayApp extends Application {
    private static final double MIN_WIDTH = 960;
    private static final double MIN_HEIGHT = 700;
    private final StackPane root = new StackPane();
    private Casino casino;
    private Image logo;

    @Override
    public void start(Stage stage) {
        casino = DatosIniciales.crear();
        logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/files/logo.png")));
        Sonidos.iniciar();
        root.getStyleClass().add("casino");
        root.setManaged(false);
        Scale zoom = new Scale(1, 1, 0, 0);
        root.getTransforms().add(zoom);
        Pane viewport = new Pane(root) {
            @Override
            protected void layoutChildren() {
                double width = Math.max(MIN_WIDTH, getWidth());
                double height = Math.max(MIN_HEIGHT, getHeight());
                double fit = Math.min(getWidth() / width, getHeight() / height);
                zoom.setX(fit);
                zoom.setY(fit);
                root.resizeRelocate((getWidth() - width * fit) / 2, (getHeight() - height * fit) / 2, width, height);
            }
        };
        viewport.getStyleClass().add("casino");
        Scene scene = new Scene(viewport, 1360, 840);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/files/casino.css")).toExternalForm());
        stage.setTitle(casino.getNombre() + " · Sistema de gestión");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        cover(scene);
        stage.show();
    }

    private void cover(Scene scene) {
        ImageView mark = new ImageView(logo);
        mark.setPreserveRatio(true);
        mark.fitWidthProperty().bind(root.widthProperty().multiply(.64));
        Label prompt = FxUi.label("PRESIONA ESPACIO PARA EMPEZAR", "title");
        VBox content = FxUi.column(mark, prompt, FxUi.label("o haz clic en cualquier parte", "hint"));
        content.setAlignment(Pos.CENTER);
        StackPane cover = new StackPane(content);
        cover.getStyleClass().add("felt");
        FadeTransition pulse = new FadeTransition(Duration.seconds(1.8), prompt);
        pulse.setFromValue(.63);
        pulse.setToValue(1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.play();
        Runnable next = () -> {
            pulse.stop();
            scene.setOnKeyPressed(null);
            login();
        };
        cover.setOnMouseClicked(event -> next.run());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) next.run();
        });
        root.getChildren().setAll(cover);
    }

    private void login() {
        TextField user = FxUi.field("Usuario");
        user.setId("employee-user");
        PasswordField password = new PasswordField();
        password.getStyleClass().add("field");
        password.setPromptText("Contraseña");
        Label error = FxUi.label("", "hint");
        error.setStyle("-fx-text-fill:#ff7a7a");
        Button enter = FxUi.button("Entrar", "primary", () -> {
            Trabajador employee = casino.iniciarSesion(user.getText().trim(), password.getText());
            if (employee == null) {
                error.setText("Usuario o contraseña incorrectos.");
                return;
            }
            root.getChildren().setAll(new FxStaff(casino, employee, this::login));
        });
        enter.setDefaultButton(true);
        Button customer = FxUi.button("Entrar como cliente", "", () -> root.getChildren().setAll(new FxCustomer(casino, this::login)));
        ImageView mark = new ImageView(logo);
        mark.setPreserveRatio(true);
        mark.setFitWidth(390);
        GridPane profiles = new GridPane();
        profiles.setHgap(8);
        profiles.setVgap(6);
        String[][] accounts = {{"Administrador", "admin"}, {"Cajero", "cajero"}, {"Crupier", "crupier"}, {"Técnico", "tecnico"}};
        for (int index = 0; index < accounts.length; index++) {
            String[] account = accounts[index];
            profiles.add(FxUi.button(account[0] + " · " + account[1], "", () -> {
                user.setText(account[1]);
                password.setText(account[1] + "123");
                password.requestFocus();
            }), index % 2, index / 2);
        }
        profiles.setAlignment(Pos.CENTER);
        HBox actions = FxUi.row(enter, customer);
        actions.setAlignment(Pos.CENTER);
        VBox card = FxUi.panel(mark, FxUi.label("Ingreso de empleados", "title"),
                FxUi.form(new String[]{"Usuario", "Contraseña"}, user, password), error,
                actions, FxUi.label("Cuentas de prueba (clic para llenar)", "hint"), profiles);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(510, VBox.USE_PREF_SIZE);
        card.setPadding(new Insets(25, 34, 25, 34));
        StackPane page = new StackPane(card);
        page.getStyleClass().add("felt");
        root.getChildren().setAll(page);
    }

    @Override
    public void stop() {
        Sonidos.setSilencio(true);
    }
}
