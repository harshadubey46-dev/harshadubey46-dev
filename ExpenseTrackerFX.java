package com.yourname.expensetracker;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.*;
//import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.*;

public class ExpenseTrackerFX extends Application {

    ObservableList<Expense> list = FXCollections.observableArrayList();
    PieChart pie = new PieChart();
    BarChart<String, Number> bar;
    Scene scene;
    boolean darkMode = false;

    Label toast = new Label();

    @Override
    public void start(Stage stage) {

        list.addAll(DBManager.loadExpenses());

        // Charts
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        bar = new BarChart<>(x, y);
        bar.setTitle("Spending Overview");

        pie.setTitle("Category Distribution");

        // Inputs
        TextField amount = new TextField();
        amount.setPromptText("Amount");

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Food","Travel","Shopping","Bills","Other");
        category.setPromptText("Category");

        DatePicker date = new DatePicker(LocalDate.now());

        Button add = new Button("Add Expense");
        Button export = new Button("Export CSV");

        // Toast style
        toast.setStyle("-fx-background-color:#333; -fx-text-fill:white; -fx-padding:8; -fx-background-radius:8;");
        toast.setVisible(false);

        add.setOnAction(e -> {
            try {
                if (category.getValue() == null || amount.getText().isEmpty()) {
                    showToast("Fill all fields!");
                    return;
                }

                double amt = Double.parseDouble(amount.getText());

                if (amt <= 0) {
                    showToast("Amount must be positive!");
                    return;
                }

                Expense ex = new Expense(category.getValue(), amt, date.getValue());

                DBManager.addExpense(ex);
                list.add(ex);
                update();

                showToast("Expense added ✔");
                amount.clear();

            } catch (Exception ex) {
                showToast("Invalid input ❌");
            }
        });

        export.setOnAction(e -> {
            Utils.exportCSV(list);
            showToast("Exported CSV ✔");
        });

        // TABLE
        TableView<Expense> table = new TableView<>(list);

        TableColumn<Expense, String> col1 = new TableColumn<>("Category");
        col1.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, Double> col2 = new TableColumn<>("Amount");
        col2.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Expense, LocalDate> col3 = new TableColumn<>("Date");
        col3.setCellValueFactory(new PropertyValueFactory<>("date"));

        // DELETE BUTTON COLUMN
        TableColumn<Expense, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<Expense, Void>() {

            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    Expense exp = getTableView().getItems().get(getIndex());
                    DBManager.deleteExpense(exp.getId());
                    list.remove(exp);
                    update();
                    showToast("Deleted ✔");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        table.getColumns().addAll(Arrays.asList(col1, col2, col3, actionCol));

        // FILTER
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll("All","Food","Travel","Shopping","Bills","Other");
        filter.setValue("All");

        filter.setOnAction(e -> {
            String selected = filter.getValue();
            if (selected.equals("All")) table.setItems(list);
            else table.setItems(list.filtered(exp -> exp.getCategory().equals(selected)));
        });

        // SIDEBAR
        Button dashboardBtn = new Button("Dashboard");
        Button reportsBtn = new Button("Reports");
        Button settingsBtn = new Button("Settings");
        Button darkBtn = new Button("🌙 Dark Mode");

        // DASHBOARD
        VBox form = new VBox(10, category, amount, date, add, export);
        form.getStyleClass().add("card");

        VBox charts = new VBox(15, pie, bar);
        charts.getStyleClass().add("card");

        VBox dashboard = new VBox(20, form, charts);

        // REPORTS + AI
        Label totalLabel = new Label();
        Label monthlyLabel = new Label();
        Label warningLabel = new Label();

        TextArea aiBox = new TextArea();
        aiBox.setEditable(false);

        VBox reports = new VBox(15,
                totalLabel,
                monthlyLabel,
                warningLabel,
                aiBox,
                filter,
                table
        );
        reports.getStyleClass().add("card");

        // SETTINGS
        Button clear = new Button("Clear Data");
        clear.setOnAction(e -> {
            list.clear();
            update();
            showToast("All data cleared!");
        });

        VBox settings = new VBox(20, clear);
        settings.getStyleClass().add("card");

        // CONTENT SWITCH
        StackPane content = new StackPane(dashboard);

        dashboardBtn.setOnAction(e -> content.getChildren().setAll(dashboard));

        reportsBtn.setOnAction(e -> {
            updateTotal(totalLabel);

            double monthly = AIAnalyzer.getMonthlyTotal(list);
            monthlyLabel.setText("This Month: ₹ " + monthly);

            aiBox.setText(AIAnalyzer.generateInsights(list));

            // Budget warning
            if (monthly > 5000) {
                warningLabel.setText("⚠ High spending this month!");
            } else {
                warningLabel.setText("✔ Spending normal");
            }

            content.getChildren().setAll(reports);
        });

        settingsBtn.setOnAction(e -> content.getChildren().setAll(settings));

        // DARK MODE
        darkBtn.setOnAction(e -> {
            darkMode = !darkMode;

            if (darkMode) {
                scene.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());
            } else {
                scene.getStylesheets().remove(getClass().getResource("/dark.css").toExternalForm());
            }
        });

        Button logoutBtn = new Button("Logout");

        logoutBtn.setOnAction(e -> {
            DBManager.currentUserId = -1;

            try {
                new LoginFX().start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(20,
                new Label("💎 Expenza"),
                dashboardBtn,
                reportsBtn,
                settingsBtn,
                darkBtn,
                spacer,
                logoutBtn
        );
        sidebar.getStyleClass().add("sidebar");

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(content);

        StackPane main = new StackPane(root, toast);

        scene = new Scene(main, 1100, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        update();

        stage.setScene(scene);
        stage.setTitle("Expenza Final");
        stage.show();
    }

    void update() {
        pie.getData().clear();
        bar.getData().clear();

        Map<String, Double> map = new HashMap<>();

        for (Expense e : list) {
            map.put(e.getCategory(),
                    map.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (String k : map.keySet()) {
            pie.getData().add(new PieChart.Data(k, map.get(k)));
            series.getData().add(new XYChart.Data<>(k, map.get(k)));
        }

        bar.getData().add(series);
    }

    void updateTotal(Label label) {
        double total = list.stream().mapToDouble(Expense::getAmount).sum();
        label.setText("Total Spending: ₹ " + total);
    }

    void showToast(String msg) {
        toast.setText(msg);
        toast.setVisible(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> toast.setVisible(false));
        pt.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}