package com.udsl.peaktraining.template;

import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.H2Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private static int currentCourseNum ;
    private static int inserted = 0;

    private static int TOM_HARDY = 5;
    private static int DAVE_HARDY = 1;

    private static final int CERT_TYPE_1 = 1;
    private static final int CERT_TYPE_2 = 2;
    private static final int CERT_TYPE_3 = 3;

    public boolean checkForPreviousImportData(){
        int importedCount = h2Conn.getSqlInt( "SELECT COUNT(*) FROM TEMPLATES_INSERTED") ;
        return importedCount > 0;
    }

    public boolean createTemplateDataCache(){
        logger.info("Creating template data cache");
        try {
            String currentMaxNum = dbConnection.getCourseDefMaxNumber();
            currentCourseNum = Integer.parseInt(currentMaxNum.substring(3)) + 1;
            logger.info("currentMaxNum: {} next: {}", currentMaxNum, currentCourseNum);
            addType1Certs();
            addType2Certs();
            addType3Certs();
            logger.info("Cache created with {} records", cache.size());
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("SQL exception! Unable to recover, terminating ...");
        }
    }

    // cert type 1 Site Safety
    private void addType1Certs(){
        List<String> elements = List.of("HEALTH AND SAFETY AT WORK ACT 1974 PROCEDURES",
                "RISK ASSESSMENTS AND METHOD STATEMENTS",
                "ANCHOR POINT AND FIXING INSPECTIONS",
                "VEHICLE LOADING AND UNLOADING PROCEDURES",
                "LOAD DISTRIBUTION AND OVERSIZED LOAD CALCULATIONS",
                "SECURE THE LOAD TECHNIQUES AND PROCEDURES",
                "PREPARATION OF VEHICLE FOR HIGHWAY RESTRICTIONS");
        addToCache(new TemplateData("VEHICLE LOADING & UNLOADING PROCEDURES", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));
    }

    // cert type 2 Various Plant
    private void addType2Certs(){
//        addToCache(new TemplateData("", "", "", 1, CERT_TYPE_2,  currentCourseNum++, DEFAULT_EXAMINER_ID));
        addToCache(new TemplateData("Fall Arrester", "SAFETY HARNESS TRAINING", "SITE SAFETY AWARENESS - BASIC HARNESS REGULATIONS", 1, CERT_TYPE_2,  currentCourseNum++, TOM_HARDY));
        addToCache(new TemplateData("WORKING @ HEIGHTS", "WORKING @ HEIGHTS SAFTY AWARENESS", "SITE SAFETY", 1, CERT_TYPE_2,  currentCourseNum++, TOM_HARDY));
        addToCache(new TemplateData("LOADING SHOVEL", "JCB436E LOADING SHOVEL", "SITE SAFETY AWARENESS - BASIC OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, TOM_HARDY));
        addToCache(new TemplateData("MOBILE SHREDDER", "MOBILE SHREDDER", "SITE SAFETY - BASIC OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, TOM_HARDY));
        addToCache(new TemplateData("HGV SHUNTER", "ON SITE HGV OPERATOR", "SITE SAFETY BASIC SHUNTING OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, TOM_HARDY));
    }

    // cert type 3 Lift Truck
    private void addType3Certs(){
        addToCache(new TemplateData("Rubber Duck", "JS 160W RUBBER DUCK - GRAB ATTACHMENT", 2, CERT_TYPE_3,  currentCourseNum++, TOM_HARDY));
    }

    private void addToCache( TemplateData data){
        cache.add(data);
        logger.info("Add to cache: {}", data);
    }

    private PreparedStatement insertedStmt = null ;
    private final static String INSERTED_SQL = "INSERT INTO TEMPLATES_INSERTED (TEMPLATE_NAME, POSTGRES_ID) VALUES (?, ?)";

    private void updateInserted(String templateName, int postgresId){
        try {
            if (insertedStmt == null) {
                insertedStmt = h2Conn.prepareStatement(INSERTED_SQL);
            }
            insertedStmt.setString(1, templateName);
            insertedStmt.setInt(2, postgresId);
            insertedStmt.executeUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean templateExists(TemplateData template){
        return dbConnection.importedTemplateExists(template);
    }

    public void writeCacheToPostgres() {
        for( TemplateData template: cache){
            if (!templateExists(template)) {
                int key = dbConnection.saveImportedTemplate(template);
                updateInserted(template.getName(), key);
                inserted++;
            }
            else{
                logger.info("Template exists - {}", template);
            }
        }
        logger.info("Templates inserted - {}", inserted);
    }
}
