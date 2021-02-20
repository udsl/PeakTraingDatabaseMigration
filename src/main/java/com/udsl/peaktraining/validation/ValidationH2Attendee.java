package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import jdk.nashorn.internal.lookup.Lookup;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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

    private String validationResult;

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

    public boolean validate(ValidationPostAttendee validationPostAttendee, Lookups lookup, ValidationAccessUtilities util){
        int accessTraineeId = util.getTraineeIdFromDeligateId(delegateID);
        int newTraineeId = lookup.getNewTrianeeId(accessTraineeId);
        int oldTraneeId = lookup.getOldTraineeId(validationPostAttendee.getTraineeId());
        int mappedOrigCourseId = lookup.getOrigCourseId(validationPostAttendee.getCourseId());
        int newCourseIdMapped = lookup.getMappedCourseInsId(courseID);

        boolean result = validationPostAttendee.getAttendentId() == id &&
               // lookup the original ID from the postgres ID should be the H2 id
                mappedOrigCourseId ==  courseID &&
               // postgres course id point to the course_ins_id
               validationPostAttendee.getCourseId() == newCourseIdMapped &&
               // trainee ID should be the trainee id of the delegate
               // we have the delegate id so now look up the trainee in access and get the mapped id.

               validationPostAttendee.getTraineeId() == newTraineeId;

        validationResult = String.format("Validation (Postgres): id (%d = %d), " +
                        "courseID (%d -> %d = %d -> %d), " +
                        "traineeID (%d -> %d = %d  -> (access trainee)%d -> (delegate)%d)",
                validationPostAttendee.getAttendentId(), id, // ID
                validationPostAttendee.getCourseId(), mappedOrigCourseId , courseID, newCourseIdMapped, // CourseId
                validationPostAttendee.getTraineeId(), oldTraneeId, delegateID, accessTraineeId, newTraineeId); // deligate id

        if (result){
            logger.info("Validation good");
        }
        else{
            logger.info("Validation failed");
            validationResult = String.format("FAILED - %s", validationResult);
        }
        return result;
    }

    public boolean validate(ValidationAccessAttendee accessAttendee){
        boolean result = accessAttendee.getAttendantID() == oldId &&
                accessAttendee.getCourseID() ==  courseID &&
                accessAttendee.getDelegateID() == delegateID;
        validationResult = String.format("Validation (Access): id (%d = %d), courseID (%d = %d), delegateID (%d = %d)",
                accessAttendee.getAttendantID(), id, accessAttendee.getCourseID(), courseID, accessAttendee.getDelegateID(), delegateID);
        if (result){
            logger.info("Validation good");
        }
        else{
            logger.info("Validation failed");
            validationResult = String.format("FAILED - %s", validationResult);
        }
        return result;
    }
}
