import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer,
} from 'recharts';
import type { ChunkResult, TopWord, Statistics } from '../../types';
import { getJobChunks } from '../../api/clients';
import './ResultsPage.css';

export default function ResultsPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const [chunks, setChunks] = useState<ChunkResult[]>([]);
  const [selectedChunk, setSelectedChunk] = useState<ChunkResult | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getJobChunks(jobId!)
      .then(data => {
        setChunks(data);
        setLoading(false);
      });
  }, [jobId]);

  const filename = chunks.length > 0 ? chunks[0].filename : '';
  const avgDifficulty = chunks.length > 0
    ? (chunks.reduce((sum, c) => sum + c.difficultyScore, 0) / chunks.length).toFixed(3)
    : '—';

  const chartData = chunks.map(c => ({
    name: `${c.chunkIndex + 1}`,
    score: parseFloat(c.difficultyScore.toFixed(3)),
    chunk: c,
  }));

  const selectedWords: TopWord[] = selectedChunk
    ? JSON.parse(selectedChunk.topWords)
    : [];

  const selectedStats: Statistics | null = selectedChunk
    ? JSON.parse(selectedChunk.statistics)
    : null;

  const CustomDot = (props: any) => {
    const { cx, cy, payload } = props;
    const isSelected = selectedChunk?.chunkId === payload.chunk.chunkId;
    return (
      <circle
        cx={cx}
        cy={cy}
        r={isSelected ? 7 : 4}
        fill={isSelected ? '#e8c547' : '#6b9fff'}
        stroke={isSelected ? '#fff' : 'transparent'}
        strokeWidth={2}
        style={{ cursor: 'pointer' }}
        onClick={() => setSelectedChunk(payload.chunk)}
      />
    );
  };

  if (loading) {
    return (
      <main className="results-page">
        <div className="results-page__loading">Loading results...</div>
      </main>
    );
  }

  return (
    <main className="results-page">
      <div className="results-page__content">

        <button className="results-page__back" onClick={() => navigate('/')}>
          ← New Analysis
        </button>

        <header className="results-page__header">
          <div className="results-page__eyebrow">Analysis Complete</div>
          <h1 className="results-page__title">{filename}</h1>
          <p className="results-page__subtitle">
            {chunks.length} chunks · avg difficulty {avgDifficulty} · click any point to explore
          </p>
        </header>

        <section className="results-page__chart">
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
              <XAxis
                dataKey="name"
                tick={{ fill: '#888', fontSize: 11 }}
                axisLine={{ stroke: 'rgba(255,255,255,0.1)' }}
                tickLine={false}
              />
              <YAxis
                domain={['auto', 'auto']}
                tick={{ fill: '#888', fontSize: 11 }}
                axisLine={false}
                tickLine={false}
                tickFormatter={v => v.toFixed(2)}
              />
              <Tooltip
                contentStyle={{ background: '#1a1a1a', border: '1px solid #333', borderRadius: 8 }}
                labelStyle={{ color: '#aaa', fontSize: 12 }}
                itemStyle={{ color: '#6b9fff' }}
                formatter={(v: any) => [v.toFixed(3), 'Difficulty']}
              />
              <Line
                type="monotone"
                dataKey="score"
                stroke="#6b9fff"
                strokeWidth={2}
                dot={<CustomDot />}
                activeDot={false}
              />
            </LineChart>
          </ResponsiveContainer>
        </section>

        {selectedChunk ? (
          <section className="results-page__detail">
            <div className="results-page__detail-header">
              <h2 className="results-page__detail-title">{selectedChunk.chunkName}</h2>
              <span className="results-page__detail-score">
                score {selectedChunk.difficultyScore.toFixed(3)}
              </span>
            </div>

            {selectedStats && (
              <div className="results-page__stats">
                <div className="stat-card">
                  <span className="stat-card__value">{selectedStats.total_words}</span>
                  <span className="stat-card__label">words</span>
                </div>
                <div className="stat-card">
                  <span className="stat-card__value">{selectedStats.unique_lemmas}</span>
                  <span className="stat-card__label">unique lemmas</span>
                </div>
                <div className="stat-card">
                  <span className="stat-card__value">{selectedStats.sentences}</span>
                  <span className="stat-card__label">sentences</span>
                </div>
                <div className="stat-card">
                  <span className="stat-card__value">{selectedStats.avg_sentence_length}</span>
                  <span className="stat-card__label">avg sentence len</span>
                </div>
                <div className="stat-card">
                  <span className="stat-card__value">{selectedStats.dependency_complexity.avg_tree_depth}</span>
                  <span className="stat-card__label">avg tree depth</span>
                </div>
                <div className="stat-card">
                  <span className="stat-card__value">{selectedChunk.processingTimeMs}ms</span>
                  <span className="stat-card__label">processed in</span>
                </div>
              </div>
            )}

            <div className="results-page__words">
              <table className="words-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Word</th>
                    <th>POS</th>
                    <th>Freq. Rank</th>
                    <th>Example</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedWords.slice(0, 25).map((w, i) => (
                    <tr key={i}>
                      <td className="words-table__rank">{i + 1}</td>
                      <td className="words-table__word">{w.word}</td>
                      <td className="words-table__pos">{w.pos}</td>
                      <td className="words-table__freq">{w.frequency_rank.toLocaleString()}</td>
                      <td className="words-table__sentence">{w.sentence.slice(0, 80)}…</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        ) : (
          <div className="results-page__placeholder">
            <span>↑ click a point on the curve to explore that chunk</span>
          </div>
        )}

      </div>
    </main>
  );
}