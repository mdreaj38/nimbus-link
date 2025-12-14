package com.nimbuslink.server;

import com.nimbuslink.protocol.NbpMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SubscriptionManager {
    // Room -> Set of ClientHandlers
    private final ConcurrentHashMap<String, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();
    // All connected clients
    private final Set<ClientHandler> allClients = new CopyOnWriteArraySet<>();

    private final WebBridge webBridge;

    public SubscriptionManager(WebBridge webBridge) {
        this.webBridge = webBridge;
    }

    public void addClient(ClientHandler client) {
        allClients.add(client);
        webBridge.onClientConnected(client.getClientId());
        webBridge.onLog("Client connected: " + client.getClientId());
    }

    public void onNicknameSet(ClientHandler client, String nickname) {
        webBridge.onNicknameSet(client.getClientId(), nickname);
        webBridge.onLog(client.getClientId() + " set nickname to: " + nickname);
    }

    public void removeClient(ClientHandler client) {
        allClients.remove(client);
        rooms.values().forEach(roomClients -> roomClients.remove(client));
        webBridge.onClientDisconnected(client.getClientId());
        webBridge.onLog("Client disconnected: " + client.getClientId());
    }

    public void joinRoom(String room, ClientHandler client) {
        rooms.computeIfAbsent(room, k -> new CopyOnWriteArraySet<>()).add(client);
        webBridge.onRoomJoin(client.getClientId(), room);
        webBridge.onLog(client.getClientId() + " joined " + room);
    }

    public void leaveRoom(String room, ClientHandler client) {
        Set<ClientHandler> roomClients = rooms.get(room);
        if (roomClients != null) {
            roomClients.remove(client);
            if (roomClients.isEmpty()) {
                rooms.remove(room);
            }
        }
        webBridge.onRoomLeave(client.getClientId(), room);
        webBridge.onLog(client.getClientId() + " left " + room);
    }

    public void broadcastToRoom(String room, NbpMessage message, ClientHandler sender) {
        Set<ClientHandler> roomClients = rooms.get(room);
        if (roomClients != null) {
            for (ClientHandler client : roomClients) {
                // Don't send back to sender? Or maybe we should?
                // Usually chat apps echo back, but let's skip sender for efficiency if needed.
                // For now, let's send to everyone including sender so they know it was
                // processed.
                client.sendMessage(message);
            }
        }
        // Extract content from message payload "ROOM SENDER: CONTENT"
        // But here we constructed it as "ROOM SENDER: CONTENT" in ClientHandler
        // Let's parse it back or just pass raw.
        // The message payload is "ROOM SENDER_ID: CONTENT"
        String payload = message.getPayload();
        // We want to show it on dashboard too
        webBridge.onMessage(room, sender.getClientId(), payload);
        webBridge.onLog("Message in " + room + ": " + payload);
    }

    public void broadcastToAll(NbpMessage message) {
        for (ClientHandler client : allClients) {
            client.sendMessage(message);
        }
        webBridge.onLog("Broadcast: " + message.toString());
    }

    public Set<String> getActiveRooms() {
        return Collections.unmodifiableSet(rooms.keySet());
    }

    public int getClientCount() {
        return allClients.size();
    }

    public Set<ClientHandler> getAllClients() {
        return Collections.unmodifiableSet(allClients);
    }
}
