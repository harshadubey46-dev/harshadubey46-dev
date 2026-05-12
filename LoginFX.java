package com.yourname.expensetracker;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginFX extends Application {

    @Override
    public void start(Stage stage) {

        // Title
        Label title = new Label("💎 Expenza");
        title.getStyleClass().add("title");

        // Inputs
        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        // Buttons
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        Label msg = new Label();

        loginBtn.setOnAction(e -> {
            if (DBManager.login(username.getText(), password.getText())) {
                try {
                    new ExpenseTrackerFX().start(new Stage());
                    stage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                msg.setText("Invalid login ❌");
            }
        });

        registerBtn.setOnAction(e -> {
            if (DBManager.registerUser(username.getText(), password.getText())) {
                msg.setText("Registered ✔ Now login");
            } else {
                msg.setText("User exists ❌");
            }
        });

        // FORM CARD
        VBox form = new VBox(15, title, username, password, loginBtn, registerBtn, msg);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.getStyleClass().add("login-card");

        // CENTER SCREEN
        StackPane root = new StackPane(form);
        root.getStyleClass().add("login-bg");
        root.setMaxWidth(350);
        form.setMaxWidth(300);

        loginBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        Scene scene = new Scene(root, 350, 600);
        scene.getStylesheets().add(getClass().getResource("/login.css").toExternalForm());

        stage.setTitle("Login - Expenza");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}