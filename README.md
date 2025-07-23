# 🛡️ AdminSecure

**AdminSecure** is a lightweight and powerful Minecraft plugin designed to secure administrative commands with a PIN code. Ensure only authorized users can execute sensitive commands, even if their account is compromised.

> ✨ Protect your server from griefing and abuse by adding an extra layer of security.

---

## 🔐 Features

- 🔒 Encode PINs for secure storage (PINs are never saved in plain text)
- 🔢 Require PIN code before executing sensitive commands
- 📜 Whitelist only selected commands to protect
- 🧩 Compatible with most permission systems
- 🔄 Easy to configure and lightweight

---

## 📦 Installation

1. Download the plugin `.jar` file.
2. Place it in your `plugins` folder.
3. Restart or reload your server.
4. Configure the plugin using the `config.yml`.

---
## 📘 Commands

### 1. `/adminsecure`
The main command for managing the PIN system and plugin configuration.

- `help`  
  Displays a list of available commands and their descriptions.  
  **Usage:** `/adminsecure help`

- `pin`  
  Allows players to set or enter their PIN to unlock protected commands.  
  **Usage:** `/adminsecure pin <old_pin> <new_pin>`

- `reload`  
  Reloads the plugin configuration without restarting the server.  
  **Usage:** `/adminsecure reload`  
  **Permission required:** `adminsecure.reload`

- `reset <player>`  
  Resets the specified player's PIN.  
  **Usage:** `/adminsecure reset Notch`  
  **Permission required:** `adminsecure.reset`

---

Each of these commands helps secure your server and ensures only authorized users have access to sensitive actions.

## ⚙️ Configuration

```yaml
# config.yml

messages:
  pin-not-set: "&cYou have not set a PIN yet."
  pin-set: "&eYour PIN has been set successfully."
  pin-reset: "&ePIN has been reset successfully."
  pin-incorrect: "&cThe PIN you entered is incorrect. Please try again."
  pin-banned: "&cYou have been banned due to too many incorrect attempts."
  pin-restriction: "&cYour PIN must be exactly {length} characters long."
  pin-attempts-exceeded: "&cYou have exceeded the maximum number of attempts. Please try again later."
  pin-expired: "&cYour PIN has expired. Please set a new one."
  pin-input-title: "&eEnter PIN:"
  not_console: "&eYou cannot execute this command from the console."
  not_exists: "&cThis player does not exist."
  reload: "&6The configuration has been reloaded."

pin-restrictions:
  length: 4 # Length of the PIN
  max-attempts: 3 # Maximum number of attempts before a user is banned
  ban-time: 2 # in hours, how long the user is banned after exceeding the maximum number of attempts
  reset-cooldown: 30 # in days
  default-pin: "1234" # Default PIN for new users, can be changed by the user

database:
  type: "mysql" # Database type, can be "mysql" or "sqlite"
  jdbc-url: "jdbc:mysql://localhost:3306/adminsecure"
  username: "root"
  password: "secret"
  table-prefix: "adminsecure_"

pin-commands:
  - "ban"
