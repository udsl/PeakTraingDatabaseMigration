package com.udsl.peaktraining;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Lookups {
    private static final Logger logger = LogManager.getLogger(Lookups.class.getName());

    private final Map<Integer, Company> companyMap = new HashMap<>();
    private final Map<Integer, Integer> dupCompanyMap = new HashMap<>();
    private final Map<Integer, Trainee> traineeMap = new HashMap<>();
    private final Map<Integer, Course> courseMap = new HashMap<>();
    private final Map<Integer, InstructorExaminer> instructorMap = new HashMap<>();
    private final Map<Integer, InstructorExaminer> examinerMap = new HashMap<>();
    private final Map<Integer, CourseIns> courseInsMap = new HashMap<>();

    public void addCo(int oldId, Company company) {
        companyMap.put(oldId, company);
    }

    public int getCo(int oldId) {
        logger.info("Looking up comany id {}", oldId);
        // is the old ID a duplicate?
        if (dupCompanyMap.containsKey(oldId)){
            return dupCompanyMap.get(oldId);
        }
        return companyMap.get(oldId).getId();
    }

    public void addTr(int oldId, Trainee trainee) {
        traineeMap.put(oldId, trainee);
    }

    public int getTr(int oldId) {
        logger.info("Looking up trainee id {}", oldId);
        return traineeMap.get(oldId).getId();
    }

    public Optional<Company> getCompany(String name) {
        return companyMap.values().stream().filter(c -> name.equals(c.getName())).findFirst();
    }

    public void addDupCoId(int duplicateCoID, int newID) {
        dupCompanyMap.put(duplicateCoID, newID);
    }

    public int getCourseId(int oldId){
        logger.debug("Looking up Course id {}", oldId);
        return courseMap.get(oldId).getCourseID();
    }

    public void addCourse(int oldId, Course course) {
        courseMap.put(oldId, course);
    }

    public void addInstructor(int oldId, InstructorExaminer instructor) {
        instructorMap.put(oldId, instructor);
    }

    public int getInstructor(int oldId){
        logger.debug("Looking up Instructor id {}", oldId);
        return instructorMap.get(oldId).getId();
    }

    public int getExaminer(int oldId){
        logger.debug("Looking up Examiner id {}", oldId);
        return examinerMap.get(oldId).getId();
    }

    public void addExaminer(int oldId, InstructorExaminer examiner) {
        examinerMap.put(oldId, examiner);
    }

    public void addCourseIns(int oldId, CourseIns courseIns) {
        courseInsMap.put(oldId, courseIns);
    }
}
