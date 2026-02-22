package com.example.demo.result;

import com.example.demo.model.ChunkResult;
import com.example.demo.model.ChunkResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/anki")
public class AnkiExportController {

    private final ChunkResultRepository chunkResultRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${libretranslate.host}")
    private String libreTranslateHost;

    public AnkiExportController(ChunkResultRepository chunkResultRepository, ObjectMapper objectMapper) {
        this.chunkResultRepository = chunkResultRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/export/{jobId}")
    public ResponseEntity<byte[]> exportAnkiDeck(@PathVariable String jobId) throws Exception {

        List<ChunkResult> chunks = chunkResultRepository.findByJob_JobIdOrderByChunkIndexAsc(jobId);
        if (chunks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> wordToSentence = new LinkedHashMap<>();
        for (ChunkResult chunk : chunks) {
            if (chunk.getTopWords() == null) continue;
            List<Map<String, Object>> topWords = objectMapper.readValue(
                chunk.getTopWords(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            for (Map<String, Object> word : topWords) {
                String lemma = (String) word.get("lemma");
                String sentence = (String) word.get("sentence");
                if (lemma != null && !wordToSentence.containsKey(lemma)) {
                    wordToSentence.put(lemma, sentence != null ? sentence : "");
                }
            }
        }

        Map<String, String> translations = new HashMap<>();
        String translateUrl = String.format("http://%s:5000/translate", libreTranslateHost);

        for (String word : wordToSentence.keySet()) {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("q", word);
                request.put("source", "fr");
                request.put("target", "en");
                request.put("format", "text");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

                Map<String, String> response = restTemplate.postForObject(
                    translateUrl, entity, Map.class
                );
                String translated = response != null ? response.get("translatedText") : word;
                translations.put(word, translated != null ? translated : word);
            } catch (Exception e) {
                System.err.println("Translation failed for word: " + word + " | Error: " + e.getMessage());
                translations.put(word, word);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("#separator:tab\n");
        sb.append("#html:false\n");
        sb.append("#columns:Front\tBack\tExtra\n");

        for (Map.Entry<String, String> entry : wordToSentence.entrySet()) {
            String word = entry.getKey();
            String sentence = entry.getValue();
            String translation = translations.getOrDefault(word, word);
            sb.append(word).append("\t")
              .append(translation).append("\t")
              .append(sentence).append("\n");
        }

        byte[] bytes = sb.toString().getBytes("UTF-8");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_PLAIN);
        responseHeaders.setContentDisposition(
            ContentDisposition.attachment().filename("anki_" + jobId + ".txt").build()
        );

        return new ResponseEntity<>(bytes, responseHeaders, HttpStatus.OK);
    }
}