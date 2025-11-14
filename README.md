# Euro Game Scorecard

A universal scorecard Android app for Euro-style board games. Track victory points across multiple categories for any game with any number of players.

## Features

- **Game Setup**: Configure game name, description, players, and scoring categories
- **Flexible Scoring**: Add any number of players and scoring categories
- **Category Details**: Add titles and optional subtitles for each scoring category
- **Live Totals**: Automatically calculates total victory points for each player
- **Winner Display**: Crown icon shows the current winner
- **Persistent Storage**: Games are saved to a local Room database
- **Material Design 3**: Modern, beautiful UI with light/dark theme support

## Architecture

This app follows modern Android development best practices:

- **MVVM Architecture**: Clear separation of concerns with ViewModels
- **Jetpack Compose**: Modern declarative UI framework
- **Room Database**: Type-safe database with Flow-based reactive queries
- **Koin**: Lightweight dependency injection
- **Kotlin Coroutines & Flow**: Asynchronous and reactive programming
- **Navigation Component**: Type-safe navigation between screens

## Project Structure

```
app/src/main/java/com/eurogame/scorecard/
├── data/
│   ├── local/
│   │   ├── dao/          # Database access objects
│   │   ├── entity/       # Room entities
│   │   └── GameDatabase.kt
│   └── repository/       # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   └── repository/      # Repository interfaces
├── presentation/
│   ├── gamesetup/       # Game setup screen & ViewModel
│   ├── scorecard/       # Scorecard screen & ViewModel
│   ├── navigation/      # Navigation graph
│   └── theme/           # Material 3 theming
├── di/                  # Koin dependency injection modules
├── GameApplication.kt
└── MainActivity.kt
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Minimum SDK 24 (Android 7.0)
- Kotlin 1.9.20+

### Installation

1. Clone the repository
```bash
git clone https://github.com/AndyB9219/EuroGameScoreCard.git
cd EuroGameScoreCard
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

### Build

```bash
./gradlew assembleDebug
```

## How to Use

1. **Setup New Game**:
   - Enter the game name (e.g., "Wingspan", "Terraforming Mars")
   - Add optional description for your game session
   - Add player names (minimum 2 players)
   - Add scoring categories with titles and optional subtitles
   - Tap "Launch Game"

2. **Track Scores**:
   - Enter scores for each player in each category
   - Totals are calculated automatically
   - The current winner is shown with a crown icon 👑

3. **Start New Game**:
   - Tap "New Game" in the app bar to start a fresh game session

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM
- **Database**: Room
- **Dependency Injection**: Koin
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Navigation Compose
- **Build Tool**: Gradle with Kotlin DSL

## Dependencies

- AndroidX Core KTX
- Jetpack Compose (BOM 2024.01.00)
- Material 3
- Lifecycle & ViewModel
- Room 2.6.1
- Koin 3.5.3
- Navigation Compose

## Testing

The project includes comprehensive unit tests for all major components:

### Test Coverage

- **ViewModel Tests** (`app/src/test/java/.../presentation/`)
  - `GameSetupViewModelTest`: 20+ tests covering state management, validation, and repository interaction
  - `ScorecardViewModelTest`: 18+ tests for score tracking, winner calculation, and error handling

- **Repository Tests** (`app/src/test/java/.../data/repository/`)
  - `GameRepositoryImplTest`: 15+ tests for data operations, entity mapping, and database interactions

- **Domain Model Tests** (`app/src/test/java/.../domain/model/`)
  - `PlayerTest`: Tests for score calculation logic and data class behavior
  - `GameTest`: Tests for game model properties and score mapping
  - `ScoreCategoryTest`: Tests for category model
  - `GameSetupDataTest` & `CategoryInputTest`: Tests for setup data structures

### Test Technologies

- **JUnit 4**: Testing framework
- **MockK**: Mocking library for Kotlin
- **Kotlinx Coroutines Test**: Testing coroutines and Flows
- **Turbine**: Testing Flow emissions
- **Koin Test**: Testing dependency injection

### Running Tests

Run unit tests from Android Studio or via command line:

```bash
./gradlew test
```

Run specific test class:

```bash
./gradlew test --tests GameSetupViewModelTest
```

View test reports:

```bash
./gradlew test
# Reports available at: app/build/reports/tests/testDebugUnitTest/index.html
```

## License

MIT