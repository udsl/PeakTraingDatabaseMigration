package com.udsl.peaktraining.template;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TemplateData {
    private String name;
    private static final boolean enabled = true;
    private String description;
    private Integer def_days;
    private String default_cert_id;
    private String course_number;
    private String course_elements;			//{"MORRIS 5000KG SWL","HOOK & BLOCK"}
    private Integer instructor_id;
    private Integer examiner_id;
    private String model;
    private String capacity;
    private String attachment;
    private String equipment;
}
