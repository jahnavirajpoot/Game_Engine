# Game Engine - A Java 2D Game Engine Demo

A lightweight Java 2D game engine featuring a fixed-rate game loop, scene management system, input handling, grid-based pathfinding, and physics utilities. Includes a standalone Snake game example.

## Features

### Core Engine Components
- **Fixed-Step Game Loop**: 60 FPS with nanosecond-level timing precision
- **Scene System**: Extensible scene management with support for multiple scenes
- **Rendering**: Double/tri-buffered rendering using Java AWT/Swing Canvas with BufferStrategy
- **Input Management**: 
  - Keyboard input with frame-accurate key detection (held, just pressed, just released)
  - Mouse input with position tracking, dragging, and scroll wheel support
  - WASD and Arrow key helpers for game controls

### Physics & Pathfinding
- **Pathfinding Algorithms**:
  - BFS (Breadth-First Search) - Fully implemented with diagnostics
  - A* (A-Star) - Scaffold with Manhattan distance heuristic (implementation in progress)
- **Grid System**: Convertible grid for world-space to grid-space mapping with obstacle support
- **Collision Detection**: AABB (Axis-Aligned Bounding Box) utilities
- **Physics Engine**: Basic physics body and vector math support

## Project Structure

```
Game_Engine/
├── game_engine/
│   └── src/
│       ├── SnakeGame.java          # Standalone Snake game example
│       ├── engine/
│       │   ├── Main.java           # Engine entry point
│       │   ├── Game.java           # Main window and game initialization
│       │   ├── GameLoop.java       # Fixed-step game loop
│       │   ├── Renderer.java       # BufferStrategy renderer
│       │   ├── Scene.java          # Base scene class
│       │   ├── SceneManager.java   # Scene registry and switching
│       │   ├── input/
│       │   │   ├── InputManager.java
│       │   │   ├── KeyboardInput.java
│       │   │   └── MouseInput.java
│       │   └── pathfinding/
│       │       ├── Grid.java       # Pathfinding grid model
│       │       ├── Node.java       # Grid node
│       │       ├── BFSPathfinder.java
│       │       └── AStarPathfinder.java
│       └── scenes/
│           └── PlayScene.java      # Demo scene with moving rectangle
├── README.md
└── REPORT.md
```

## Running the Engine

### Engine Demo
Run the main engine entry point to see a demo with a moving rectangle:
```bash
java -cp game_engine/src engine.Main
```

The demo window will open at 800x600 resolution with a PlayScene showing a red rectangle bouncing across a black background.

### Snake Game
Run the standalone Snake game:
```bash
java -cp game_engine/src SnakeGame
```

**Controls:**
- **Arrow Keys** or **WASD**: Move the snake
- **Objective**: Eat food (red squares) to grow
- **Game Over**: Colliding with walls or yourself resets the game

## Key Classes

### Engine Core
- **Game**: Creates the main window (800x600) and initializes the game engine
- **GameLoop**: Manages the fixed-step game loop running at 60 FPS
- **Renderer**: Handles BufferStrategy rendering to the canvas
- **SceneManager**: Manages scene lifecycle and transitions

### Input System
- **InputManager**: Singleton that coordinates keyboard and mouse input
- **KeyboardInput**: Tracks key states with per-frame precision
- **MouseInput**: Tracks mouse position, dragging, and scroll events

### Pathfinding
- **Grid**: 2D grid for pathfinding with obstacle support
- **BFSPathfinder**: Fully functional breadth-first search implementation
- **AStarPathfinder**: A* framework (search logic pending)

### Example Implementation
- **SnakeGame**: A complete, playable Snake game using Swing components
- **PlayScene**: A simple demo scene for the engine

## Technical Details

- **Language**: Java 8+
- **Graphics**: AWT/Swing with Canvas and BufferStrategy
- **Rendering**: Double/tri-buffered rendering for smooth animation
- **Timing**: Nanosecond-precision timing for fixed 60 FPS loop
- **Build**: Compiled .class files provided in `game_engine/src` and `game_engine/out`

## Current Development Status

### Implemented
- ✅ Fixed-step 60 FPS game loop
- ✅ Scene management and switching
- ✅ Input handling (keyboard and mouse)
- ✅ Rendering system with BufferStrategy
- ✅ BFS pathfinding with full diagnostics
- ✅ Grid-based pathfinding utilities
- ✅ Standalone Snake game
- ✅ A* Pathfinind

## Notes

- Mixed compiled (.class) and source files are present in `game_engine/src` for convenience
- Additional compiled output available in `game_engine/out`
- The engine is designed as a learning/demo project showcasing fundamental game engine concepts


