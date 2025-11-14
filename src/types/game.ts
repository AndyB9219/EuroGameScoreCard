export interface ScoreCategory {
  id: string;
  title: string;
  subtitle?: string;
}

export interface Player {
  id: string;
  name: string;
  scores: Record<string, number>; // categoryId -> score
}

export interface GameSetup {
  gameName: string;
  description: string;
  players: Player[];
  categories: ScoreCategory[];
}

export interface GameState extends GameSetup {
  isGameStarted: boolean;
}
