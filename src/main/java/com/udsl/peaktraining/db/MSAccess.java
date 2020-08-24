package com.udsl.peaktraining.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;


public class MSAccess {
    private static final Logger logger = LogManager.getLogger(MSAccess.class.getName());

    private Connection conn = null;

    public Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection(
                    "jdbc:ucanaccess://C:\\Users\\iango\\Clients\\Peak Training\\Existing System\\Peak_Train.mdb");
        }
        return conn;
    }

    public Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    public ResultSet excuteSQL(String sql) throws SQLException {
        logger.info("MSAccess - {}", sql);
        return getStatement().executeQuery( sql );
    }
}
