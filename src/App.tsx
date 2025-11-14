import { useState } from 'react';
import GameSetup from './components/GameSetup';
import Scorecard from './components/Scorecard';
import { GameState, GameSetup as GameSetupType } from './types/game';
import './App.css';

function App() {
  const [gameState, setGameState] = useState<GameState | null>(null);

  const handleGameStart = (setup: GameSetupType) => {
    setGameState({
      ...setup,
      isGameStarted: true
    });
  };

  const handleReset = () => {
    if (confirm('Are you sure you want to start a new game? All scores will be lost.')) {
      setGameState(null);
    }
  };

  return (
    <div className="app">
      {!gameState ? (
        <GameSetup onGameStart={handleGameStart} />
      ) : (
        <Scorecard gameState={gameState} onReset={handleReset} />
      )}
    </div>
  );
}

export default App;
