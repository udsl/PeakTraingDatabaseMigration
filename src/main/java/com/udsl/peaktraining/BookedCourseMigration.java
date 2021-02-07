package com.udsl.peaktraining;

import com.udsl.peaktraining.db.H2Connection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class BookedCourseMigration {
    private static final Logger logger = LogManager.getLogger(BookedCourseMigration.class.getName());

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private Lookups lookups ;

    @Autowired
    H2Connection conn ;

    public void doMigration(){
        logger.info("Starting Booked Course Migration");
        try(Statement stmt = conn.createStatement()) {
            stmt.execute(String.format("TRUNCATE TABLE BOOKED_COURSE_MAP"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (migratePopulateBookedCoursesMap()){
             logger.info("BookedCoursesMap populated.");
        }
    }

    private boolean migratePopulateBookedCoursesMap(){
        logger.info("Starting migratePopulateBookedCoursesMap");
        String sql = "SELECT [CourseID], [TemplateCourseID], [CourseReference], [CourseStartDate] [CourseEndDate], [TrainerId], [Examiner] FROM [BookedCourses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                BookedCoursesRecord record = new BookedCoursesRecord(rs);
                if (record.persist(conn)) {
                    record.updateField(conn, "COURSE_ID", lookups.getCourseInsId(record.getCourseId()));
                 }
                else{
                    logger.error("Failed to persist {}", record);
                }
             }
            return true;
        }
        catch (SQLException e){
            logger.error(e);
            return false;
        }
    }

    private void checkDataIntegrety() throws SQLException {
        logger.debug("Processing data integrety check");
        String sql = "SELECT [CertificateNo], [AttendentID], [CourseID], [CompanyID], [DelegateID], [DateIssued], [RenewalDate] FROM [CertificateWalletIssued]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            int missmatch = 0;
            while (rs.next()) {
                int attendentId = rs.getInt("AttendentID");
                int deligateId = rs.getInt("DelegateID");
                int companyId = rs.getInt("CompanyID");
                String attendabtSQL = String.format("SELECT [AttendantID], [CourseID], [CompanyID], [DelegateID] FROM [Attendants] WHERE [AttendantID] = %d",  attendentId) ;
                try (ResultSet atendeeRs = mAccess.excuteSQL(attendabtSQL)) {
                    while (atendeeRs.next()) {
                        int atDelId = atendeeRs.getInt("DelegateID");
                        int atCoId = atendeeRs.getInt("CompanyID");
                        logger.info("CertificateWalletIssued { DelegateID: {}, CompanyID {} }, Attendants {DelegateID: {}, CompanyID {} } ", deligateId, companyId, atDelId, atCoId);
                        if (deligateId != atDelId){
                            logger.info("DelegateID miss match!");
                            missmatch++;
//                            throw new RuntimeException("DelegateID miss match!");
                        }
                    }
                }
            }
            logger.info("Total miss match: {}", missmatch);
        }
    }
}
