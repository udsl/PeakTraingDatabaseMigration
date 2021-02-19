package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.Lookups;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ToString
@Getter
public class ValidationH2Course {
    private static final Logger logger = LogManager.getLogger(ValidationH2Course.class.getName());

    private int id ;
    private int origId ;
    private int courseTemplateId ;
    private int instanceNumbner ;
    private String description ;
    private LocalDate startDate ;
    private LocalDate endDate ;
    private int days;
    private String heldAt ;
    private int examiner ;

    private String validationResult;

    public ValidationH2Course(ResultSet rs){
        try {
            id = rs.getInt("ID");
            origId = rs.getInt("ORIG_ID");
            courseTemplateId = rs.getInt("COURSE_TEMPLATE_ID");
            instanceNumbner = rs.getInt("INSTANCE_NUMBNER");
            description = rs.getString("DESCRIPTION");
            startDate = rs.getDate("START_DATE").toLocalDate();
            endDate = rs.getDate("END_DATE").toLocalDate();
            days = rs.getInt("DAYS");
            heldAt = rs.getString("HELD_AT");
            examiner = rs.getInt("EXAMINER");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating ValidationAttendee - {}", e.getMessage());
        }
    }

    public boolean validate(ValidationPostCourse post, Lookups lookup){
        int originalCourseId = lookup.getOriginalCourseInsId(post.getCourseInsId());
        long daysTillEndDate = startDate.until(endDate, ChronoUnit.DAYS);
        boolean result = post.getCourseInsId() == id &&
                originalCourseId == origId &&
                post.getDescription().equals(description) &&
                post.getHeldAt().equals(heldAt) &&
                post.getStartDate().equals(startDate) &&
                post.getCourseDefId() == lookup.getCourseDefId(courseTemplateId) &&
                post.getDays() == daysTillEndDate;

        validationResult = String.format("Validation (Postgres): id (%d = %d), " + // ID
                        "courseID (%d -> %d = %d), " +  // courseId
                        "held at ('%s' = '%s' " + // held at
                        "start date (%s = %s) " + // Start date
                        "days (%d = %d)", // Days
                post.getCourseInsId(), id, // ID
                post.getCourseInsId(), originalCourseId , origId, // courseId
                post.getHeldAt(), heldAt, // held at
                post.getStartDate().toString(), startDate.toString(), // Start date
                daysTillEndDate, days // Days
                );

        if (!result){
            validationResult = String.format("FAILED - %s", validationResult);
        }

        return result;
    }

}

