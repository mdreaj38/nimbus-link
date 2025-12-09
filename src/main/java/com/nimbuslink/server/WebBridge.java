package com.nimbuslink.server;

public interface WebBridge {
    void onClientConnected(String clientId);

    void onClientDisconnected(String clientId);

    void onRoomJoin(String clientId, String room);

    void onRoomLeave(String clientId, String room);

    void onNicknameSet(String clientId, String nickname);

    void onMessage(String room, String senderId, String content);

    void onLog(String message);
}
