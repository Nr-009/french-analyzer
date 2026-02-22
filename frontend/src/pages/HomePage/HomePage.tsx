import { useNavigate } from 'react-router-dom';
import UploadForm from '../../components/UploadForm/UploadForm';
import './HomePage.css';

export default function HomePage() {
  const navigate = useNavigate();

  const handleUploadSuccess = (jobId: string) => {
    navigate(`/jobs/${jobId}`);
  };

  return (
    <main className="home-page">
      <div className="home-page__bg" aria-hidden="true">
        <div className="home-page__orb home-page__orb--1" />
        <div className="home-page__orb home-page__orb--2" />
      </div>
      <div className="home-page__content">
        <header className="home-page__header">
          <div className="home-page__eyebrow">
            <span className="home-page__flag">🇫🇷</span>
            <span>NLP Analysis</span>
          </div>
          <h1 className="home-page__title">
            French Book<br />
            <span className="home-page__title--accent">Difficulty Analyzer</span>
          </h1>
          <p className="home-page__subtitle">
            Upload a French PDF to analyze its difficulty chapter by chapter.
            Optionally add your Anki deck to personalize the difficulty to you.
          </p>
        </header>
        <UploadForm onUploadSuccess={handleUploadSuccess} />
      </div>
    </main>
  );
}