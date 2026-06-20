package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    // Credentials loaded from db.properties at runtime — never hardcoded.
    // This way the file can be gitignored and credentials stay off GitHub.
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    // Static block runs once when the class is first loaded.
    // Think of it like a one-time setup step before anyone calls getConnection().
    static {
        try {
            Properties props = new Properties();

            // Load db.properties from the classpath (works in Eclipse since
            // src/util/ is on the classpath — Eclipse copies it to bin/util/)
            InputStream input = DBConnection.class
                    .getClassLoader()
                    .getResourceAsStream("util/db.properties");

            if (input == null) {
                throw new RuntimeException(
                    "db.properties not found on classpath. " +
                    "Copy db.properties.example → db.properties and fill in your credentials.");
            }

            props.load(input);
            URL      = props.getProperty("db.url");
            USERNAME = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    // Private constructor — this is a utility class, no instances needed.
    private DBConnection() {}

    // Returns a fresh connection. Caller is responsible for closing it.
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}