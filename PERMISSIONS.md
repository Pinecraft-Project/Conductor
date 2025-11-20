# Bot Permission Nodes

This bot uses a Bukkit-like permission system layered on top of Discord roles. Nodes are checked via the internal PermissionService and can be granted to roles or individual members through groups. Below is a catalog of permission nodes referenced in the codebase and what they allow.

## Music Player

- music.ctrl: Access the player control panel and buttons.
- music.play: Queue tracks or playlists via `/player play`.
- music.pause: Pause/resume playback via `/player pause` or controls.
- music.skip: Skip current track via `/player skip` or controls.
- music.prev: Play previous track via `/player prev` or controls.
- music.next: Play next track via `/player next` or controls.
- music.jump: Jump to a position in queue via `/player jump` or controls.
- music.queue: View queue via `/player queue` or controls.
- music.np: View now-playing via `/player np`.
- music.mode: Change repeat mode (used by settings and controls).
- music.bye: Stop and disconnect via `/player bye` or controls.

### Personal Playlists

- music.pl.create: Create a new playlist.
- music.pl.add: Add a track to a playlist.
- music.pl.remove: Remove a track from a playlist.
- music.pl.delete: Delete a playlist.
- music.pl.list: List user playlists.
- music.pl.play: Play a saved playlist.
- music.pl.save: Save current queue as a playlist.

## Moderation

- mod.clear: Clear messages in a channel via `/clear`.

## Ranking

- rank.view: View rank/leaderboard commands.
- rank.manage: Manage rank stats and settings via admin rank commands.

## Permissions Admin

- perms.admin: Manage permission groups, mappings and checks via `perms-*` commands.

Notes:
- Discord Administrator permission always grants access as a fallback where indicated in code.
- Some actions in the music module support voting. Whether a vote is required can be configured per-user in settings.

