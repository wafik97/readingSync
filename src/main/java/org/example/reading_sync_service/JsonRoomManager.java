package org.example.reading_sync_service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonRoomManager {

    // Singleton instance
    private static JsonRoomManager instance;

    static final Map<String, String> bookForRoom = new ConcurrentHashMap<>();
    static {
        bookForRoom.put("room1", "none");
        bookForRoom.put("room2", "none");
        bookForRoom.put("room3", "none");
    }

    // Private constructor to prevent instantiation
    private JsonRoomManager() {}

    // Public method to provide access to the singleton instance
    public static JsonRoomManager getInstance() {
        if (instance == null) {
            synchronized (JsonRoomManager.class) {
                if (instance == null) {
                    instance = new JsonRoomManager();
                }
            }
        }
        return instance;
    }

    public Map<String, String> getRooms() {
        return bookForRoom;
    }

    public String getRoomValue(String roomName) {
        return bookForRoom.get(roomName);
    }

    public void setRoomValue(String roomName, String newValue) {
        bookForRoom.put(roomName, newValue);
    }
}
