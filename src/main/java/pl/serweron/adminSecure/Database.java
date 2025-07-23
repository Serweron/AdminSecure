package pl.serweron.adminSecure;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Database {
    private Connection sqlConnection = null;
    private String tablePrefix;
    private final AdminSecure plugin;

    public Database(AdminSecure plugin) {
        this.plugin = plugin;
        this.tablePrefix = plugin.getConfig().getString("database.table-prefix", "adminsecure_");
    }


    public void connect(String jdbc, String username, String password) throws SQLException {
        sqlConnection = DriverManager.getConnection(jdbc, username, password);
        if (sqlConnection == null || sqlConnection.isClosed() || !sqlConnection.isValid(2)) {
            throw new SQLException("Failed to connect to the database.");
        }
        plugin.getLogger().info("Connected to the database.");

        Statement stmt = sqlConnection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "pins (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "pin VARCHAR(60) NOT NULL, " +
                "expired_at TIMESTAMP NULL" +
                ")");

        stmt.close();

    }

    public void disconnect() {
        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return sqlConnection;
    }

    public void setPin(UUID uuid, String pin) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + tablePrefix + "pins (uuid, pin, expired_at) VALUES (?, ?, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET pin = excluded.pin, expired_at = excluded.expired_at"
            )) {
                ps.setString(1, uuid.toString());
                ps.setString(2, pin);
                ps.setLong(3, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Optional<String> getPin(UUID uuid) {
        try {
            PreparedStatement stmt  = sqlConnection.prepareStatement("SELECT pin FROM " + tablePrefix + "pins WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("pin"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void removePin(UUID uuid) {
        try {
            PreparedStatement stmt = sqlConnection.prepareStatement("DELETE FROM " + tablePrefix + "pins WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public boolean isPinExpired(UUID uuid) {
        try {
            PreparedStatement stmt = sqlConnection.prepareStatement("SELECT expired_at FROM " + tablePrefix + "pins WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp expiredAt = rs.getTimestamp("expired_at");
                return expiredAt != null && expiredAt.before(new Timestamp(System.currentTimeMillis()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

}
