package com.example.demo.parsing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PdfExtractor {

    private static final int CHUNK_SIZE = 500;

    public Map<String, String> extract(MultipartFile file) throws Exception {
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();

        Map<String, String> chunks;

        if (outline != null && outline.getFirstChild() != null) {
            chunks = extractByOutline(document, outline);
        } else {
            chunks = extractByWordCount(document);
        }

        document.close();
        return chunks;
    }

    private Map<String, String> extractByOutline(PDDocument document, PDDocumentOutline outline) throws Exception {
        Map<String, String> chunks = new LinkedHashMap<>();
        PDFTextStripper stripper = new PDFTextStripper();
        int totalPages = document.getNumberOfPages();

        PDOutlineItem current = outline.getFirstChild();
        while (current != null) {
            PDOutlineItem next = current.getNextSibling();

            int startPage = getPageNumber(document, current);
            int endPage = next != null ? getPageNumber(document, next) - 1 : totalPages;

            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            String text = stripper.getText(document);

            chunks.put(current.getTitle(), text.trim());
            current = next;
        }

        return chunks;
    }

    private Map<String, String> extractByWordCount(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        String fullText = stripper.getText(document);
        String[] words = fullText.split("\\s+");

        Map<String, String> chunks = new LinkedHashMap<>();
        int totalChunks = (int) Math.ceil((double) words.length / CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int start = i * CHUNK_SIZE;
            int end = Math.min(start + CHUNK_SIZE, words.length);
            String[] chunkWords = Arrays.copyOfRange(words, start, end);
            chunks.put("chunk_" + (i + 1), String.join(" ", chunkWords));
        }

        return chunks;
    }

    private int getPageNumber(PDDocument document, PDOutlineItem item) throws Exception {
        return document.getDocumentCatalog().getPages()
            .indexOf(item.findDestinationPage(document)) + 1;
    }
}