const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export async function uploadPdf(file: File, ankiFile?: File) {
  const formData = new FormData();
  formData.append('file', file);

  if (ankiFile) {
    formData.append('ankiFile', ankiFile);
    formData.append('hasAnki', 'true');
  } else {
    formData.append('hasAnki', 'false');
  }

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

export async function exportAnkiDeck(jobId: string): Promise<void> {
  const response = await fetch(`${API_URL}/api/anki/export/${jobId}`);

  if (!response.ok) {
    throw new Error(`Export failed: ${response.statusText}`);
  }
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `anki_${jobId}.txt`;
  a.click();
  window.URL.revokeObjectURL(url);
}