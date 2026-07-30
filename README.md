# Hardcore Multiplayer (Spigot Plugin)
[![](https://jitpack.io/v/cykvta/HardcoreMP.svg)](https://jitpack.io/#cykvta/HardcoreMP)
[![](https://img.shields.io/badge/Spigot-HardcoreMP-ED8106?logo=spigotmc)](https://www.spigotmc.org/resources/122461/)

## Overview
This plugin adds a shared hardcore experience for Minecraft servers. If one player dies, the entire world is deleted, and a new world is created with a random seed.

## Features
- **Shared Hardcore Mode**: Players share a single hardcore experience. If any player dies, the current world is removed, and a new one is generated.
- **Random World Generation**: Each time the world is recreated, it uses a new random seed.
- **Async World Generation**: Worlds are generated in the background, so the server stays responsive while the next run is prepared.
- **Lives System**: Configure how many deaths the group gets before the world is reset.
- **Built-in Portals**: Nether and End portals are linked to the session worlds — no Multiverse required.
- **Lang Support**: Every message is configurable in the `config.yml` file.

## Installation
1. Download the plugin `.jar` file and place it in your server's `plugins` directory.
2. Restart your server to load the plugin and generate the default `config.yml`.
3. Set `lobby-world` in `config.yml` to the name of the world players wait in (defaults to `world`), then run `/hcmp reload`.

## Usage

### Commands
All commands require the `hardcoremp.command` permission. Aliases: `/hardcoremp`, `/hmp`.

| Command | Description |
| --- | --- |
| `/hcmp help` | Show the command list. |
| `/hcmp info` | Show details of the world you are in: seed, difficulty, spawn, time, weather, world type and remaining lives. |
| `/hcmp reload` | Reload `config.yml` without restarting the server. |

### How a run works
1. Start the server. Players wait in the lobby world while the game world is generated in the background.
2. Once the world is ready, players are moved into the shared hardcore session (overworld, nether and end).
3. When a player dies, the group loses a life. Players listed in `user-bypass-list` do not consume lives.
4. When lives reach zero, players are moved to spectator mode while the session worlds are deleted, a new world is generated with a random seed, and everyone is teleported into it.

### Configuration
Key options in `config.yml`:

| Option | Description |
| --- | --- |
| `lobby-world` | Name of the world players wait in while the game world is generated. |
| `remove-old-worlds` | Delete the old world folders after a reset. |
| `motd` | Show the session status in the server MOTD. |
| `user-bypass-list` | Players whose deaths do not trigger the death event. |
| `offline-player-inventory-clear` | Clear the inventory and ender chest of offline players on reset. |
| `max-lives` | Deaths allowed before the world is reset. |

> `create-time` and `reset-id` are written by the plugin. Do not edit them manually.

## Development
To use this plugin as a dependency in your own project.

Add the JitPack repository to your build file.
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency.
```xml
<dependency>
    <groupId>com.github.cykvta</groupId>
    <artifactId>HardcoreMP</artifactId>
    <version>{version}</version>
</dependency>
```

## Known Issues
- The plugin will throw an error if the default lobby world is not available. Ensure this world exists and is loaded properly.

## Support
If you have any issues or feature requests, please contact me.

