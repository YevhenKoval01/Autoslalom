# Autoslalom - Java Swing 2D Game

**Autoslalom** is a retro-style 2D driving game developed in Java. The player controls a car and must dodge obstacles on a moving track. The game features a dynamic environment and a custom-built 7-segment digital score display.

## 🚀 Key Features

- **Custom Graphics Engine**: Built using `Java Swing` and `AWT`, utilizing manual rendering (`paintComponent`) for high-performance 2D visuals.
- **Dynamic Obstacle Generation**: Obstacles appear randomly on three lanes with logic to ensure the track remains traversable.
- **Progressive Difficulty**: The game speed increases over time (`tickInterval` decreases), challenging the player's reaction speed.
- **Custom 7-Segment Display**: Instead of using standard text, the score is shown via a custom-coded 7-segment digit class that renders segments geometrically.
- **Animated Environment**: Features a scrolling road effect using synchronized background images and roadside animations.
- **Multi-threaded Game Loop**: Implements the `Runnable` interface and a dedicated `Thread` for smooth gameplay logic and rendering updates.

## 🛠️ Technologies Used

- **Language**: Java
- **Library**: Swing & AWT (Abstract Window Toolkit)
- **Concepts**: Multi-threading, Event Handling (KeyListener), Geometric Rendering, Object-Oriented Design.

## 🎮 Controls

| Key | Action |
|-----|--------|
| **A** | Move Left |
| **D** | Move Right |
| **S** | Start / Restart Game |

## 📦 Setup & Installation

1. **Prerequisites**: Ensure you have Java JDK 8 or higher installed.
2. **Clone the repository**:
   ```bash
   git clone [https://github.com/YevhenKoval01/Autoslalom.git](https://github.com/YevhenKoval01/Autoslalom.git)
