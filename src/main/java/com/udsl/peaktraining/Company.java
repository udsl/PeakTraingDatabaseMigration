package com.udsl.peaktraining;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@ToString
@Getter
public class Company {
    private static final Logger logger = LogManager.getLogger(Company.class.getName());

    private int id;
    private int oldId;
    private String name;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postcode;

    private Contact contact;

    public Company(ResultSet rs) {
        try {
            oldId = rs.getInt("companyid");
            name = rs.getString("name");
            address1 = rs.getString("address1");
            address2 = rs.getString("address2");
            address3 = rs.getString("address3");
            address4 = rs.getString("address4");
            postcode= rs.getString("postcode");
            // save to postgress DB

        } catch (SQLException e) {
            logger.error("Exception creating company - {}", e.getMessage());
        }
    }


    public void setId(int id) {
        this.id = id;
    }
}
