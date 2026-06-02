package com.distributedclearance.gui.screens;

import com.distributedclearance.gui.navigation.SceneManager;
import com.distributedclearance.gui.screens.auth.LoginScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public abstract class BaseScreen extends BorderPane {

    public BaseScreen() {
        // Sets a clean margin around the window and applies the global dark blue background color
        setPadding(new Insets(10, 20, 20, 20));
        setStyle("-fx-background-color: #0b1329;");
    }

    /**
     * Builds a universal top navigation bar for all dashboards.
     * Contains the page title on the left and a functional red logout button on the right.
     *
     * @param systemViewTitle The text title to show on the left side of the screen header.
     */
    protected void setupTopHeaderBar(String systemViewTitle) {
        HBox headerContainer = new HBox();
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        headerContainer.setPadding(new Insets(10, 0, 15, 0));
        // Adds a subtle gray dividing line under the top bar
        headerContainer.setStyle("-fx-border-color: #1e293b; -fx-border-width: 0 0 1 0;");

        // Page title header configuration
        Label titleLabel = new Label(systemViewTitle);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        // Transparent structural spacer that pushes the logout button completely to the right edge
        Region structuralSpacer = new Region();
        HBox.setHgrow(structuralSpacer, Priority.ALWAYS);

        // Professional Red Logout Action Button
        Button logoutActionBtn = new Button("Logout");
        logoutActionBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;"
        );
        
        // Hover effect styling via listener bindings
        logoutActionBtn.setOnMouseEntered(event -> logoutActionBtn.setStyle(
                "-fx-background-color: #dc2626;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;"
        ));
        logoutActionBtn.setOnMouseExited(event -> logoutActionBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 8px 16px;" +
                "-fx-cursor: hand;"
        ));

        // Destruction sequence that resets screen context back to user authentication center
        logoutActionBtn.setOnAction(event -> {
            System.out.println("[Session Action] Terminating workspace context. Redirecting back to authentication portal.");
            LoginScreen loginScreen = new LoginScreen();
            SceneManager.switchScene(loginScreen.createScene());
        });

        // Add elements to the horizontal strip layout and assign it to the top slot of the BorderPane
        headerContainer.getChildren().addAll(titleLabel, structuralSpacer, logoutActionBtn);
        setTop(headerContainer);
    }

    protected abstract void initialize();
}
