package com.udsl.peaktraining.db;

import com.udsl.peaktraining.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class DbConnection {
    private static final Logger logger = LogManager.getLogger(DbConnection.class.getName());

    private Connection conn = null;

    public DbConnection() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            if ("XENOSMILUS".equals(hostname)) { // Home
                conn = DriverManager.getConnection("jdbc:postgresql://smilodon:5432/postgres", "iangodman", "password");
            }
            else{ // Peak Training
                conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.127:5432/postgres", "postgres", "password");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(99);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(101);
        }
    }

    public void closeConection() throws SQLException {
        if (conn != null) {
            conn.close();
            conn = null;
        }
    }

    public void startTrans() throws SQLException {
        conn.setAutoCommit(false);
    }

    public void doCommit() throws SQLException {
        conn.commit();
    }

    public void doRollback() throws SQLException {
        conn.rollback();
    }

    private static final String SAVE_COMPANY_SQL = "INSERT INTO company (company_name, addr1, addr2, addr3, addr4, postcode) VALUES (?, ?, ?, ?, ?, ?)";

    private PreparedStatement saveCompanyStmt = null;
    public int saveCompany(Company company) throws SQLException {
        int generatedkey = 0;
        if (saveCompanyStmt == null) {
            saveCompanyStmt = conn.prepareStatement(SAVE_COMPANY_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveCompanyStmt.setString(1, company.getName());
        saveCompanyStmt.setString(2, company.getAddress1());
        saveCompanyStmt.setString(3, company.getAddress2());
        saveCompanyStmt.setString(4, company.getAddress3());
        saveCompanyStmt.setString(5, company.getAddress4());
        saveCompanyStmt.setString(6, company.getPostcode());
        int inserted = saveCompanyStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveCompanyStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Company Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_CONTACT_SQL = "INSERT INTO contact (company_id, name, email, phone, mobile) VALUES (?, ?, ?, ?, ?)";

    PreparedStatement saveContactStmt = null ;
    public int saveContact(Contact contact) throws SQLException {
        int generatedkey = 0;
        if (saveContactStmt == null) {
            saveContactStmt = conn.prepareStatement(SAVE_CONTACT_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveContactStmt.setInt(1, contact.getCompanyId());
        saveContactStmt.setString(2, contact.getName());
        saveContactStmt.setString(3, contact.getEmail());
        saveContactStmt.setString(4, contact.getPhone());
        saveContactStmt.setString(5, contact.getMobile());

        int inserted = saveContactStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveContactStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Contact Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_TRAINEE_SQL = "INSERT INTO trainee (company_id, forename, surname) VALUES (?, ?, ?)";
    PreparedStatement saveTraineeStmt = null ;
    public int saveTrainee(Trainee trainee, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        if (saveTraineeStmt == null) {
            saveTraineeStmt = conn.prepareStatement(SAVE_TRAINEE_SQL, Statement.RETURN_GENERATED_KEYS);
        }

        saveTraineeStmt.setInt(1, lookups.getNewCompanyId(trainee.getCompanyId()));
        saveTraineeStmt.setString(2, trainee.getForename());
        saveTraineeStmt.setString(3, trainee.getSurname());

        int inserted = saveTraineeStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveTraineeStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Trainee Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_SQL = "INSERT INTO course_def (name, description, course_number, def_days, default_cert_id) VALUES (?, ?, ?, 1, 0)";
    PreparedStatement saveCourseStmt = null ;

    public int saveCourse(Course course) throws SQLException {
        int generatedkey = 0;
        if (saveCourseStmt == null) {
            saveCourseStmt = conn.prepareStatement(SAVE_COURSE_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveCourseStmt.setString(1, course.getCoursetitle());
        saveCourseStmt.setString(2, course.getCoursetitle());
        saveCourseStmt.setString(3, course.getNextCourseRef());

        int inserted = saveCourseStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveCourseStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Course Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_INS_SQL = "INSERT INTO course_ins (course_def_id, instance_number, description, start_date, days, held_at, instructor_id, examiner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement saveCourseInsStmt = null ;
    public int saveCourseIns(CourseIns courseIns, Lookups lookups) throws SQLException {
        logger.info("Saving courseIns: {}", courseIns);
        int generatedkey = 0;
        if (saveCourseInsStmt == null) {
            saveCourseInsStmt = conn.prepareStatement(SAVE_COURSE_INS_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveCourseInsStmt.setInt(1, lookups.getCourseDefId(courseIns.getCourseTemplateId()));
        saveCourseInsStmt.setInt(2, courseIns.getAndSetInstanceNumber());
        saveCourseInsStmt.setString(3, courseIns.getDescription());
        saveCourseInsStmt.setDate(4, Date.valueOf(courseIns.getStartDate()));
        saveCourseInsStmt.setInt(5, courseIns.getDays());
        saveCourseInsStmt.setString(6, courseIns.getHeldAt());
        // Old system only has examiner so set Instructor and Examiner to same value.
        saveCourseInsStmt.setInt(7, lookups.getInstructor(courseIns.getExaminer()));
        saveCourseInsStmt.setInt(8, lookups.getExaminer(courseIns.getExaminer()));

        int inserted = saveCourseInsStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveCourseInsStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Course Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_INSTRUCTOR_SQL = "INSERT INTO instructor (name, reg_num) VALUES (?, ?)";
    PreparedStatement saveInstructorStmt = null ;
    public int saveTrainer(InstructorExaminer instructor, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        if (saveInstructorStmt == null) {
            saveInstructorStmt = conn.prepareStatement(SAVE_INSTRUCTOR_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveInstructorStmt.setString(1, instructor.getForename() + " " + instructor.getSurname());
        saveInstructorStmt.setString(2, instructor.getRegNumber());

        int inserted = saveInstructorStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveInstructorStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Instructor Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_EXAMINER_SQL = "INSERT INTO examiner (name, reg_num) VALUES (?, ?)";
    PreparedStatement saveExaminerStmt = null ;
    public int saveExaminer(InstructorExaminer examiner, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        if (saveExaminerStmt == null) {
            saveExaminerStmt = conn.prepareStatement(SAVE_EXAMINER_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveExaminerStmt.setString(1, examiner.getForename() + " " + examiner.getSurname());
        saveExaminerStmt.setString(2, examiner.getRegNumber());

        int inserted = saveExaminerStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveExaminerStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Examiner Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_ATTENDEE_SQL = "INSERT INTO attendees (trainee_id, course_ins_id) VALUES (?, ?)";
    PreparedStatement saveAttendeeStmt = null ;
    // Attendants records become Attendee and results records
    public int saveAttendee(Attendants attendee, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        if (saveAttendeeStmt == null) {
            saveAttendeeStmt = conn.prepareStatement(SAVE_ATTENDEE_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveAttendeeStmt.setInt(1, lookups.getNewTrianeeId(attendee.getDelegateID()));
        saveAttendeeStmt.setInt(2, lookups.getCourseInsId(attendee.getCourseID()));

        int inserted = saveAttendeeStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveAttendeeStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Attendee Primary Key {}", generatedkey);
            }
        }
        return generatedkey;
    }

    private static final String SAVE_COURSE_RESULTS_SQL = "INSERT INTO course_results (attendee_id, course_ins, pass, theory, faults, notes, issued_id) VALUES (?, ?, ?, ?, ?, ?, 0)";
    PreparedStatement saveCourseResultsStmt = null ;
    public void saveResults(Attendants attendee, Lookups lookups) throws SQLException {
        if (saveCourseResultsStmt == null) {
            saveCourseResultsStmt = conn.prepareStatement(SAVE_COURSE_RESULTS_SQL);
        }
        saveCourseResultsStmt.setInt(1, attendee.getId());
        saveCourseResultsStmt.setInt(2, lookups.getCourseInsId(attendee.getCourseID()));
        saveCourseResultsStmt.setBoolean(3, attendee.isPassed());

        if (attendee.getTheory() == null) {
            saveCourseResultsStmt.setInt(4, 0);
        }
        else {
            saveCourseResultsStmt.setInt(4, attendee.getTheory());
        }

        if (attendee.getPracticalFaults() == null) {
            saveCourseResultsStmt.setNull(5, java.sql.Types.INTEGER);
        }
        else {
            saveCourseResultsStmt.setInt(5, attendee.getPracticalFaults());
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
        saveCourseResultsStmt.setString(6, str.toString());
        saveCourseResultsStmt.executeUpdate();
    }

    private static final String SAVE_CERTIFICATE_TYPE_SQL = "INSERT INTO certificate_type (certificate_type_name, prefix) VALUES ('%s', '%s')";

    private static final String GET_COURSE_INS_COUNT_SQL = "SELECT count(*) from course_ins WHERE course_def_id = ?";
    private static final String GET_COURSE_DAYS_SQL = "SELECT days, count(days) as days_count from course_ins WHERE course_def_id = ? and days != 0 group by days order by days_count desc;";
    private static final String UPDATE_COURSE_DEF_SQL = "UPDATE course_def SET next_instance = ?, def_days = ? WHERE course_def_id = ?";
    PreparedStatement getCourseInsCountStmt = null ;
    PreparedStatement getCourseDaysStmt = null ;
    PreparedStatement updateCourseDefStmt = null ;

    public void updateCourseDef(List<Integer> courseDefIdList) throws SQLException {
        if (getCourseInsCountStmt == null) {
            getCourseInsCountStmt = conn.prepareStatement(GET_COURSE_INS_COUNT_SQL);
        }
        if (getCourseDaysStmt == null) {
            getCourseDaysStmt = conn.prepareStatement(GET_COURSE_DAYS_SQL);
        }
        if (saveCourseResultsStmt == null) {
            updateCourseDefStmt = conn.prepareStatement(UPDATE_COURSE_DEF_SQL);
        }
        for(Integer id : courseDefIdList) {
            getCourseInsCountStmt.setInt(1, id);
            try (ResultSet rsCount = getCourseInsCountStmt.executeQuery()) {
                rsCount.next(); // Count query always returns a result
                int count = rsCount.getInt(1);
                int days = 1; // default value
                getCourseDaysStmt.setInt(1, id);
                try (ResultSet rsDays = getCourseDaysStmt.executeQuery()) {
                    if (rsDays.next()){ // query may not return a result!
                        days = rsDays.getInt(1);
                    }
                    logger.info("Found {} records for course def {}", count, id);
                    updateCourseDefStmt.setInt(1, count + 1);
                    updateCourseDefStmt.setInt(2, days);
                    updateCourseDefStmt.setInt(3, id);
                    updateCourseDefStmt.executeUpdate();
                }
            }
        }
    }

    private static final String UPDATE_COURSE_DEF_CERT_SQL = "UPDATE course_def SET default_cert_id = ? WHERE name = ?";
    PreparedStatement updateCourseDefCertStmt = null ;

    public void updateCourseDefCert( String name, int key) throws SQLException {
        logger.info("name '{}', key {}", name, key);
        if (updateCourseDefCertStmt == null) {
            updateCourseDefCertStmt = conn.prepareStatement(UPDATE_COURSE_DEF_CERT_SQL);
        }
        updateCourseDefCertStmt.setInt(1, key);
        updateCourseDefCertStmt.setString(2, name);
        updateCourseDefCertStmt.executeUpdate();
    }

    private static final String GET_COURSE_DEF_NAMES_SQL = "SELECT name from course_def";
    PreparedStatement getCourseDefNamesStmt = null ;

    public List<String> getCourseDefName() throws SQLException {
        if (getCourseDefNamesStmt == null) {
            getCourseDefNamesStmt = conn.prepareStatement(GET_COURSE_DEF_NAMES_SQL);
        }
        List<String> nameList = new ArrayList<>();
        ResultSet rs = getCourseDefNamesStmt.executeQuery();
        while(rs.next()){
            nameList.add(rs.getString(1));
        }
        return nameList;
    }

    public void executeSQL( String sql)throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.execute(sql);
    }

    /**
     * Get string of field names.
     * @param rs
     * @return string representation of field names
     * @throws SQLException
     */
    public String resultsetFieldsAndValuesToString(ResultSet rs) throws SQLException {
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
    public void logResultset(ResultSet rs) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("Result set = '{}'", resultsetFieldsAndValuesToString(rs));
        }
    }
}
