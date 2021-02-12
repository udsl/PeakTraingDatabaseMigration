package com.udsl.peaktraining.data;

import com.udsl.peaktraining.db.H2Connection;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;

@Data
public class BookedCoursesRecord {
    private static final Logger logger = LogManager.getLogger(BookedCoursesRecord.class.getName());

    private int id;
    private int courseId;
    private int templateCourseID;
    private String courseReference;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;

    public BookedCoursesRecord(ResultSet rs ) throws SQLException {
        courseId = rs.getInt("CourseID");
        templateCourseID= rs.getInt("TemplateCourseID");
        courseReference = rs.getString("CourseReference");
        courseStartDate = rs.getDate("CourseStartDate").toLocalDate();
        courseEndDate = rs.getDate("CourseEndDate").toLocalDate();

        logger.info("CourseID: {}, TemplateCourseID: {}, CourseReference: '{}', CourseStartDate: {}, CourseEndDate: {}",
                    courseId, templateCourseID, courseReference, courseStartDate, courseEndDate
            );
    }

    private static final String PERSIST_SQL = "INSERT INTO BOOKED_COURSE_MAP (ORIG_COURSE_ID, TEMPLATE_ID, COURSE_REF, START_DATE, END_DATE) VALUES (?, ?, ?, ?, ?)";
    private static PreparedStatement PERSIST_SMT = null;

    public boolean persist(H2Connection conn) {
        try {
            if (PERSIST_SMT == null) {
                PERSIST_SMT = conn.prepareStatement(PERSIST_SQL );
            }
            PERSIST_SMT.setInt(1, courseId);
            PERSIST_SMT.setInt(2, templateCourseID);
            PERSIST_SMT.setString(3, courseReference);
            PERSIST_SMT.setDate(4, Date.valueOf(courseStartDate));
            PERSIST_SMT.setDate(5, Date.valueOf(courseEndDate));
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

    public boolean updateField(H2Connection conn, String fieldName, String value) throws SQLException {
        String sql = String.format("UPDATE BOOKED_COURSE_MAP SET %s = %s WHERE ID = %d", fieldName, value, id);
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sql) > 0;
    }

    public boolean updateField(H2Connection conn, String fieldName, int value) throws SQLException {
        String sql = String.format("UPDATE BOOKED_COURSE_MAP SET %s = %d WHERE ID = %d", fieldName, value, id);
        Statement stmt = conn.createStatement();
        return stmt.executeUpdate(sql) > 0;
    }

}
