# Security Audit: Quasar Companion Client Mod

**Audit Date:** March 2025  
**Context:** Open-source client JAR acting as companion to a **private** server-side mod  
**Primary Concerns:** Privilege elevation on servers, risks to other users, client modification/exploitation

---

## Executive Summary

The Quasar Companion client is a Minecraft 1.20.1 mod that adds custom chat channels, party chat, and admin tools. As an open-source, downloadable JAR, it will be trivially modifiable and reverse-engineerable. **The server-side mod is the critical security boundary**—all privilege checks and data validation must be enforced server-side. This audit identifies client-side risks and server-side assumptions that must hold.

---

## 1. Privilege Elevation Risks

### 1.1 Client-Side `isAdmin` Flag (CRITICAL – Server Must Enforce)

**Location:** `QCompanionMod.isAdmin` (line 33)

```java
public static boolean isAdmin = false;
// Set from server: isAdmin = o.has("isAdmin") && o.get("isAdmin").getAsBoolean();
```

**Risk:** A modified client can trivially set `isAdmin = true` (e.g., via bytecode patching, a wrapper mod, or debugger). This only controls **UI visibility**—whether the user sees TP, Kick, Ban, and Argus buttons.

**Mitigation:** The server **must** enforce all permissions. Commands sent via `sendCommand()` (`tp`, `kick`, `/q ban`, etc.) must be validated server-side. A modified client can always send these commands directly; the client-side `isAdmin` check is purely cosmetic.

**Server-side requirements:**
- Never trust the client for permission state
- Validate every `tp`, `kick`, `/q ban`, `/q ignore`, `/q unignore` against the player’s actual permissions
- Reject unauthorized commands with an appropriate response

### 1.2 Raw Command Sending (HIGH)

**Location:** `QChatScreen.handleChatInput()` (lines 86–91)

```java
if (input.startsWith("/")) {
    this.minecraft.player.connection.sendCommand(input.substring(1));
}
```

**Risk:** Any command starting with `/` is sent to the server as-is. A modified or malicious user can send arbitrary commands (e.g. `/op`, `/give`, `/execute`). The client does not restrict or whitelist commands.

**Mitigation:** The server must enforce command permissions. Vanilla Minecraft already restricts most dangerous commands; custom commands and mod commands must be permission-checked server-side.

---

## 2. Command Injection & Argument Manipulation

### 2.1 Username from Server-Controlled Click Events (MEDIUM)

**Location:** `QChatScreen.mouseClicked()` (lines 157–199)

```java
String username = clickEvent.getValue().substring(6).split(" ")[0];
// Used in: /tell, /q ignore, /q unignore, tp, /kick, /q ban
```

**Risk:** The click event value is server-controlled. A malicious server could craft values like:
- `/tell victim%20/tp%20@s` → username could be `victim/tp @s` (split on space gives `victim/tp`)
- Usernames with spaces: `split(" ")[0]` takes only the first token, but the rest could be crafted for injection

When the user clicks "TP", "Kick", or "Ban", the username is concatenated into commands:
```java
sendCommand("tp " + username);
sendCommand("/kick " + username + " ");
sendCommand("/q ban " + username + " ");
```

**Mitigation:**
- **Client:** Validate usernames (e.g. alphanumeric + underscore, max length 16) before using in commands
- **Server:** Always validate and sanitize command arguments; never trust client-provided usernames for permission checks or targeting

### 2.2 Server-Controlled `sendCommand` Format String (MEDIUM)

**Location:** `QChatScreen.handleChatInput()` (line 91)

```java
this.minecraft.player.connection.sendCommand(
    QCompanionMod.getSelectedChannel().sendCommand.formatted(input).substring(1)
);
```

**Risk:** `sendCommand` comes from `QCChatChannel`, which is deserialized from server JSON. If the server sends `sendCommand = "say %s"`, user input is interpolated. A malicious or buggy server could use format strings that allow injection (e.g. `%s` with input containing `" "` to add extra arguments).

**Mitigation:**
- **Server:** Use fixed, safe format strings; validate channel `sendCommand` templates
- **Client:** Consider avoiding `formatted()` with user input; use explicit concatenation with validated input

### 2.3 NullPointerException / Crash (LOW)

**Location:** Line 91

If `getSelectedChannel()` returns `null` (e.g. channel was closed but `selectedChannel` wasn’t reset), `.sendCommand` causes an NPE and client crash.

**Mitigation:** Add a null check and fall back to `"*"` or raw chat.

---

## 3. Custom Protocol & Message Spoofing

### 3.1 Opaque API Package (HIGH for Audit)

**Location:** `common/src/main/java/anar4732/quasar/api` is in `.gitignore`

The network layer (`QCompanionNetworkManager`, `QCChatChannel`, `QCPlayerMessage`) is not in the repository. From usage:

- `createObject("init")` – sends version on login
- `createObject("closeChannel")` – sends channel name
- `sendMessage(JsonObject)` – sends arbitrary JSON

**Risk:** A modified client can:
1. Send arbitrary message types (e.g. fake "admin" or "auth" messages)
2. Send malformed or oversized payloads
3. Spoof `closeChannel` for channels the user doesn’t own
4. Omit or alter the `init` version

**Mitigation:**
- **Server:** Validate every message type; reject unknown types; validate all fields (channel ownership, etc.)
- **Open source:** Include the API package in the repo for full auditability; document the protocol

### 3.2 Version Spoofing (LOW)

**Location:** `QCompanionMod.onLogin()` (lines 39–42)

```java
JsonObject o = QCompanionNetworkManager.createObject("init");
o.addProperty("version", VERSION);
```

**Risk:** A modified client can report a fake version. If the server uses this for compatibility or feature flags, it could be bypassed.

**Mitigation:** Don’t rely on client-reported version for security decisions; use it only for compatibility warnings.

---

## 4. Deserialization & Data Handling

### 4.1 GSON Deserialization of Server Data (MEDIUM)

**Location:** `QCompanionMod.init()` (lines 68–82)

Server JSON is deserialized with GSON into `Set<UUID>`, `QCChatChannel`, `QCPlayerMessage`, etc. No schema validation or type safety beyond GSON’s defaults.

**Risk:**
- Malformed JSON can cause exceptions or unexpected behavior
- Deeply nested or large payloads could cause resource exhaustion
- `Component.Serializer.fromJson()` for chat components—untrusted JSON could trigger parsing edge cases

**Mitigation:**
- Validate payload size limits
- Use strict deserialization; catch and log exceptions without crashing
- Consider schema validation for critical structures

### 4.2 Empty `displayName` Crash (LOW)

**Location:** `QChatScreen.init()` (line 60)

```java
Component.literal(isCollapsed ? c.displayName.substring(0, 1) : c.displayName)
```

If `displayName` is empty, `substring(0, 1)` throws `StringIndexOutOfBoundsException`.

**Mitigation:** Check `displayName` length before `substring`.

---

## 5. External URL Handling

**Location:** `QChatScreen` (line 194)

```java
Util.getPlatform().openUri("https://spy.playcdu.co/player/" + pi.getProfile().getId());
```

**Risk:** The UUID comes from `PlayerInfo` (server-provided). A malicious server could influence which UUID is used. The base URL is fixed, so the main risk is opening an unexpected player profile. Low severity for privilege elevation.

---

## 6. Configuration & Persistence

**Location:** `QConfig.java`

- Config path: `{config}/quasar_client/config.json`
- Stored data: `collapsedTabs` (channel names)
- No credentials stored
- `setLenient()` and `disableHtmlEscaping()` make JSON parsing more permissive—acceptable for local config

---

## 7. Access Widener & Mixins

The access widener exposes Minecraft internals (e.g. `ChatComponent` fields). This is necessary for the mod’s functionality. Other mods depending on Quasar could use the same access. No direct security impact for privilege elevation.

---

## 8. Summary: Server-Side Checklist

| Check | Description |
|-------|-------------|
| ✅ | Validate permissions for every command (`tp`, `kick`, `/q ban`, `/q ignore`, etc.) |
| ✅ | Never trust `isAdmin` or any client state for authorization |
| ✅ | Validate and sanitize all command arguments (usernames, channel names) |
| ✅ | Validate all custom protocol messages; reject unknown types |
| ✅ | Enforce ownership for `closeChannel` (user can only close their own channels) |
| ✅ | Apply size limits on incoming JSON/channel data |
| ✅ | Don’t use client-reported version for security decisions |

---

## 9. Client-Side Hardening Recommendations

1. **Null check:** Validate `getSelectedChannel()` before use; fall back to `"*"` or safe behavior.
2. **Username validation:** Validate usernames (e.g. `[a-zA-Z0-9_]{1,16}`) before using in commands.
3. **Display name:** Check for empty `displayName` before `substring(0, 1)`.
4. **API package:** Include `anar4732.quasar.api` in the repository for full auditability.
5. **Format string:** Avoid using server-controlled `sendCommand` as a format string with user input; prefer validated concatenation.

---

## 10. Threat Model Summary

| Attacker | Capability | Defense |
|----------|------------|---------|
| Modified client | Set `isAdmin = true`, send any command | Server enforces all permissions |
| Modified client | Spoof custom protocol messages | Server validates message types and fields |
| Malicious server | Grant admin UI, craft malicious click events | Client validates usernames; server is trusted for UI state |
| Malicious server | Send malformed JSON | Client handles exceptions; server should validate its own output |

**Bottom line:** The client is inherently untrustworthy. All security guarantees depend on the server-side mod correctly validating permissions, commands, and protocol messages.
