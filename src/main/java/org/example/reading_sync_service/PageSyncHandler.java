package org.example.reading_sync_service;

import org.example.model.User;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageSyncHandler extends TextWebSocketHandler {

    // A map that holds the list of sessions per room
    private static final Map<String, CopyOnWriteArrayList<WebSocketSession>> rooms = new HashMap<>();
    private static final Map<String, User> userSessions = new HashMap<>();

    // Maximum number of users allowed per room
    private static final int MAX_USERS_PER_ROOM = 5;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String room = "room1"; // You can change this logic to dynamically assign rooms to users

        // Create a new user instance and associate it with the session
        User user = new User(session.getId(), room, 1);  // Start the user at page 1

        // Get the list of users in the room
        CopyOnWriteArrayList<WebSocketSession> roomSessions = rooms.getOrDefault(room, new CopyOnWriteArrayList<>());

        // Check if the room has reached the maximum number of users
        if (roomSessions.size() >= MAX_USERS_PER_ROOM) {
            // If the room is full, reject the connection
            session.sendMessage(new TextMessage("Room is full"));
            session.close();
            return;
        }

        // Add the session to the room and the user session map
        roomSessions.add(session);
        rooms.put(room, roomSessions);
        userSessions.put(session.getId(), user);

        // Send a message to the room about the new user
        sendMessageToRoom(room, "New user joined: " + user.getId());

        System.out.println("User connected: " + user.getId() + " to room: " + room);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Extract the message content
        String messageContent = message.getPayload();

        // Check if it's a page update or chat message
        if (messageContent.startsWith("page:")) {
            int page = Integer.parseInt(messageContent.substring(5));
            User user = userSessions.get(session.getId());
            if (user != null) {
                user.setCurrentPage(page);
                // Send the page update to all users in the room
                sendMessageToRoom(user.getRoom(), "User " + user.getId() + " is now on page " + page);
            }
        } else {
            // It's a chat message, send it to the room
            User user = userSessions.get(session.getId());
            if (user != null) {
                sendMessageToRoom(user.getRoom(), user.getId() + ": " + messageContent);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        // Remove the session and the user from the session map when the connection is closed
        User user = userSessions.remove(session.getId());
        if (user != null) {
            rooms.get(user.getRoom()).remove(session);
            sendMessageToRoom(user.getRoom(), "User " + user.getId() + " left the room");
        }
    }

    // Method to send a message to all users in a particular room
    private void sendMessageToRoom(String room, String message) throws IOException {
        for (WebSocketSession s : rooms.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }
}
