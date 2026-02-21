const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export async function uploadPdf(file: File) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${API_URL}/api/upload`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.statusText}`);
  }

  return response.json();
}

export async function getJob(jobId: string) {
  const response = await fetch(`${API_URL}/api/jobs/${jobId}`);

  if (!response.ok) {
    throw new Error(`Failed to fetch job: ${response.statusText}`);
  }

  return response.json();
}

export async function getJobChunks(jobId: string) {
  const response = await fetch(`${API_URL}/api/jobs/${jobId}/chunks`);
  if (!response.ok) {
    throw new Error(`Failed to fetch chunks: ${response.statusText}`);
  }
  return response.json();
}