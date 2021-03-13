package com.udsl.peaktraining.migration;

import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

@Component
public class MigrationUtilities {
    private static final Logger logger = LogManager.getLogger(MigrationUtilities.class.getName());

    @Autowired
    private MSAccess mAccess ;

    @Value("${pauseActive:false}")
    static boolean pauseActive;

    private static final String COUNT_QUERY_SQL = "select count(*) from [?]";

    public int getRecordCount(String tableName) throws SQLException {
        try (ResultSet rsCount = mAccess.excuteSQL(COUNT_QUERY_SQL.replace("?", tableName))) {
            rsCount.next();
            int count = rsCount.getInt(1);
            logger.info("Table {} has {} records.", tableName, count);
            return count;
        }
    }

    public static void promptEnterKey(String prompt){
        logger.info("\n\n***** {} *****\n\n", prompt);
        if (pauseActive) {
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
        }
    }
}
