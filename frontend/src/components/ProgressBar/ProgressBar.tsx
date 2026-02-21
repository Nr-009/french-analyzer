import './ProgressBar.css';

interface ProgressBarProps {
  completed: number;
  total: number;
}

export default function ProgressBar({ completed, total }: ProgressBarProps) {
  const percentage = total === 0 ? 0 : Math.round((completed / total) * 100);

  return (
    <div className="progress-bar">
      <div className="progress-bar__header">
        <span className="progress-bar__label">Analyzing chunks</span>
        <span className="progress-bar__count">{completed} / {total}</span>
      </div>
      <div className="progress-bar__track">
        <div
          className="progress-bar__fill"
          style={{ width: `${percentage}%` }}
        />
      </div>
      <div className="progress-bar__percentage">{percentage}%</div>
    </div>
  );
}