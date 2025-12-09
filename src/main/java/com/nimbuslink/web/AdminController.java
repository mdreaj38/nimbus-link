package com.nimbuslink.web;

import com.nimbuslink.protocol.NbpCommand;
import com.nimbuslink.protocol.NbpMessage;
import com.nimbuslink.server.ClientHandler;
import com.nimbuslink.server.SubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SubscriptionManager subscriptionManager;

    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        if (message != null && !message.isEmpty()) {
            subscriptionManager.broadcastToAll(new NbpMessage(NbpCommand.SEND, "SYSTEM: " + message));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Message required");
    }

    @PostMapping("/kick")
    public ResponseEntity<?> kick(@RequestBody Map<String, String> payload) {
        String clientId = payload.get("clientId");
        for (ClientHandler client : subscriptionManager.getAllClients()) {
            if (client.getClientId().equals(clientId)) {
                client.kick();
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/clients")
    public ResponseEntity<?> getClients() {
        java.util.List<Map<String, String>> clients = new java.util.ArrayList<>();
        for (ClientHandler client : subscriptionManager.getAllClients()) {
            Map<String, String> clientInfo = new java.util.HashMap<>();
            clientInfo.put("clientId", client.getClientId());
            clientInfo.put("displayName", client.getDisplayName());
            clients.add(clientInfo);
        }
        return ResponseEntity.ok(clients);
    }
}
