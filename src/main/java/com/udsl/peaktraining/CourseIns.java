package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private int days;
    private String heldAt;
    private int instanceNumbner =0;

    public CourseIns(ResultSet rs) {
        try {
            oldId = rs.getInt("courseId");
            startDate = rs.getDate("CourseStartDate").toLocalDate();
            endDate = rs.getDate("CourseEndDate").toLocalDate();
            days = (int)startDate.until(endDate, DAYS);
            heldAt = rs.getString("CourseVenue");
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
