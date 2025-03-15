# Hardcore Multiplayer (Spigot Plugin)
[![](https://jitpack.io/v/cykvta/HardcoreMP.svg)](https://jitpack.io/#cykvta/HardcoreMP)
[![](https://img.shields.io/badge/Spigot-HardcoreMP-ED8106?logo=spigotmc)](https://www.spigotmc.org/resources/122461/)

## Overview
This plugin adds a shared hardcore experience for Minecraft servers. If one player dies, the entire world is deleted, and a new world is created with a random seed.

## Dependencies
Ensure the following plugins are installed and configured before using the Shared Hardcore Plugin:

- [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core)
- [Multiverse-NetherPortals](https://dev.bukkit.org/projects/multiverse-netherportals)

## Features
- **Shared Hardcore Mode**: Players share a single hardcore experience. If any player dies, the current world is removed, and a new one is generated.
- **Random World Generation**: Each time the world is recreated, it uses a new random seed.
- **World Regeneration**: Players are kicked from the server when a player dies, and the world is regenerated automatically.
- **Lang Support**: The plugin supports multiple languages configurable in the `config.yml` file.

## Installation
1. Download the plugin `.jar` file and place it in your server's `plugins` directory.
2. Ensure `Multiverse-Core` and `Multiverse-NetherPortals` are installed and working.
3. Restart your server to load the plugin.

## Usage
1. Start the server and ensure the lobby world is active.
2. Players can join the shared hardcore experience in the new generated world.
3. If a player dies, the current world will be deleted, and a new world will be generated automatically.

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

