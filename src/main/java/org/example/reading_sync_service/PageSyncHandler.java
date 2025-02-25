package org.example.reading_sync_service;

import org.example.model.User;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PageSyncHandler extends TextWebSocketHandler {

    static final Map<String, CopyOnWriteArrayList<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    static final Map<String, User> userSessions = new ConcurrentHashMap<>();
    private static final int MAX_USERS_PER_ROOM = 5;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String room = "room1";  // Default room
        CopyOnWriteArrayList<WebSocketSession> roomSessions = rooms.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>());

        if (roomSessions.size() >= MAX_USERS_PER_ROOM) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Room is full\"}"));
            session.close();
            return;
        }

        // Create the user but don't assign a userId yet
        User user = new User(session.getId(), room, 1);
        userSessions.put(session.getId(), user);
        roomSessions.add(session);

        sendMessageToRoom(room, "{\"type\":\"user_join\",\"userId\":\"" + user.getId() + "\",\"page\":1}");
        sendUserListUpdate(room);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        boolean exists = false;
        String payload = message.getPayload();
        User user = userSessions.get(session.getId());

        if (user == null) return;

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            if ("user_join".equals(type)) {
                // When receiving the user_join message, update userId

                String userId = (String) data.get("userId");

                exists = userSessions.values().stream().anyMatch(myUser -> userId.equals(myUser.getId()));
                if(exists){
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Username already exists\"}"));
                    my_afterConnectionClosed( session);
                    return;
                }
                user.setId(userId);  // Update userId
                sendMessageToRoom(user.getRoom(), "{\"type\":\"user_join\",\"userId\":\"" + user.getId() + "\",\"page\":1}");
            }

            if ("page_update".equals(type)) {
                int page = (int) data.get("page");
                user.setCurrentPage(page);
                sendMessageToRoom(user.getRoom(), "{\"type\":\"page_update\",\"userId\":\"" + user.getId() + "\",\"page\":" + page + "}");
            }

        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Invalid message format\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        User user = userSessions.remove(session.getId());
        if (user != null) {
            rooms.get(user.getRoom()).remove(session);
            sendMessageToRoom(user.getRoom(), "{\"type\":\"user_leave\",\"userId\":\"" + user.getId() + "\"}");
            sendUserListUpdate(user.getRoom());
        }
    }

    public void my_afterConnectionClosed(WebSocketSession session) throws IOException {
        User user = userSessions.remove(session.getId());
        if (user != null) {
            rooms.get(user.getRoom()).remove(session);
            sendMessageToRoom(user.getRoom(), "{\"type\":\"user_leave\",\"userId\":\"" + user.getId() + "\"}");
            sendUserListUpdate(user.getRoom());
        }
        session.close();
    }

    private void sendMessageToRoom(String room, String message) throws IOException {
        for (WebSocketSession s : rooms.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }

    private void sendUserListUpdate(String room) throws IOException {
        StringBuilder userListJson = new StringBuilder("{\"type\":\"user_list\",\"users\":[");

        boolean first = true;
        for (WebSocketSession session : rooms.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            User user = userSessions.get(session.getId());
            if (user != null) {
                if (!first) userListJson.append(",");
                userListJson.append("{\"userId\":\"").append(user.getId()).append("\",\"page\":").append(user.getCurrentPage()).append("}");
                first = false;
            }
        }
        userListJson.append("]}");
        sendMessageToRoom(room, userListJson.toString());
    }
}