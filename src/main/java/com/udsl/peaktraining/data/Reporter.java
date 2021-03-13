package com.udsl.peaktraining.data;

import com.udsl.peaktraining.Lookups;
import com.udsl.peaktraining.db.MSAccess;
import com.udsl.peaktraining.validation.ReportFile;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Reporter {
    private static final Logger logger = LogManager.getLogger(Reporter.class.getName());

    @Autowired
    private MSAccess mAccess ;

    @Autowired
    private Lookups lookups ;

    @Value("${reportFileName}")
    private String reportFileName;

    @Value("${reportdetails}")
    Integer reportOn;

    public void report(int reportOn){
        logger.info("Reporting on: {}", reportOn);
        // Get data from Access
        Company reportingOn = getCompany(reportOn);
        List<ReportTrainee> traineeList = null;
        if (reportingOn == null){
            logger.info("No commany found with ID {}", reportOn);
        }
        else{
            reportingOn.setId(reportOn);
            // get company trainees
            traineeList = getTrainees(reportOn);
            if (traineeList.isEmpty() ){
                logger.error("No trianees found.");
            }
            else{
                // check migration data for trainee maping.
                for(Trainee t: traineeList){
                    t.setId(getTraineeMappingId(t.getOldId(), t.getCompanyId()));
                }
            }
            writeReport(reportingOn, traineeList);
        }
    }


    private void writeReport(Company co, List<ReportTrainee> traineeList){
        clearReportFile();
        writeToReportFile(co.toString());
        int traineeCount = 0;
        for( ReportTrainee t: traineeList){
            writeToReportFile( String.format("    %3d %s", ++traineeCount, t.toString()));
            int courseCount = 0;
            for(BookedCoursesRecord r: t.getCourseRecord()){
                writeToReportFile( String.format("        %3d %s", ++courseCount, r.toString()));
            }
        }
    }

    private int getCoMappingId(int forCoId){
        try {
            return lookups.getNewCompanyId(forCoId);
            // get company trainees
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    private int getTraineeMappingId(int forOldTraineeId, int forCoId){
        return lookups.getNewTrianeeId(forOldTraineeId);
    }

    private Company getCompany(int coId) {
        logger.debug("Getting company with id {}", coId);
        String sql = String.format("SELECT [companyID], [name], [address1], [address2], [address3], [address4], [postcode], [contact], [telephone], [email], [mobile] FROM [CompanySelfSponsored] WHERE [companyID] = %d", coId);
        try (ResultSet rs = mAccess.excuteSQL(sql)) {
            if (rs.next()) {
                String name = rs.getString("name");
                logger.info("Company name '{}'", name);
                return new Company(rs);
             }
            logger.info("While was false!");
        } catch (Exception e) {
            logger.error("Exception!", e);
        }
        return null;
    }

    private List<ReportTrainee> getTrainees(int forCo){
        List<ReportTrainee> traineeList = new ArrayList<>();
        try {
            logger.debug("getting trainees for {}", forCo);
            String sql = String.format("SELECT [DelegateId], [companyID], [DelegateFirstName], [DelegateSurname] FROM [trainees] WHERE [companyID] = %d", forCo);
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    ReportTrainee trainee = new ReportTrainee(rs);
                    // Now get the courses this Trainee has attended
                    // First get the Attendants IDs
                    String attendantsSql = String.format("SELECT [AttendantID], [CourseID], [CompanyID], [DelegateID], [Passed], [Theory] [PracticalFaults], [FailReason], [FurtherTraining]  FROM [Attendants] WHERE [DelegateID] = %d", trainee.getOldId());
                    try (ResultSet attendantsRs = mAccess.excuteSQL(attendantsSql)){
                        while (attendantsRs.next()) {
                            Attendants attendee = new Attendants(attendantsRs);
                            logger.info("Getting course data attended, courseId {}", attendee.getCourseID());
                            String bookedSql = String.format("SELECT [courseId], [TemplateCourseID], [CourseTemplateName], [CourseReference], [trainerID], [CourseVenue], [CourseStartDate], [CourseEndDate], [Examiner] FROM [BookedCourses] WHERE [courseId] = %d", attendee.getCourseID() );
                            try (ResultSet bookedRs = mAccess.excuteSQL(bookedSql)) {
                                while (bookedRs.next()) {
                                    BookedCoursesRecord course = new BookedCoursesRecord(bookedRs);
                                    course.setCourseId(lookups.getCourseDefId(course.getCourseId()));
                                    trainee.addCourseRecord(course);
                                }
                            }
                        }
                    }
                    traineeList.add(trainee);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return traineeList;
    }

    File file;

    void clearReportFile() {
        Path path = Paths.get(reportFileName);
        String header = String.format("Report on Company ID %d\n\n", reportOn);
        ReportFile report = new ReportFile(reportFileName);
        report.writeHeader(header);
    }

    void writeToReportFile(String str){
        try {
            FileUtils.writeStringToFile(file, str + "\n", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
