package org.example.reading_sync_service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String BASE_DIRECTORY = "src/main/resources/static/myData/"; // Change this path if needed

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
}
