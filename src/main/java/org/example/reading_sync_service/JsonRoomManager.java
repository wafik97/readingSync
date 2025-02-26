package org.example.reading_sync_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;

public class JsonRoomManager {

    private static final String JSON_FILE_PATH = "target/classes/static/files.json";
    private ObjectMapper objectMapper;
    private JsonNode rootNode;

    // Constructor to initialize the ObjectMapper and JSON file
    public JsonRoomManager() throws IOException {
        this.objectMapper = new ObjectMapper();
        this.rootNode = objectMapper.readTree(new File(JSON_FILE_PATH));
    }

    // Method to get the value of a specific room
    public String getRoomValue(String roomName) {
        JsonNode roomsNode = rootNode.path("rooms");
        return roomsNode.path(roomName).asText();
    }

    // Method to update the value of a specific room
    public void setRoomValue(String roomName, String newValue) throws IOException {
        ObjectNode roomsNode = (ObjectNode) rootNode.path("rooms");
        roomsNode.put(roomName, newValue);

        // Write the updated JSON back to the file
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(JSON_FILE_PATH), rootNode);
    }
}
