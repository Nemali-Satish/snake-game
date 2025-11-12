# 🐍 Advanced Snake Game

A modern implementation of the classic Snake game with enhanced features, built using Java and Java Swing.

## 🎮 Features

- 🚀 Smooth and responsive controls
- ⚡ Multiple power-ups with different effects
- 🏆 Persistent high score system
- 🎚️ Increasing difficulty levels
- 🕹️ Toggleable wrap-around mode
- 🎨 Clean and intuitive UI
- 🎵 Sound effects (coming soon!)

## 🎯 How to Play

### Controls

- **Arrow Keys**: Control the snake's direction
- **P**: Pause/Resume the game
- **Space**: Toggle wrap-around mode
- **ESC**: Exit the game

### Game Rules

1. Eat the food (green squares) to grow longer
2. Avoid hitting the walls or yourself (unless wrap mode is on)
3. Collect power-ups (orange squares) for special abilities
4. The game speeds up as you level up
5. Try to beat your high score!

## ⚙️ Installation

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven (for building from source)

### Running the Game

#### Option 1: Using Pre-built JAR

1. Download the latest release JAR file
2. Open a terminal in the download directory
3. Run: `java -jar SnakeGame.jar`

#### Option 2: Building from Source

```bash
# Clone the repository
git clone https://github.com/Nemali-Satish/snake-game.git
cd snake-game

# Build the project
mvn clean package

# Run the game
java -jar target/SnakeGame-1.0-SNAPSHOT.jar
```

## 🛠️ Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/snake/
│   │   ├── GamePanel.java    # Main game logic and rendering
│   │   ├── Snake.java        # Snake behavior and movement
│   │   ├── Food.java         # Food generation and effects
│   │   ├── PowerUp.java      # Power-up system
│   │   └── ...
│   └── resources/            # Game assets (images, sounds)
└── test/                     # Unit tests
```

### Building

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```
