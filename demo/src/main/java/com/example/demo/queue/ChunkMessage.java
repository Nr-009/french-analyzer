package com.example.demo.queue;

import java.util.List;
import java.util.Set;

public class ChunkMessage {
    private String jobId;
    private String filename;
    private int chunkIndex;
    private String chunkName;
    private String text;
    private boolean hasAnki;
    private Set<String> knownWords;

    public ChunkMessage(String jobId, String filename, int chunkIndex, 
                        String chunkName, String text, 
                        boolean hasAnki, Set<String> knownWords) {
        this.jobId = jobId;
        this.filename = filename;
        this.chunkIndex = chunkIndex;
        this.chunkName = chunkName;
        this.text = text;
        this.hasAnki = hasAnki;
        this.knownWords = knownWords;
    }

    public String getJobId() { return jobId; }
    public String getFilename() { return filename; }
    public int getChunkIndex() { return chunkIndex; }
    public String getChunkName() { return chunkName; }
    public String getText() { return text; }
    public boolean isHasAnki() { return hasAnki; }
    public Set<String> getKnownWords() { return knownWords; }
}