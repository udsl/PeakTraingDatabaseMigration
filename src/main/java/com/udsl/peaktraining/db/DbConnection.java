package com.udsl.peaktraining.db;

import com.udsl.peaktraining.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DbConnection {
    private static final Logger logger = LogManager.getLogger(DbConnection.class.getName());

    private static Connection instance = null;

    private static Connection getInstance() throws SQLException {
        if (instance == null) {
            instance = DriverManager.getConnection("jdbc:postgresql://smilodon:5432/postgres", "iangodman", "password");
        }
        return instance;
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

    private static String SAVE_COMPANY_SQL = "INSERT INTO company (company_name, addr1, addr2, addr3, addr4, postcode) VALUES (?, ?, ?, ?, ?, ?)";

    public static int saveCompany(Company company) {
        int generatedkey = 0;
        try {
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
                    System.out.println("Auto Generated Company Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_CONTACT_SQL = "INSERT INTO contact (company_id, name, email, phone, mobile) VALUES (?, ?, ?, ?, ?)";

    public static int saveContact(Contact contact) {
        int generatedkey = 0;
        try {
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
                    System.out.println("Auto Generated Contact Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_TRAINEE_SQL = "INSERT INTO trainee (company_id, forename, surname) VALUES (?, ?, ?)";

    public static int saveTrainee(Trainee trainee, Lookups lookups) {
        int generatedkey = 0;
        try {
            Connection conn = getInstance();
            PreparedStatement stmt = conn.prepareStatement(SAVE_TRAINEE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, lookups.getCo(trainee.getOldId()));
            stmt.setString(2, trainee.getForename());
            stmt.setString(3, trainee.getSurname());

            int inserted = stmt.executeUpdate();
            if (inserted == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedkey = rs.getInt(1);
                    System.out.println("Auto Generated Trainee Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_COURSE_SQL = "INSERT INTO course_def (name, description, course_number, def_days, default_cert_id) VALUES (?, ?, ?, 1, 0)";

    public static int saveCourse(Course course, Lookups lookups) {
        int generatedkey = 0;
        try {
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
                    System.out.println("Auto Generated Course Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_COURSE_INS_SQL = "INSERT INTO course_ins (course_def_id, instance_number, description, start_date, days, held_at) VALUES (?, ?, ?, ?, ?, ?)";


    public static int saveCourseIns(CourseIns courseIns, Lookups lookups) {
        int generatedkey = 0;
        try {
            Connection conn = getInstance();
            PreparedStatement stmt = conn.prepareStatement(SAVE_COURSE_INS_SQL, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, lookups.getCourseId(courseIns.getCourseTemplateId()));
            stmt.setInt(2, courseIns.getAndSetInstanceNumber());
            stmt.setString(3, courseIns.getDescription());
            stmt.setDate(4, Date.valueOf(courseIns.getStartDate()));
            stmt.setInt(5, courseIns.getDays());
            stmt.setString(6, courseIns.getHeldAt());

            int inserted = stmt.executeUpdate();
            if (inserted == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedkey = rs.getInt(1);
                    System.out.println("Auto Generated Course Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            logger.error("CourseIns: {}", courseIns);
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_INSTRUCTOR_SQL = "INSERT INTO instructor (name, reg_num) VALUES (?, ?)";

    public static int saveTrainer(InstructorExaminer instructor, Lookups lookups) {
        int generatedkey = 0;
        try {
            Connection conn = getInstance();
            PreparedStatement stmt = conn.prepareStatement(SAVE_INSTRUCTOR_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, instructor.getForename() + " " + instructor.getSurname());
            stmt.setString(2, instructor.getRegNumber());

            int inserted = stmt.executeUpdate();
            if (inserted == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedkey = rs.getInt(1);
                    System.out.println("Auto Generated Instructor Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

    private static String SAVE_EXAMINER_SQL = "INSERT INTO examiner (name, reg_num) VALUES (?, ?)";

    public static int saveExaminer(InstructorExaminer examiner, Lookups lookups) {
        int generatedkey = 0;
        try {
            Connection conn = getInstance();
            PreparedStatement stmt = conn.prepareStatement(SAVE_EXAMINER_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, examiner.getForename() + " " + examiner.getSurname());
            stmt.setString(2, examiner.getRegNumber());

            int inserted = stmt.executeUpdate();
            if (inserted == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedkey = rs.getInt(1);
                    System.out.println("Auto Generated Examiner Primary Key " + generatedkey);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return generatedkey;
    }

}
