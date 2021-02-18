package com.udsl.peaktraining;

import com.udsl.peaktraining.data.Reporter;
import com.udsl.peaktraining.validation.ValidationReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeakTraingDataMigrationApp implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(PeakTraingDataMigrationApp.class.getName());

    @Autowired
    PeakTrainingMigration migration;

    @Autowired
    Reporter reporter;

    @Autowired
    ValidationReport validationReport;

    @Value("${migration}")
    private boolean doMigration;

    @Value("${reportdetails}")
    Integer reportOn;

    @Value("${validationReport}")
    boolean validationReportRequired;

    public static void main(String[] args) {
        SpringApplication.run(PeakTraingDataMigrationApp.class, args);
    }


    public void run(String... args) throws Exception {
        logger.info("Peak Training data migration starting . . .");
        try {
            // Migration and reportOn are mutuialy exclusinv
            if (doMigration) {
                migration.runMigration();
            }
            else if (reportOn != null){
                reporter.report(reportOn);
            }
            // validation can be run with or without either of the above.
            if (validationReportRequired){
                validationReport.doReport();
            }
            System.exit(0);
        } catch (Exception e) {
            logger.error("Caught exception {}", e.getMessage(), e);
        }
    }
}
