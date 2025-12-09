package com.nimbuslink.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TcpServer {
    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    private final SubscriptionManager subscriptionManager;
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    public TcpServer(SubscriptionManager subscriptionManager, @Value("${tcp.server.port:8888}") int port) {
        this.subscriptionManager = subscriptionManager;
        this.port = port;
    }

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                logger.info("TCP Server started on port " + port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, subscriptionManager);
                        executorService.submit(handler);
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Error accepting client connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Could not start TCP server", e);
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        executorService.shutdown();
    }
}
