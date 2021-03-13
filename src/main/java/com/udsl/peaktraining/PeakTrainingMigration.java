package com.udsl.peaktraining;

import com.udsl.DataException;
import com.udsl.peaktraining.data.*;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.MSAccess;
import com.udsl.peaktraining.migration.AttendeeMigration;
import com.udsl.peaktraining.migration.MigrationUtilities;
import com.udsl.peaktraining.validation.ReportFile;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.udsl.peaktraining.migration.MigrationUtilities.promptEnterKey;

@Component
public class PeakTrainingMigration {
    private static final Logger logger = LogManager.getLogger(PeakTrainingMigration.class.getName());

    private ApplicationContext context;

    String[] knowDuplicates = {"IAN YOUNG", "GARRY SHAW ROOFING SERVICES", "EKSPAN", "DARREN EDWARDS"};

    @Autowired
    private Lookups lookups ;

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    private BookedCourseMigration courseMigration;

    @Autowired
    MigrationUtilities migrationUtil;

    @Autowired
    AttendeeMigration attendeeMigration;

    @Value("${fullMigration}")
    private boolean fullMigration;

    @Value("${traineeReportFileName}")
    String traineeReportFileName;

    void runMigration() throws SQLException {
        try {
            if (fullMigration) {
                logger.info("Full migration");
                promptEnterKey("Company migration");
                createCompanys();
                promptEnterKey("Trainee migration");
                createTrainees();
                promptEnterKey("Course migration");
                createCourseDef();
                promptEnterKey("Course instance migration");
                createCourseInst();
                promptEnterKey("Updating course defs");
                updateCourseDef();
                attendeeMigration.processAttendants();
            }
            courseMigration.doMigration();
            logger.info("END!");
            System.exit(0);
        }
        finally {
            dbConnection.closeConnection();
        }
    }

    private void createCourseInst() {
        try {
            logger.debug("Processing {} BookedCourses records", migrationUtil.getRecordCount("BookedCourses"));
            String sql = "SELECT [courseId], [TemplateCourseID], [CourseTemplateName], [CourseReference], [trainerID], [CourseVenue], [CourseStartDate], [CourseEndDate], [Examiner] FROM [BookedCourses]";
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    CourseIns courseIns = new CourseIns(rs);
                    int courseId = dbConnection.saveCourseIns(courseIns, lookups);
                    courseIns.setId(courseId);
                    lookups.addCourseIns(courseIns);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void updateCourseDef() {
        try {
            logger.debug("Updating course definitions with next instance number");
            List<Integer> courseIds = lookups.getCourseDefIds();
            dbConnection.updateCourseDef(courseIds);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void createCourseDef() {
        try {
            logger.debug("Processing {} courses records", migrationUtil.getRecordCount("courses"));
            String sql = "SELECT [courseID], [Coursetitle], [certificatePrefix], [certificateCount] FROM [courses]";
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    Course course = new Course(rs);
                    int courseId = dbConnection.saveCourse(course);
                    course.setId(courseId);
                    lookups.addCourse(course);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void createCompanys() {
        try {
            logger.debug("Processing {} CompanySelfSponsored records", migrationUtil.getRecordCount("CompanySelfSponsored"));
            String sql = "SELECT [companyID], [name], [address1], [address2], [address3], [address4], [postcode], [contact], [telephone], [email], [mobile] FROM [CompanySelfSponsored] ORDER BY [name]";
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
                        } catch (Exception e) {
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
            } catch (Exception e) {
                logger.error("Exception!", e);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void createTrainees(){
        ReportFile reportFile = new ReportFile(traineeReportFileName);
        try {
            logger.debug("Processing {} trainees records", migrationUtil.getRecordCount("trainees"));
             String sql = "SELECT [DelegateId], [companyID], [DelegateFirstName], [DelegateSurname] FROM [trainees]";
            reportFile.write(sql);
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    String.format("Data read: DelegateId - %d, companyID - %d, DelegateFirstName - '%s', DelegateSurname - '%s'",
                            rs.getInt("DelegateId"),
                            rs.getInt("companyID"),
                            rs.getString("DelegateFirstName"),
                            rs.getString("DelegateSurname"));
                    Trainee trainee = new Trainee(rs);
                    int traineeId = dbConnection.saveTrainee(trainee, lookups);
                    trainee.setId(traineeId);
                    reportFile.write(trainee.toString());
                    lookups.addTrainee(trainee);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
