package com.distributedclearance.gui.screens.officer;

import java.util.List;
import com.distributedclearance.database.dao.ApprovalDAO;
import com.distributedclearance.gui.screens.BaseScreen;
import com.distributedclearance.models.OfficerRequestRecord;
import com.distributedclearance.models.DeptOfficer;
import com.distributedclearance.models.enums.ApprovalStatus;
import com.distributedclearance.server.networking.NotificationSender;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextArea;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OfficerDashboard extends BaseScreen {

    private final DeptOfficer officer;
    private final ApprovalDAO approvalDAO = new ApprovalDAO();

    private TableView<OfficerRequestRecord> requestTable;
    private Label statusLabel;
    private TextArea commentArea;
    private Timeline refreshTimeline;

    public OfficerDashboard(DeptOfficer officer) {
        this.officer = officer;
        initialize();
    }

    @Override
    protected void initialize() {
        // Step 1: Render the standardized top banner with working logout functionality
        setupTopHeaderBar(officer.getDepartment() + " Officer Dashboard");

        VBox container = new VBox(15);
        container.setPadding(new Insets(20, 0, 0, 0));
        container.setAlignment(Pos.TOP_CENTER);

        statusLabel = new Label("Select a pending request and choose a decision.");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");

        // Step 2: Build TableView layout
        requestTable = new TableView<>();
        requestTable.setPrefHeight(320);
        requestTable.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #ffffff; -fx-border-color: #334155;");
        requestTable.setPlaceholder(new Label("No pending requests for your department."));

        // Step 3: Define explicit graphic header labels to force dark text compatibility override loops
        TableColumn<OfficerRequestRecord, String> studentNameColumn = new TableColumn<>();
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentNameColumn.setPrefWidth(250);
        Label lblName = new Label("Student Name");
        lblName.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        studentNameColumn.setGraphic(lblName);

        TableColumn<OfficerRequestRecord, Integer> requestIdColumn = new TableColumn<>();
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        requestIdColumn.setPrefWidth(120);
        Label lblId = new Label("Request ID");
        lblId.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        requestIdColumn.setGraphic(lblId);

        TableColumn<OfficerRequestRecord, String> currentStatusColumn = new TableColumn<>();
        currentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        currentStatusColumn.setPrefWidth(150);
        Label lblStatus = new Label("Current Status");
        lblStatus.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 13px;");
        currentStatusColumn.setGraphic(lblStatus);

        requestTable.getColumns().addAll(studentNameColumn, requestIdColumn, currentStatusColumn);

        // Step 4: Text input panel setup
        commentArea = new TextArea();
        commentArea.setPromptText("Enter review comments or reasons for rejection here...");
        commentArea.setPrefHeight(100);
        commentArea.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: #ffffff; -fx-border-color: #334155; -fx-border-radius: 4px;");

        // Step 5: Action buttons container layout setup
        Button approveButton = new Button("Approve Request");
        Button rejectButton = new Button("Reject Request");

        approveButton.setPrefWidth(160);
        approveButton.setPrefHeight(40);
        rejectButton.setPrefWidth(160);
        rejectButton.setPrefHeight(40);

        approveButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;");
        rejectButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4px; -fx-cursor: hand;");

        approveButton.setOnAction(event -> handleDecision(ApprovalStatus.APPROVED));
        rejectButton.setOnAction(event -> handleDecision(ApprovalStatus.REJECTED));

        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(approveButton, rejectButton);

        // Step 6: Initial execution data fetch
        loadPendingRequests(true);

        // Auto table refresh heartbeat synchronization loop runs every 3 seconds
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> loadPendingRequests(true))
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        container.getChildren().addAll(statusLabel, requestTable, commentArea, buttonContainer);
        setCenter(container);
    }

    private void loadPendingRequests(boolean preserveSelection) {
        Integer selectedApprovalId = null;

        if (preserveSelection) {
            OfficerRequestRecord selectedRequest = requestTable.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                selectedApprovalId = selectedRequest.getApprovalId();
            }
        }

        List<OfficerRequestRecord> requests = approvalDAO.getPendingRequestsForDepartment(officer.getDepartment());
        requestTable.getItems().setAll(requests);

        if (preserveSelection && selectedApprovalId != null) {
            for (int index = 0; index < requests.size(); index++) {
                if (requests.get(index).getApprovalId() == selectedApprovalId) {
                    requestTable.getSelectionModel().select(index);
                    requestTable.scrollTo(index);
                    return;
                }
            }
        }
        requestTable.getSelectionModel().clearSelection();
    }

    private void handleDecision(ApprovalStatus status) {
        OfficerRequestRecord selectedRequest = requestTable.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            statusLabel.setText("Warning: Please click on a student request row from the table first.");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f59e0b;");
            return;
        }

        // Commits the review note left inside commentArea directly to the database
        approvalDAO.updateApprovalStatus(selectedRequest.getApprovalId(), status, commentArea.getText().trim());
        approvalDAO.processFinalRequestStatus(selectedRequest.getRequestId());

        String actionWord = (status == ApprovalStatus.APPROVED) ? "approved" : "rejected";

        // Dispatch notification update payload parameters across socket pipelines
        NotificationSender.send(officer.getDepartment().name() + " officer " + officer.getFullName() + " " + actionWord + " request " + selectedRequest.getRequestId());

        statusLabel.setText("Success: Request #" + selectedRequest.getRequestId() + " has been " + actionWord + ".");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #22c55e;");

        commentArea.clear();
        loadPendingRequests(true);
    }

    public Scene createScene() {
        return new Scene(this, 1000, 700);
    }
}
