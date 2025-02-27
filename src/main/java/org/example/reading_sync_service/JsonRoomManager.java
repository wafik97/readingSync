package org.example.reading_sync_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;

public class JsonRoomManager {

    private static final String JSON_FILE_PATH = "static/files.json"; // Path in resources folder
    private ObjectMapper objectMapper;
    private JsonNode rootNode;

    // Constructor to initialize the ObjectMapper and JSON file
    public JsonRoomManager() throws IOException {
        this.objectMapper = new ObjectMapper();
        // Load JSON from resources
        ClassPathResource resource = new ClassPathResource(JSON_FILE_PATH);
        this.rootNode = objectMapper.readTree(resource.getFile());
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
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new ClassPathResource(JSON_FILE_PATH).getFile(), rootNode);
    }
}
