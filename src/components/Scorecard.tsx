import { useState } from 'react';
import { GameState, Player } from '../types/game';
import './Scorecard.css';

interface ScorecardProps {
  gameState: GameState;
  onReset: () => void;
}

export default function Scorecard({ gameState, onReset }: ScorecardProps) {
  const [players, setPlayers] = useState<Player[]>(gameState.players);

  const updateScore = (playerId: string, categoryId: string, value: string) => {
    const numValue = parseInt(value) || 0;
    setPlayers(players.map(player =>
      player.id === playerId
        ? { ...player, scores: { ...player.scores, [categoryId]: numValue } }
        : player
    ));
  };

  const calculateTotal = (player: Player): number => {
    return Object.values(player.scores).reduce((sum, score) => sum + score, 0);
  };

  const getWinner = (): Player | null => {
    if (players.length === 0) return null;
    return players.reduce((prev, current) =>
      calculateTotal(current) > calculateTotal(prev) ? current : prev
    );
  };

  const winner = getWinner();

  return (
    <div className="scorecard">
      <div className="scorecard-header">
        <div>
          <h1>{gameState.gameName}</h1>
          {gameState.description && <p className="game-description">{gameState.description}</p>}
        </div>
        <button onClick={onReset} className="btn-reset">
          New Game
        </button>
      </div>

      <div className="scorecard-container">
        <div className="scorecard-table">
          {/* Header Row */}
          <div className="table-header">
            <div className="cell cell-category">Category</div>
            {players.map(player => (
              <div key={player.id} className="cell cell-player">
                {player.name}
                {winner && winner.id === player.id && <span className="winner-badge">👑</span>}
              </div>
            ))}
          </div>

          {/* Category Rows */}
          {gameState.categories.map(category => (
            <div key={category.id} className="table-row">
              <div className="cell cell-category">
                <div className="category-title">{category.title}</div>
                {category.subtitle && <div className="category-subtitle">{category.subtitle}</div>}
              </div>
              {players.map(player => (
                <div key={player.id} className="cell cell-score">
                  <input
                    type="number"
                    value={player.scores[category.id] || 0}
                    onChange={(e) => updateScore(player.id, category.id, e.target.value)}
                    className="score-input"
                  />
                </div>
              ))}
            </div>
          ))}

          {/* Total Row */}
          <div className="table-row table-total">
            <div className="cell cell-category">
              <strong>Total Victory Points</strong>
            </div>
            {players.map(player => (
              <div key={player.id} className="cell cell-total">
                <strong>{calculateTotal(player)}</strong>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
