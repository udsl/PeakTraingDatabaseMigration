package com.udsl.peaktraining.template;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TemplateData {
    @NonNull private String name;
    private static final boolean enabled = true;
    private String description;
    @NonNull private Integer def_days;
    @NonNull private Integer default_cert_id;
    @NonNull private String course_number;
    private String course_elements;			//{"MORRIS 5000KG SWL","HOOK & BLOCK"}
    private Integer instructor_id;
    private Integer examiner_id;
    private String model;
    private String capacity;
    private String attachment;
    private String equipment;


}
