package com.udsl.peaktraining.data;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Attendants {
    private static final Logger logger = LogManager.getLogger(Attendants.class.getName());

    private int id;
    private int oldId;      // Original AttendentId
    private int delegateID; // references original Trainees.DeligateId
    private int companyId;  // Original CompanyId
    private int courseID;   // Original course ID
    private boolean passed;
    private Integer theory;
    private Integer practicalFaults = null;
    private String failReason = "";
    private String furtherTraining = "";

    public Attendants(ResultSet rs) {
        try {
            oldId = rs.getInt("AttendantID");
            delegateID = rs.getInt("DelegateID");
            companyId = rs.getInt("CompanyID");
            courseID = rs.getInt("CourseID");
            passed = StringUtils.equalsIgnoreCase(rs.getString("Passed"), "y");

            String theoryStr = rs.getString("Theory");
            logger.info("theoryStr: {}", theoryStr);

            if (StringUtils.isEmpty(theoryStr)){
                theory = 0;
            }
            else {
                theoryStr = theoryStr.toUpperCase().replace("`", "");
                if (theoryStr.contains("N/A") || theoryStr.equalsIgnoreCase("PASS")
                        || theoryStr.equalsIgnoreCase("N")) {
                    theory = 0;
                }
                else{
                    if (theoryStr.contains("/")){
                        theoryStr = theoryStr.substring(0, theoryStr.indexOf("/"));
                    }
                    if (theoryStr.contains(" ")){
                        theoryStr = theoryStr.substring(0, theoryStr.indexOf(" "));
                    }
                    if (theoryStr.contains("-")){
                        theoryStr = theoryStr.substring(0, theoryStr.indexOf("-"));
                    }
                    if (StringUtils.isNumeric(theoryStr)) {
                        theory = Integer.valueOf(theoryStr);
                    }
                }
            }

            String practicalFaultsStr = rs.getString("PracticalFaults");
            logger.info("practicalFaultsStr: {}", practicalFaultsStr);

            if (StringUtils.isEmpty(practicalFaultsStr)) {
                practicalFaults = 0;
            }
            else {
                practicalFaultsStr = practicalFaultsStr.toUpperCase().replace("`", "");
                if (practicalFaultsStr.contains("N/A")
                        || practicalFaultsStr.equalsIgnoreCase("PASS")
                        || theoryStr.equalsIgnoreCase("N")) {
                    practicalFaults = 0;
                } else {
                    if (practicalFaultsStr.contains("/")) {
                        practicalFaultsStr = practicalFaultsStr.substring(0, practicalFaultsStr.indexOf("/"));
                    }
                    if (practicalFaultsStr.contains(" ")) {
                        practicalFaultsStr = practicalFaultsStr.substring(0, practicalFaultsStr.indexOf(" "));
                    }
                    if (practicalFaultsStr.contains("-")) {
                        practicalFaultsStr = practicalFaultsStr.substring(0, practicalFaultsStr.indexOf("-"));
                    }
                    if (StringUtils.isNumeric(practicalFaultsStr)) {
                        practicalFaults = Integer.valueOf(practicalFaultsStr);
                    }
                }
            }

            String failReasonStr = rs.getString("FailReason");
            if (failReasonStr != null && !failReasonStr.equalsIgnoreCase("N/A")) {
                failReason = failReasonStr;
            }

            String furtherTrainingStr = rs.getString("FurtherTraining");
            if (!StringUtils.isEmpty(furtherTrainingStr) && !furtherTrainingStr.equalsIgnoreCase("N/A")) {
                furtherTraining = furtherTrainingStr;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating Attendants - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}
