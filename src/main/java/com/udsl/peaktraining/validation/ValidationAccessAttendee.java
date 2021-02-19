package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class ValidationAccessAttendee {
    private static final Logger logger = LogManager.getLogger(com.udsl.peaktraining.validation.ValidationAccessAttendee.class.getName());

    int attendantID;
    int courseID;
    int companyID;
    int delegateID;
    String compOrSelf;
    int certificate;
    int walletID;
    String actualCost;
    String costCode;
    String passed;
    String theory;
    String practicalFaults;
    String failReason;
    String furtherTraining;

    private String validationResult;

    public ValidationAccessAttendee(ResultSet rs){
        try {
            attendantID = rs.getInt("AttendantID");
            courseID = rs.getInt("CourseID");
            companyID = rs.getInt("CompanyID");
            delegateID = rs.getInt("DelegateID");
            compOrSelf = rs.getString("CompOrSelf");
            certificate = rs.getInt("Certificate");
            walletID = rs.getInt("WalletID");
            actualCost = rs.getString("ActualCost");
            costCode = rs.getString("CostCode");
            passed = rs.getString("Passed");
            theory = rs.getString("Theory");
            practicalFaults = rs.getString("PracticalFaults");
            failReason = rs.getString("FailReason");
            furtherTraining = rs.getString("FurtherTraining");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating ValidationAccessAttendee - {}", e.getMessage());
        }
    }

}
