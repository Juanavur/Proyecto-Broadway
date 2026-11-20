package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import modelos.Administrador;
import modelos.Caja;
import modelos.Cajero;
import modelos.Casino;
import modelos.Cliente;
import modelos.Crupier;
import modelos.Juego;
import modelos.MaquinaTragaMoneda;
import modelos.Mesa;
import modelos.MovimientoCaja;
import modelos.TecnicoMaquina;
import modelos.Trabajador;
import modelos.TurnoCaja;
import servicios.Formato;

import java.util.List;
import java.util.function.Supplier;

final class FxStaff extends BorderPane implements FxPlayContext {
    private static final List<String> ROLES = List.of("Administrador", "Cajero", "Crupier", "Técnico de máquinas");
    private final Casino casino;
    private final Trabajador employee;
    private final VBox menu = FxUi.column(FxUi.label("MENÚ", "section-title"));
    private final Label status = FxUi.label("", "hint");
    private final Label balance = FxUi.label("", "hint");
    private final ComboBox<Cliente> customers = new ComboBox<>();
    private Cliente selectedCustomer;
    private FxCustomerGame currentGame;

    FxStaff(Casino casino, Trabajador employee, Runnable back) {
        this.casino = casino;
        this.employee = employee;
        setTop(header(back));
        status.setWrapText(true);
        status.setPadding(new Insets(7, 20, 7, 20));
        setBottom(status);
        menu.setPrefWidth(205);
        menu.setPadding(new Insets(23, 13, 18, 13));
        menu.getStyleClass().add("staff-menu");
        setLeft(menu);
        if (employee instanceof Administrador administrator) {
            link("Empleados", () -> employees(administrator));
            link("Mesas", () -> tables(administrator));
            link("Clientes", () -> clients(null));
            open(() -> employees(administrator));
        } else if (employee instanceof Cajero teller) {
            link("Clientes", () -> clients(teller));
            link("Caja", this::cashDesk);
            open(() -> clients(teller));
        } else if (employee instanceof Crupier) {
            link("Mesas de juego", () -> games(false));
            link("Sala de máquinas", () -> games(true));
            open(() -> games(false));
        } else if (employee instanceof TecnicoMaquina technician) {
            link("Máquinas", () -> machines(technician));
            open(() -> machines(technician));
        }
    }

    private Node header(Runnable back) {
        HBox heading = FxUi.row(FxUi.label("BROADWAY", "brand"), FxUi.spacer(),
                FxUi.column(new Label(employee.getNombre()), FxUi.label(employee.getCargo(), "section-title")),
                FxUi.button("Cerrar sesión", "danger", () -> {
                    if (leaveGame()) back.run();
                }));
        heading.getStyleClass().addAll("toolbar", "staff-toolbar");
        return heading;
    }

    private void link(String name, Supplier<Node> page) {
        Button button = FxUi.button(name, "", () -> open(page));
        button.setMaxWidth(Double.MAX_VALUE);
        menu.getChildren().add(button);
    }

    private boolean leaveGame() {
        if (currentGame != null && !currentGame.leave()) return false;
        currentGame = null;
        return true;
    }

    private void open(Supplier<Node> page) {
        if (!leaveGame()) return;
        status.setText("");
        setCenter(page.get());
    }

    private Button action(String title, String style, Runnable operation, Runnable reload) {
        return FxUi.button(title, style, () -> {
            status.setText("");
            if (attempt(operation)) reload.run();
        });
    }

    private Node employees(Administrador admin) {
        TableView<Trabajador> table = table();
        FxUi.column(table, "Id", Trabajador::getId);
        FxUi.column(table, "Nombre", Trabajador::getNombre);
        FxUi.column(table, "Documento", Trabajador::getDocumento);
        FxUi.column(table, "Cargo", Trabajador::getCargo);
        FxUi.column(table, "Usuario", Trabajador::getUsuario);
        Runnable reload = () -> table.getItems().setAll(casino.getEmpleados());
        reload.run();
        TextField document = FxUi.field("Documento");
        TextField name = FxUi.field("Nombre");
        DatePicker birth = FxUi.birthDateField();
        TextField user = FxUi.field("Usuario");
        PasswordField password = new PasswordField();
        password.getStyleClass().add("field");
        ComboBox<String> role = selector(ROLES);
        Button add = action("Registrar empleado", "primary", () -> {
            Trabajador worker = admin.definirCargoEmpleado(role.getValue(), document.getText(), name.getText(),
                    FxUi.birthDate(birth), user.getText(), password.getText());
            if (!admin.registrarEmpleados(worker)) throw new IllegalArgumentException("El usuario ya existe.");
            document.clear(); name.clear(); user.clear(); password.clear();
            notice("Empleado registrado.", false);
        }, reload);
        VBox form = FxUi.panel(FxUi.label("Nuevo empleado", "section-title"),
                FxUi.form(new String[]{"Documento", "Nombre", "Fecha de nacimiento", "Usuario", "Contraseña", "Cargo"},
                        document, name, birth, user, password, role), add);
        return split("Empleados", "Registra empleados y asigna el cargo que habilita sus funciones.", table, form);
    }

    private Node tables(Administrador admin) {
        TableView<Mesa> table = table();
        FxUi.column(table, "Código", Mesa::getCodigoMesa);
        FxUi.column(table, "Juego", row -> row.getJuegoAsignado().getNombreJuego());
        FxUi.column(table, "Mínima", row -> Formato.dinero(row.getApuestaMinima()));
        FxUi.column(table, "Máxima", row -> Formato.dinero(row.getApuestaMaxima()));
        FxUi.column(table, "Cupo", row -> Integer.toString(row.getJugadoresMaximos()));
        FxUi.column(table, "Crupier", row -> row.getCrupierAsignado().getNombre());
        FxUi.column(table, "Estado", row -> row.estaDisponible() ? "Abierta" : "Cerrada");
        FxUi.column(table, "Rondas", row -> Integer.toString(row.getRondasJugadas()));
        Runnable reload = () -> reload(table, casino.getMesas());
        reload.run();
        TextField code = FxUi.field("Código");
        ComboBox<Juego> game = selector(casino.getJuegos());
        TextField minimum = amount("10.000");
        TextField maximum = amount("500.000");
        Spinner<Integer> capacity = new Spinner<>(1, 7, 5);
        ComboBox<Crupier> dealer = selector(casino.getCrupieres());
        Button add = action("Crear mesa", "primary", () -> {
            Mesa row = admin.crearMesa(code.getText(), game.getValue(), Formato.parseAmount(minimum.getText()),
                    Formato.parseAmount(maximum.getText()), capacity.getValue(), dealer.getValue());
            if (!admin.registrarMesaDisponible(row)) throw new IllegalArgumentException("Ya existe una mesa con ese código.");
            row.abrir();
            code.clear();
        }, reload);
        VBox form = FxUi.panel(FxUi.label("Nueva mesa", "section-title"),
                FxUi.form(new String[]{"Código", "Juego", "Apuesta mínima", "Apuesta máxima", "Capacidad", "Crupier"},
                        code, game, minimum, maximum, capacity, dealer), add);
        HBox toggle = FxUi.row(action("Abrir mesa", "", () -> selected(table).abrir(), reload),
                action("Cerrar mesa", "danger", () -> selected(table).cerrar(), reload));
        return split("Mesas de juego", "Administra el catálogo y el estado de las mesas.", table, form, toggle);
    }

    private Node clients(Cajero teller) {
        TableView<Cliente> table = table();
        FxUi.column(table, "Documento", Cliente::getDocumento);
        FxUi.column(table, "Nombre", Cliente::getNombre);
        FxUi.column(table, "Edad", row -> Integer.toString(row.getEdad()));
        FxUi.column(table, "Fichas", row -> Formato.dinero(row.getSaldoFichas()));
        FxUi.column(table, "Total apostado", row -> Formato.dinero(row.getTotalApostado()));
        FxUi.column(table, "Nivel", row -> row.getNivel().toString());
        FxUi.column(table, "Pérdidas del periodo", row -> Formato.dinero(row.getPerdidasPeriodo()));
        Runnable reload = () -> reload(table, casino.getClientes());
        reload.run();
        if (teller == null) return split("Clientes", "Consulta los clientes, el saldo y el nivel.", table, null);
        TextField document = FxUi.field("Documento");
        TextField name = FxUi.field("Nombre");
        DatePicker birth = FxUi.birthDateField();
        VBox register = FxUi.panel(FxUi.label("Nuevo cliente", "section-title"),
                FxUi.form(new String[]{"Documento", "Nombre", "Fecha de nacimiento"}, document, name, birth),
                action("Registrar cliente", "primary", () -> {
                    if (!teller.registrarClientes(document.getText(), name.getText(), FxUi.birthDate(birth))) {
                        throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
                    }
                    document.clear(); name.clear(); birth.setValue(null);
                }, reload));
        TextField amount = amount("100.000");
        VBox chips = FxUi.panel(FxUi.label("Fichas y cashback", "section-title"), FxUi.form(new String[]{"Monto"}, amount),
                FxUi.row(action("Vender fichas", "primary", () -> teller.venderFichas(selected(table),
                        Formato.parseAmount(amount.getText())), reload),
                        action("Canjear por dinero", "", () -> teller.canjearFichas(selected(table),
                                Formato.parseAmount(amount.getText())), reload)),
                action("Entregar cashback", "", () -> {
                    double credit = selected(table).reclamarCashback();
                    notice("Fichas de cashback acreditadas: " + Formato.dinero(credit), false);
                }, reload));
        return split("Clientes", "Selecciona un cliente para gestionar sus fichas.", table, FxUi.column(register, chips));
    }

    private Node cashDesk() {
        Caja caja = casino.getCaja();
        TableView<MovimientoCaja> table = table();
        FxUi.column(table, "Fecha", movement -> movement.getFecha().toString());
        FxUi.column(table, "Tipo", MovimientoCaja::getTipo);
        FxUi.column(table, "Monto", movement -> Formato.dinero(movement.getMonto()));
        Label shift = FxUi.label("", "total");
        Label base = new Label();
        Label sales = new Label();
        Label redemptions = new Label();
        Label expected = new Label();
        TextField startingCash = amount("2.000.000");
        TextField closingCash = FxUi.field("Efectivo del turno contado (sin la base)");
        Button open = FxUi.button("Abrir turno", "primary", () -> {});
        Button close = FxUi.button("Cerrar turno", "danger", () -> {});
        Runnable reload = () -> {
            TurnoCaja active = caja.turnoActual();
            boolean closed = active == null;
            shift.setText(closed ? "Cerrada" : "Abierto · N.º " + active.getId());
            List<MovimientoCaja> movements = closed ? List.of() : active.getMovimientos();
            table.getItems().setAll(movements);
            base.setText(closed ? "—" : Formato.dinero(active.getSaldoInicial()));
            sales.setText(shiftTotal(movements, MovimientoCaja.VENTA));
            redemptions.setText(shiftTotal(movements, MovimientoCaja.CANJE));
            expected.setText(closed ? "—" : Formato.dinero(active.calcularSaldoEsperado()));
            open.setDisable(!closed);
            close.setDisable(closed);
        };
        open.setOnAction(event -> {
            if (attempt(() -> caja.abrirTurno(Formato.parseAmount(startingCash.getText())))) reload.run();
        });
        close.setOnAction(event -> {
            if (attempt(() -> {
                String result = caja.cerrarTurno(Formato.parseAmount(closingCash.getText()));
                notice(result, result.startsWith("DESCUADRE"));
                closingCash.clear();
            })) reload.run();
        });
        reload.run();
        VBox form = FxUi.column(FxUi.panel(shift, FxUi.form(new String[]{"Base inicial", "Ventas de fichas", "Canjes de fichas", "Saldo esperado"},
                        base, sales, redemptions, expected)),
                FxUi.panel(FxUi.label("Abrir y cerrar", "section-title"),
                        FxUi.form(new String[]{"Base para abrir", "Efectivo contado"}, startingCash, closingCash), FxUi.row(open, close)));
        return split("Caja", "Ventas y canjes registrados en el turno abierto.", table, form);
    }

    private static String shiftTotal(List<MovimientoCaja> movements, String type) {
        return Formato.dinero(movements.stream().filter(row -> row.getTipo().equals(type)).mapToDouble(MovimientoCaja::getMonto).sum());
    }

    private Node machines(TecnicoMaquina technician) {
        TableView<MaquinaTragaMoneda> table = table();
        FxUi.column(table, "Código", MaquinaTragaMoneda::getCodigoMaquina);
        FxUi.column(table, "Valor por giro", row -> Formato.dinero(row.getCostoPorGiro()));
        FxUi.column(table, "Estado", row -> row.estaDisponible() ? "En servicio" : "En mantenimiento");
        FxUi.column(table, "Jugadas", row -> Integer.toString(row.getJugadas().size()));
        FxUi.column(table, "Premios pagados", row -> Formato.dinero(row.getJugadas().stream().mapToDouble(MaquinaTragaMoneda.RondaMaquina::getPremio).sum()));
        Runnable reload = () -> reload(table, casino.getMaquinas());
        reload.run();
        TextField code = FxUi.field("Código");
        TextField cost = amount("2.000");
        VBox form = FxUi.panel(FxUi.label("Nueva máquina", "section-title"), FxUi.form(new String[]{"Código", "Valor por giro"}, code, cost),
                action("Registrar máquina", "primary", () -> {
                    technician.registrarMaquinasTragaMonedas(code.getText(), Formato.parseAmount(cost.getText()));
                    code.clear();
                }, reload));
        Button toggle = action("Poner en mantenimiento / volver a servicio", "", () -> {
            MaquinaTragaMoneda machine = selected(table);
            if (machine.estaDisponible()) technician.ponerEnMantenimiento(machine); else technician.returnToService(machine);
        }, reload);
        return split("Máquinas tragamonedas", "Solo las máquinas en servicio permiten jugar.", table, form, toggle);
    }

    private Node games(boolean slots) {
        customers.setOnAction(null);
        customers.getItems().setAll(casino.getClientes());
        customers.getSelectionModel().selectFirst();
        selectedCustomer = customers.getValue();
        refresh();
        BorderPane games = new BorderPane();
        HBox heading = FxUi.row(new Label("Cliente"), customers, balance, FxUi.spacer());
        heading.getStyleClass().add("heading");
        games.setTop(heading);
        customers.setOnAction(event -> {
            if (selectedCustomer == customers.getValue()) return;
            Cliente previous = selectedCustomer;
            FxCustomerGame game = currentGame;
            if (game != null && !game.leave()) {
                customers.setValue(previous);
                return;
            }
            selectedCustomer = customers.getValue();
            if (game != null) game.enter();
            refresh();
        });
        if (slots) {
            displayGame(games, new FxSlots(this));
        } else {
            FxRoulette roulette = new FxRoulette(this);
            FxBlackjack blackjack = new FxBlackjack(this);
            heading.getChildren().addAll(FxUi.button("Ruleta", "", () -> displayGame(games, roulette)),
                    FxUi.button("Blackjack", "", () -> displayGame(games, blackjack)));
            displayGame(games, roulette);
        }
        return games;
    }

    private void displayGame(BorderPane parent, FxCustomerGame game) {
        if (!leaveGame()) return;
        currentGame = game;
        game.enter();
        parent.setCenter(game);
    }

    @Override public Casino casino() { return casino; }
    @Override public Cliente customer() {
        if (selectedCustomer == null) throw new IllegalArgumentException("Selecciona un cliente registrado.");
        return selectedCustomer;
    }
    @Override public boolean acceptsTable(Mesa table) { return table.getCrupierAsignado() == employee; }
    @Override public boolean dealerMode() { return true; }
    @Override public void refresh() {
        balance.setText(selectedCustomer == null ? "" : "Fichas: " + Formato.dinero(selectedCustomer.getSaldoFichas()));
    }
    @Override public void notice(String text, boolean error) {
        status.setText(text);
        status.setStyle(error ? "-fx-text-fill:#ff7a7a" : "-fx-text-fill:#4cd08a");
    }

    private static TextField amount(String initial) {
        TextField amount = FxUi.field(initial);
        amount.setText(initial);
        return amount;
    }

    private static <T> ComboBox<T> selector(List<T> items) {
        ComboBox<T> selector = new ComboBox<>();
        selector.getItems().setAll(items);
        selector.getSelectionModel().selectFirst();
        return selector;
    }

    private static <T> TableView<T> table() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private static <T> T selected(TableView<T> table) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) throw new IllegalArgumentException("Selecciona un registro de la tabla.");
        return selected;
    }

    private static <T> void reload(TableView<T> table, List<T> items) {
        T previous = table.getSelectionModel().getSelectedItem();
        table.getItems().setAll(items);
        if (items.contains(previous)) table.getSelectionModel().select(previous);
    }

    private static Node split(String title, String description, TableView<?> table, Node form, Node... actions) {
        VBox left = FxUi.column(table);
        left.getChildren().addAll(actions);
        FxUi.fill(left, table);
        HBox content = FxUi.row(left);
        HBox.setHgrow(left, Priority.ALWAYS);
        content.setAlignment(Pos.TOP_LEFT);
        content.setSpacing(14);
        if (form != null) {
            if (form instanceof VBox box) box.setPrefWidth(330);
            content.getChildren().add(form);
        }
        VBox page = FxUi.page(FxUi.label(title, "title"), FxUi.label(description, "hint"), content);
        VBox.setVgrow(content, Priority.ALWAYS);
        return page;
    }
}
