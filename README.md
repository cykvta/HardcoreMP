# Hardcore Multiplayer (Spigot Plugin)

## Overview

This plugin adds a shared hardcore experience for Minecraft servers, allowing friends to play together in a high-stakes environment. If one player dies, the entire world is deleted, and a new world is created with a random seed.

### Default Behavior

- The default lobby world is named `world`. If this world does not exist, the plugin will throw an error.
- The plugin requires specific dependencies to function correctly. See the list below.

## Dependencies

Ensure the following plugins are installed and configured before using the Shared Hardcore Plugin:

- [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core)
- [Multiverse-NetherPortals](https://dev.bukkit.org/projects/multiverse-netherportals)

## Features

- **Shared Hardcore Mode**: Players share a single hardcore experience. If any player dies, the current world is removed, and a new one is generated.
- **Random World Generation**: Each time the world is recreated, it uses a new random seed.
- **Lobby Integration**: Players are sent to the default lobby (`world`) when the main hardcore world is deleted.

## Installation

1. Download the plugin `.jar` file and place it in your server's `plugins` directory.
2. Ensure `Multiverse-Core` and `Multiverse-NetherPortals` are installed and working.
3. Restart your server to load the plugin.

## Usage

1. Start the server and ensure the lobby world (`world`) is active.
2. Players can join the shared hardcore experience in the new generated world.
3. If a player dies, the current world will be deleted, and a new world will be generated automatically.

## GitHub Packages Repository

To use this plugin as a dependency in your own project, add the following repository and dependency to your `pom.xml`:
```xml
<dependency>
  <groupId>icu.cykuta</groupId>
  <artifactId>hardcore-mp</artifactId>
  <version>2.0-SNAPSHOT</version>
</dependency>
```

## Known Issues

- The plugin will throw an error if the default lobby world (`world`) is not available. Ensure this world exists and is loaded properly.

## Support

If you encounter any issues or have feature requests, please contact me.

