# Packet Log

The Packet Log records every packet the proxy observes between the Minecraft client and the remote server, in the order
it happened. It lives in the ImGui debug overlay and can optionally stream to a file. Each entry keeps the raw wire
bytes plus the decoded packet's `toString()`, so you can inspect packets as hex, binary, or their decoded field dump.

## Opening it

1. Make sure debug resource packs are enabled (the overlay is part of the debug tooling).
2. Bring up the ImGui debug overlay - cycle the debug screens with **F3** (the overlay shows when you cycle past the
   last screen), or run the `/test imgui` command.
3. In the **Debug** window, click the **Packet Log** tab.

## Window layout

### Toolbar

| Control            | Description                                                                                                                           |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **Filter by name** | Case-insensitive substring match on the packet name. Only affects what the table shows - nothing is dropped from the log or the file. |
| **Pause**          | Temporarily stops capturing new packets. Existing entries stay.                                                                       |
| **Auto-scroll**    | Keeps the table pinned to the newest packet while you are at the bottom.                                                              |
| **Clear**          | Empties the in-memory list (does not touch any file).                                                                                 |
| **Log to file**    | Starts/stops writing captured packets to a `.log` file (see below).                                                                   |

The toolbar also shows the current packet count, the active log file path (when logging to file), and
`(capture disabled in settings)` when capture is turned off in the settings menu.

### Table

| Column   | Meaning                                                                                  |
|----------|------------------------------------------------------------------------------------------|
| **#**    | Monotonic sequence number in capture order.                                              |
| **Time** | Local time the packet was captured (`HH:mm:ss.SSS`).                                     |
| **Dir**  | Direction the packet actually travelled.                                                 |
| **ID**   | The packet id from the codec definition (falls back to the wire id for unknown packets). |
| **Name** | The packet's simple class name, e.g. `PlayerAuthInputPacket`.                            |
| **Size** | Raw packet frame size in bytes.                                                          |

Direction is colour-coded:

- `C->S` (green) - **serverbound**: sent from the client to the server.
- `S->C` (blue) - **clientbound**: sent from the server to the client.
- `C<->S` (yellow) - used for the *definition recipient* `BOTH` (a packet type allowed in both directions). This appears
  in the detail pane, not the per-packet Dir column, since any single captured packet only ever travels one way.

### Detail pane

Select a row to inspect it. The pane shows the name, id (decimal + hex), size, the observed direction, and the **Packet
Recipient** the codec declares for that packet type (`CLIENT`, `SERVER`, or `BOTH`).

Three view modes switch what the byte pane shows:

- **Hex** - a hex dump (offset, hex bytes, ASCII).
- **Binary** - eight bytes per line as base-2, with an ASCII gutter.
- **toString** - the decoded packet's `toString()`, i.e. every field. Long lines wrap to the pane width.

**Copy** puts whichever view is currently shown onto the clipboard.

## File logging

Enabling **Log to file** opens `data/logs/packets-<yyyyMMdd-HHmmss>.log` and appends every captured packet as it
arrives. Each entry is a header line, the decoded `toString()`, then the hex dump. All timestamps in the file - the
filename and each entry's header - are in **UTC**, so logs line up regardless of the machine's timezone.

Toggling the option off closes the file. Toggling it on again starts a new, separate file.

## Settings

Two settings control the log. Both live in `../data/settings.json`.

### Packet Log Enabled

- **Config key:** `packet_log_enabled` (boolean, default `true`)
- **Menu:** Settings form (**F7**) -> *Packet Log Enabled* toggle

A master switch for capture. When off, no packets are recorded in memory or written to file, and the overlay shows
`(capture disabled in settings)`. This is distinct from the overlay's own **Pause**
(temporary) and **Log to file** (file sink) controls. Changes take effect immediately.

### Exclusion list

- **Config key:** `packet_log_exclusion_list` (list of strings, default empty)
- Config file only - not exposed in the settings menu.

Packet class names listed here are never logged - not shown in the overlay and not written to file. Matching is
case-insensitive against the packet's simple class name, and excluded packets are skipped before their bytes are copied
or `toString()` is computed, so they cost almost nothing.

Example:

```json
{
  "packet_log_exclusion_list": [
    "PlayerAuthInputPacket",
    "LevelChunkPacket"
  ]
}
```

## Notes

- The in-memory log keeps the most recent **8192** entries; older ones are dropped (the file log is unbounded while
  enabled).
- `toString()` is captured at log time and wrapped in a guard, so a misbehaving packet cannot break capture.