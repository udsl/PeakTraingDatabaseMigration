package com.udsl.peaktraining;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;

public class Lookups {
    private static final Logger logger = LogManager.getLogger(Lookups.class.getName());

    private final Map<Integer, Integer> dupCompanyMap = new HashMap<>();
    private final Map<Integer, InstructorExaminer> instructorMap = new HashMap<>();
    private final Map<Integer, InstructorExaminer> examinerMap = new HashMap<>();

    private Connection conn;
    private Server h2Server = null;

    public Lookups() throws SQLException {
        h2Server = Server.createTcpServer().start();
        if (h2Server.isRunning(true)) {
            logger.error("H2 server was started and is running.");
        } else {
            throw new RuntimeException("Could not start H2 server.");
        }

        JdbcDataSource ds = new JdbcDataSource();
        // ds.setURL("jdbc:h2:~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test");
        ds.setURL("jdbc:h2:tcp://localhost:9092/~/IdeaProjects/PeakTraingDatabaseMigration/app_db/test");
        ds.setUser("sa");
        ds.setPassword("sa");
        try {
            conn = ds.getConnection();
            clearDatabaseTables();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    void closeConection() {
        try {
            if (h2Server != null) {
                conn.close();
                conn = null ;
                h2Server.stop();
                h2Server.shutdown();
                h2Server = null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void clearDatabaseTables(){
        String[] tables = {"COMPANY_MAP", "TRAINEE_MAP", "COURSE_MAP", "ATTENDEE_MAP", "COURSE_INS"} ;
        try(Statement stmt = conn.createStatement()) {
            for (String tableName : tables) {
                stmt.execute(String.format("TRUNCATE TABLE %s", tableName));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static final String SAVE_COMAPNY_MAP_SQL = "INSERT INTO COMPANY_MAP (ID, ORIG_ID, NAME, ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, POSTCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement putCompanyMapStatment = null;

    public void addCo(Company company) throws SQLException {
        if (putCompanyMapStatment == null){
            putCompanyMapStatment = conn.prepareStatement(SAVE_COMAPNY_MAP_SQL);
        }
        putCompanyMapStatment.setInt(1, company.getId());
        putCompanyMapStatment.setInt(2, company.getOldId());
        putCompanyMapStatment.setString(3, company.getName());
        putCompanyMapStatment.setString(4, company.getAddress1());
        putCompanyMapStatment.setString(5, company.getAddress2());
        putCompanyMapStatment.setString(6, company.getAddress3());
        putCompanyMapStatment.setString(7, company.getAddress4());
        putCompanyMapStatment.setString(8, company.getPostcode());
        putCompanyMapStatment.executeUpdate();
     }

    private static final String GET_NEW_ID_FROM_COMPANY_MAP = "SELECT ID FROM COMPANY_MAP WHERE ORIG_ID = ?";
    private PreparedStatement newIdCompanyStatment = null ;

    public int getNewCompanyId(int oldId) throws SQLException {
        logger.info("Looking up company id {}", oldId);
        // Check if its a duplication?
        if (dupCompanyMap.containsKey(oldId)){
            return dupCompanyMap.get(oldId);
        }

        if (newIdCompanyStatment == null ) {
            newIdCompanyStatment = conn.prepareStatement(GET_NEW_ID_FROM_COMPANY_MAP);
        }
        newIdCompanyStatment.setInt(1, oldId);
        ResultSet res = newIdCompanyStatment.executeQuery();
        if (res.next()){
            return res.getInt(1);
        }
        throw new SQLException("Record not found!");
    }

    private static final String SAVE_TRAINEE_MAP_SQL = "INSERT INTO TRAINEE_MAP (ID, ORIG_ID, COMPANY_ID, FORENAME, SURNAME) VALUES (?, ?, ?, ?, ?)";
    private PreparedStatement saveTraineeStatment = null ;

    public void addTrainee(Trainee trainee) {
        logger.debug("Adding Trainee from id {} -> new id {}", trainee.getOldId(), trainee.getId());
        try {
            if (saveTraineeStatment == null) {
                saveTraineeStatment = conn.prepareStatement(SAVE_TRAINEE_MAP_SQL);
            }
            saveTraineeStatment.setInt(1, trainee.getId());
            saveTraineeStatment.setInt(2, trainee.getOldId());
            saveTraineeStatment.setInt(3, trainee.getCompanyId());
            saveTraineeStatment.setString(4, trainee.getForename());
            saveTraineeStatment.setString(5, trainee.getSurname());
            saveTraineeStatment.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static final String GET_TRAINEE_MAP_SQL = "SELECT ID FROM TRAINEE_MAP WHERE ORIG_ID = ?";
    private PreparedStatement newTraineeIdStatment = null ;

    public int getNewTrianeeId(int oldId) throws SQLException {
        logger.info("Looking up trainee id {}", oldId);
        if (newTraineeIdStatment == null) {
            newTraineeIdStatment = conn.prepareStatement(GET_TRAINEE_MAP_SQL);
        }
        newTraineeIdStatment.setInt(1, oldId);
        try(ResultSet res = newTraineeIdStatment.executeQuery()) {
            if (res.next()) {
                return res.getInt(1);
            }
            throw new SQLException("Record not found for trainee id" + oldId);
        }
    }

    private static final String GET_COMAPNY_ID_BY_NAME_SQL = "SELECT ID FROM COMPANY_MAP WHERE LOWER(NAME) = ?";
    private PreparedStatement newCompanyIdStatment = null ;

    public int getCompanyNewIdByName(String name) throws SQLException {
        logger.debug("Looking up company with name '{}'", name);
        if (newCompanyIdStatment == null) {
            newCompanyIdStatment = conn.prepareStatement(GET_COMAPNY_ID_BY_NAME_SQL);
        }
        newCompanyIdStatment.setString(1, name.toLowerCase());
        try(ResultSet res = newCompanyIdStatment.executeQuery()) {
            if (res.next()) {
                return res.getInt(1);
            }
            throw new SQLException("Record not found for " + name);
        }
     }

    public void addDupCoId(int duplicateCoID, int newID) {
        logger.debug("Adding DupCoId from for id {} -> new id {}", duplicateCoID, newID);
        dupCompanyMap.put(duplicateCoID, newID);
    }

    private static final String GET_ID_COURSE_INS_SQL = "SELECT ID FROM COURSE_INS WHERE ORIG_ID = ?";
    private PreparedStatement getIdCourseInsStatment = null ;

    public int getCourseInsId(int oldId) throws SQLException {
        logger.info("Looked up courseInsId {}", oldId);
        if (getIdCourseInsStatment == null){
            getIdCourseInsStatment = conn.prepareStatement(GET_ID_COURSE_INS_SQL);
        }
        int courseInsId = -1;
        getIdCourseInsStatment.setInt(1, oldId);
        try(ResultSet rs = getIdCourseInsStatment.executeQuery()) {
            if (rs.next()) {
                courseInsId = rs.getInt(1);
            }
            else {
                logger.debug("courseIns not found for id {}", oldId);
            }
        }

        logger.info("Returning courseInsId {}", courseInsId);
        return courseInsId;
    }

    private static final String GET_OLD_COURSE_ID_LIST_SQL = "SELECT ORIG_ID FROM COURSE_MAP";
    private PreparedStatement getOldCourseIdListStatment = null ;

    public List<Integer> getCourseDefIds() throws SQLException {
        if (getOldCourseIdListStatment == null){
            getOldCourseIdListStatment = conn.prepareStatement(GET_OLD_COURSE_ID_LIST_SQL);
        }
        List<Integer> result = new ArrayList<>();
        try(ResultSet rs = getOldCourseIdListStatment.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getInt(1));
            }
        }
        return result;
    }

    private static final String GET_NEW_COURSE_ID_SQL = "SELECT ID FROM COURSE_MAP WHERE ORIG_ID = ?";
    private PreparedStatement getNewCourseIdStatment = null ;

    public int getCourseDefId(int oldId) throws SQLException {
        logger.debug("Looked up CourseDef id {}", oldId);
        if (getNewCourseIdStatment == null){
            getNewCourseIdStatment = conn.prepareStatement(GET_NEW_COURSE_ID_SQL);
        }
        getNewCourseIdStatment.setInt(1, oldId);
        try(ResultSet res = getNewCourseIdStatment.executeQuery()) {
            if (res.next()) {
                return res.getInt(1);
            }
        }
        throw new SQLException("Record not found for old course id" + oldId);
    }

    public int getInstructor(int oldId){
        logger.debug("Looking up Instructor id {}", oldId);
        int newInstructorId = instructorMap.get(oldId).getId();
        logger.debug("Returning instructorId {}", newInstructorId);
        return newInstructorId;
    }

    public int getExaminer(int oldId){
        logger.debug("Looking up Examiner id {}", oldId);
        int newExaminerId = examinerMap.get(oldId).getId();
        logger.debug("Returning examinerId {}", newExaminerId);
        return newExaminerId;
    }

    private static final String PUT_COURSE_MAP_SQL = "INSERT INTO COURSE_MAP (ID, ORIG_ID, COURSE_TITLE, CERTIFICATE_PREFIX, CERTIFICATE_COUNT) VALUES (?, ?, ?, ?, ?)";
    private PreparedStatement putCourseStatment = null ;

    public void addCourse(Course course) throws SQLException {
        logger.debug("Adding Course for id {} with new id {}", course.getOldId(), course.getCourseID());
        if (putCourseStatment == null){
            putCourseStatment = conn.prepareStatement(PUT_COURSE_MAP_SQL);
        }
        putCourseStatment.setInt(1, course.getCourseID());
        putCourseStatment.setInt(2, course.getOldId());
        putCourseStatment.setString(3, course.getCoursetitle());
        putCourseStatment.setString(4, course.getCertificatePrefix());
        putCourseStatment.setInt(5, course.getCertificateCount());
        putCourseStatment.executeUpdate();
    }

    public void addInstructor(int oldId, InstructorExaminer instructor) {
        logger.debug("Adding Instructor from old id {} -> {}", oldId, instructor);
        instructorMap.put(oldId, instructor);
    }

     public void addExaminer(int oldId, InstructorExaminer examiner) {
        logger.debug("Adding Examinier from old id {} -> {}", oldId, examiner);
        examinerMap.put(oldId, examiner);
    }

    private static final String PUT_COURSE_INS_MAP_SQL = "INSERT INTO COURSE_INS (ID, ORIG_ID, COURSE_TEMPLATE_ID, INSTANCE_NUMBNER, DESCRIPTION , START_DATE, END_DATE, DAYS, HELD_AT, EXAMINER ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement putCourseInsStatment = null ;

    public void addCourseIns(CourseIns courseIns) throws SQLException {
        logger.debug("Adding CourseIns from old id {} -> {}", courseIns.getOldId(), courseIns.getId());
        if (putCourseInsStatment == null){
            putCourseInsStatment = conn.prepareStatement(PUT_COURSE_INS_MAP_SQL);
        }
        putCourseInsStatment.setInt(1, courseIns.getId());
        putCourseInsStatment.setInt(2, courseIns.getOldId());
        putCourseInsStatment.setInt(3, courseIns.getCourseTemplateId());
        putCourseInsStatment.setInt(4, courseIns.getInstanceNumbner());
        putCourseInsStatment.setString(5, courseIns.getDescription());
        putCourseInsStatment.setDate(6, Date.valueOf(courseIns.getStartDate()));
        putCourseInsStatment.setDate(7, Date.valueOf(courseIns.getEndDate()));
        putCourseInsStatment.setInt(8, courseIns.getDays());
        putCourseInsStatment.setString(9, courseIns.getHeldAt());
        putCourseInsStatment.setInt(10, courseIns.getExaminer());
        putCourseInsStatment.executeUpdate();
    }

    private static final String PUT_ATTENDEE_MAP_SQL = "INSERT INTO ATTENDEE_MAP (ID, ORIG_ID, DELEGATE_ID, COURSE_ID, PASSED, THEORY, PRACTICAL_FAULTS, FAIL_REASON, FURTHER_TRAINING) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement putAttendeeStatment = null ;

    public void addAttendee(Attendants attendee) throws SQLException {
        logger.debug("Adding attendee from old id {} -> {}", attendee.getOldId(), attendee.getId());
        if (putAttendeeStatment == null){
            putAttendeeStatment = conn.prepareStatement(PUT_ATTENDEE_MAP_SQL);
        }
        putAttendeeStatment.setInt(1, attendee.getId());
        putAttendeeStatment.setInt(2, attendee.getOldId());
        putAttendeeStatment.setInt(3, attendee.getDelegateID());
        putAttendeeStatment.setInt(4, attendee.getCourseID());
        putAttendeeStatment.setBoolean(5, attendee.isPassed());

        if (attendee.getTheory() == null){
            putAttendeeStatment.setNull(6, java.sql.Types.INTEGER);
        }
        else {
            putAttendeeStatment.setInt(6, attendee.getTheory());
        }

        if (attendee.getPracticalFaults() == null){
            putAttendeeStatment.setNull(7, java.sql.Types.INTEGER);
        }
        else {
            putAttendeeStatment.setInt(7, attendee.getPracticalFaults());
        }

        putAttendeeStatment.setString(8, attendee.getFailReason());
        putAttendeeStatment.setString(9, attendee.getFurtherTraining());
        putAttendeeStatment.executeUpdate();
    }
}
