export interface Job {
  jobId: string;
  filename: string;
  status: 'PROCESSING' | 'COMPLETE';
  totalChunks: number;
  completedChunks: number;
  createdAt: string;
}

export interface ChunkResult {
  chunkId: string;
  jobId: string;
  filename: string;
  chunkIndex: number;
  chunkName: string;
  difficultyScore: number;
  processingTimeMs: number;
  topWords: string;
  statistics: string;
}

export interface TopWord {
  word: string;
  lemma: string;
  frequency_rank: number;
  pos: string;
  sentence: string;
  dependency_label: string;
}

export interface Statistics {
  total_words: number;
  unique_lemmas: number;
  sentences: number;
  avg_sentence_length: number;
  pos_distribution: Record<string, number>;
  named_entities: Record<string, string[]>;
  dependency_complexity: {
    avg_tree_depth: number;
    max_tree_depth: number;
    complex_structures: number;
  };
}

export interface JobResult extends Job {
  chunks: ChunkResult[];
}

export interface UploadResponse {
  jobId: string;
  filename: string;
  totalChunks: number;
}