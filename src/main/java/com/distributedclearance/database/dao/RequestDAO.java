package com.distributedclearance.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.distributedclearance.database.DatabaseManager;
import com.distributedclearance.models.ClearanceRequest;
import com.distributedclearance.models.Student;
import com.distributedclearance.models.enums.RequestStatus;

public class RequestDAO {
    private final Connection connection;

    public RequestDAO() {
        connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean submitRequest(Student student) {
        String sql = "INSERT INTO clearance_requests (student_id, submitted_at, status) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, student.getId());
            pstmt.setObject(2, LocalDateTime.now());
            pstmt.setString(3, "PENDING");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int requestId = generatedKeys.getInt(1);
                        ApprovalDAO approvalDAO = new ApprovalDAO();
                        approvalDAO.createInitialApprovals(requestId);
                    }
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getLatestRequestId(int studentId) {
        String sql = "SELECT request_id FROM clearance_requests WHERE student_id = ? ORDER BY request_id DESC LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("request_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getTotalRequests() {
        String sql = "SELECT COUNT(*) AS total_requests FROM clearance_requests";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_requests");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getApprovedRequests() {
        return getRequestCountByStatuses("APPROVED", "CLEARED", "FULLY_APPROVED");
    }

    public int getRejectedRequests() {
        return getRequestCountByStatuses("REJECTED");
    }

    public int getPendingRequests() {
        return getRequestCountByStatuses("PENDING");
    }

    public List<String> getAllRequestsSummary() {
        List<String> requests = new ArrayList<>();
        String sql = "SELECT r.request_id, u.full_name, r.submitted_at, r.status " +
                     "FROM clearance_requests r " +
                     "JOIN students s ON r.student_id = s.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "ORDER BY r.request_id DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                requests.add(
                        "#" + rs.getInt("request_id") +
                        " | " + rs.getString("full_name") +
                        " | " + rs.getTimestamp("submitted_at") +
                        " | " + rs.getString("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private int getRequestCountByStatuses(String... statuses) {
        if (statuses == null || statuses.length == 0) return 0;
        
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total_requests FROM clearance_requests WHERE status IN (");
        for (int index = 0; index < statuses.length; index++) {
            sql.append("?");
            if (index < statuses.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < statuses.length; index++) {
                pstmt.setString(index + 1, statuses[index]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_requests");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ClearanceRequest> getRequestsByStudentId(int studentId) {
        List<ClearanceRequest> requests = new ArrayList<>();
        String sql = "SELECT request_id, student_id, submitted_at, status FROM clearance_requests WHERE student_id = ? ORDER BY request_id DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp submittedAtTimestamp = rs.getTimestamp("submitted_at");
                    LocalDateTime submittedAt = submittedAtTimestamp != null ? submittedAtTimestamp.toLocalDateTime() : null;

                    ClearanceRequest request = new ClearanceRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setSubmittedAt(submittedAt);

                    String statusText = rs.getString("status");
                    try {
                        request.setStatus(mapRequestStatus(statusText));
                    } catch (IllegalArgumentException e) {
                        request.setStatus(RequestStatus.PENDING);
                    }
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private RequestStatus mapRequestStatus(String statusText) {
        if (statusText == null || statusText.trim().isEmpty()) {
            return RequestStatus.PENDING;
        }
        if ("APPROVED".equalsIgnoreCase(statusText) || "FULLY_APPROVED".equalsIgnoreCase(statusText)) {
            return RequestStatus.CLEARED;
        }
        return RequestStatus.valueOf(statusText.toUpperCase());
    }

    public void updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE clearance_requests SET status = ? WHERE request_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
