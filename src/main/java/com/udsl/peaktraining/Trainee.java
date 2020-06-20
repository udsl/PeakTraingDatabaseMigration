package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Trainee {
    private static final Logger logger = LogManager.getLogger(Company.class.getName());

    private int id;
    private int oldId;
    private int companyId;
    private String forename;
    private String surname;

    public Trainee(ResultSet rs) {
        try {
            oldId = rs.getInt("DelegateId");
            companyId = rs.getInt("companyID");
            forename = rs.getString("DelegateFirstName");
            surname = rs.getString("DelegateSurname");
        } catch (SQLException e) {
            logger.error("Exception creating Trainee - {}", e.getMessage());
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}