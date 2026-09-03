# The Lost Island

A 2D platformer built with **Java + LibGDX**, developed as a portfolio project. You play as a pirate exploring an island in search of treasure, moving through three increasingly complex stages: **Beach**, **Jungle**, and **Temple**.

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Java](https://img.shields.io/badge/Java-8-orange)
![LibGDX](https://img.shields.io/badge/LibGDX-1.14.2-red)

## About

This project was built to explore core 2D platformer mechanics from scratch — custom physics, collision, and animation systems — without relying on a physics engine like Box2D. The goal was full control over movement "feel," which is critical for a responsive platformer.

**Story:** a pirate lands on a lost island in search of treasure, fighting through a beach, navigating jungle parkour while dodging monkeys, and finally surviving a poisonous temple guarded by skeletons before solving one last puzzle to claim the treasure.

## Features implemented so far

- **Custom AABB collision system** — no physics engine; collision is resolved per-axis (X then Y) for precise platformer control
- **Player movement**
  - Walk / Run (run trades control for speed — useful and risky depending on the situation)
  - SOCD handling (simultaneous opposite direction keys resolve to "last key pressed wins")
  - Jump with physically-calculated velocity (`v = sqrt(2 * gravity * jumpHeight)`)
  - Jump buffering (inputs pressed slightly before landing are still honored)
  - Duck (hold to crouch — locks horizontal movement and reduces hitbox height)
- **Frame-accurate jump animation** — takeoff, rising, falling, and landing poses driven by actual velocity/state, not just a timer
- **Tiled map integration** — levels built in [Tiled](https://www.mapeditor.org/), loaded via `TmxMapLoader`
- **Texture atlas pipeline** — sprites packed via LibGDX's `TexturePacker` for efficient rendering

## Planned

- [ ] Beach stage (crabs, seagulls, sword combat)
- [ ] Jungle stage (parkour, monkeys throwing bananas — deflectable with the sword)
- [ ] Temple stage (poison snakes, skeleton guardians, final puzzle)
- [ ] Hit-based life system
- [ ] Sword attack hitbox (shared between offense and deflecting projectiles)

## Tech stack

| | |
|---|---|
| Language | Java 8 |
| Framework | [LibGDX](https://libgdx.com/) |
| Build tool | Gradle (scaffolded with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff)) |
| Map editor | [Tiled](https://www.mapeditor.org/) |
| Asset pipeline | LibGDX TexturePacker |
| IDE | IntelliJ IDEA |

## Project structure

```
TheLostIsland/
├── assets/
│   ├── sprites/
│   │   └── player/
│   │       ├── raw_frames/     # individual animation frames (source)
│   │       ├── player.atlas    # packed texture atlas
│   │       └── player.png
│   └── maps/
│       └── test/               # test level for movement/collision prototyping
├── core/                       # shared game logic (platform-independent)
│   └── src/main/java/.../
│       ├── Main.java           # entry point, extends Game
│       ├── Player.java         # movement, physics, animation state machine
│       ├── CollisionHandler.java
│       └── TestScreen.java
└── lwjgl3/                     # desktop launcher
    └── src/main/java/.../
        ├── Lwjgl3Launcher.java
        └── TexturePackerRunner.java  # one-off tool to (re)generate the atlas
```

## Running the project

1. Clone the repository
2. Open the project folder in IntelliJ IDEA (it will detect the Gradle build automatically)
3. Let Gradle sync
4. Run `Lwjgl3Launcher` (inside the `lwjgl3` module), or from the terminal:
   ```
   ./gradlew lwjgl3:run
   ```

## Controls (current test build)

| Key | Action |
|---|---|
| `A` / `D` | Move left / right |
| `Shift` | Run |
| `Space` / `W` | Jump |
| `S` / `Ctrl` | Duck (hold) |

## Credits

- Pixel art: custom character sprites, 32x32 base resolution
- Built solo as a portfolio project by [Gabriel Speziali](https://github.com/gabrielspeziali) <!-- adjust link if needed -->

## License

This project is licensed under the [MIT License](LICENSE).
