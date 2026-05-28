package com.distributedclearance.server.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.distributedclearance.models.enums.Department;

public class DepartmentServer {

    private final int port;
    private final String departmentName;

    public DepartmentServer(int port, String departmentName) {
        this.port = port;
        this.departmentName = departmentName;
    }

    public void startServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println(
                    departmentName
                    + " Server running on port "
                    + port
            );

            while (true) {

                try (Socket socket = serverSocket.accept()) {

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            socket.getInputStream()
                                    )
                            );

                    String message = reader.readLine();

                    if (!isValidMessage(message)) {
                        System.err.println(
                                "[" + departmentName + "] Ignoring malformed message: "
                                + message
                        );
                        continue;
                    }

                    System.out.println(
                            "[" + departmentName + "] "
                            + "Received: "
                            + message
                    );

                    int requestId;

                    try {
                        String[] parts = message.split(":", 2);
                        requestId = Integer.parseInt(parts[0].trim());

                    } catch (NumberFormatException e) {
                        System.err.println(
                                "[" + departmentName + "] Invalid request id in message: "
                                + message
                        );
                        continue;
                    }

                    DistributedApprovalService service =
                            new DistributedApprovalService();

                    service.processApproval(
                            requestId,
                            Department.valueOf(departmentName)
                    );

                    System.out.println(
                            departmentName
                            + " finished processing "
                            + requestId
                    );

                    NotificationSender.send(
                            departmentName
                            + " processed request "
                            + requestId
                    );

                } catch (IOException e) {
                    System.err.println(
                            "[" + departmentName + "] Socket handling failed: "
                            + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "[" + departmentName + "] Server failed to start on port "
                    + port
                    + ": "
                    + e.getMessage()
            );
        }
    }

    private boolean isValidMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        if (!message.contains(":")) {
            return false;
        }

        String[] parts = message.split(":", 2);
        return parts.length >= 2 && !parts[0].trim().isEmpty();
    }
}