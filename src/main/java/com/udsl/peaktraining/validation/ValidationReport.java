package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.H2Connection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ValidationReport {
    private static final Logger logger = LogManager.getLogger(ValidationReport.class.getName());

    @Autowired
    H2Connection h2Conn;

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    private Lookups lookups ;

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private ValidationAccessUtilities accessUtilities;

    @Value("${validationsRequired}")
    String[] validationsRequired;

    @Value("${h2AttendeesReport}")
    String h2AttendeesReportFile;

    @Value("${h2ACourseReport}")
    String h2ACourseReportFile;


    public void doReport(){
        logger.info("doing report for {}", String.join(", ", validationsRequired));
        for(String report: validationsRequired){
            switch (report){
                case "attendants":
                    doAttendantsReport();
                    break;
                case "companies":
                    doCompaniesReport();
                    break;
                case "course":
                    doCourseReport();
                    break;
                default:
                    logger.error("Undefined report '{}' requested.", report);
            }
        }
    }

    private void doCourseReport() {
        try {
            logger.info("Producing Attendants report.");
            Statement stmt = h2Conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM COURSE_INS");
            if (rs != null) {
                if (rs.next()) {
                    int courseInsCount = rs.getInt(1);
                    boolean h2DataPresent = courseInsCount > 0;
                    // If we have H2 data we can iterate that list
                    if (h2DataPresent) {
                        iterDateH2Course(courseInsCount);
                    }
                }
            } else {
                logger.error("NO H2 DATA.");
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void iterDateH2Course(int courseInsCount) {
        int validationFails = 0;
        int validationsGood = 0;
        File file;
        try {
            String header = String.format("Report on H2 Course validation \n\n");
            file = new File(h2ACourseReportFile);
            FileUtils.writeStringToFile(file, header);

            // Get the attendee map at a list
            List<ValidationH2Course> h2Course = getCourseList();
            if (courseInsCount != h2Course.size()) {
                String message = "H2 data error - terminating!";
                FileUtils.writeStringToFile(file, message, true);
                throw new RuntimeException(message);
            }
            for (ValidationH2Course h2 : h2Course) {
                FileUtils.writeStringToFile(file, String.format("H2 record: ID %d, origId %d, courseTemplateId %d, instanceNumbner %d, description %s, startDate %s, endDate %s, days '%d', heldAt '%s', examiner %d\n",
                        h2.getId(), h2.getOrigId(), h2.getCourseTemplateId(), h2.getInstanceNumbner(), h2.getDescription(), h2.getStartDate().toString(), h2.getEndDate().toString(), h2.getDays(), h2.getHeldAt(), h2.getExaminer()), true);

                // Validate Postgres
                Optional<ValidationPostCourse> post = getPostressCourse(h2.getId());
                if (post.isPresent()) {
                    if (!h2.validate(post.get(), lookups)) {
                        validationFails++;
                        logger.error("Validation Error H2 id {}", h2.getId());
                    } else {
                        validationsGood++;
                        logger.info("Validated OK H2 id {} ", h2.getId());
                    }
                    FileUtils.writeStringToFile(file, plusCR(h2.getValidationResult()), true);
                }
                else{
                    FileUtils.writeStringToFile(file, String.format("Lookup failed for %d\n", h2.getId()), true);
                }
                // Validate Access
//                Optional<ValidationAccessAttendee> access = getAccessAttendee(h2.getOldId());
//                if (access.isPresent()) {
//                    if (!h2.validate(access.get())) {
//                        accessValidationFails++;
//                        logger.error("Access Validation Error H2 id {}", h2.getId());
//                    } else {
//                        accessValidationsGood++;
//                        logger.info("Access Validated OK H2 id {} ", h2.getId());
//                    }
//                    FileUtils.writeStringToFile(file, plusCR(h2.getValidationResult()), true);
//                }
//                else{
//                    FileUtils.writeStringToFile(file, String.format("Access Lookup failed for %d\n", h2.getId()), true);
//                }
                FileUtils.writeStringToFile(file, " \n", true);
            }
            String resultStr = String.format("\n\nValidation results: Postgres: OK %d, fails %d. Total should be %d.", validationsGood, validationFails, courseInsCount);
            logger.info(resultStr);
            FileUtils.writeStringToFile(file, resultStr, true);
        } catch (IOException e) {
            logger.error("Faile to write to report file {}", h2AttendeesReportFile);
            e.printStackTrace();
        }

    }

    private void doAttendantsReport(){
        try {
            logger.info("Producing Attendants report.");
            // Verify we have H2 data
            Statement stmt = h2Conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ATTENDEE_MAP");
            if (rs != null) {
                if (rs.next()) {
                    int attendeeMapCount = rs.getInt(1);
                    boolean h2DataPresent = attendeeMapCount > 0;
                    // If we have H2 data we can iterate that list
                    if (h2DataPresent) {
                        iterDateH2Attendees(attendeeMapCount);
                    } else {
                        iterateAccessAttendees();
                    }
                }
            } else {
                logger.error("NO H2 DATA.");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void iterateAccessAttendees() {
        logger.info("iterateAccessAttendees not implemented.");
    }

    private void iterDateH2Attendees(int attendeeMapCount) {
        int validationFails = 0;
        int validationsGood = 0;
        int accessValidationFails = 0;
        int accessValidationsGood = 0;
        File file;
        try {
            String header = String.format("Report on H2 Attendee validation \n\n");
            file = new File(h2AttendeesReportFile);
            FileUtils.writeStringToFile(file, header);

            // Get the attendee map at a list
            List<ValidationH2Attendee> h2Attendees = getAttendeeList();
            if (attendeeMapCount != h2Attendees.size()) {
                String message = "H2 data error - terminating!";
                FileUtils.writeStringToFile(file, message, true);
                throw new RuntimeException(message);
            }
            for (ValidationH2Attendee h2 : h2Attendees) {
                FileUtils.writeStringToFile(file, String.format("H2 record: ID %d, ORIG_ID %d, DELEGATE_ID %d, COMPANY_ID %d, COURSE_ID %d, THEORY %d, PRACTICAL_FAULTS %d, FAIL_REASON '%s', FURTHER_TRAINING '%s'\n",
                        h2.getId(), h2.getOldId(), h2.getDelegateID(), h2.getCompanyId(), h2.getCourseID(), h2.getTheory(), h2.getPracticalFaults(), h2.getFailReason(), h2.getFurtherTraining()), true);

                // Validate Postgres
                Optional<ValidationPostAttendee> post = getPostressAttendee(h2.getId());
                if (post.isPresent()) {
                    if (!h2.validate(post.get(), lookups, accessUtilities)) {
                        validationFails++;
                        logger.error("Validation Error H2 id {}", h2.getId());
                    } else {
                        validationsGood++;
                        logger.info("Validated OK H2 id {} ", h2.getId());
                    }
                    FileUtils.writeStringToFile(file, plusCR(h2.getValidationResult()), true);
                }
                else{
                    FileUtils.writeStringToFile(file, String.format("Lookup failed for %d\n", h2.getId()), true);
                }
                // Validate Access
                Optional<ValidationAccessAttendee> access = getAccessAttendee(h2.getOldId());
                if (access.isPresent()) {
                    if (!h2.validate(access.get())) {
                        accessValidationFails++;
                        logger.error("Access Validation Error H2 id {}", h2.getId());
                    } else {
                        accessValidationsGood++;
                        logger.info("Access Validated OK H2 id {} ", h2.getId());
                    }
                    FileUtils.writeStringToFile(file, plusCR(h2.getValidationResult()), true);
                }
                else{
                    FileUtils.writeStringToFile(file, String.format("Access Lookup failed for %d\n", h2.getId()), true);
                }
                FileUtils.writeStringToFile(file, " \n", true);
            }
            String resultStr = String.format("\n\nValidation results: Postgres: OK %d, fails %d, Access OK %d, fails %d. Total should be %d.", validationsGood, validationFails, accessValidationsGood, accessValidationFails, attendeeMapCount);
            logger.info(resultStr);
            FileUtils.writeStringToFile(file, resultStr, true);
        } catch (IOException e) {
            logger.error("Faile to write to report file {}", h2AttendeesReportFile);
            e.printStackTrace();
        }
    }

    private Optional<ValidationAccessAttendee> getAccessAttendee(int id) {
        String sql = String.format("SELECT [AttendantID], [CourseID], [CompanyID], [DelegateID], [CompOrSelf], [Certificate], [WalletID], " +
                "[ActualCost], [CostCode], [Passed],  [Theory], [PracticalFaults], [FailReason], [FurtherTraining] FROM [Attendants] WHERE [AttendantID] = %d", id);
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            if (rs.next()) {
                return Optional.of(new ValidationAccessAttendee(rs));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<ValidationPostAttendee> getPostressAttendee(int id) {
        return dbConnection.getValidationPostAttendee(id);
    }

    private Optional<ValidationPostCourse> getPostressCourse(int id) {
        return dbConnection.getValidationPostCourse(id);
    }


    private String plusCR(String str){
        return String.format("%s\n", str);
    }

    private List<ValidationH2Course> getCourseList(){
        List<ValidationH2Course> courses = new ArrayList<>();

        try {
            Statement stmt = h2Conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM COURSE_INS");
            while (rs.next()) {
                courses.add(new ValidationH2Course(rs));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return courses;
    }

    private List<ValidationH2Attendee> getAttendeeList() {
        List<ValidationH2Attendee> attendees = new ArrayList<>();

        try {
            Statement stmt = h2Conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM ATTENDEE_MAP");
            while (rs.next()) {
                attendees.add(new ValidationH2Attendee(rs));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return attendees;
    }

    private void doCompaniesReport(){
        logger.info("Producing Companies report.");
    }
}
