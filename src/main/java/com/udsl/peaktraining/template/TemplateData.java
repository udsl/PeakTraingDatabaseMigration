package com.udsl.peaktraining.template;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class TemplateData {
    @NonNull private String name;
    private static final boolean enabled = true;
    private String description;
    @NonNull private Integer def_days;
    @NonNull private Integer default_cert_id;
    @NonNull private String course_number;
    private List<String> course_elements;			//{"MORRIS 5000KG SWL","HOOK & BLOCK"}
    private Integer instructor_id;
    @NonNull private Integer examiner_id;
    private String course_type;
    private String model;
    private String capacity;
    private String attachment;
    private String equipment;

    public TemplateData(String templateName, String desc, int defDays, int certType, int courseNum, int examinerId){
        name = templateName;
        def_days = defDays;
        default_cert_id = certType;
        course_number = "PT:" + courseNum;
        description = desc;
        examiner_id = examinerId;
    }

    public TemplateData(String templateName, String desc, String courseType, int defDays, int certType, int courseNum, int examinerId){
        name = templateName;
        def_days = defDays;
        default_cert_id = certType;
        course_number = "PT:" + courseNum;
        description = desc;
        course_type = courseType;
        examiner_id = examinerId;
    }

    public TemplateData(String templateName, String desc, List<String> elements, int defDays, int certType, int courseNum, int examinerId, int instructorId){
        name = templateName;
        description = templateName;
        def_days = defDays;
        default_cert_id = certType;
        course_number = "PT:" + courseNum;
        description = desc;
        examiner_id = examinerId;
        instructor_id = instructorId;
        course_elements = elements;
    }

    public TemplateData(String templateName, List<String> elements, int defDays, int certType, int courseNum, int examinerId, int instructorId){
        name = templateName;
        description = templateName;
        def_days = defDays;
        default_cert_id = certType;
        course_number = "PT:" + courseNum;
        examiner_id = examinerId;
        instructor_id = instructorId;
        course_elements = elements;
    }

}
