package com.udsl.peaktraining;

import com.udsl.DataException;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class PeakTraingDataMigration {
    private static final Logger logger = LogManager.getLogger(PeakTraingDataMigration.class.getName());
    private static final Logger errorsLogger = LogManager.getLogger("errors-log");

    String[] knowDuplicates = {"IAN YOUNG", "GARRY SHAW ROOFING SERVICES", "EKSPAN", "DARREN EDWARDS"};

    Lookups lookups ;
    MSAccess mAccess ;

    public static void main(String[] args) throws SQLException {
        logger.info("Peak Training data migration starting . . .");
        PeakTraingDataMigration app = new PeakTraingDataMigration();
        try {
            app.createCompanys();
            app.createTrainees();
            app.createInstructorTrainers(); // Old system trainers and examiners where same list of people
            app.createCourseDef();
            app.createCourseInst();
            app.updateCourseDef();
            app.createNoneCertificate();
            app.processAttendants();
        } catch (Exception e) {
            logger.error("Caught exception {}", e.getMessage(), e);
        } finally {
            app.closeConection();
        }
        logger.info("END!");
    }

    PeakTraingDataMigration() throws SQLException {
        lookups = new Lookups();
        mAccess = new MSAccess();
    }

    void closeConection() throws SQLException {
        DbConnection.closeConection();
        lookups.closeConection();
    }
    /**
     * Create certificate zero. Used to idnicate the certificate was not transfered from the original system (because there was no record of it!).
     */
    private void createNoneCertificate() throws SQLException {
        DbConnection.createCertificateType();
        DbConnection.createCertificateDef();
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
                    errorsLogger.debug("Attendants record fond with DelegateID = 0, skipping - {}", DbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (lookups.getCourseInsId(rs.getInt("CourseID")) < 0){
                    errorsLogger.debug("Attendants record found with invalid CourseID, skipping - {}", DbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (rs.getString("Passed") == null){
                    errorsLogger.debug("Attendants record found with no result data, skipping - {}", DbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else if (lookups.getNewTrianeeId(rs.getInt("DelegateID")) < 0){
                    errorsLogger.debug("Attendants record found with invalid DelegateID, skipping - {}", DbConnection.resultsetFieldsAndValuesToString(rs));
                }
                else {
                    Attendants attendee = new Attendants(rs);
                    int attendeeId = DbConnection.saveAttendee(attendee, lookups);
                    attendee.setId(attendeeId);
                    lookups.addAttendee(attendee);
                    DbConnection.saveResults(attendee, lookups);
                }
            }
        }
    }

    private void createInstructorTrainers() throws SQLException {
        logger.debug("Processing {} Trainers records", getRecordCount("Trainers"));
        String sql = "SELECT [TrainerID], [Firstname], [Surname], [RegNumber] FROM [Trainers]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                InstructorExaminer trainer = new InstructorExaminer(rs);
                int trainerId = DbConnection.saveTrainer(trainer, lookups);
                trainer.setId(trainerId);
                lookups.addInstructor(trainer.getOldId(), trainer);
                InstructorExaminer examiner = new InstructorExaminer(rs);
                int examinerId = DbConnection.saveExaminer(examiner, lookups);
                examiner.setId(examinerId);
                lookups.addExaminer(examiner.getOldId(), examiner);
            }
        }
    }

    private void createCourseInst() throws SQLException {
        logger.debug("Processing {} BookedCourses records", getRecordCount("BookedCourses"));
        String sql = "SELECT [courseId], [TemplateCourseID], [CourseTemplateName], [CourseReference], [trainerID], [CourseVenue], [CourseStartDate], [CourseEndDate], [Examiner] FROM [BookedCourses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                CourseIns courseIns = new CourseIns(rs);
                int courseId = DbConnection.saveCourseIns(courseIns, lookups);
                courseIns.setId(courseId);
                lookups.addCourseIns(courseIns);
            }
        }
    }


    private void updateCourseDef() throws SQLException {
        logger.debug("Updating course definitions with next instance number");
        List<Integer> courseIds = lookups.getCourseDefIds();
        DbConnection.updateCourseDef(courseIds);
    }

    private void createCourseDef() throws SQLException {
        logger.debug("Processing {} courses records", getRecordCount("courses"));
        String sql = "SELECT [courseID], [Coursetitle], [certificatePrefix], [certificateCount] FROM [courses]";
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            while (rs.next()) {
                Course course = new Course(rs);
                int courseId = DbConnection.saveCourse(course);
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
                int id = DbConnection.saveCompany(company);
                company.setId(id);
                lookups.addCo(company);
                logger.info("Company created - {}", company.toString());
                try {
                    Contact contact = new Contact(id, rs);
                    int contactId = DbConnection.saveContact(contact);
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
                int traineeId = DbConnection.saveTrainee(trainee, lookups);
                trainee.setId(traineeId);
                logger.info("Trainee created - {}", trainee.toString());
                lookups.addTrainee(trainee);
            }
        }
    }

}
