package pl.serweron.adminSecure;

import org.bukkit.plugin.java.JavaPlugin;

public class MessagesManager {
    private final JavaPlugin plugin;

    public MessagesManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String get(String key) {
        return (plugin.getConfig().getString("prefix") + plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key)).replace('&', '§');
    }

    public String pinNotSet() { return get("pin-not-set"); }
    public String pinSet() { return get("pin-set"); }
    public String pinReset() { return get("pin-reset"); }
    public String pinIncorrect() { return get("pin-incorrect"); }
    public String pinBanned() { return get("pin-banned"); }
    public String pinRestriction() { return get("pin-restriction").replace("{length}", plugin.getConfig().getInt("pin-restrictions.length", 4) + ""); }
    public String pinAttemptsExceeded() { return get("pin-attempts-exceeded"); }
    public String pinExpired() { return get("pin-expired"); }
    public String pinInputTitle() { return get("pin-input-title"); }
    public String notConsole() { return get("not_console"); }
    public String notExists() { return get("not_exists"); }
    public String reload() { return get("reload"); }
}
