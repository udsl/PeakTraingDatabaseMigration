package com.udsl.peaktraining.validation;


import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@ToString
@Getter
public class ValidationPostCourse {
    private static final Logger logger = LogManager.getLogger(ValidationPostCourse.class.getName());

    private int courseInsId;
    private int courseDefId;
    private String description;
    private LocalDate startDate;
    private int days ;
    private String heldAt ;
    private String courseElements;
    private int certificateId ;
    private int instructorId;
    private int examinerId;
    private String courseType ;
    private String model ;
    private String capacity ;
    private String attachment ;
    private String equipment ;

    public ValidationPostCourse(ResultSet rs ){
        try {
            courseInsId = rs.getInt("course_ins_id");
            courseDefId = rs.getInt("course_def_id");
            description = rs.getString("description");
            startDate = rs.getDate("start_date").toLocalDate();
            days = rs.getInt("days");
            heldAt = rs.getString("held_at");
            courseElements = rs.getString("course_elements");
            certificateId = rs.getInt("certificate_id");
            instructorId = rs.getInt("instructor_id");
            examinerId = rs.getInt("examiner_id");
            courseType = rs.getString("course_type");
            model = rs.getString("model");
            capacity = rs.getString("capacity");
            attachment = rs.getString("attachment");
            equipment = rs.getString("equipment");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Exception creating ValidationPostAttendee - {}", e.getMessage());
        }
    }

}
