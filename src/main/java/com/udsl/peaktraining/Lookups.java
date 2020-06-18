package com.udsl.peaktraining;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Lookups {
    private static final Logger logger = LogManager.getLogger(Lookups.class.getName());

    private Map<Integer, Company> companyMap = new HashMap<>();
    private Map<Integer, Integer> dupCompanyMap = new HashMap<>();
    private Map<Integer, Trainee> traineeMap = new HashMap<>();
    private Map<Integer, Course> courseMap = new HashMap<>();
    private Map<Integer, InstructorExaminer> trainerMap = new HashMap<>();
    private Map<Integer, InstructorExaminer> examinerMap = new HashMap<>();
    private Map<Integer, CourseIns> courseInsMap = new HashMap<>();

    public void addCo(int oldId, Company company) {
        companyMap.put(oldId, company);
    }

    public int getCo(int oldId) {
        logger.info("Looking up comany id {}", oldId);
        // is the old ID a duplicate?
        if (dupCompanyMap.containsKey(oldId)){
            return companyMap.get(dupCompanyMap.get(oldId)).getId();
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
        return courseMap.get(oldId).getCourseID();
    }

    public void addCourse(int oldId, Course course) {
        courseMap.put(oldId, course);
    }

    public void addTrainer(int oldId, InstructorExaminer trainer) {
        trainerMap.put(oldId, trainer);
    }

    public void addExaminer(int oldId, InstructorExaminer examiner) {
        examinerMap.put(oldId, examiner);
    }

    public void addCourseIns(int oldId, CourseIns courseIns) {
        courseInsMap.put(oldId, courseIns);
    }
}
