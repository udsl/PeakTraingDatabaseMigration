package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@ToString
@Getter
public class CourseIns {
    private static final Logger logger = LogManager.getLogger(CourseIns.class.getName());

    private static int nextInstanceNumbner = 1;

    private int courseDefId;
    private int oldId;
    private int courseTemplateId;
    private int instanceNumbner ;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int days;
    private String heldAt;
    private int examiner;

    public CourseIns(ResultSet rs) {
        try {
            oldId = rs.getInt("courseId");
            String v = rs.getString("CourseTemplateName");
            v = v.substring(0, v.indexOf('(')-1);
            description = v;
            startDate = rs.getDate("CourseStartDate").toLocalDate();
            endDate = rs.getDate("CourseEndDate").toLocalDate();
            days = (int)startDate.until(endDate, DAYS);
            heldAt = rs.getString("CourseVenue");
            courseTemplateId = rs.getInt("TemplateCourseID");
            examiner = rs.getInt("Examiner");
            examiner = examiner == 0?1:examiner;
        } catch (SQLException e) {
            logger.error("Exception creating company - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.courseDefId = id;
    }

    public int getAndSetInstanceNumber(){
        instanceNumbner = nextInstanceNumbner++;
        return instanceNumbner;
    }
}
