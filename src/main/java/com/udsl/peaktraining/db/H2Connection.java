package com.udsl.peaktraining.db;

import com.udsl.peaktraining.Lookups;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.stereotype.Component;
import org.h2.tools.Server;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

@Component
public class H2Connection {
    private static final Logger logger = LogManager.getLogger(H2Connection.class.getName());

    private JdbcDataSource ds ;

    private Connection conn;
    private Server h2Server = null;
    private String h2Url;

     public H2Connection() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            if ("XENOSMILUS".equals(hostname) || "SABERTOOTH".equals(hostname)) { // Home
                h2Url = "jdbc:h2:tcp://localhost:9092/~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test";
            } else { // Peak Training
                h2Url = "jdbc:h2:tcp://localhost:9092/~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test";
            }

            h2Server = Server.createTcpServer().start();
          } catch (SQLException | UnknownHostException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("H2 Connection failure");
        }
        if (h2Server.isRunning(true)) {
            logger.info("H2 server was started and is running.");
        } else {
            throw new RuntimeException("Could not start H2 server.");
        }
        ds = new JdbcDataSource();
        ds.setURL(h2Url);
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
