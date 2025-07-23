package pl.serweron.adminSecure;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PinManager {
    private final Database database;
    private final AdminSecure plugin;

    public PinManager(Database database, AdminSecure plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    public boolean checkPin(UUID uuid, String pin) {
        Optional<String> stored = database.getPin(uuid);
        return stored.isPresent() && BCrypt.checkpw(pin, stored.get());
    }


    public void setPin(UUID uuid, String newPin) {
        String hashedPin = BCrypt.hashpw(newPin, BCrypt.gensalt());
        database.setPin(uuid, hashedPin);
    }

    public CompletableFuture<String> getPinFromPlayer(Player player) {
        CompletableFuture<String> future = new CompletableFuture<>();

        String pinLengthError = plugin.getMessagesManager().pinRestriction();
        int pinLength = plugin.getConfig().getInt("pin-restrictionss.length", 4);

        new AnvilGUI.Builder()
                .onClose(p -> {
                    future.completeExceptionally(new RuntimeException(pinLengthError));
                })
                .onClick((slot, stateSnapshot) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return AnvilGUI.Response.close();
                    }
                    String input = stateSnapshot.getText();
                    if (input == null || input.isEmpty() || input.length() < pinLength) {
                        future.completeExceptionally(new RuntimeException(pinLengthError));
                        return AnvilGUI.Response.close();
                    }
                    future.complete(input);
                    return AnvilGUI.Response.close();
                })
                .text("")
                .title(plugin.getMessagesManager().pinInputTitle())
                .plugin(plugin)
                .open(player);

        return future;
    }

    public boolean isPinExpired(UUID uuid) {
        return database.isPinExpired(uuid);
    }
}
