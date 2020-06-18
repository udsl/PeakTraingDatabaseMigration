package com.udsl.peaktraining;

import com.udsl.DataException;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@ToString
public class Contact {
    private static final Logger logger = LogManager.getLogger(Contact.class.getName());

    private int id;
    private int companyId;
    private String name;
    private String email;
    private String phone;
    private String mobile;
    private boolean isPrimary;

    public void setId(int id) {
        this.id = id;
    }

    public Contact(int companyId, ResultSet rs) throws DataException {
        this.companyId = companyId;
        name = getValueOrNull( "name", rs);
        if (name == null){
            throw new DataException();
        }
        email = getValueOrNull("email", rs);
        phone = getValueOrNull("phone", rs);
        mobile = getValueOrNull("mobile", rs);
        isPrimary = true;
    }
    private String getValueOrNull(String fieldName, ResultSet rs){
        try {
            return rs.getString(fieldName);
        } catch (SQLException e) {
            return null ;
        }
    }
}
