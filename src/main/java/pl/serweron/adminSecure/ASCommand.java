package pl.serweron.adminSecure;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ASCommand implements CommandExecutor, TabCompleter {

    private final AdminSecure plugin;

    public ASCommand(AdminSecure plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help();
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "pin":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(plugin.getMessagesManager().notConsole());
                        return true;
                    }

                    if (args.length == 3) {
                        Player player = (Player) sender;
                        String oldPin = args[1];
                        String newPin = args[2];
                        if (plugin.getPinManager().checkPin(player.getUniqueId(), oldPin)) {
                            plugin.getPinManager().setPin(player.getUniqueId(), newPin);
                            sender.sendMessage(plugin.getMessagesManager().pinSet());
                        } else {
                            sender.sendMessage(plugin.getMessagesManager().pinIncorrect());
                        }
                    } else {
                        sender.sendMessage("§cUsage: /adminsecure pin <oldPin> <newPin>");
                    }
                    return true;
                case "reset":
                    if (args.length == 2 && sender.isOp()) {
                        String playerName = args[1];
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                            sender.sendMessage(plugin.getMessagesManager().notExists());
                            return true;
                        }

                        plugin.getDatabase().removePin(offlinePlayer.getUniqueId());
                        sender.sendMessage(plugin.getMessagesManager().pinReset());
                    } else {
                        sender.sendMessage("§cUsage: /adminsecure reset <player>");
                    }
                    return true;
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage(plugin.getMessagesManager().reload());
                    return true;
                case "help":
                    help();
                    return true;
                default:
                    sender.sendMessage("§cUnknown command. Use /adminsecure help for a list of commands.");
                    return false;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        } else {
            return List.of("pin", "reset", "reload", "help");
        }
    }

    private void help() {
        plugin.getLogger().info("§eAdminSecure Commands:");
        plugin.getLogger().info("§e/adminsecure pin <oldPin> <newPin> - manage your admin pin.");
        plugin.getLogger().info("§e/adminsecure reset <player> - Reset admin pin.");
        plugin.getLogger().info("§e/adminsecure reload - Reload the plugin configuration.");
        plugin.getLogger().info("§e/adminsecure help - Show this help message.");
    }
}
