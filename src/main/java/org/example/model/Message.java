package org.example.model;

import java.util.List;

public class Message {
    private String type;
    private String userId;
    private String chatMessage;
    private int page;
    private List<User> users; // For the user list message
    private String room; // Add room field for the room information

    // Constructor for user join or user list messages
    public Message(String type, String userId, int page, String room) {
        this.type = type;
        this.userId = userId;
        this.page = page;
        this.room = room; // Initialize room field
    }

    // Constructor for chat messages
    public Message(String type, String userId, String chatMessage) {
        this.type = type;
        this.userId = userId;
        this.chatMessage = chatMessage;
    }

    // Constructor for user list messages
    public Message(String type, List<User> users) {
        this.type = type;
        this.users = users;
    }

    // Constructor for error messages
    public Message(String type, String message) {
        this.type = type;
        this.chatMessage = message;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getRoom() {
        return room; // Get room information
    }

    public void setRoom(String room) {
        this.room = room; // Set room information
    }
}
