package pl.serweron.adminSecure;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class AdminSecure extends JavaPlugin {

    private PinManager pinManager;
    private Database database;
    private MessagesManager messagesManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("AdminSecure is starting...");

        // Configuring the plugin
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        messagesManager = new MessagesManager(this);

        // Database
        String type = getConfig().getString("database.type", "sqlite").toLowerCase();
        String jdbcUrl = getConfig().getString("database.jdbc-url");
        String dbUser = getConfig().getString("database.username");
        String dbPass = getConfig().getString("database.password");
        database = new Database(this);

        try {
            if (type.equals("sqlite")) {
                File dbFile = new File(getDataFolder(), "database.db");
                if (!dbFile.exists()) {
                    dbFile.createNewFile();
                }
                jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            }

            database.connect(jdbcUrl, dbUser, dbPass);
        } catch (Exception e) {
            getLogger().severe("Could not connect to database! Plugin remains disabled.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        pinManager = new PinManager(database, this);

        // Registering the command
        ASCommand command = new ASCommand(this);
        getCommand("adminsecure").setExecutor(command);
        getCommand("adminsecure").setTabCompleter(command);

        // Registering the event listener
        getServer().getPluginManager().registerEvents(new ASListener(this), this);

        getLogger().info("AdminSecure has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.disconnect();
    }

    public PinManager getPinManager() {
        return pinManager;
    }
    public Database getDatabase() {
        return database;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}
