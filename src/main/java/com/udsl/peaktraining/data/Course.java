package com.udsl.peaktraining.data;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Course {
    private static final Logger logger = LogManager.getLogger(Course.class.getName());

    private static int courseRef= 1000;

    private int courseID;
    private int oldId;
    private String Coursetitle;
    private String certificatePrefix;
    private int certificateCount;

    public Course(ResultSet rs) {
        try {
            oldId = rs.getInt("courseid");
            Coursetitle = rs.getString("Coursetitle");
            certificatePrefix = rs.getString("certificatePrefix");
            certificateCount = rs.getInt("certificateCount");
        } catch (SQLException e) {
            logger.error("Exception creating company - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.courseID = id;
    }

    public String getNextCourseRef(){
        return "PT:"+courseRef++;
    }
}
