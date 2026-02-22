import { useState, useCallback, useRef } from 'react';
import { uploadPdf } from '../../api/clients';
import './UploadForm.css';

interface UploadFormProps {
  onUploadSuccess: (jobId: string) => void;
}

interface DropZoneProps {
  accept: string;
  label: string;
  sublabel: string;
  validate: (f: File) => string | null;
  file: File | null;
  onFile: (f: File) => void;
  onError: (e: string) => void;
}

function DropZone({ accept, label, sublabel, validate, file, onFile, onError }: DropZoneProps) {
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleFile = (f: File) => {
    const err = validate(f);
    if (err) { onError(err); return; }
    onError('');
    onFile(f);
  };

  const onDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(true);
  }, []);

  const onDragLeave = useCallback(() => setDragging(false), []);

  const onDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped) handleFile(dropped);
  }, []);

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0];
    if (selected) handleFile(selected);
  };

  return (
    <div
      className={`drop-zone ${dragging ? 'dragging' : ''} ${file ? 'has-file' : ''}`}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
      onClick={() => inputRef.current?.click()}
    >
      <input ref={inputRef} type="file" accept={accept} onChange={onChange} hidden />
      <div className="drop-zone__icon">
        {file ? (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        )}
      </div>
      {file ? (
        <div className="drop-zone__filename">{file.name}</div>
      ) : (
        <>
          <div className="drop-zone__label">{label}</div>
          <div className="drop-zone__sublabel">{sublabel}</div>
        </>
      )}
    </div>
  );
}

export default function UploadForm({ onUploadSuccess }: UploadFormProps) {
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [ankiFile, setAnkiFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const validatePdf = (f: File) =>
    f.type !== 'application/pdf' ? 'Only PDF files are supported.' : null;

  const validateAnki = (f: File) =>
    !f.name.endsWith('.txt') ? 'Please export your Anki deck as a .txt file.' : null;

  const handleSubmit = async () => {
    if (!pdfFile) return;
    setLoading(true);
    setError(null);
    try {
      const data = await uploadPdf(pdfFile, ankiFile ?? undefined);
      onUploadSuccess(data.job_id);
    } catch (err: any) {
      setError(err.message ?? 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-form">
      <DropZone
        accept="application/pdf"
        label="Drag your PDF here"
        sublabel="or click to browse"
        validate={validatePdf}
        file={pdfFile}
        onFile={setPdfFile}
        onError={(e) => setError(e || null)}
      />

      <DropZone
        accept=".txt"
        label="Drag your Anki export here (optional)"
        sublabel="File → Export → Notes in Plain Text"
        validate={validateAnki}
        file={ankiFile}
        onFile={setAnkiFile}
        onError={(e) => setError(e || null)}
      />

      {error && <p className="upload-form__error">{error}</p>}

      {pdfFile && (
        <button
          className="upload-form__submit"
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? <span className="upload-form__spinner" /> : 'Analyze'}
        </button>
      )}
    </div>
  );
}