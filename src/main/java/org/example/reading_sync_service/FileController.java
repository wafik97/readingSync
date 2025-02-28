package org.example.reading_sync_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final WebClient webClient;
    private static final String BASE_PDF_URL = "https://raw.githubusercontent.com/wafik97/proj_3_data/main/pdfFiles/"; // Remote PDF URL
    private static final String JSON_FILE_URL = "https://raw.githubusercontent.com/wafik97/proj_3_data/main/data/files.json"; // Remote JSON URL

    private final JsonRoomManager roomManager = JsonRoomManager.getInstance(); // Singleton instance of JsonRoomManager

    public FileController(WebClient webClient) {
        this.webClient = webClient;
    }

    // Fetch a PDF file from a URL
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        String fileUrl = BASE_PDF_URL + filename;

        byte[] pdfBytes = webClient.get()
                .uri(fileUrl)
                .retrieve()
                .bodyToMono(byte[].class)  // Get PDF as byte array
                .block();

        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    // Fetch JSON data from a URL
    @GetMapping("/data")
    public Map<String, Object> getJsonData() throws IOException {
        // Fetch the JSON file as byte array from URL
        byte[] jsonData = webClient.get()
                .uri(JSON_FILE_URL)
                .retrieve()
                .bodyToMono(byte[].class)  // Get JSON as byte array
                .block();

        if (jsonData == null || jsonData.length == 0) {
            throw new IOException("Failed to fetch JSON data");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, Map.class);
    }

    // New endpoint to return room status
    @GetMapping("/roomStatus")
    public ResponseEntity<Map<String, String>> getRoomStatus() {
        // Get the room status from JsonRoomManager
        Map<String, String> roomStatus = roomManager.getRooms();

        if (roomStatus.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Return the room status as a JSON response
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(roomStatus);
    }


    // New combined endpoint to return both the JSON data and room status
    @GetMapping("/fullData")
    public ResponseEntity<Map<String, Object>> getCombinedData() throws IOException {
        // Fetch JSON data from /data endpoint
        byte[] jsonData = webClient.get()
                .uri(JSON_FILE_URL)
                .retrieve()
                .bodyToMono(byte[].class)  // Get JSON as byte array
                .block();

        if (jsonData == null || jsonData.length == 0) {
            throw new IOException("Failed to fetch JSON data");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonDataMap = objectMapper.readValue(jsonData, Map.class);

        // Fetch room status from /roomStatus endpoint
        Map<String, String> roomStatus = roomManager.getRooms();

        // Combine both JSON data and room status into a single map
        jsonDataMap.put("rooms", roomStatus);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonDataMap);
    }

}
