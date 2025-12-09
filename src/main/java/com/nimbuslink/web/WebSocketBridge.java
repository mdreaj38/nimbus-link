package com.nimbuslink.web;

import com.nimbuslink.server.WebBridge;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketBridge implements WebBridge {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onClientConnected(String clientId) {
        sendUpdate("CLIENT_CONNECT", clientId);
    }

    @Override
    public void onClientDisconnected(String clientId) {
        sendUpdate("CLIENT_DISCONNECT", clientId);
    }

    @Override
    public void onRoomJoin(String clientId, String room) {
        Map<String, String> payload = new HashMap<>();
        payload.put("clientId", clientId);
        payload.put("room", room);
        messagingTemplate.convertAndSend("/topic/events", new DashboardEvent("ROOM_JOIN", payload));
    }

    @Override
    public void onRoomLeave(String clientId, String room) {
        Map<String, String> payload = new HashMap<>();
        payload.put("clientId", clientId);
        payload.put("room", room);
        messagingTemplate.convertAndSend("/topic/events", new DashboardEvent("ROOM_LEAVE", payload));
    }

    @Override
    public void onMessage(String room, String senderId, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("room", room);
        payload.put("sender", senderId);
        payload.put("content", content);
        messagingTemplate.convertAndSend("/topic/messages", payload);
    }

    @Override
    public void onLog(String message) {
        messagingTemplate.convertAndSend("/topic/logs", message);
    }

    private void sendUpdate(String type, Object data) {
        messagingTemplate.convertAndSend("/topic/events", new DashboardEvent(type, data));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class DashboardEvent {
        private String type;
        private Object data;
    }
}
