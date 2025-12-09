package com.nimbuslink.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;

        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to NimbusLink Server at " + host + ":" + port);

            // Reader thread
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println("< " + line);
                    }
                } catch (IOException e) {
                    System.out.println("Server disconnected.");
                    System.exit(0);
                }
            }).start();

            System.out.println("Commands: JOIN <room>, SEND <room> <msg>, LEAVE <room>, PING, QUIT");

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("QUIT".equalsIgnoreCase(line.trim())) {
                    break;
                }
                out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
