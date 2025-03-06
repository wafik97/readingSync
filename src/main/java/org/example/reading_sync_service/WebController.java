package org.example.reading_sync_service;  // Make sure this package is correct

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

}
