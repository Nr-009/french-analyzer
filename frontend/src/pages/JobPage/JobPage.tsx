import { useParams } from 'react-router-dom';
import { useJobSocket } from '../../hooks/useJobSocket';
import ProgressBar from '../../components/ProgressBar/ProgressBar';
import './JobPage.css';

export default function JobPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const progress = useJobSocket(jobId!);

  return (
    <main className="job-page">
      <div className="job-page__content">
        <header className="job-page__header">
          <div className="job-page__eyebrow">{progress.status}</div>
          <h1 className="job-page__title">
            {progress.status === 'COMPLETE' ? 'Analysis Complete' : 'Analyzing your book...'}
          </h1>
          <p className="job-page__id">{jobId}</p>
        </header>

        <ProgressBar
          completed={progress.completedChunks}
          total={progress.totalChunks}
        />
      </div>
    </main>
  );
}