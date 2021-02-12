package com.udsl.peaktraining.data;

import com.udsl.peaktraining.db.H2Connection;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BookedCoursesContent {
    private static final Logger logger = LogManager.getLogger(BookedCoursesContent.class.getName());

    private int id;
    private int originalCourseId ;
    private int mappedCourseId ;
    private String equipment ;
    private String model;
    private String capacity;
    private String attachment;
    private List<String> courseElements = new ArrayList<>();

    private static final String PERSIST_SQL = "INSERT INTO BOOKED_COURSE_CONTENT (ORIG_COURSE_ID) VALUES (?)";
    private static PreparedStatement PERSIST_SMT = null;

    public BookedCoursesContent(int originalCourseId ){
        this.originalCourseId = originalCourseId;
    }

    public boolean persist(H2Connection conn) {
        try {
            if (PERSIST_SMT == null) {
                PERSIST_SMT = conn.prepareStatement(PERSIST_SQL );
            }
            PERSIST_SMT.setInt(1, originalCourseId);
            PERSIST_SMT.executeUpdate();

            ResultSet rs = PERSIST_SMT.getGeneratedKeys();
            if ( rs.next() ) {
                id = rs.getInt( 1 );
            }

            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public void addCourseElement( String element){
        courseElements.add(element);
    }

    private static final String SET_STRING_FIELD_SQL = "UPDATE BOOKED_COURSE_CONTENT SET %s = ? WHERE ID = ?";
    private static PreparedStatement SET_STRING_FIELD_SMT = null;

    public boolean updateField(H2Connection conn, String fieldName, String value) throws SQLException {
        SET_STRING_FIELD_SMT = conn.prepareStatement(String.format(SET_STRING_FIELD_SQL, fieldName) );
        SET_STRING_FIELD_SMT.setString(1, value);
        SET_STRING_FIELD_SMT.setInt(2, id);
        return SET_STRING_FIELD_SMT.executeUpdate() > 0;

    }

    public boolean updateField(H2Connection conn, String fieldName, int value) throws SQLException {
        String sql = String.format("UPDATE BOOKED_COURSE_CONTENT SET %s = %d WHERE ID = %d", fieldName, value, id);
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sql) > 0;
    }

    public String getCourseElimentString(){
        return String.join("\n", courseElements);
    }
}
