package com.example.demo.upload;

import com.example.demo.parsing.PdfExtractor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final PdfExtractor pdfExtractor;

    public UploadController(PdfExtractor pdfExtractor) {
        this.pdfExtractor = pdfExtractor;
    }

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String text = pdfExtractor.extract(file);
        return Map.of(
            "status", "extracted",
            "filename", file.getOriginalFilename(),
            "characters", String.valueOf(text.length()),
            "preview", text.substring(0, Math.min(200, text.length()))
        );
    }
}