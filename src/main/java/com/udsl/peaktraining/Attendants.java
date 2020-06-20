package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Attendants {
    private static final Logger logger = LogManager.getLogger(Attendants.class.getName());

    private int id;
    private int oldId;
    private int delegateID;
    private int courseID;
    private boolean passed;
    private int theory;
    private int practicalFaults;
    private String failReason;
    private String furtherTraining;

    public Attendants(ResultSet rs) {
        try {
            oldId = rs.getInt("AttendantID");
            delegateID = rs.getInt("DelegateID");
            courseID = rs.getInt("CourseID");
            passed = rs.getString("Passed").equalsIgnoreCase("y");
            theory = rs.getInt("Theory");
            practicalFaults = rs.getInt("PracticalFaults");
            failReason= rs.getString("FailReason");
            furtherTraining= rs.getString("FurtherTraining");
        } catch (SQLException e) {
            logger.error("Exception creating company - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}
