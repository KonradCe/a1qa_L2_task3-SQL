package utils;

import aquality.selenium.core.logging.Logger;
import aquality.selenium.core.utilities.ISettingsFile;
import aquality.selenium.core.utilities.JsonSettingsFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

    private static final Logger logger = Logger.getInstance();
    private static final ISettingsFile dbCredentials = new JsonSettingsFile("DbCredentials.json");
    private static final ISettingsFile dbData = new JsonSettingsFile("ConfigData.json");

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(dbData.getValue("/dbUrl").toString(),
                        dbCredentials.getValue("/user").toString(),
                        dbCredentials.getValue("/password").toString());
                logger.info("Connected to the database");
            } catch (SQLException | ClassNotFoundException exception) {
                logger.error("Error while creating connection to db: " + exception.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
            connection = null;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
