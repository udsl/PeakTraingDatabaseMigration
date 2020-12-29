package com.udsl.peaktraining;

import com.udsl.DataException;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class PeakTraingDataMigration  implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(PeakTraingDataMigration.class.getName());
    private static final Logger errorsLogger = LogManager.getLogger("errors-log");

    String[] knowDuplicates = {"IAN YOUNG", "GARRY SHAW ROOFING SERVICES", "EKSPAN", "DARREN EDWARDS"};

    Lookups lookups ;
    MSAccess mAccess ;
    DbConnection dbConnection;

    public static void main(String[] args) {
        SpringApplication.run(PeakTraingDataMigration.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Peak Training data migration starting . . .");
        try {
            PeakTraingDataMigration app = new PeakTraingDataMigration();
            app.run();
        } catch (Exception e) {
            logger.error("Caught exception {}", e.getMessage(), e);
        }
    }

    void run() throws SQLException{
        try {
            dbConnection.startTrans();
            createCompanys();
            createTrainees();
//            createInstructorsExaminers(); // Old system trainers and examiners where same list of people
            createCourseDef();
            createCourseInst();
            updateCourseDef();
            processAttendants();
            dbConnection.doCommit();
            logger.info("END!");
        }
        finally {
            closeConection();
        }
    }

    PeakTraingDataMigration() throws SQLException {
        lookups = new Lookups();
        mAccess = new MSAccess();
        dbConnection = new DbConnection();
    }

    void closeConection() throws SQLException {
        dbConnection.closeConection();
        lookups.closeConection();
    }

    private static final String COUNT_QUERY_SQL = "select count(*) from [?]";

    private int getRecordCount(String tableName) throws SQLException {
        try (ResultSet rsCount = mAccess.excuteSQL(COUNT_QUERY_SQL.replace("?", "Attendants"))) {
            rsCount.next();
            return rsCount.getInt(1);
        }
    }

    private void processAttendants() throws SQLException {
        logger.debug("Processing {} Attendants records", getRecordCount("Attendants"));
        String sql = "SELECT [AttendantID], [CourseID], [DelegateID], [Passed], [Theory] [PracticalFaults], [FailReason], [FurtherTraining]  FROM [Attendants]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                if (rs.getInt("DelegateID")==0){
                    errorsLogger.debug("Attendants record fond with DelegateID = 0, skipping - {}", dbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (lookups.getCourseInsId(rs.getInt("CourseID")) < 0){
                    errorsLogger.debug("Attendants record found with invalid CourseID, skipping - {}", dbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (rs.getString("Passed") == null){
                    errorsLogger.debug("Attendants record found with no result data, skipping - {}", dbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (lookups.getNewTrianeeId(rs.getInt("DelegateID")) < 0){
                    errorsLogger.debug("Attendants record found with invalid DelegateID, skipping - {}", dbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else {
                    Attendants attendee = new Attendants(rs);
                    logger.debug("Created attendee {}", attendee);
                    int attendeeId = dbConnection.saveAttendee(attendee, lookups);
                    attendee.setId(attendeeId);
                    lookups.addAttendee(attendee);
                    dbConnection.saveResults(attendee, lookups);
                }
            }
        }
    }

//    private void createInstructorsExaminers() throws SQLException {
//        logger.debug("Processing {} Instructor records", getRecordCount("Trainers"));
//        String sql = "SELECT [TrainerID], [Firstname], [Surname], [RegNumber] FROM [Trainers]";
//        try (ResultSet rs = mAccess.excuteSQL(sql)) {
//            while (rs.next()) {
//                InstructorExaminer trainer = new InstructorExaminer(rs);
//                int trainerId = dbConnection.saveTrainer(trainer, lookups);
//                trainer.setId(trainerId);
//                lookups.addInstructor(trainer.getOldId(), trainer);
//                InstructorExaminer examiner = new InstructorExaminer(rs);
//                int examinerId = dbConnection.saveExaminer(examiner, lookups);
//                examiner.setId(examinerId);
//                lookups.addExaminer(examiner.getOldId(), examiner);
//            }
//        }
//    }

    private void createCourseInst() throws SQLException {
        logger.debug("Processing {} BookedCourses records", getRecordCount("BookedCourses"));
        String sql = "SELECT [courseId], [TemplateCourseID], [CourseTemplateName], [CourseReference], [trainerID], [CourseVenue], [CourseStartDate], [CourseEndDate], [Examiner] FROM [BookedCourses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                CourseIns courseIns = new CourseIns(rs);
                int courseId = dbConnection.saveCourseIns(courseIns, lookups);
                courseIns.setId(courseId);
                lookups.addCourseIns(courseIns);
            }
        }
    }


    private void updateCourseDef() throws SQLException {
        logger.debug("Updating course definitions with next instance number");
        List<Integer> courseIds = lookups.getCourseDefIds();
        dbConnection.updateCourseDef(courseIds);
    }

    private void createCourseDef() throws SQLException {
        logger.debug("Processing {} courses records", getRecordCount("courses"));
        String sql = "SELECT [courseID], [Coursetitle], [certificatePrefix], [certificateCount] FROM [courses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                Course course = new Course(rs);
                int courseId = dbConnection.saveCourse(course);
                course.setId(courseId);
                lookups.addCourse(course);
            }
        }
    }

    void createCompanys() throws SQLException {
        logger.debug("Processing {} CompanySelfSponsored records", getRecordCount("CompanySelfSponsored"));
        String sql = "SELECT [companyID], [name], [address1], [address2], [address3], [address4], [postcode], [contact], [telephone], [mobile] FROM [CompanySelfSponsored] ORDER BY [name]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                logger.info("Processing '{}'", name);
                if (Arrays.asList(knowDuplicates).contains(name)) {
                    logger.info("Known duplicate '{}' found - checking to see if already added", name);
                    try {
                        int newId = lookups.getCompanyNewIdByName(name);
                        logger.info("Already added");
                        // Already added, save the ID and carry on
                        int duplicateCoID = rs.getInt("companyID");
                        lookups.addDupCoId(duplicateCoID, newId);
                        logger.info("Old ID {} maping to {} which is duplicate of id {}", duplicateCoID, newId, name);
                        continue;
                    }
                    catch (Exception e) {
                        logger.error("Exception looking up company name", e);
                    }
                }
                Company company = new Company(rs);
                int id = dbConnection.saveCompany(company);
                company.setId(id);
                lookups.addCo(company);
                logger.info("Company created - {}", company.toString());
                try {
                    Contact contact = new Contact(id, rs);
                    int contactId = dbConnection.saveContact(contact);
                    logger.info("Contact created - {}", contact.toString());
                } catch (DataException e) {
                    logger.error("Contact not created!");
                }
            }
            logger.info("While was false!");
        }
        catch(Exception e){
            logger.error("Exception!", e);
        }
    }

    void createTrainees() throws SQLException {
        logger.debug("Processing {} trainees records", getRecordCount("trainees"));
        String sql = "SELECT [DelegateId], [companyID], [DelegateFirstName], [DelegateSurname] FROM [trainees]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                Trainee trainee = new Trainee(rs);
                int traineeId = dbConnection.saveTrainee(trainee, lookups);
                trainee.setId(traineeId);
                logger.info("Trainee created - {}", trainee.toString());
                lookups.addTrainee(trainee);
            }
        }
    }

    String getSigFileName( String name){
        if ("THOMAS HARDY".equals(name)){
            return "thomas-hardy-sig.jpg";
        }
        else if ("DAVE HARDY".equals(name)){
            return "dave-hardy-sig.jpg";
        }
        else if ("MICK HUMPHRY".equals(name)){
            return "mick-sig.jpg";
        }
        else {
            return "blank-sig.jpg";
        }

    }
}
