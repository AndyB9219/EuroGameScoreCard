import { useState } from 'react';
import { GameSetup as GameSetupType, ScoreCategory, Player } from '../types/game';
import './GameSetup.css';

interface GameSetupProps {
  onGameStart: (setup: GameSetupType) => void;
}

export default function GameSetup({ onGameStart }: GameSetupProps) {
  const [gameName, setGameName] = useState('');
  const [description, setDescription] = useState('');
  const [playerNames, setPlayerNames] = useState<string[]>(['', '']);
  const [categories, setCategories] = useState<ScoreCategory[]>([
    { id: '1', title: '', subtitle: '' }
  ]);

  const addPlayer = () => {
    setPlayerNames([...playerNames, '']);
  };

  const removePlayer = (index: number) => {
    if (playerNames.length > 2) {
      setPlayerNames(playerNames.filter((_, i) => i !== index));
    }
  };

  const updatePlayerName = (index: number, name: string) => {
    const newNames = [...playerNames];
    newNames[index] = name;
    setPlayerNames(newNames);
  };

  const addCategory = () => {
    const newId = String(Math.max(...categories.map(c => parseInt(c.id)), 0) + 1);
    setCategories([...categories, { id: newId, title: '', subtitle: '' }]);
  };

  const removeCategory = (id: string) => {
    if (categories.length > 1) {
      setCategories(categories.filter(c => c.id !== id));
    }
  };

  const updateCategory = (id: string, field: 'title' | 'subtitle', value: string) => {
    setCategories(categories.map(c =>
      c.id === id ? { ...c, [field]: value } : c
    ));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validate inputs
    if (!gameName.trim()) {
      alert('Please enter a game name');
      return;
    }

    const validPlayerNames = playerNames.filter(name => name.trim());
    if (validPlayerNames.length < 2) {
      alert('Please enter at least 2 player names');
      return;
    }

    const validCategories = categories.filter(cat => cat.title.trim());
    if (validCategories.length === 0) {
      alert('Please add at least one score category');
      return;
    }

    // Create players with initialized scores
    const players: Player[] = validPlayerNames.map((name, index) => ({
      id: String(index + 1),
      name: name.trim(),
      scores: validCategories.reduce((acc, cat) => {
        acc[cat.id] = 0;
        return acc;
      }, {} as Record<string, number>)
    }));

    // Clean up categories
    const cleanCategories = validCategories.map(cat => ({
      ...cat,
      title: cat.title.trim(),
      subtitle: cat.subtitle?.trim()
    }));

    onGameStart({
      gameName: gameName.trim(),
      description: description.trim(),
      players,
      categories: cleanCategories
    });
  };

  return (
    <div className="game-setup">
      <h1>Euro Game Scorecard</h1>
      <h2>Setup New Game</h2>

      <form onSubmit={handleSubmit}>
        <section className="setup-section">
          <h3>Game Details</h3>
          <div className="form-group">
            <label htmlFor="gameName">Game Name *</label>
            <input
              id="gameName"
              type="text"
              value={gameName}
              onChange={(e) => setGameName(e.target.value)}
              placeholder="e.g., Wingspan, Terraforming Mars"
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description (optional)</label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Add any notes about this game session..."
              rows={3}
            />
          </div>
        </section>

        <section className="setup-section">
          <h3>Players</h3>
          {playerNames.map((name, index) => (
            <div key={index} className="form-group player-group">
              <label htmlFor={`player-${index}`}>Player {index + 1}</label>
              <div className="input-with-button">
                <input
                  id={`player-${index}`}
                  type="text"
                  value={name}
                  onChange={(e) => updatePlayerName(index, e.target.value)}
                  placeholder={`Player ${index + 1} name`}
                />
                {playerNames.length > 2 && (
                  <button
                    type="button"
                    onClick={() => removePlayer(index)}
                    className="btn-remove"
                  >
                    Remove
                  </button>
                )}
              </div>
            </div>
          ))}
          <button type="button" onClick={addPlayer} className="btn-add">
            + Add Player
          </button>
        </section>

        <section className="setup-section">
          <h3>Score Categories</h3>
          {categories.map((category) => (
            <div key={category.id} className="category-group">
              <div className="form-group">
                <label htmlFor={`category-title-${category.id}`}>Category Title</label>
                <input
                  id={`category-title-${category.id}`}
                  type="text"
                  value={category.title}
                  onChange={(e) => updateCategory(category.id, 'title', e.target.value)}
                  placeholder="e.g., Birds, Cards, Bonus Objectives"
                />
              </div>

              <div className="form-group">
                <label htmlFor={`category-subtitle-${category.id}`}>Subtitle (optional)</label>
                <div className="input-with-button">
                  <input
                    id={`category-subtitle-${category.id}`}
                    type="text"
                    value={category.subtitle || ''}
                    onChange={(e) => updateCategory(category.id, 'subtitle', e.target.value)}
                    placeholder="e.g., End of round bonuses"
                  />
                  {categories.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeCategory(category.id)}
                      className="btn-remove"
                    >
                      Remove
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
          <button type="button" onClick={addCategory} className="btn-add">
            + Add Category
          </button>
        </section>

        <button type="submit" className="btn-launch">
          Launch Game
        </button>
      </form>
    </div>
  );
}
