package com.udsl.peaktraining.data;

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
    private Integer theory;
    private Integer practicalFaults = null;
    private String failReason = "";
    private String furtherTraining = "";

    public Attendants(ResultSet rs) {
        try {
            oldId = rs.getInt("AttendantID");
            delegateID = rs.getInt("DelegateID");
            courseID = rs.getInt("CourseID");
            passed = rs.getString("Passed").equalsIgnoreCase("y");

            String theoryStr = rs.getString("Theory");
            if (theoryStr != null && theoryStr.equalsIgnoreCase("N/A")) {
                theory = 0;
            }
            else{
                theory = rs.getInt("Theory");
            }

            String practicalFaultsStr = rs.getString("PracticalFaults");
            if (practicalFaultsStr == null || practicalFaultsStr.equalsIgnoreCase("N/A")) {
                practicalFaults = null;
            }
            else{
                practicalFaults = rs.getInt("PracticalFaults");
            }

            String failReasonStr = rs.getString("FailReason");
            if (failReasonStr != null && !failReasonStr.equalsIgnoreCase("N/A")) {
                failReason = failReasonStr;
            }

            String furtherTrainingStr = rs.getString("FurtherTraining");
            if (furtherTrainingStr != null && !furtherTrainingStr.equalsIgnoreCase("N/A")) {
                furtherTraining = furtherTrainingStr;
            }
        } catch (SQLException e) {
            logger.error("Exception creating company - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}
