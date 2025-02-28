package org.example.reading_sync_service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    private final WebClient webClient;

    // Inject the WebClient configured in WebClientConfig
    public PdfController(WebClient webClient) {
        this.webClient = webClient;
    }

    private static final String PDF_URL = "https://raw.githubusercontent.com/wafik97/proj_3_data/main/pdfFiles/L2.pdf";

    @GetMapping("/fetch")
    public ResponseEntity<Resource> fetchPdf() {
        byte[] pdfBytes = webClient.get()
                .uri(PDF_URL)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(); // Fetch the PDF as byte array

        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=sample.pdf")
                .body(resource);
    }
}
