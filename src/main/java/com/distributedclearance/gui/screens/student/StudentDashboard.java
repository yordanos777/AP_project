package com.distributedclearance.gui.screens.student;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.distributedclearance.database.dao.ApprovalDAO;
import com.distributedclearance.database.dao.RequestDAO;
import com.distributedclearance.gui.screens.BaseScreen;
import com.distributedclearance.models.Approval;
import com.distributedclearance.models.ClearanceRequest;
import com.distributedclearance.models.Student;
import com.distributedclearance.models.enums.ApprovalStatus;
import com.distributedclearance.models.enums.RequestStatus;
import com.distributedclearance.server.networking.NotificationClient;
import com.distributedclearance.server.networking.NotificationListener;
import com.distributedclearance.server.networking.SocketClient;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class StudentDashboard extends BaseScreen implements NotificationListener {

    private final Student student;
    private final RequestDAO requestDAO = new RequestDAO();
    private final ApprovalDAO approvalDAO = new ApprovalDAO();

    private Label overallStatusLabel;
    private Label statusLabel;
    private ListView<String> notificationHistoryList;
    private TableView<ClearanceRequest> requestHistoryTable;
    private ListView<String> approvalList;

    private int currentRequestId = -1;

    public StudentDashboard(Student student) {
        this.student = student;
        initialize();
    }

    @Override
    protected void initialize() {
        // Step 1: Inject the universal top bar header
        setupTopHeaderBar("Student Clearance Portal");

        VBox container = new VBox(15);
        container.setPadding(new Insets(15, 0, 0, 0));
        container.setAlignment(Pos.TOP_CENTER);

        Label welcomeLabel = new Label("Logged in as: " + student.getFullName() + " (" + student.getStudentId() + ")");
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");

        overallStatusLabel = new Label("Overall Clearance Status: PENDING");
        overallStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        // Split tracking layouts row
        HBox historySplitBox = new HBox(20);
        historySplitBox.setAlignment(Pos.CENTER);

        VBox notificationBox = new VBox(8);
        notificationBox.setPrefWidth(470);
        Label notifTitle = new Label("Live Alerts Stream Log");
        notifTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        notificationHistoryList = new ListView<>();
        notificationHistoryList.setPrefHeight(150);
        notificationHistoryList.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #22c55e;");
        notificationHistoryList.setPlaceholder(new Label("No system alerts broadcasted yet."));
        notificationBox.getChildren().addAll(notifTitle, notificationHistoryList);

        VBox approvalDeptBox = new VBox(8);
        approvalDeptBox.setPrefWidth(470);
        Label deptTitle = new Label("Inter-Department Sign-off Breakdowns");
        deptTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        approvalList = new ListView<>();
        approvalList.setPrefHeight(150);
        approvalList.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #ffffff;");
        approvalDeptBox.getChildren().addAll(deptTitle, approvalList);

        historySplitBox.getChildren().addAll(notificationBox, approvalDeptBox);

        // Core Action Submission Button Configuration
        Button submitButton = new Button("File New Clearance Request");
        submitButton.setPrefWidth(300);
        submitButton.setPrefHeight(40);
        submitButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");

        // Processing History Table Configuration
        VBox tableWrapper = new VBox(8);
        Label tableTitle = new Label("Your Clearance Applications History Logs");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        requestHistoryTable = new TableView<>();
        requestHistoryTable.setPrefHeight(200);
        requestHistoryTable.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #ffffff; -fx-border-color: #334155;");
        requestHistoryTable.setPlaceholder(new Label("No clearance processing requests on record."));

        TableColumn<ClearanceRequest, Integer> requestIdColumn = new TableColumn<>();
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        requestIdColumn.setPrefWidth(150);
        // FIX: Force Header Label Color
        Label lblId = new Label("Request ID");
        lblId.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        requestIdColumn.setGraphic(lblId);

        TableColumn<ClearanceRequest, String> submittedAtColumn = new TableColumn<>();
        submittedAtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatSubmittedAt(cellData.getValue().getSubmittedAt())));
        submittedAtColumn.setPrefWidth(250);
        // FIX: Force Header Label Color
        Label lblTime = new Label("Filing Timestamp");
        lblTime.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        submittedAtColumn.setGraphic(lblTime);

        TableColumn<ClearanceRequest, String> overallRequestStatusColumn = new TableColumn<>();
        overallRequestStatusColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getStatus() != null) {
                return new SimpleStringProperty(cellData.getValue().getStatus().name());
            }
            return new SimpleStringProperty("PENDING");
        });
        overallRequestStatusColumn.setPrefWidth(200);
        // FIX: Force Header Label Color
        Label lblStatus = new Label("Processing Status");
        lblStatus.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        overallRequestStatusColumn.setGraphic(lblStatus);

        requestHistoryTable.getColumns().addAll(requestIdColumn, submittedAtColumn, overallRequestStatusColumn);
        tableWrapper.getChildren().addAll(tableTitle, requestHistoryTable);

        // Submission Event Handler Wireframe
        submitButton.setOnAction(event -> {
            boolean success = requestDAO.submitRequest(student);
            if (success) {
                currentRequestId = requestDAO.getLatestRequestId(student.getId());
                SocketClient.sendMessage(currentRequestId + ":" + student.getFullName());
                statusLabel.setText("Clearance processing order filed successfully.");
                statusLabel.setStyle("-fx-text-fill: #22c55e;");
                loadApprovals();
                loadRequestHistory();
            } else {
                statusLabel.setText("Error: Database system rejected application filing command.");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
            }
        });

        // Assemble nodes into container
        container.getChildren().addAll(welcomeLabel, overallStatusLabel, historySplitBox, submitButton, statusLabel, tableWrapper);
        setCenter(container);

        // Turn on underlying multi-threaded network pipeline listener daemon
        NotificationClient client = new NotificationClient(this);
        Thread notificationThread = new Thread(client);
        notificationThread.setDaemon(true);
        notificationThread.start();

        // Run initial interface calculations data mapping parameters load loop
        loadRequestHistory();
        currentRequestId = requestDAO.getLatestRequestId(student.getId());
        if (currentRequestId != -1) {
            loadApprovals();
        }
    }

    private void loadRequestHistory() {
        List<ClearanceRequest> requests = requestDAO.getRequestsByStudentId(student.getId());
        ObservableList<ClearanceRequest> historyItems = FXCollections.observableArrayList(requests);
        requestHistoryTable.setItems(historyItems);
    }

    private void loadApprovals() {
        if (currentRequestId == -1) return;
        approvalList.getItems().clear();
        List<Approval> approvals = approvalDAO.getApprovalsByRequestId(currentRequestId);

        for (Approval approval : approvals) {
            approvalList.getItems().add(" • " + approval.getDepartment().name() + " Department Status: [" + approval.getStatus() + "]");
        }
        updateOverallStatus(approvals);
    }

        private void updateOverallStatus(List<Approval> approvals) {
        String overallStatus = "PENDING";
        overallStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        if (approvals != null && !approvals.isEmpty()) {
            boolean allApproved = true;
            for (Approval approval : approvals) {
                if (approval.getStatus() == ApprovalStatus.REJECTED) {
                    overallStatus = "REJECTED";
                    overallStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                    allApproved = false;
                    break; // Exit loop immediately if even one department rejects
                }
                if (approval.getStatus() != ApprovalStatus.APPROVED) {
                    allApproved = false;
                }
            }
            if (allApproved) {
                overallStatus = "CLEARED";
                overallStatusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");
            }
        }
        overallStatusLabel.setText("Overall Clearance Status: " + overallStatus);
    }
    private String formatSubmittedAt(LocalDateTime submittedAt) {
        if (submittedAt == null) return "N/A";
        return submittedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void onNotificationReceived(String message) {
        // Enforce execution safely inside the main JavaFX rendering timeline loop
        Platform.runLater(() -> {
            notificationHistoryList.getItems().add(0, "[Alert] " + message);
            loadRequestHistory();
            loadApprovals();
        });
    }

    public Scene createScene() {
        return new Scene(this, 1000, 700);
    }
}

