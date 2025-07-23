package pl.serweron.adminSecure;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class ASListener implements Listener {

    private final AdminSecure plugin;
    private final Map<UUID, Integer> pinAttempts = new HashMap<>();
    private final Map<UUID, Integer> commandPinAttempts = new HashMap<>();

    public ASListener(AdminSecure plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!player.hasPermission("adminsecure.admin")) return;

        if (!plugin.getDatabase().getPin(uuid).isPresent()) {
            player.sendMessage(plugin.getMessagesManager().pinNotSet());
            forceNewPin(player);
        } else if (plugin.getPinManager().isPinExpired(uuid)) {
            player.sendMessage(plugin.getMessagesManager().pinExpired());
            verifyOldThenSetNewPin(player);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String msg = e.getMessage();
        UUID uuid = player.getUniqueId();

        for (String cmd : plugin.getConfig().getStringList("pin-commands")) {
            if (msg.toLowerCase().startsWith("/" + cmd.toLowerCase())) {
                if (!player.hasPermission("adminsecure.admin")) return;
                e.setCancelled(true);

                if (!plugin.getDatabase().getPin(uuid).isPresent()) {
                    player.sendMessage(plugin.getMessagesManager().pinNotSet());
                    forceNewPin(player);
                } else if (plugin.getPinManager().isPinExpired(uuid)) {
                    player.sendMessage(plugin.getMessagesManager().pinExpired());
                    verifyOldThenSetNewPin(player);
                }

                int maxAttempts = plugin.getConfig().getInt("pin-restrictions.max-attempts", 3);
                commandPinAttempts.putIfAbsent(uuid, 0);

                plugin.getPinManager().getPinFromPlayer(player).thenAccept(input -> {
                    if (plugin.getPinManager().checkPin(uuid, input)) {
                        commandPinAttempts.remove(uuid);
                        Bukkit.dispatchCommand(player, msg.substring(1));
                    } else {
                        int attempts = commandPinAttempts.get(uuid) + 1;
                        commandPinAttempts.put(uuid, attempts);

                        if (attempts >= maxAttempts) {
                            commandPinAttempts.remove(uuid);
                            banPlayer(player, plugin.getMessagesManager().pinBanned());
                        } else {
                            player.sendMessage(plugin.getMessagesManager().pinIncorrect());
                        }
                    }
                }).exceptionally(ex -> {
                    player.sendMessage(plugin.getMessagesManager().pinRestriction());
                    return null;
                });
                break;
            }
        }
    }

    private void verifyOldThenSetNewPin(Player player) {
        UUID uuid = player.getUniqueId();
        int maxAttempts = plugin.getConfig().getInt("pin-restrictions.max-attempts", 3);
        pinAttempts.putIfAbsent(uuid, 0);

        plugin.getPinManager().getPinFromPlayer(player).thenAccept(oldPin -> {
            if (plugin.getPinManager().checkPin(uuid, oldPin)) {
                pinAttempts.remove(uuid);
                forceNewPin(player);
            } else {
                int attempts = pinAttempts.get(uuid) + 1;
                pinAttempts.put(uuid, attempts);

                if (attempts >= maxAttempts) {
                    pinAttempts.remove(uuid);
                    banPlayer(player, plugin.getMessagesManager().pinBanned());
                } else {
                    player.sendMessage(plugin.getMessagesManager().pinIncorrect());
                    player.sendMessage(plugin.getMessagesManager().pinAttemptsExceeded());
                    verifyOldThenSetNewPin(player);
                }
            }
        }).exceptionally(ex -> {
            player.sendMessage(plugin.getMessagesManager().pinRestriction());
            return null;
        });
    }

    private void forceNewPin(Player player) {
        plugin.getPinManager().getPinFromPlayer(player).thenAccept(newPin -> {
            plugin.getPinManager().setPin(player.getUniqueId(), newPin);
            player.sendMessage(plugin.getMessagesManager().pinSet());
        }).exceptionally(ex -> {
            player.sendMessage(plugin.getMessagesManager().pinRestriction());
            return null;
        });
    }

    private void banPlayer(Player player, String reasonMsg) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, plugin.getConfig().getInt("pin-restrictions.ban-time", 2));
        player.ban(reasonMsg, calendar.getTime(), "AdminSecure", true);
    }

}
