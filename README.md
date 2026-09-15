# Hollow Knight

A 2D action-platformer inspired by **Hollow Knight**, developed from scratch using **Java** and **LibGDX**.

This project was developed as an academic game-development project with a focus on gameplay implementation, object-oriented design, MVC-oriented architecture, and clean separation of responsibilities.

> **Academic Project — Full Presentation Score**

---

## 🎮 Overview

The project recreates the core gameplay experience of a 2D action-platformer, including player movement, combat, enemies, boss encounters, world exploration, checkpoints, UI, audio, and persistent game state.

The implementation is structured into separate **Model, View, and Controller** components to keep gameplay logic, game state, and rendering responsibilities organized and maintainable.

---

## ✨ Features

### Player

The Knight has a state-based movement and gameplay system supporting:

* Idle
* Running
* Jumping
* Falling
* Landing
* Double Jump
* Dash
* Wall Sliding
* Wall Jumping
* Attacking
* Focusing
* Direction and movement handling
* Attack hitboxes
* Pogo attacks

### Combat

* Player attack system
* Attack timing and states
* Hitbox-based combat
* Enemy damage
* Player damage
* Pogo attacks
* Boss combat interactions

### Enemies & Bosses

The project includes multiple enemy types with different behaviors, including:

* Crawlid
* Mosscreep
* Mosquito
* Mossfly
* Hornhead
* False Knight
* Crystal Guardian

Boss encounters are integrated with the world and room systems.

### Player Systems

* Health management
* Soul management
* Player statistics
* Death and respawn
* Checkpoints
* Respawn data
* Persistent game state

### World

The game world is organized into reusable components such as:

* Areas
* Rooms
* Platforms
* Walls
* Spikes
* Checkpoints
* Map data
* World transitions
* Interactive objects

### Dynamic World Interaction

World elements can react to gameplay state.

For example, boss-room doors can change their state depending on the player's interaction with the boss room and the outcome of the encounter.

### UI & Menus

The project contains dedicated systems for:

* Main menus
* In-game menus
* HUD
* Gameplay information
* Game-state presentation

### Audio

The project integrates:

* Background music
* Sound effects
* Gameplay audio
* Menu/gameplay audio management

---

## 🏗️ Architecture

The project follows an **MVC-oriented architecture**.

### Model

Responsible for game state and gameplay entities.

```text
model/
├── core/
├── player/
├── enemy/
├── boss/
└── world/
```

Examples include:

* `Entity`
* `Knight`
* `HealthComponent`
* `SoulComponent`
* `PlayerStats`
* `RespawnData`
* `Enemy`
* `Room`
* `Area`
* `Platform`
* `Wall`
* `Spike`
* `Checkpoint`

### Controller

Responsible for coordinating gameplay behavior and player interaction.

```text
controller/
├── core/
├── player/
└── ...
```

Controllers handle responsibilities such as:

* Input
* Player control
* Combat coordination
* World interaction
* Gameplay state
* Game flow

### View

Responsible for rendering and presentation.

The View layer contains rendering-oriented components for:

* Player
* World
* HUD
* Menus
* Other visual game elements

This separation prevents gameplay logic from becoming tightly coupled with rendering code.

---

## 🔄 Gameplay Flow

A simplified gameplay loop is:

```text
Input
  ↓
Controller
  ↓
Model Update
  ↓
World / Entity State
  ↓
Rendering
  ↓
Next Frame
```

Player death follows a separate respawn flow:

```text
Player Death
     ↓
Respawn Data
     ↓
Checkpoint
     ↓
Player Respawn
     ↓
Gameplay Continues
```

---

## 📁 Project Structure

```text
Hollow Knight/
│
├── assets/
│   ├── HKassets/
│   ├── sounds/
│   ├── ui/
│   └── ...
│
├── core/
│   ├── build.gradle
│   └── src/
│       └── main/
│           └── java/
│               └── AP/
│                   └── HollowKinght/
│                       ├── controller/
│                       ├── model/
│                       ├── view/
│                       └── Main.java
│
├── lwjgl3/
│   ├── build.gradle
│   └── src/
│
├── gradle/
│   └── wrapper/
│
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🛠️ Technologies

| Technology      | Purpose                         |
| --------------- | ------------------------------- |
| **Java**        | Main programming language       |
| **LibGDX**      | Game development framework      |
| **LWJGL3**      | Desktop backend                 |
| **Gradle**      | Build and dependency management |
| **Tiled / TMX** | Map and level data              |
| **Git**         | Version control                 |
| **GitHub**      | Project hosting                 |

---

## 🧠 Software Engineering

The project was designed with the following principles:

* Object-Oriented Programming
* Separation of responsibilities
* MVC-oriented architecture
* Encapsulation
* Reusable game components
* Maintainable gameplay systems
* Separation of gameplay and rendering
* Avoidance of oversized God Classes
* Focused controllers and models
* Extensible enemy and world systems

The goal was not only to create a playable game, but also to maintain a codebase that can be extended and maintained without concentrating the entire game logic inside a single class.

---

## 🚀 Getting Started

### Requirements

* Java Development Kit (JDK)
* IntelliJ IDEA or another Java IDE
* Git

Gradle does not need to be installed separately because the project includes the **Gradle Wrapper**.

### Clone

```bash
git clone https://github.com/amabna/hollow-knight.git
cd hollow-knight
```

### Run on Windows

```bash
gradlew.bat lwjgl3:run
```

### Run on Linux / macOS

```bash
./gradlew lwjgl3:run
```

---

## 🔨 Build

Build the complete project:

```bash
gradlew.bat build
```

Build the desktop application:

```bash
gradlew.bat lwjgl3:jar
```

Generated desktop artifacts are located in:

```text
lwjgl3/build/libs/
```

---

## 🎮 Controls

The game supports controls for:

| Action   | Description            |
| -------- | ---------------------- |
| Movement | Move the Knight        |
| Jump     | Jump / aerial movement |
| Attack   | Perform an attack      |
| Dash     | Perform a dash         |
| Focus    | Use the focus mechanic |

> Exact keyboard mappings are defined by the project's input configuration.

---

## 📚 What This Project Demonstrates

This project demonstrates practical experience with:

* Java
* Object-Oriented Programming
* LibGDX
* 2D game development
* Game loops
* Player state machines
* Platformer mechanics
* Collision and interaction systems
* Combat systems
* Enemy behavior
* Boss encounters
* World and room architecture
* Rendering
* UI systems
* Audio integration
* Persistent game state
* Gradle
* Git/GitHub
* MVC-oriented architecture
* Code organization and refactoring

---

## 🎓 Academic Project

This project was developed as an academic game-development project.

The implementation was evaluated through an academic presentation and received **full marks**.

The project combines both:

**Gameplay implementation**
and
**Software architecture / code organization**

---

## ⚠️ Disclaimer

This is an independent educational project inspired by **Hollow Knight**.

It is not affiliated with or endorsed by **Team Cherry**.

Hollow Knight and its original characters, artwork, music, and intellectual property belong to their respective rights holders.

This repository is intended for **educational and portfolio purposes**.

---

## 👨‍💻 Author

**AmirAbbas Naghavi**

GitHub:
[https://github.com/amabna](https://github.com/amabna)

Project:
[https://github.com/amabna/hollow-knight](https://github.com/amabna/hollow-knight)

---

<p align="center">
  <strong>Built with Java & LibGDX</strong>
</p>
