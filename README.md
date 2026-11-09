# GoldRushAC
Staff-focused anti-cheat monitoring for GoldRush Paper servers.

## Requirements
- Paper 1.21.11
- Java 21
- Maven 3.9+

## Build
Run `mvn clean package`. The jar is created in `target/goldrushac-1.0.0.jar`.

## Install
Back up the server, stop it, put the jar in `plugins/`, start Paper, then review `plugins/GoldRushAC/`.

## Commands
- `/sus <player>`: current violation levels, counts and latest evidence (online players).
- `/alerts` or `/ac alerts`: toggle staff alerts.
- `/ac debug <player>`: toggle verbose messages for a player.
- `/ac reload`: reload YAML configuration.
- `/ac info`: plugin version and check catalogue size.

## Permissions
`goldrushac.staff`, `goldrushac.sus`, `goldrushac.alerts`, `goldrushac.reload`, `goldrushac.bypass`.

## Safety / coverage
Automatic punishments are disabled. Initial event-based heuristics cover conservative movement speed/high-jump samples, attack reach, remote block break/interact and placement reach. Other enum/config entries are a roadmap, not implemented detectors yet. Paper events cannot see a client's local Freecam camera or reliably identify every client module. Review alerts before taking action.