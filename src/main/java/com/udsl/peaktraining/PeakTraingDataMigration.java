package com.udsl.peaktraining;

import com.udsl.DataException;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Arrays;
import java.util.Optional;


public class PeakTraingDataMigration {
    private static final Logger logger = LogManager.getLogger(PeakTraingDataMigration.class.getName());

    String knowDuplicates[] = {"IAN YOUNG", "GARRY SHAW ROOFING SERVICES", "EKSPAN", "DARREN EDWARDS"};

    Lookups lookups = new Lookups();
    MSAccess mAccess = new MSAccess();

    public static void main(String[] args) throws SQLException {
        try {
            PeakTraingDataMigration app = new PeakTraingDataMigration();
            DbConnection.startTrans();
            app.createCompanys();
            app.createTrainees();
            app.createTrainers(); // Old system trainers and examiners where same list of people
            app.createCourseDef();
            app.createCourseInst();
        }
        finally{
            DbConnection.doRollback();
        }
    }

    private void createTrainers() throws SQLException {
        String sql = "SELECT [TrainerID], [Firstname], [Surname], [RegNumber] FROM [Trainers]";
        ResultSet rs = mAccess.excuteSQL(sql);
        while (rs.next()) {
            InstructorExaminer trainer = new InstructorExaminer(rs);
            int trainerId = DbConnection.saveTrainer(trainer, lookups);
            trainer.setId(trainerId);
            lookups.addTrainer(trainer.getOldId(), trainer);
            InstructorExaminer examiner = new InstructorExaminer(rs);
            int examinerId = DbConnection.saveExaminer(examiner, lookups);
            examiner.setId(examinerId);
            lookups.addExaminer(examiner.getOldId(), examiner);
        }
    }

    private void createCourseInst() throws SQLException {
        String sql = "SELECT [courseId], [TemplateCourseID], [CourseTemplateName], [CourseReference], [trainerID], [CourseVenue], [CourseStartDate], [CourseEndDate], [Examiner] FROM [BookedCourses]";
        ResultSet rs = mAccess.excuteSQL(sql);
        while (rs.next()) {
            CourseIns courseIns = new CourseIns(rs);
            int courseId = DbConnection.saveCourseIns(courseIns, lookups);
            courseIns.setId(courseId);
            lookups.addCourseIns(courseIns.getOldId(), courseIns);
        }
    }

    private void createCourseDef() throws SQLException {
        String sql = "SELECT [courseID], [Coursetitle], [certificatePrefix], [certificateCount] FROM [courses]";
        ResultSet rs = mAccess.excuteSQL(sql);
        while (rs.next()) {
            Course course = new Course(rs);
            int courseId = DbConnection.saveCourse(course, lookups);
            course.setId(courseId);
            lookups.addCourse(course.getOldId(), course);
        }
    }

    void createCompanys() throws SQLException {
        String sql = "SELECT [companyID], [name], [address1], [address2], [address3], [address4], [postcode], [contact], [telephone], [mobile] FROM [CompanySelfSponsored] ORDER BY [name]";
        ResultSet rs = mAccess.excuteSQL(sql);
        while (rs.next()) {
            String name = rs.getString("name");
            boolean a = Arrays.stream(knowDuplicates).anyMatch(n -> (name.equals(n)));
            if (Arrays.stream(knowDuplicates).anyMatch(n -> (name.equals(n)))){
                logger.info("Known duplicate '{}' found - checking to see if already added", name);
                Optional<Company> co = lookups.getCompany(name);
                if (co.isPresent()){
                    logger.info("Already added");
                    // Already added, save the ID and carry on
                    int duplicateCoID = rs.getInt("companyID");
                    lookups.addDupCoId(duplicateCoID, co.get().getId());
                    logger.info("Old ID {} maping to {} which is duplicate of id {}", duplicateCoID, co.get().getId(), co.get().getName());
                    continue;
                }
            }
            Company company = new Company(rs);
            int id = DbConnection.saveCompany(company);
            company.setId(id);
            lookups.addCo(company.getOldId(), company);
            logger.info("Company created - {}", company.toString());
            try {
                Contact contact = new Contact(id, rs);
                int contactId = DbConnection.saveContact(contact);
                logger.info("Contact created - {}", contact.toString());
            } catch (DataException e) {
                logger.error("Contact not created!");
            }
        }
    }

    void createTrainees() throws SQLException {
        String sql = "SELECT [companyID], [DelegateFirstName], [DelegateSurname] FROM [trainees]";
        ResultSet rs = mAccess.excuteSQL(sql);
        while (rs.next()) {
            Trainee trainee = new Trainee(rs);
            int traineeId = DbConnection.saveTrainee(trainee, lookups);
            trainee.setId(traineeId);
            logger.info("Trainee created - {}", trainee.toString());
            lookups.addTr(trainee.getOldId(), trainee);
        }
    }

}
