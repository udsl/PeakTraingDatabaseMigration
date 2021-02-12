package com.udsl.peaktraining;

import com.udsl.peaktraining.data.BookedCoursesContent;
import com.udsl.peaktraining.data.BookedCoursesRecord;
import com.udsl.peaktraining.db.H2Connection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class BookedCourseMigration {
    private static final Logger logger = LogManager.getLogger(BookedCourseMigration.class.getName());

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private Lookups lookups ;

    @Autowired
    H2Connection conn ;

    List<BookedCoursesRecord> bookedCourseList = new ArrayList<>();

    public void doMigration(){
        logger.info("Starting Booked Course Migration");
        try(Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE BOOKED_COURSE_MAP");
            stmt.execute("TRUNCATE TABLE BOOKED_COURSE_CONTENT");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (migratePopulateBookedCoursesMap()){
             logger.info("BookedCoursesMap populated.");
             if (migratePopulateBookedCourseContent()){
                 logger.info("BookedCoursesContent populated.");
             }
        }
    }

    private boolean migratePopulateBookedCourseContent(){
        logger.info("Starting migratePopulateBookedCourseContent");
        String sql = "SELECT [ElementId], [CourseID], [Value], [InputtedValue] FROM [BookedCourseContent] WHERE [CourseID] = %d";
        for (BookedCoursesRecord record: bookedCourseList){
            try (ResultSet rs = mAccess.excuteSQL(String.format(sql, record.getCourseId()))){
                if (rs.next()) {
                    BookedCoursesContent content =  new BookedCoursesContent(record.getCourseId());
                    content.persist(conn);
                    processCourseContentRecord(rs, content);
                    content.updateField(conn, "MAPPED_COURSE_ID", lookups.getMappedCourseInsId(content.getOriginalCourseId()));
                    content.updateField(conn, "EQUIPMENT", content.getEquipment());
                    content.updateField(conn, "MODEL", content.getModel());
                    content.updateField(conn, "CAPACITY", content.getCapacity());
                    content.updateField(conn, "ATTACHMENT", content.getAttachment());
                    content.updateField(conn, "COURSE_ELEMENTS", content.getCourseElements());
                }
            } catch (SQLException e) {
                logger.error(e);
                return false;
            }
        }
        return true;
    }

    private static final int[] attachmentValues = {11, 18, 28, 32, 47, 51, 55, 59};
    private static final int[] capacityValues = {10, 17, 27, 31, 38, 46, 50, 54, 58};
    private static final int[] modelValues = {9, 16, 30, 37, 45, 49, 53, 57};
    private static final int[] equipmentValues = {14, 20, 22};

    private void processCourseContentRecord(ResultSet rs, BookedCoursesContent content) throws SQLException {
        do{
            int elementId = rs.getInt("ElementId");
            String value = rs.getString("Value");
            String inputtedValue = rs.getString("InputtedValue");
            if (IntStream.of(attachmentValues).anyMatch(x -> x == elementId)){
                // attachment
                content.setAttachment(inputtedValue);
            }
            else if (IntStream.of(capacityValues).anyMatch(x -> x == elementId)){
                // capacity
                content.setCapacity(inputtedValue);
            }
            else if (IntStream.of(modelValues).anyMatch(x -> x == elementId)){
                // model
                content.setModel(inputtedValue);
            }
            else if (IntStream.of(equipmentValues).anyMatch(x -> x == elementId)){
                // equipment
                content.setEquipment(inputtedValue);
            }
            else{
                // free text
                content.addCourseElement(value + ": " + inputtedValue);
            }
        } while (rs.next()) ;
    }

    private boolean migratePopulateBookedCoursesMap(){
        logger.info("Starting migratePopulateBookedCoursesMap");
        String sql = "SELECT [CourseID], [TemplateCourseID], [CourseReference], [CourseStartDate] [CourseEndDate], [TrainerId], [Examiner] FROM [BookedCourses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                BookedCoursesRecord record = new BookedCoursesRecord(rs);
                if (record.persist(conn)) {
                    bookedCourseList.add(record);
                    record.updateField(conn, "ORIG_COURSE_ID", lookups.getMappedCourseInsId(record.getCourseId()));
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
