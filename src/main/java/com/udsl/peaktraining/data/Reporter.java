package com.udsl.peaktraining.data;

import com.udsl.peaktraining.Lookups;
import com.udsl.peaktraining.db.MSAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
        List<Trainee> traineeList = null;
        if (reportingOn == null){
            logger.info("No commany found with ID {}", reportOn);
        }
        else{
            // Check migration data for mapings
            reportingOn.setId(getCoMappingId(reportOn));
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
            writeReport(reportingOn);
        }

    }

    private void writeReport(Company co){
        clearReportFile();
        writeToReportFile(co.toString());
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
        try {
            return lookups.getNewTrianeeId(forOldTraineeId, forCoId);
            // get company trainees
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }

    private Company getCompany(int coId) {
        logger.debug("Getting company with id {}", coId);
        String sql = "SELECT [companyID], [name], [address1], [address2], [address3], [address4], [postcode], [contact], [telephone], [email], [mobile] FROM [CompanySelfSponsored] ORDER BY [name]";
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

    private List<Trainee> getTrainees(int forCo){
        List<Trainee> traineeList = new ArrayList<>();
        try {
            logger.debug("getting trainees for {}", forCo);
            String sql = String.format("SELECT [DelegateId], [companyID], [DelegateFirstName], [DelegateSurname] FROM [trainees] WHERE [companyID] = %d", forCo);
            try (ResultSet rs = mAccess.excuteSQL(sql)) {
                while (rs.next()) {
                    Trainee trainee = new Trainee(rs);
                    traineeList.add(trainee);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return traineeList;
    }

    void clearReportFile() {
        try {
            String header = String.format("Report on Comapny ID %d\n\n", reportOn);
            BufferedWriter writer = new BufferedWriter(new FileWriter(reportFileName, true));
            writer.append(header);
            writer.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    void writeToReportFile(String str){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(reportFileName, true));
            writer.append(str + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
