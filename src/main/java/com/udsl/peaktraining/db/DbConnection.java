package com.udsl.peaktraining.db;

import com.udsl.peaktraining.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.List;

public class DbConnection {
    private static final Logger logger = LogManager.getLogger(DbConnection.class.getName());

    private static Connection instance = null;

    private static Connection getInstance() throws SQLException {
        if (instance == null) {
          // Home
          instance = DriverManager.getConnection("jdbc:postgresql://smilodon:5432/postgres", "iangodman", "password");
          // Peak Training
          // instance = DriverManager.getConnection("jdbc:postgresql://192.168.1.127:5432/postgres", "postgres", "password");
        }
        return instance;
    }

    public static void closeConection() throws SQLException {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    public static void startTrans() throws SQLException {
        getInstance().setAutoCommit(false);
    }

    public static void doCommit() throws SQLException {
        getInstance().commit();
    }

    public static void doRollback() throws SQLException {
        getInstance().rollback();
    }

    private static final String SAVE_COMPANY_SQL = "INSERT INTO company (company_name, addr1, addr2, addr3, addr4, postcode) VALUES (?, ?, ?, ?, ?, ?)";

    public static int saveCompany(Company company) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_COMPANY_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, company.getName());
        stmt.setString(2, company.getAddress1());
        stmt.setString(3, company.getAddress2());
        stmt.setString(4, company.getAddress3());
        stmt.setString(5, company.getAddress4());
        stmt.setString(6, company.getPostcode());
        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Company Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_CONTACT_SQL = "INSERT INTO contact (company_id, name, email, phone, mobile) VALUES (?, ?, ?, ?, ?)";

    public static int saveContact(Contact contact) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_CONTACT_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, contact.getCompanyId());
        stmt.setString(2, contact.getName());
        stmt.setString(3, contact.getEmail());
        stmt.setString(4, contact.getPhone());
        stmt.setString(5, contact.getMobile());

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Contact Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_TRAINEE_SQL = "INSERT INTO trainee (company_id, forename, surname) VALUES (?, ?, ?)";

    public static int saveTrainee(Trainee trainee, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_TRAINEE_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, lookups.getNewCompanyId(trainee.getCompanyId()));
        stmt.setString(2, trainee.getForename());
        stmt.setString(3, trainee.getSurname());

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Trainee Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_SQL = "INSERT INTO course_def (name, description, course_number, def_days, default_cert_id) VALUES (?, ?, ?, 1, 0)";

    public static int saveCourse(Course course, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_COURSE_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, course.getCoursetitle());
        stmt.setString(2, course.getCoursetitle());
        stmt.setString(3, course.getNextCourseRef());

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Course Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_INS_SQL = "INSERT INTO course_ins (course_def_id, instance_number, description, start_date, days, held_at, instructor_id, examiner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static int saveCourseIns(CourseIns courseIns, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_COURSE_INS_SQL, Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, lookups.getCourseDefId(courseIns.getCourseTemplateId()));
        stmt.setInt(2, courseIns.getAndSetInstanceNumber());
        stmt.setString(3, courseIns.getDescription());
        stmt.setDate(4, Date.valueOf(courseIns.getStartDate()));
        stmt.setInt(5, courseIns.getDays());
        stmt.setString(6, courseIns.getHeldAt());
        // Old system only has examiner so set Instructor and Examiner to same value.
        stmt.setInt(7, lookups.getInstructor(courseIns.getExaminer()));
        stmt.setInt(8, lookups.getExaminer(courseIns.getExaminer()));

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Course Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_INSTRUCTOR_SQL = "INSERT INTO instructor (name, reg_num) VALUES (?, ?)";

    public static int saveTrainer(InstructorExaminer instructor, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_INSTRUCTOR_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, instructor.getForename() + " " + instructor.getSurname());
        stmt.setString(2, instructor.getRegNumber());

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Instructor Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_EXAMINER_SQL = "INSERT INTO examiner (name, reg_num) VALUES (?, ?)";

    public static int saveExaminer(InstructorExaminer examiner, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_EXAMINER_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, examiner.getForename() + " " + examiner.getSurname());
        stmt.setString(2, examiner.getRegNumber());

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Examiner Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_ATTENDEE_SQL = "INSERT INTO attendees (trainee_id, course_ins_id) VALUES (?, ?)";

    // Attendants records become Attendee and results records
    public static int saveAttendee(Attendants attendee, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_ATTENDEE_SQL, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, lookups.getNewTrianeeId(attendee.getDelegateID()));
        stmt.setInt(2, lookups.getCourseInsId(attendee.getCourseID()));

        int inserted = stmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Attendee Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_RESULTS_SQL = "INSERT INTO course_results (attendee_id, pass, theory, faults, notes, issued_id) VALUES (?, ?, ?, ?, ?, 0)";

    public static void saveResults(Attendants attendee, Lookups lookups) throws SQLException {
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_COURSE_RESULTS_SQL);
        stmt.setInt(1, attendee.getId());
        stmt.setBoolean(2, attendee.isPassed());

        if (attendee.getTheory() == null) {
            stmt.setInt(3, 0);
        }
        else {
            stmt.setInt(3, attendee.getTheory());
        }

        if (attendee.getPracticalFaults() == null) {
            stmt.setNull(4, java.sql.Types.INTEGER);
        }
        else {
            stmt.setInt(4, attendee.getPracticalFaults());
        }

        StringBuilder str = new StringBuilder();
        String failReason = attendee.getFailReason();
        if (failReason != null && failReason.length() > 0) {
            str.append("Reason for failure: ");
            str.append(failReason);
        }
        String furtherTraining = attendee.getFurtherTraining();
        if (furtherTraining != null && furtherTraining.length() > 0) {
            if (str.length() > 0) str.append("\n");
            str.append("Further training: ");
            str.append(furtherTraining);
        }
        stmt.setString(5, str.toString());
        stmt.executeUpdate();
    }

    private static final String SAVE_CERTIFICATE_TYPE_SQL = "INSERT INTO certificate_type (certificate_type_id, prefix, next_num) VALUES (0, 'N/A', 0)";

    public static void createCertificateType() throws SQLException {
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_CERTIFICATE_TYPE_SQL);
        stmt.executeUpdate();
    }

    private static final String SAVE_CERTIFICATE_DEF_SQL = "INSERT INTO certificate_def (certificate_def_id, certificate_type_id, certificate_name, certificate_title, certificate_template) VALUES (0, 0, 'NOT IMPORTED', 'N/A', 'NOT TO BE USED')";

    public static void createCertificateDef() throws SQLException {
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(SAVE_CERTIFICATE_DEF_SQL);
        stmt.executeUpdate();
    }

    private static final String GET_COURSE_INS_COUT_SQL = "SELECT count(*) from course_ins WHERE course_def_id = ?";
    private static final String UPDATE_COURSE_DEF_SQL = "UPDATE course_def SET next_instance = ? WHERE course_def_id = ?";

    public static void updateCourseDef(List<Integer> courseDefIdList) throws SQLException {
        Connection conn = getInstance();
        PreparedStatement stmt = conn.prepareStatement(GET_COURSE_INS_COUT_SQL);
        PreparedStatement stmtUpdate = conn.prepareStatement(UPDATE_COURSE_DEF_SQL);
        for(Integer id : courseDefIdList) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            logger.info("Found {} records for course def {}", count, id);
            stmtUpdate.setInt(1, count);
            stmtUpdate.setInt(2, id);
            stmtUpdate.executeUpdate();
        }
    }

    /**
     * Get string of field names.
     * @param rs
     * @return string representation of field names
     * @throws SQLException
     */
    public static String resultsetFieldsAndValuesToString(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        StringBuilder str = new StringBuilder();
        for (int i = 1; i <= columnsNumber; i++) {
            if (i > 1) str.append(",  ");
            str.append(rsmd.getColumnName(i));
            str.append(" = ");
            str.append(rs.getString(i));
        }
        return str.toString();
    }

    /**
     * Logs the field names in the result set only if logger is debug.
     *
     * @param rs the result set you wnat to log the filed names of.
     * @throws SQLException
     */
    public static void logResultset(ResultSet rs) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Result set = '{}'", resultsetFieldsAndValuesToString(rs));
        }
    }


}
