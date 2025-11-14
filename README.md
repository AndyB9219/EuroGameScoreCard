# Euro Game Scorecard

A universal scorecard app for Euro-style board games. Track victory points across multiple categories for any game with any number of players.

## Features

- **Game Setup**: Configure game name, description, players, and scoring categories
- **Flexible Scoring**: Add any number of players and scoring categories
- **Category Details**: Add titles and optional subtitles for each scoring category
- **Live Totals**: Automatically calculates total victory points for each player
- **Winner Display**: Crown icon shows the current winner
- **Responsive Design**: Works on desktop and mobile devices

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Open your browser to the URL shown in the terminal (typically http://localhost:5173)

### Build

```bash
npm run build
```

## How to Use

1. **Setup New Game**:
   - Enter the game name (e.g., "Wingspan", "Terraforming Mars")
   - Add optional description for your game session
   - Add player names (minimum 2 players)
   - Add scoring categories with titles and optional subtitles
   - Click "Launch Game"

2. **Track Scores**:
   - Enter scores for each player in each category
   - Totals are calculated automatically
   - The current winner is shown with a crown icon

3. **Start New Game**:
   - Click "New Game" to reset and setup a new game session

## Technology Stack

- React 18
- TypeScript
- Vite
- CSS3

## License

MIT