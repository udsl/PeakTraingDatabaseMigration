package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.H2Connection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${validationsRequired}")
    String[] validationsRequired;

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
                default:
                    logger.error("Undefined report '{}' requested.", report);
            }
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
        // Get the attendee map at a list
        List<ValidationH2Attendee> h2Attendees = getAttendeeList();
        if (attendeeMapCount != h2Attendees.size()){
            throw new RuntimeException("H2 data error - terminating!");
        }
        for(ValidationH2Attendee h2: h2Attendees){
            // Validate Postgres
            Optional<ValidationPostAttendee> post = getPostressAttendee(h2.getId());
            if (post.isPresent()){
                if (!h2.validate(post.get(), lookups)){
                    validationFails++;
                    logger.error("Validation Error H2 {} against Postgres {}", h2, post);
                }
                else{
                    validationsGood++;
                    logger.info("Validated OK H2 {} against Postgres {}", h2, post);
                }
            }
            // Validate Access
        }
        logger.info("Validation results: OK {}, fails {} total should be {}.", validationsGood, validationFails, attendeeMapCount);
    }

    private Optional<ValidationPostAttendee> getPostressAttendee(int id) {
        return dbConnection.getValidationPostAttendee(id);
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
