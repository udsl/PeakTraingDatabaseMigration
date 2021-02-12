package com.udsl.peaktraining;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeakTraingDataMigrationApp implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(PeakTraingDataMigrationApp.class.getName());

    @Autowired
    PeakTrainingMigration migration;

    public static void main(String[] args) {
        SpringApplication.run(PeakTraingDataMigrationApp.class, args);
    }


    public void run(String... args) throws Exception {
        logger.info("Peak Training data migration starting . . .");
        try {
            migration.runMigration();
        } catch (Exception e) {
            logger.error("Caught exception {}", e.getMessage(), e);
        }
    }
}
