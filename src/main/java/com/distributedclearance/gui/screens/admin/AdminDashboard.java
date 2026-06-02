package com.distributedclearance.gui.screens.admin;

import java.util.List;
import com.distributedclearance.database.dao.RequestDAO;
import com.distributedclearance.database.dao.UserDAO;
import com.distributedclearance.gui.screens.BaseScreen;
import com.distributedclearance.models.Admin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class AdminDashboard extends BaseScreen {
    private final Admin admin;
    private final UserDAO userDAO = new UserDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    private Label totalUsersLabel;
    private Label totalRequestsLabel;
    private Label totalApprovedLabel;
    private Label totalRejectedLabel;
    private Label totalPendingLabel;
    private TextArea detailsArea;

    public AdminDashboard(Admin admin) {
        this.admin = admin;
        initialize();
    }

    @Override
    protected void initialize() {
        // Step 1: Set up the top layout header bar with working logout text
        setupTopHeaderBar("Admin Control Center");

        // Step 2: Main Content Layout Container (Dark Blue Theme)
        VBox coreLayoutContainer = new VBox(20);
        coreLayoutContainer.setPadding(new Insets(20, 0, 0, 0));
        coreLayoutContainer.setAlignment(Pos.TOP_CENTER);
        coreLayoutContainer.setStyle("-fx-background-color: #0b1329;"); // Dark Blue window color

        // Subtitle text
        Label welcomeLabel = new Label("Welcome back, operations administrator " + admin.getFullName());
        welcomeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");

        // Step 3: Setup Metrics Labels with FORCED DARK TEXT COLOR (#1e293b)
        totalUsersLabel = new Label();
        totalRequestsLabel = new Label();
        totalApprovedLabel = new Label();
        totalRejectedLabel = new Label();
        totalPendingLabel = new Label();

        // This inline code forces the text to be a dark color so it is visible inside the white box panel
        String statStylePattern = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;";
        totalUsersLabel.setStyle(statStylePattern);
        totalRequestsLabel.setStyle(statStylePattern);
        totalApprovedLabel.setStyle(statStylePattern);
        totalRejectedLabel.setStyle(statStylePattern);
        totalPendingLabel.setStyle(statStylePattern);

        // White Statistics Box Container
        VBox statsBox = new VBox(8);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(15, 20, 15, 20));
        statsBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");
        statsBox.getChildren().addAll(
                totalUsersLabel,
                totalRequestsLabel,
                totalApprovedLabel,
                totalRejectedLabel,
                totalPendingLabel
        );

        // Step 4: System Action Buttons Layout
        Button viewUsersButton = new Button("View System Users");
        Button viewRequestsButton = new Button("View Active Requests");
        Button refreshButton = new Button("Sync Realtime Metrics");

        String buttonStylePattern = "-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;";
        viewUsersButton.setStyle(buttonStylePattern);
        viewRequestsButton.setStyle(buttonStylePattern);
        refreshButton.setStyle(buttonStylePattern);

        viewUsersButton.setPrefWidth(200);
        viewRequestsButton.setPrefWidth(200);
        refreshButton.setPrefWidth(200);

        HBox controlRowLayout = new HBox(15);
        controlRowLayout.setAlignment(Pos.CENTER);
        controlRowLayout.getChildren().addAll(viewUsersButton, viewRequestsButton, refreshButton);

        // Step 5: Data Output Feed Terminal Console Layout
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefHeight(280);
        detailsArea.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #ffffff; -fx-border-color: #334155; -fx-border-radius: 6px;");
        detailsArea.setPromptText("Select a data system command view loop above to query records console.");

        // Data Query Action Listeners
        viewUsersButton.setOnAction(event -> {
            List<String> users = userDAO.getAllUsersSummary();
            detailsArea.setText(buildSectionText("Active Accounts Directory", users));
        });

        viewRequestsButton.setOnAction(event -> {
            List<String> requests = requestDAO.getAllRequestsSummary();
            detailsArea.setText(buildSectionText("Clearance Processing Orders Submissions", requests));
        });

        refreshButton.setOnAction(event -> refreshStatistics());

        // Step 6: Assemble all items into the main center panel view context
        coreLayoutContainer.getChildren().addAll(
                welcomeLabel,
                statsBox,
                controlRowLayout,
                detailsArea
        );

        // Set the dark layout pane as the core center view
        setCenter(coreLayoutContainer);
        
        // Load the initial database data numbers onto the text fields
        refreshStatistics();
    }

    private void refreshStatistics() {
        totalUsersLabel.setText("Total Profile Accounts: " + userDAO.getTotalUsers());
        totalRequestsLabel.setText("Total Clearance Requests Filed: " + requestDAO.getTotalRequests());
        totalApprovedLabel.setText("Total Signed-Off Cleared Applications: " + requestDAO.getApprovedRequests());
        totalRejectedLabel.setText("Total Denied/Disapproved Records: " + requestDAO.getRejectedRequests());
        totalPendingLabel.setText("Total Inter-Dept Pending Processing: " + requestDAO.getPendingRequests());
    }

    private String buildSectionText(String title, List<String> items) {
        StringBuilder builder = new StringBuilder("=== ").append(title.toUpperCase()).append(" ===\n\n");
        if (items == null || items.isEmpty()) {
            builder.append("No records found inside database logs.");
            return builder.toString();
        }
        for (String item : items) {
            builder.append(item).append("\n");
        }
        return builder.toString();
    }

    public Scene createScene() {
        return new Scene(this, 1000, 700);
    }
}
