package com.udsl.peaktraining.db;

import com.udsl.peaktraining.Lookups;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.stereotype.Component;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class H2Connection {
    private static final Logger logger = LogManager.getLogger(H2Connection.class.getName());

    private JdbcDataSource ds = new JdbcDataSource();

    private Connection conn;
    private Server h2Server = null;

    public H2Connection() {
        try {
            h2Server = Server.createTcpServer().start();
          } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("H2 Connection failure");
        }
        if (h2Server.isRunning(true)) {
            logger.info("H2 server was started and is running.");
        } else {
            throw new RuntimeException("Could not start H2 server.");
        }
        ds = new JdbcDataSource();
        // ds.setURL("jdbc:h2:~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test");
        ds.setURL("jdbc:h2:tcp://localhost:9092/~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test");
        ds.setUser("sa");
        ds.setPassword("sa");
        try {
            conn = ds.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void closeConection() {
        try {
            if (h2Server != null) {
                conn.close();
                conn = null ;
                h2Server.stop();
                h2Server.shutdown();
                h2Server = null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Statement createStatement(){
        try {
            return conn.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Failed to create statement.");
        }
    }

    public PreparedStatement prepareStatement(String sql){
        try {
            return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("Failed to create prepared statement.");
        }
    }
}
