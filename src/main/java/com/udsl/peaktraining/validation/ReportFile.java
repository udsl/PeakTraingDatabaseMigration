package com.udsl.peaktraining.validation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ReportFile {
    private File theFile;

    private boolean reportingActive(){
        return ValidationReport.validationReport;
    }

    public ReportFile(String traineeReportFileName){
        theFile = new File( traineeReportFileName);
    }

    public void writeHeader(String header) {
        try{
            FileUtils.writeStringToFile(theFile, header+ "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String str){
        try {
            if (reportingActive()) {
                FileUtils.writeStringToFile(theFile, str + "\n", true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
