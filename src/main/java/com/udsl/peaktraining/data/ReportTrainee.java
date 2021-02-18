package com.udsl.peaktraining.data;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@ToString(callSuper=true)
@Getter
public class ReportTrainee extends Trainee {
    private static final Logger logger = LogManager.getLogger(ReportTrainee.class.getName());

    private List<BookedCoursesRecord> courseRecord = new ArrayList<>();

    public ReportTrainee(ResultSet rs) {
        super(rs);
    }

    public void addCourseRecord(BookedCoursesRecord record){
        courseRecord.add(record);
    }
}
