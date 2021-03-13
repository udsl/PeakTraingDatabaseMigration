package com.udsl.peaktraining.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class MSAccess {
    private static final Logger logger = LogManager.getLogger(MSAccess.class.getName());

    @Value("${msaccess.showsql}")
    boolean showSQL;

    @Value("${msaccess.db}")
    String accessDb;

    private Connection conn = null;

    public Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection(
                    "jdbc:ucanaccess://"+accessDb);
        }
        return conn;
    }

    public Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    public ResultSet excuteSQL(String sql) throws SQLException {
        if (showSQL) {
            logger.info("MSAccess - {}", sql);
        }
        return getStatement().executeQuery( sql );
    }
}
