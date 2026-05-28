package com.distributedclearance.server.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            String message =
                    reader.readLine();

            if (!isValidMessage(message)) {
                System.err.println(
                        "[MainServer] Ignoring malformed message: "
                        + message
                );
                socket.close();
                return;
            }

            System.out.println(
                    "Main Server Received: "
                    + message
            );

            forwardToDepartment(
                    "FINANCE",
                    6001,
                    message
            );

            forwardToDepartment(
                    "LIBRARY",
                    6002,
                    message
            );

            forwardToDepartment(
                    "REGISTRAR",
                    6003,
                    message
            );

            socket.close();

        } catch (IOException e) {
                        System.err.println(
                                        "[MainServer] Connection handling failed: "
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

                if (parts.length < 2 || parts[0].trim().isEmpty()) {
                        return false;
                }

                try {
                        Integer.parseInt(parts[0].trim());
                        return true;

                } catch (NumberFormatException e) {
                        return false;
                }
        }

    private void forwardToDepartment(
            String department,
            int port,
            String message
    ) {

        try {

            Socket deptSocket = new Socket("localhost", port);

            PrintWriter writer =
                    new PrintWriter(
                            deptSocket.getOutputStream(),
                            true
                    );

            writer.println(message);
            deptSocket.close();

            System.out.println("Forwarded to " + department);

        } catch (IOException e) {
                        System.err.println(
                                        "[MainServer] Failed to forward to "
                                        + department
                                        + ": "
                                        + e.getMessage()
                        );
        }
    }
}