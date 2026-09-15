# Hollow Knight

<p align="center">
  <strong>A 2D Action-Platformer built with Java and LibGDX</strong>
</p>

<p align="center">
  An independent academic implementation inspired by Hollow Knight,
  featuring platforming, combat, enemies, boss encounters, world interaction,
  UI systems, audio, and persistent game state.
</p>

---

## 📌 About

**Hollow Knight** is a 2D action-platformer developed in **Java using the LibGDX framework**.

The project was developed as an academic game-development project with an emphasis on implementing a complete playable game while maintaining a structured and maintainable software architecture.

The implementation covers the major gameplay systems required for the project, including:

- Player movement and state management
- Platforming mechanics
- Combat
- Enemy systems
- Boss encounters
- Health and Soul systems
- Checkpoints and respawning
- Rooms and world transitions
- Interactive environmental objects
- Menus and HUD
- Audio and sound effects
- Persistent game data
- MVC-oriented separation of responsibilities

The project received **full marks during its academic presentation**.

---

# 🎮 Gameplay

The game is built around exploration, movement, combat, and progression.

The player controls the Knight while navigating through a 2D environment containing platforms, obstacles, enemies, checkpoints, and combat encounters.

The gameplay architecture is designed so that player behavior, world state, enemy behavior, and rendering are handled by separate components rather than being concentrated in a single game class.

---

# ✨ Features

## 🧍 Player System

The Knight is implemented as a dedicated gameplay entity with its own state and statistics.

The player system supports multiple gameplay states, including:

- Idle
- Running
- Run-to-idle transition
- Jumping
- Airborne
- Falling
- Landing
- Double Jump
- Dashing
- Wall Sliding
- Wall Jumping
- Attacking
- Focusing

The player system also manages:

- Facing direction
- Ground detection
- Air movement
- Double-jump availability
- Dash availability
- Attack timing
- Attack hitboxes
- Pogo attacks
- Health
- Soul
- Player statistics
- Respawn information
- Death and respawn

The state-based approach allows player behavior to change according to the current gameplay state while keeping the individual actions organized.

---

# ⚔️ Combat System

Combat is implemented as an independent gameplay concern rather than being directly tied to rendering.

The combat system handles interactions between the player and enemies, including:

- Player attacks
- Attack timing
- Attack hitboxes
- Damage interactions
- Enemy damage
- Player damage
- Pogo attacks
- Combat states
- Boss combat interactions

Attack behavior is coordinated with the player's movement and state system so that combat remains consistent with the rest of the gameplay loop.

---

# 👾 Enemy System

The project contains multiple enemy types with different gameplay behaviors.

Implemented enemy types include:

- Crawlid
- Mosscreep
- Mosquito
- Mossfly
- Hornhead
- False Knight
- Crystal Guardian

Enemies are organized around common gameplay concepts while allowing individual enemy implementations to define their own behavior.

This makes it possible to extend the game with additional enemy types without having to redesign the entire enemy system.

---

# 👑 Boss System

Bosses are represented separately from ordinary enemies so that boss-specific behavior can be handled independently.

The project includes boss encounters and dedicated boss-room interaction.

Boss gameplay is integrated with the world system, allowing the environment to react to the state of a boss encounter.

For example, boss-room doors can change their state depending on whether the player enters the boss room, the boss is defeated, or the player dies.

---

# ❤️ Health System

Player health is handled through a dedicated health component.

The system supports:

- Health tracking
- Damage
- Death
- Respawn
- Checkpoint-based recovery
- Integration with player state

Separating health from the main Knight object keeps health-related responsibilities isolated from unrelated movement and rendering logic.

---

# 🔵 Soul System

The Knight also has a dedicated Soul component.

Soul is treated separately from health and can therefore be managed independently by gameplay systems.

The separation of Health and Soul allows both systems to evolve independently without coupling their internal logic.

---

# 🏁 Checkpoints & Respawning

The world contains checkpoints that can be used as respawn locations.

Respawn information is represented separately so that the player's current respawn state can be maintained independently of the rendering system.

The system supports:

1. Player death
2. Determining the current respawn information
3. Returning the player to the appropriate location
4. Restoring the required gameplay state
5. Continuing the game

---

# 🌍 World System

The game world is represented using multiple reusable concepts.

The world model includes:

- Areas
- Rooms
- Platforms
- Walls
- Spikes
- Checkpoints
- Map data
- World transitions

This structure allows the game to represent the environment as a collection of logical gameplay objects instead of treating the entire level as one large object.

---

# 🚪 Room & World Transitions

The game supports transitions between different rooms and gameplay areas.

Rooms can contain their own:

- Geometry
- Enemies
- Interactive objects
- Checkpoints
- Environmental elements
- Gameplay state

World transitions are coordinated separately from the individual rendering objects.

---

# 🧱 Environmental Interaction

The environment contains interactive gameplay elements such as:

- Platforms
- Walls
- Spikes
- Checkpoints
- Boss-room doors

Environmental objects can affect player movement and gameplay state.

For example, spikes act as hazards while walls and platforms participate in movement and collision behavior.

---

# 🚪 Boss Room Door System

Boss-room access is connected to the state of the boss encounter.

When the Knight enters the boss room, the corresponding door can become non-passable.

The door state is also updated when the encounter ends or the player dies, allowing the player to interact with the boss room repeatedly.

This creates a direct connection between:


Player
   ↓
Room Transition
   ↓
Boss Encounter
   ↓
World State
   ↓
Door State

🖥️ UI & HUD

The project includes dedicated rendering components for the game's user interface.

The UI layer is responsible for displaying gameplay information without directly owning the underlying gameplay state.

The HUD can present information related to the player's current gameplay state while remaining separated from the core model.

📋 Menus

The project includes multiple game menus for navigating between different parts of the application.

Menu logic is kept separate from the main gameplay systems so that menu behavior does not become coupled to the game world.

The project includes both general menus and in-game menu functionality.

🔊 Audio System

Audio is integrated into the project using LibGDX's audio functionality.

The game contains:

Background music
Sound effects
Gameplay audio
Menu/gameplay audio integration

Audio management is separated from the core gameplay entities to prevent game objects from becoming responsible for the entire audio system.

🎨 Rendering

Rendering is separated from the underlying game model.

The project uses dedicated rendering components for different parts of the game, including:

Player rendering
HUD rendering
Game/world rendering
Menu rendering

This separation allows gameplay objects to represent game state while rendering components are responsible for presenting that state visually.

🏗️ Architecture

The project follows an MVC-oriented architecture.

The primary architectural goal is to separate:

Model
  ↓
Game State & Entities

Controller
  ↓
Gameplay Logic & Coordination

View
  ↓
Rendering & Presentation

This prevents the main game screen from becoming a single "God Class" containing all gameplay, rendering, input, world, and entity logic.

📦 Model

The Model layer represents the game's state and gameplay entities.

Major model categories include:

model/
├── core/
├── player/
├── enemy/
├── boss/
└── world/
Core

Contains common gameplay concepts and shared abstractions.

Examples include:

Entity
Damageable
Movable
Direction
Player

Contains player-specific gameplay state.

Examples include:

Knight
HealthComponent
SoulComponent
RespawnData
PlayerStats
Enemy

Contains enemy-related entities and implementations.

Examples include:

Enemy
Crawlid
Mosscreep
Mosquito
Mossfly
Hornhead
False Knight
Crystal Guardian
World

Contains the logical representation of the game environment.

Examples include:

Platform
Wall
Spike
Checkpoint
Room
Area
MapData
🎮 Controller

The Controller layer coordinates gameplay behavior and user interaction.

Controller responsibilities include:

Input processing
Player control
Combat coordination
World interaction
Game-state coordination
Gameplay transitions

The controller layer acts as the bridge between player input, model state, and the game's presentation layer.

🖼️ View

The View layer is responsible for displaying the current game state.

It contains rendering-oriented components for:

Player
HUD
Game world
Menus
Other visual game elements

The View does not need to own the underlying gameplay rules.

Instead, it reads the relevant state and presents it to the player.

🔄 Game Loop

The game follows the standard LibGDX update/render model.

Conceptually:

Input
  ↓
Controller
  ↓
Model Update
  ↓
World / Entity State
  ↓
View / Renderer
  ↓
Frame

This keeps gameplay updates and rendering responsibilities conceptually separated.

🗂️ Project Structure

The repository follows the standard LibGDX Gradle project structure.

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
│       └── main/
│           ├── java/
│           └── resources/
│
├── gradle/
│   └── wrapper/
│
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── .gitignore
├── .gitattributes
├── .editorconfig
└── README.md
🛠️ Technologies
Technology	Usage
Java	Main programming language
LibGDX	2D game-development framework
LWJGL3	Desktop backend
Gradle	Build and dependency management
Tiled / TMX	Map and level representation
Git	Version control
GitHub	Source-code hosting
🧠 Software Engineering Principles

The project was designed with several software-engineering principles in mind.

Separation of Responsibilities

Different parts of the game have dedicated responsibilities.

For example:

Knight
    → Player state

KnightController
    → Player control

Renderer
    → Visual representation

World
    → Environment

Enemy
    → Enemy state/behavior
Avoiding God Classes

The game logic is distributed between multiple classes and systems instead of placing every responsibility inside the main game screen.

This makes the project easier to:

Understand
Debug
Maintain
Extend
Test
Refactor
Encapsulation

Game entities own their relevant state and expose controlled operations for modifying that state.

This reduces unnecessary coupling between unrelated systems.

Extensibility

The architecture is designed so that additional:

Enemies
Rooms
World objects
Player mechanics
Rendering components
Controllers

can be added without rewriting the entire game.

📐 Design Approach

The project separates the game into three major concerns:

Data
What is the current state of the game?

Handled primarily by the Model.

Behavior
What should happen when the game state changes?

Handled primarily by Controllers and gameplay systems.

Presentation
How should the current state be displayed?

Handled primarily by Views and Renderers.

This separation is particularly important in a game project because gameplay logic and rendering logic can otherwise become tightly coupled.

🚀 Getting Started
Requirements

To run the project, install:

JDK
Git
IntelliJ IDEA or another Java IDE

Gradle does not need to be installed separately because the repository contains the Gradle Wrapper.

Clone the Repository
git clone https://github.com/amabna/hollow-knight.git

Then enter the project directory:

cd hollow-knight
Run the Game
Windows
gradlew.bat lwjgl3:run
Linux / macOS
./gradlew lwjgl3:run
🔨 Build the Project

To build all modules:

Windows
gradlew.bat build
Linux / macOS
./gradlew build

To build the desktop application:

gradlew.bat lwjgl3:jar

The generated JAR can be found under:

lwjgl3/build/libs/
🧹 Useful Gradle Commands

Clean generated build files:

gradlew.bat clean

Build the complete project:

gradlew.bat build

Run the desktop application:

gradlew.bat lwjgl3:run

Build the desktop JAR:

gradlew.bat lwjgl3:jar
🎮 Controls

The exact control configuration is defined by the project's input/controller implementation.

The gameplay supports controls for actions such as:

Action	Description
Movement	Move the Knight
Jump	Jump / aerial movement
Attack	Perform a melee attack
Dash	Perform a dash
Focus	Use the focus/healing mechanic

The exact keyboard mapping should be checked against the current input configuration of the project.

🧪 Gameplay Systems

The project combines the individual systems into a complete gameplay loop.

A simplified gameplay flow is:

Game Start
    ↓
Menu
    ↓
Gameplay
    ↓
Player Input
    ↓
Controller
    ↓
Player / World Update
    ↓
Enemy Interaction
    ↓
Combat
    ↓
Health / Soul / State Update
    ↓
Rendering
    ↓
Next Frame

When the player dies:

Player Death
    ↓
Respawn Data
    ↓
Checkpoint
    ↓
Player Respawn
    ↓
Gameplay Continues
🗺️ World Flow

The world is divided into logical rooms and areas.

A simplified world interaction model is:

Area
 ├── Room
 │    ├── Platforms
 │    ├── Walls
 │    ├── Spikes
 │    ├── Enemies
 │    ├── Checkpoints
 │    └── Interactive Objects
 │
 └── Room Transition

This organization makes it possible for gameplay systems to reason about individual rooms instead of treating the entire map as one monolithic structure.

🏆 Academic Project

This project was developed as part of an academic game-development project.

The implementation was evaluated through an academic presentation and received full marks.

The project was developed with a focus on both:

Gameplay implementation
Software architecture and code organization
📚 Learning Outcomes

Working on this project provided practical experience with:

Object-Oriented Programming
Java game development
LibGDX
Real-time game loops
Player state machines
Collision handling
Platformer mechanics
Combat systems
Enemy design
Boss encounters
World/room architecture
Rendering systems
UI development
Audio integration
Persistent game state
Gradle
Git
GitHub
MVC-oriented architecture
Refactoring and separation of responsibilities
🔮 Possible Future Improvements

Although the current project implements the required gameplay systems, a game architecture can always be extended.

Potential future improvements could include:

Additional enemies
Additional rooms and areas
More boss encounters
More player abilities
Additional visual effects
More environmental interactions
Expanded save-state functionality
Additional UI polish
More automated gameplay tests

These are potential extensions rather than requirements for the current implementation.

⚠️ Project Scope

This repository is an independent educational implementation inspired by Hollow Knight.

It is not an official Hollow Knight project and is not affiliated with Team Cherry.

Hollow Knight, its characters, original artwork, music, and related intellectual property belong to their respective rights holders.

This project is intended for educational and portfolio purposes.

👨‍💻 Author

AmirAbbas Naghavi

GitHub:

github.com/amabna

Project Repository:

github.com/amabna/hollow-knight

📜 License

This repository is provided for educational and portfolio purposes.

Third-party assets and intellectual property remain the property of their respective owners.
