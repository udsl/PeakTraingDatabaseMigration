package com.udsl.peaktraining;

import com.udsl.DataException;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Getter
@ToString
public class Contact {
    private static final Logger logger = LogManager.getLogger(Contact.class.getName());

    private int id;
    private final int companyId;
    private final String name;
    private String email = "";
    private String phone = "";
    private final boolean isPrimary;

    public void setId(int id) {
        this.id = id;
    }

    public Contact(int companyId, ResultSet rs) throws DataException {
        this.companyId = companyId;
        name = getValueOrNull( "contact", rs);
        if (name == null){
            throw new DataException();
        }
        email = getValueOrNull("email", rs);

        phone = getOptionalOfField("telephone", rs).orElseGet(() -> getValueOrNull("mobile", rs));

        isPrimary = true;
    }

    private String getValueOrNull(String fieldName, ResultSet rs){
        try {
            return rs.getString(fieldName);
        } catch (SQLException e) {
            return null ;
        }
    }

    private Optional<String> getOptionalOfField(String fieldName, ResultSet rs){
        try {
            return fieldValue(rs.getString(fieldName));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private Optional<String> fieldValue(String v){
        if (v == null || v.equals("")){
            return Optional.empty();
        }
        return Optional.of(v);
    }
}
