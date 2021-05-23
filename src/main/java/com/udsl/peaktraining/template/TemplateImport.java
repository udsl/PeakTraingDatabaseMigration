package com.udsl.peaktraining.template;

import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.H2Connection;
import com.udsl.peaktraining.migration.MigrationUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateImport {
    private static final Logger logger = LogManager.getLogger(TemplateImport.class.getName());

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    H2Connection h2Conn ;

    private static final List<TemplateData> cache = new ArrayList<>();

    public boolean checkForPreviousImportData(){
        int importedCount = h2Conn.getSqlInt( "SELECT COUNT(*) FROM TEMPLATES_INSERTED") ;
        return importedCount > 0;
    }

    public boolean createTemplateDataCache(){
        logger.info("Creating template data cache");
        try {
            String currentMaxNum = dbConnection.getCourseDefMaxNumber();
            int currentCourseNum = Integer.parseInt(currentMaxNum.substring(3)) + 1;
            logger.info("currentMaxNum: {} next: {}", currentMaxNum, currentCourseNum);
            TemplateData rubberDuck = new TemplateData("Rubber Duck", 2, 3, "PT:" + currentCourseNum++);
            rubberDuck.setDescription("JS 160W RUBBER DUCK - GRAB ATTACHMENT");
            addToCache(rubberDuck);
            logger.info("Cache created with {} records", cache.size());
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("SQL exception! Unable to recover, terminating ...");
        }
    }

    private void addToCache( TemplateData data){
        cache.add(data);
        logger.info("Addto to cache: {}", data);
    }

    private PreparedStatement insertedStmt = null ;
    private final static String INSETRED_SQL = "INSERT INTO TEMPLATES_INSERTED (TEMPLATE_NAME, POSTGRES_ID) VALUES (?, ?)";

    private void updateInserted(String templateName, int postgresId){
        try {
            if (insertedStmt == null) {
                insertedStmt = h2Conn.prepareStatement(INSETRED_SQL);
            }
            insertedStmt.setString(1, templateName);
            insertedStmt.setInt(2, postgresId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void writeCacheToPostgres() {
        for( TemplateData template: cache){
            int key = dbConnection.saveImportedTemplate(template);
            updateInserted(template.getName(), key);
        }
    }
}
