package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class InstructorExaminer {
    private static final Logger logger = LogManager.getLogger(InstructorExaminer.class.getName());

    private int id;
    private int oldId;
    private String forename;
    private String surname;
    private String regNumber;

    public InstructorExaminer(ResultSet rs) {
        try {
            oldId = rs.getInt("TrainerID");
            forename = rs.getString("FirstName");
            surname = rs.getString("Surname");
            regNumber = rs.getString("RegNumber");
        } catch (SQLException e) {
            logger.error("Exception creating TrainerExaminer - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}
