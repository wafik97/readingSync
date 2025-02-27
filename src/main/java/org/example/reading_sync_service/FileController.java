package org.example.reading_sync_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String BASE_DIRECTORY = "src/main/resources/static/myData/"; // Change this path if needed
    private static final String FILE_PATH = "static/files.json";

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
        File file = new File(BASE_DIRECTORY + filename);

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        Path path = file.toPath();
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF) // Ensure only PDFs are served
                .body(resource);
    }

    @GetMapping("/data")
    public Map<String, Object> getJsonData() throws IOException {
        ClassPathResource resource = new ClassPathResource(FILE_PATH);
        byte[] jsonData = Files.readAllBytes(Paths.get(resource.getURI()));

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonData, Map.class);
    }
}
