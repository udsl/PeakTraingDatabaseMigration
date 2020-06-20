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
    private final Map<Integer, Attendants> attendantsMap = new HashMap<>();

    public void addCo(int oldId, Company company) {
        companyMap.put(oldId, company);
    }

    public int getNewCompanyId(int oldId) {
        logger.info("Looking up comany id {}", oldId);
        // is the old ID a duplicate?
        if (dupCompanyMap.containsKey(oldId)){
            return dupCompanyMap.get(oldId);
        }
        return companyMap.get(oldId).getId();
    }

    public void addTrainee(int oldId, Trainee trainee) {
        logger.debug("Adding Trainee from id {} -> new id {}", oldId, trainee);
        traineeMap.put(oldId, trainee);
    }

    public int getNewTrianeeId(int oldId) {
        logger.info("Looking up trainee id {}", oldId);
        Trainee newTrainee = traineeMap.get(oldId);
        if (newTrainee == null){
            logger.debug("courseIns not found for id {}", oldId);
            return -1;
        }
        int newTraineeId = newTrainee.getId();
        logger.debug("Returning traineeId {}", newTraineeId);
        return newTraineeId;
    }

    public Optional<Company> getCompany(String name) {
        logger.debug("Looking up company with name '{}'", name);
        return companyMap.values().stream().filter(c -> name.equals(c.getName())).findFirst();
    }

    public void addDupCoId(int duplicateCoID, int newID) {
        logger.debug("Adding DupCoId from for id {} -> new id {}", duplicateCoID, newID);
        dupCompanyMap.put(duplicateCoID, newID);
    }

    public int getCourseInsId(int oldId){
        logger.debug("Looked up courseInsId {}", oldId);
        CourseIns courseIns = courseInsMap.get(oldId);
        if (courseIns == null){
            logger.debug("courseIns not found for id {}", oldId);
            return -1;
        }
        int courseInsId = courseIns.getId();
        logger.debug("Returning courseInsId {}", courseInsId);
        return courseInsId;
    }

    public int getCourseDefId(int oldId){
        logger.debug("Looked up CourseDEf id {}", oldId);
        int courseDefId = courseMap.get(oldId).getCourseID();
        logger.debug("Returning courseDefId {}", courseDefId);
        return courseDefId;
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


    public void addCourse(int oldId, Course course) {
        logger.debug("Adding Course for id {} with new id {}", oldId, course.getCourseID());
        courseMap.put(oldId, course);
    }

    public void addInstructor(int oldId, InstructorExaminer instructor) {
        logger.debug("Adding Instructor from old id {} -> {}", oldId, instructor);
        instructorMap.put(oldId, instructor);
    }

     public void addExaminer(int oldId, InstructorExaminer examiner) {
        logger.debug("Adding Examinier from old id {} -> {}", oldId, examiner);
        examinerMap.put(oldId, examiner);
    }

    public void addCourseIns(int oldId, CourseIns courseIns) {
        logger.debug("Adding CourseIns from old id {} -> {}", oldId, courseIns);
        courseInsMap.put(oldId, courseIns);
    }

    public void addAttendee(int oldId, Attendants attendee) {
        logger.debug("Adding attendee from old id {} -> {}", oldId, attendee);
        attendantsMap.put(oldId, attendee);
    }
}
