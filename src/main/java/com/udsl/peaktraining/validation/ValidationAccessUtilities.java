package com.udsl.peaktraining.validation;

import com.udsl.peaktraining.db.MSAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ValidationAccessUtilities {

    @Autowired
    MSAccess access;

    public int getTraineeIdFromDeligateId( int deligateId){
        String sql = String.format("SELECT AttendantId FROM Attendants WHERE DelegateId = %d", deligateId);
        return getIntResult(sql);
    }

    private int getIntResult( String sql ){
        try {
            ResultSet rs = access.excuteSQL(sql);
            if (rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return -1;
    }
}
