import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

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
      brokerURL: `ws://localhost:8080/ws/websocket`,
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