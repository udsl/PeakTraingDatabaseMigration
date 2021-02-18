package com.udsl.peaktraining.validation;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class ValidationPostAttendee {
    private static final Logger logger = LogManager.getLogger(ValidationPostAttendee.class.getName());

    private int attendentId;
    private int traineeId;
    private int courseId;

    public ValidationPostAttendee(ResultSet rs ){
        try {
            attendentId = rs.getInt("attendee_id");
            traineeId = rs.getInt("trainee_id");
            courseId = rs.getInt("course_ins_id");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating ValidationPostAttendee - {}", e.getMessage());
        }
    }
}
