package org.example.model;

public class User {
    private String id;
    private String room;
    private int currentPage;

    // Constructor
    public User(String id, String room, int currentPage) {
        this.id = id;
        this.room = room;
        this.currentPage = currentPage;
    }


    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
