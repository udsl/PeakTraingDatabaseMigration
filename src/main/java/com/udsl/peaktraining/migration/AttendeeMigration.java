package com.udsl.peaktraining.migration;

import com.udsl.peaktraining.Lookups;
import com.udsl.peaktraining.data.Attendants;
import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttendeeMigration {
    private static final Logger logger = LogManager.getLogger(AttendeeMigration.class.getName());

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    private Lookups lookups ;

    @Autowired
    MigrationUtilities migrationUtil;

    private List<Integer> undefinedCourses = new ArrayList<>();

    public void processAttendants() {
        int toProcess = 0;
        int processed = 0;
        try {
            toProcess = migrationUtil.getRecordCount("Attendants");
            logger.debug("Processing {} Attendants records", toProcess);
            String sql = "SELECT [AttendantID], [CourseID], [CompanyID], [DelegateID], [Passed], [Theory] [PracticalFaults], [FailReason], [FurtherTraining]  FROM [Attendants]";
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    int courseId = rs.getInt("CourseID");
                    processed = processed + 1 ;
                    if (courseExists(courseId)) {
                        int delegateId = rs.getInt("DelegateID");
                        int companyId = rs.getInt("CompanyID");
                        if (lookups.getMappedCourseInsId(courseId) < 0) {
                            logger.debug("Attendants record found with invalid CourseID {}", courseId);
                        } else if (lookups.getNewTrianeeId(delegateId) < 0) {
                            logger.debug("Attendants record found with DelegateID {} or CompanyId {}", delegateId, companyId);
                        } else {
                            Attendants attendee = new Attendants(rs);
                            logger.debug("Created attendee {}", attendee);
                            int attendeeId = dbConnection.saveAttendee(attendee, lookups);
                            attendee.setId(attendeeId);
                            lookups.addAttendee(attendee);
                            dbConnection.saveResults(attendee, lookups);
                        }
                    }
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (toProcess != processed){
            throw new RuntimeException("Procesing count error.");
        }
    }

    private boolean courseExists(int id){
        String sql = String.format("SELECT count(*) FROM [BookedCourses] WHERE [CourseId] = %d", id);
        try (ResultSet rs = mAccess.excuteSQL(sql)){
            if (rs.next()) {
                int v = rs.getInt(1);
                if (v != 1){
                    addToUndefinedCourses(id);
                    return false;
                }
                else{
                    return true;
                }
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private void addToUndefinedCourses(int id){
        logger.info("Course with ID {} not defined in access DB", id);
        undefinedCourses.add(id);
    }
}
