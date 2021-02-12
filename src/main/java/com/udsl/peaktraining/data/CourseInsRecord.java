package com.udsl.peaktraining.data;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

@ToString
@Getter
public class CourseInsRecord {
    private static final Logger logger = LogManager.getLogger(CourseInsRecord.class.getName());

    private int course_ins_id;
    private int course_def_id;
    private String description;
    private Date start_date ;
    private int days;
    private String course_elements;
    private int  certificate_id;
    private int instructor_id;
    private int examiner_id;
    private String course_type;
    private String model;
    private String capacity;
    private String attachment;
    private String equipment;

    public CourseInsRecord(ResultSet rs) {
        try {
            course_ins_id = rs.getInt("course_ins_id");
            course_def_id = rs.getInt("course_def_id");
            description = rs.getString("description");
            start_date  = rs.getDate("start_date");
            days = rs.getInt("days");

            course_elements = rs.getString("course_elements");

            certificate_id = rs.getInt("certificate_id");
            instructor_id = rs.getInt("instructor_id");
            examiner_id = rs.getInt("examiner_id");
            course_type = rs.getString("course_type");
            model = rs.getString("model");
            capacity = rs.getString("capacity");
            attachment = rs.getString("attachment");
            equipment = rs.getString("equipment");
        } catch (SQLException e) {
            logger.error("Exception loading CourseInsRecord - {}", e.getMessage());
        }
    }

}
