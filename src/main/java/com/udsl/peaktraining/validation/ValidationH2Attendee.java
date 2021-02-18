package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import jdk.nashorn.internal.lookup.Lookup;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class ValidationH2Attendee {
    private static final Logger logger = LogManager.getLogger(ValidationH2Attendee.class.getName());

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

    public ValidationH2Attendee(ResultSet rs){
        try {
            id = rs.getInt("ID");
            oldId = rs.getInt("ORIG_ID");
            delegateID = rs.getInt("DELEGATE_ID");
            companyId = rs.getInt("COMPANY_ID");
            courseID = rs.getInt("COURSE_ID");
            passed = StringUtils.equalsIgnoreCase(rs.getString("PASSED"), "y");
            theory = rs.getInt("THEORY");
            practicalFaults = rs.getInt("PRACTICAL_FAULTS");
            failReason = rs.getString("FAIL_REASON");
            furtherTraining = rs.getString("FURTHER_TRAINING");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating ValidationAttendee - {}", e.getMessage());
        }
    }

    public boolean validate(ValidationPostAttendee validationPostAttendee, Lookups lookup){
        boolean result = validationPostAttendee.getAttendentId() == id &&
               lookup.getOrigCourseId(validationPostAttendee.getCourseId()) ==  courseID &&
               validationPostAttendee.getTraineeId() == lookup.getNewTrianeeId(delegateID);
        if (result){
            logger.info("Validation good");
        }
        else{
            logger.info("Validation fails: id ({} = {}), courseID ({} = {}), delegateID ({} = {})",
                    validationPostAttendee.getAttendentId(),id, validationPostAttendee.getCourseId(), courseID, validationPostAttendee.getTraineeId(), delegateID);
        }
        return result;
    }
}
