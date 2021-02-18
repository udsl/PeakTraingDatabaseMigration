package com.udsl.peaktraining.data;

import com.udsl.peaktraining.data.Company;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Trainee {
    private static final Logger logger = LogManager.getLogger(Trainee.class.getName());

    private int id;
    private int oldId;
    private int companyId;
    private String forename;
    private String surname;

    private Trainee(){}

    public Trainee(ResultSet rs) {
        try {
            logger.info("Data read: DelegateId - {}, companyID - {}", rs.getInt("DelegateId"), rs.getInt("companyID"));
            oldId = rs.getInt("DelegateId");
            companyId = rs.getInt("companyID");
            forename = rs.getString("DelegateFirstName");
            surname = rs.getString("DelegateSurname");
        } catch (SQLException e) {
            logger.error("Exception creating Trainee - {}", e.getMessage());
        }
    }

    public Trainee(String forename, String surname, int companyId){
        this.forename = forename;
        this.surname = surname;
        this.companyId = companyId;
    }

    public void setId(int id) {
        this.id = id;
    }
}