package com.udsl.peaktraining.db;

import com.udsl.peaktraining.*;
import com.udsl.peaktraining.data.*;
import com.udsl.peaktraining.template.TemplateData;
import com.udsl.peaktraining.validation.ValidationPostAttendee;
import com.udsl.peaktraining.validation.ValidationPostCourse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.max;

@Component
public class DbConnection {
    private static final Logger logger = LogManager.getLogger(DbConnection.class.getName());

    @Value("${needTransaction}")
    private boolean needTransaction;

    @Value("${clearPostgress}")
    private boolean clearPostgress;

    @Value("${doImport}")
    private boolean doImport;

    @Value("#{'${tablesToClearList}'.split(',')}")
    List<String> tablesClearList;

    private Connection conn = null;

    @PostConstruct
    public void openConnection() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            if ("XENOSMILUS".equals(hostname) || "SABERTOOTH".equals(hostname)) { // Home
                conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.7:5432/postgres", "iangodman", "password");
            } else { // Peak Training
                conn = DriverManager.getConnection("jdbc:postgresql://192.168.1.127:5432/postgres", "postgres", "password");
            }
            if (clearPostgress && !doImport){
                doClearPostgres();
            }
            if (needTransaction) {
                conn.setAutoCommit(false);
            }
        }catch (RuntimeException e) {
            System.out.println("except");
        }catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(99);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(101);
        }
    }

    private void doClearPostgres() {
        try {
            String truncateSQL = "TRUNCATE " ;
            for(String tableName : tablesClearList){
                String theSQL = truncateSQL + tableName + " CASCADE";
                logger.info(theSQL);
                PreparedStatement stmt = conn.prepareStatement(theSQL);
                stmt.execute();
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    @PreDestroy
    public void closeConnection() throws SQLException {
        if (conn != null) {
            if (!conn.getAutoCommit()){
                conn.commit();
            }
            conn.close();
            conn = null;
        }
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

    private static final String SAVE_CONTACT_SQL = "INSERT INTO contact (company_id, name, email, phone) VALUES (?, ?, ?, ?)";

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

    private static final String SAVE_COURSE_SQL = "INSERT INTO course_def (name, description, course_number, def_days, default_cert_id) VALUES (?, ?, ?, 1, 1)";
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

    private static final String SAVE_COURSE_INS_SQL = "INSERT INTO course_ins (course_def_id, description, start_date, days, held_at, instructor_id, examiner_id) VALUES (?, ?, ?, ?, ?, 1, 1)";
    PreparedStatement saveCourseInsStmt = null ;
    public int saveCourseIns(CourseIns courseIns, Lookups lookups) throws SQLException {
        logger.info("Saving courseIns: {}", courseIns);
        int generatedkey = 0;
        if (saveCourseInsStmt == null) {
            saveCourseInsStmt = conn.prepareStatement(SAVE_COURSE_INS_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        saveCourseInsStmt.setInt(1, lookups.getCourseDefId(courseIns.getCourseTemplateId()));
        saveCourseInsStmt.setString(2, courseIns.getDescription());
        saveCourseInsStmt.setDate(3, Date.valueOf(courseIns.getStartDate()));
        saveCourseInsStmt.setInt(4, courseIns.getDays());
        saveCourseInsStmt.setString(5, courseIns.getHeldAt());
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

    private static final String SAVE_ATTENDEE_SQL = "INSERT INTO attendees (trainee_id, course_ins_id) VALUES (?, ?)";
    PreparedStatement saveAttendeeStmt = null ;
    // Attendants records become Attendee and results records
    public int saveAttendee(Attendants attendee, Lookups lookups) throws SQLException {
        int generatedkey = 0;
        if (saveAttendeeStmt == null) {
            saveAttendeeStmt = conn.prepareStatement(SAVE_ATTENDEE_SQL, Statement.RETURN_GENERATED_KEYS);
        }
        int mappedTranee = lookups.getNewTrianeeId(attendee.getDelegateID());
        int mappedCourse = lookups.getMappedCourseInsId(attendee.getCourseID());
        saveAttendeeStmt.setInt(1, mappedTranee);
        saveAttendeeStmt.setInt(2, mappedCourse);
        int inserted = saveAttendeeStmt.executeUpdate();
        if (inserted == 1) {
            ResultSet rs = saveAttendeeStmt.getGeneratedKeys();
            if (rs.next()) {
                generatedkey = rs.getInt(1);
                logger.info("Auto Generated Attendee Primary Key {}", generatedkey);
            }
        }
        logger.info("Inserted ID {}, attendee {}, course {}", generatedkey, mappedTranee, mappedCourse);
        return generatedkey;
    }

    private static final String SAVE_COURSE_RESULTS_SQL = "INSERT INTO course_results (attendee_id, course_ins, pass, theory, faults, notes, issued_id) VALUES (?, ?, ?, ?, ?, ?, 0)";
    PreparedStatement saveCourseResultsStmt = null ;
    public void saveResults(Attendants attendee, Lookups lookups) throws SQLException {
        if (saveCourseResultsStmt == null) {
            saveCourseResultsStmt = conn.prepareStatement(SAVE_COURSE_RESULTS_SQL);
        }
        saveCourseResultsStmt.setInt(1, attendee.getId());
        saveCourseResultsStmt.setInt(2, lookups.getMappedCourseInsId(attendee.getCourseID()));
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


    private static final String SAVE_IMPORTED_TEMPLATE_SQL = """
            INSERT INTO course_def 
            (name, description, course_type, course_number, def_days, default_cert_id, instructor_id, examiner_id, course_elements) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    PreparedStatement saveImportedTemplateStmt = null ;

    public int saveImportedTemplate(TemplateData template) {
        int generatedkey = 0;
        try {
            if (saveImportedTemplateStmt == null) {
                saveImportedTemplateStmt = conn.prepareStatement(SAVE_IMPORTED_TEMPLATE_SQL, Statement.RETURN_GENERATED_KEYS);
            }
            saveImportedTemplateStmt.setString(1, template.getName());
            saveImportedTemplateStmt.setString(2, template.getDescription());
            saveImportedTemplateStmt.setString(3, template.getCourse_type());
            saveImportedTemplateStmt.setString(4, template.getCourse_number());
            saveImportedTemplateStmt.setInt(5, template.getDef_days());
            saveImportedTemplateStmt.setInt(6, template.getDefault_cert_id());

            Integer insId = template.getInstructor_id();
            if (insId == null) {
                saveImportedTemplateStmt.setNull(7, Types.INTEGER);
            }
            else {
                saveImportedTemplateStmt.setInt(7, template.getInstructor_id());
            }

//            Integer examId = template.getExaminer_id();
//            if (examId == null) {
//                saveImportedTemplateStmt.setNull(8, Types.INTEGER);
//            }
//            else{
               saveImportedTemplateStmt.setInt(8, template.getExaminer_id());
//            }

            Array elementArray = null;
            List<String> elements = template.getCourse_elements();
            if (elements != null) {
                String[] strArray = elements.toArray(new String[0]);
                elementArray = conn.createArrayOf("text", strArray);
            }

            saveImportedTemplateStmt.setArray(9, elementArray);

            int inserted = saveImportedTemplateStmt.executeUpdate();
            if (inserted == 1) {
                ResultSet rs = saveImportedTemplateStmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedkey = rs.getInt(1);
                    logger.info("Auto Generated TemplateInserted Primary Key {}", generatedkey);
                }
            }
            logger.info("Inserted ID {}, template {}, course {}", generatedkey, template.getName(), template.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedkey;
    }


    private static final String GET_IMPORTED_TEMPLATE_COUNT_SQL = "SELECT COUNT(*) FROM course_def WHERE name = ? AND description = ?";
    PreparedStatement getImportedTemplateCountStmt = null ;

    public boolean importedTemplateExists(TemplateData template) {
        try {
            if (getImportedTemplateCountStmt == null) {
                getImportedTemplateCountStmt = conn.prepareStatement(GET_IMPORTED_TEMPLATE_COUNT_SQL);
            }
            getImportedTemplateCountStmt.setString(1, template.getName());
            getImportedTemplateCountStmt.setString(2, template.getDescription());

            try (ResultSet rs = getImportedTemplateCountStmt.executeQuery()) {
                if (rs.next()) {
                    int result = rs.getInt(1);
                    return result > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static final String CHECK_COURSE_INS_SQL = "SELECT * FROM course_ins WHERE course_ins_id = ?";
    PreparedStatement checkCourseInsStmt = null ;
    private boolean checkCourseRecord(int courseId) {
        logger.info("checkCourseRecord (courseId: {})", courseId);
        try {
            if (checkCourseInsStmt == null) {
                checkCourseInsStmt = conn.prepareStatement(CHECK_COURSE_INS_SQL);
            }
            checkCourseInsStmt.setInt(1, courseId);
            try (ResultSet rs = checkCourseInsStmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return false;
    }

    private static final String UPDATE_COURSE_INS_SQL = "UPDATE course_ins SET model = ?, capacity = ?, attachment = ?, equipment = ?, course_elements = ?  WHERE course_ins_id = ?";
    PreparedStatement updateCourseInsStmt = null ;
    public void updateCourseInsRecord (int courseId, String model, String capacity, String attachment, String equipment, List<String> course_elements) throws SQLException {
        logger.info("updateCourseInsRecord (courseId: {}, model: {}, capacity: {}, attachment: {}, equipment: {}, course_elements: {})", courseId, model, capacity, attachment, equipment, course_elements);

        if (checkCourseRecord(courseId)) {
            if (updateCourseInsStmt == null) {
                updateCourseInsStmt = conn.prepareStatement(UPDATE_COURSE_INS_SQL);
            }
            updateCourseInsStmt.setString(1, model);
            updateCourseInsStmt.setString(2, capacity);
            updateCourseInsStmt.setString(3, attachment);
            updateCourseInsStmt.setString(4, equipment);

            String[] strArray = course_elements.toArray(new String[course_elements.size()]);
            Array elementArray = conn.createArrayOf("text", strArray);
            updateCourseInsStmt.setArray(5, elementArray);

            updateCourseInsStmt.setInt(6, courseId);
            updateCourseInsStmt.executeUpdate();
        }
        else{
            logger.error("course_ins record not found to update! ");
        }
    }

    private static final String GRT_COURSE_INS_SQL = "SELECT * FROM course_ins WHERE course_ins_id = ?";
    PreparedStatement getCourseInsStmt = null ;
    public CourseInsRecord getCourseInsRecord(int courseId) throws SQLException {
        CourseInsRecord result = null;
        if (getCourseInsStmt == null) {
            getCourseInsStmt = conn.prepareStatement(GRT_COURSE_INS_SQL);
        }
        getCourseInsStmt.setInt(1, courseId);
        try (ResultSet rs = getCourseInsStmt.executeQuery()) {
            if (rs.next()) {
                result = new CourseInsRecord(rs);
            }
        }
        return result;
    }

    private static final String GET_COURSE_INS_COUNT_SQL = "SELECT count(*) from course_ins WHERE course_def_id = ?";
    private static final String GET_COURSE_DAYS_SQL = "SELECT days, count(days) as days_count from course_ins WHERE course_def_id = ? and days != 0 group by days order by days_count desc;";
    private static final String UPDATE_COURSE_DEF_SQL = "UPDATE course_def SET def_days = ? WHERE course_def_id = ?";
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
                    updateCourseDefStmt.setInt(1, days);
                    updateCourseDefStmt.setInt(2, id);
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

    private static final String GET_VALIDATION_ATTENDEE_SQL = "SELECT attendee_id, trainee_id, course_ins_id FROM attendees WHERE attendee_id = ?";
    PreparedStatement getValidationAttendeeStmt = null ;
    public Optional<ValidationPostAttendee> getValidationPostAttendee(int id){
        try {
            if (getValidationAttendeeStmt == null) {
                getValidationAttendeeStmt = conn.prepareStatement(GET_VALIDATION_ATTENDEE_SQL);
            }
            getValidationAttendeeStmt.setInt(1, id);
            ResultSet rs = getValidationAttendeeStmt.executeQuery();
            if(rs.next()){
                return Optional.of(new ValidationPostAttendee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static final String GET_VALIDATION_COURSE_SQL = "SELECT * FROM course_ins WHERE course_ins_id = ?";
    PreparedStatement getValidationCourseStmt = null ;
    public Optional<ValidationPostCourse> getValidationPostCourse(int id){
        try {
            if (getValidationCourseStmt == null) {
                getValidationCourseStmt = conn.prepareStatement(GET_VALIDATION_COURSE_SQL);
            }
            getValidationCourseStmt.setInt(1, id);
            ResultSet rs = getValidationCourseStmt.executeQuery();
            if(rs.next()){
                return Optional.of(new ValidationPostCourse(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
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


    private static final String GET_MAX_COURSE_NUMBER_SQL = "SELECT course_number FROM course_def WHERE course_def_id = (SELECT MAX(course_def_id) FROM course_def)";
    PreparedStatement getCourseDefMaxNumberStmt = null ;

    public String getCourseDefMaxNumber() throws SQLException {
        if (getCourseDefMaxNumberStmt == null) {
            getCourseDefMaxNumberStmt = conn.prepareStatement(GET_MAX_COURSE_NUMBER_SQL);
        }

        ResultSet rs = getCourseDefMaxNumberStmt.executeQuery();
        if(rs.next()){
            return rs.getString(1);
        }
        throw new SQLException("No records returned");
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

    private PreparedStatement getExistingKeyStmt = null ;
    private final static String EXISTING_KEY_SQL = "SELECT course_def_id FROM course_def WHERE UPPER(name) = ? AND name = description";

    public int getExistingKey(String key){
        try {
            if (getExistingKeyStmt == null) {
                getExistingKeyStmt = conn.prepareStatement(EXISTING_KEY_SQL);
            }
            getExistingKeyStmt.setString(1, key);
            ResultSet rs = getExistingKeyStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            logger.debug("Record not found for {}", key);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        throw new RuntimeException("Key not found for " + key);
    }

    private PreparedStatement setElementsStmt = null ;
    private final static String SET_ELEMENTS__SQL = "UPDATE course_def SET course_elements = ? WHERE course_def_id = ?";

    public void setElements(int id, List<String> elements){
        try {
            if (setElementsStmt == null) {
                setElementsStmt = conn.prepareStatement(SET_ELEMENTS__SQL);
            }

            String[] strArray = elements.toArray(new String[0]);
            Array elementArray = conn.createArrayOf("text", strArray);
            setElementsStmt.setArray(1, elementArray);
            setElementsStmt.setInt(2, id);
            int updated = setElementsStmt.executeUpdate();
            logger.debug("{} records updated for {}", updated, id);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
