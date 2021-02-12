package com.udsl.peaktraining.data;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class InstructorExaminer {
    private static final Logger logger = LogManager.getLogger(InstructorExaminer.class.getName());

    private int id;
    private int oldId;
    private String forename;
    private String surname;
    private String regNumber;
    private String sigFileName;

    public InstructorExaminer(ResultSet rs) {
        try {
            oldId = rs.getInt("TrainerID");
            forename = rs.getString("FirstName").trim();
            surname = rs.getString("Surname").trim();
            regNumber = rs.getString("RegNumber");
            sigFileName = sigFileName();
        } catch (SQLException e) {
            logger.error("Exception creating TrainerExaminer - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    private String sigFileName(){
        if ("hardy".equalsIgnoreCase(surname)){
            if ("dave".equalsIgnoreCase(forename)){
                return "dave-hardy-sig.jpg";
            }
            else{
                return "thomas-hardy-sig.jpg";
            }
        }
        else if (forename != "") {
            return "mick-sig.jpg";
        }
        else{
            return null;
        }

    }
}

