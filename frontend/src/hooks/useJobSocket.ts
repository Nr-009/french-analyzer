import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const WS_URL = API_URL.replace(/^http/, 'ws');

interface JobProgress {
  completedChunks: number;
  totalChunks: number;
  status: 'PROCESSING' | 'COMPLETE';
}

export function useJobSocket(jobId: string) {
  const [progress, setProgress] = useState<JobProgress>({
    completedChunks: 0,
    totalChunks: 0,
    status: 'PROCESSING',
  });

  useEffect(() => {
    const client = new Client({
      brokerURL: `${WS_URL}/ws/websocket`,
      onConnect: () => {
        client.subscribe(`/topic/job/${jobId}`, (message) => {
          const [, completed, total, status] = message.body.split(':');
          setProgress({
            completedChunks: parseInt(completed),
            totalChunks: parseInt(total),
            status: status as 'PROCESSING' | 'COMPLETE',
          });
        });
      },
    });
    client.activate();
    return () => {
      client.deactivate();
    };
  }, [jobId]);

  return progress;
}