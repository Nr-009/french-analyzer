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
}

export interface JobResult extends Job {
  chunks: ChunkResult[];
}

export interface UploadResponse {
  jobId: string;
  filename: string;
  totalChunks: number;
}