package com.udsl.peaktraining.template;

import com.udsl.peaktraining.db.DbConnection;
import com.udsl.peaktraining.db.H2Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateImport {
    private static final Logger logger = LogManager.getLogger(TemplateImport.class.getName());

    @Autowired
    private DbConnection dbConnection;

    @Autowired
    H2Connection h2Conn ;

    private static final List<TemplateData> cache = new ArrayList<>();
    private static int currentCourseNum ;
    private static int inserted = 0;

    private static final int DAVE_HARDY = 1;

    private static final int CERT_TYPE_1 = 1;
    private static final int CERT_TYPE_2 = 2;
    private static final int CERT_TYPE_3 = 3;

    private static final String HELTH_SAFTY_1974 = "HEALTH AND SAFETY AT WORK ACT 1974 PROCEDURES";
    private static final String PPE_SELECTION = "PERSONAL PROTECTIVE EQUIPMENT SELECTION";
    private static final String CLOSE_DOWN = "CLOSE DOWN PROCEDURES";
    private static final String FIRE_SAFETY = "FIRE SAFETY LEGISLATION AND PROCEDURES";
    private static final String CHEMISTRY_OF_COMBUSTION = "CHEMISTRY OF COMBUSTION - HOW FIRES START AND SPREAD";
    private static final String FIRE_PRECISIONS = "FIRE PRECISIONS IN THE WORK PLACE";
    private static final String CLASSES_OF_FIRE = "CLASSES OF FIRE AND IDENTIFICATION";
    private static final String RISK_ASSESSMENTS = "RISK ASSESSMENTS AND METHOD STATEMENTS";
    private static final String CONTROL_OF_SUBSISTENCE = "CONTROL OF SUBSISTENCE HAZARDOUS TO HEALTH PROCEDURES";
    private static final String BASIC_WINCHING = "BASIC WINCHING PROCEDURES AND TECHNIQUES";
    private static final String SIDE_BED = "SIDE BED RECOVERY TECHNIQUES AND PROCEDURES";
    private static final String SPEC_LIGHT = "SPEC LIGHT RECOVERY TECHNIQUES AND PROCEDURES";
    private static final String BASIC_FIRST_AID = "BASIC FIRST AID PROCEDURE";

    public boolean checkForPreviousImportData(){
        int importedCount = h2Conn.getSqlInt( "SELECT COUNT(*) FROM TEMPLATES_INSERTED") ;
        return importedCount > 0;
    }

    public boolean createTemplateDataCache(){
        logger.info("Creating template data cache");
        try {
            String currentMaxNum = dbConnection.getCourseDefMaxNumber();
            currentCourseNum = Integer.parseInt(currentMaxNum.substring(3)) + 1;
            logger.info("currentMaxNum: {} next: {}", currentMaxNum, currentCourseNum);
            addType1Certs();
            addType2Certs();
            addType3Certs();
            logger.info("Cache created with {} records", cache.size());
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException("SQL exception! Unable to recover, terminating ...");
        }
    }

    public void performUpdatesToImported() {
        List<String> elements = List.of(HELTH_SAFTY_1974,
                "SITE SAFETY AWARENESS",
                PPE_SELECTION,
                "EMERGENCY FIRST AID PROCEDURES",
                "HAZARDS AND RISK ASSESSMENTS",
                "FIRE @ WORK PROCEDURES",
                CONTROL_OF_SUBSISTENCE);
        updateExisting("SAFETY AWARENESS", elements);

        elements = List.of(BASIC_FIRST_AID,
                "SITE EQUIPMENT",
                "BASIC CARE AND MAINTENANCE OF EQUIPMENT",
                "BASIC MACHINERY KNOWLEDGE",
                "SAFE OPERATION OF THE ABRASIVE WHEEL");

        updateExisting("ABRASIVE WHEEL", elements);


        elements = List.of("BASIC SLINGING SIGNALS",
                "SAFETY CHECKS",
                BASIC_FIRST_AID,
                "PREPARATION OF LOADS FOR LIFTING",
                "BASIC SLINGING TECHNIQUES");

        updateExisting("SLINGING TECHNIQUES", elements);


        elements = List.of("INTRODUCTION & PRE TEST",
                "OPERATING FUNCTIONS AND CONTROLS",
                "PRESTART PROCEDURES AND VISUAL CHECKS",
                "PLATFORMS, SAFETY PRECAUTIONS IN WORKING AREA",
                "SCISSOR OPERATIONS",
                "SITING AND STABILITY, MAINTENANCE AND INSPECTIONS",
                "WIND EFFECTS WITH PLATFORMS");

        updateExisting("ACCESS PLATFORM", elements);


        elements = List.of("BASIC SIGNALING TECHNIQUES",
                "SAFETY CHECKS",
                "SAFE OPERATION OF TEH ABOVE PLANT EQUIPMENT",
                "PREPARATION OF LOAD FOR LIFTING");

        updateExisting("PENDANT CRANE", elements);


        elements = List.of("SITE SAFETY CHECKS TO BE MADE",
                "SAFETY EQUIPMENT TO BE USED",
                "COMMUNICATION SKILLS",
                "BANKSMAN SIGNALS",
                "BANKSMAN PROCEDURES AND TECHNIQUES",
                "BASIC EMERGENCY FIRST AID PROCEDURES"
        );

        updateExisting("SLINGER BANKSMAN", elements);


        elements = List.of("BASIC CARE AND MAINTENANCE OF THE CHAINSAW",
                "BASICS OF A CHAINSAW",
                "BASIC FIRST AID PROCEDURES",
                "BASICS OF SAFETY EQUIPMENT TO BE WORN",
                "PUBLIC SAFETY AWARENESS",
                "SAFE OPERATION OF THE CHAINSAW (FELL WORK, CROSSCUT AND STACK)"
        );

        updateExisting("CHAINSAW SAFETY AWARENESS", elements);


    }

     // cert type 1 Site Safety
    private void addType1Certs(){
        // PEAK_update1.pdf

        List<String> elements = List.of(HELTH_SAFTY_1974,
                RISK_ASSESSMENTS,
                "ANCHOR POINT AND FIXING INSPECTIONS",
                "VEHICLE LOADING AND UNLOADING PROCEDURES",
                "LOAD DISTRIBUTION AND OVERSIZED LOAD CALCULATIONS",
                "SECURE THE LOAD TECHNIQUES AND PROCEDURES",
                "PREPARATION OF VEHICLE FOR HIGHWAY RESTRICTIONS");
        addToCache(new TemplateData("VEHICLE LOADING & UNLOADING PROCEDURES", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("HEALTH AND SAFETY OVERVIEW ON WORKING WITH CHEMICALS",
                "CLASSIFICATION OF SUBSTANCES INCLUDING FORMS OF CHEMICALS",
                "HEALTH HAZARDS OF SPECIFIC AGENTS",
                "WORKPLACE EXPOSURE LIMITS AND OTHER SOURCES OF INFORMATION",
                PPE_SELECTION,
                "CONTROL MEASURES AND CLEAN DOWN PROCEDURES",
                "EMERGENCY FIRST AID AND DE-CONTAMINATION PROCEDURES");
        addToCache(new TemplateData("WORKING WITH CHEMICALS", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        // PEAK_update2.pdf

        elements = List.of("LEGISLATIONS RELATING TO PEDESTRIAN APPLICATOR USE",
                "ENVIRONMENTAL FACTORS RELATING TO MIXING AND SAPPLICATION",
                "PERSONAL PROTECTIVE EQUIPMENT SELECTION AND USE",
                "PREPARE AND CALIBRATE A HAND HELD PEDESTRIAN APPLICATOR",
                "SAFE OPERATION OF THE HAND HELD PEDESTRIAN APPLICATOR",
                CONTROL_OF_SUBSISTENCE,
                "CLEAN DOWN AND DE-CONTAMINATION PROCEDURES");
        addToCache(new TemplateData("HAND HELD PEDESTRIAN APPLICATOR", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("COMPLY WITH ALL LEGAL REQUIREMENTS AND SAFE PRACTICES",
                "UNDERSTAND THE MAIN CAUSES OF ACCIDENCE AND INCIDENTS",
                "IDENTIFY THE CONTROLS AND MACHINE COMPONENTS",
                "CARRY OUT APPROPRIATE PRE USE CHECKS",
                "RISK ASSESSMENT AND ROUTE PLANNING",
                "IDENTIFY LOAD AND TOWING CAPACITIES",
                "SAFE USE OF UTILITY VEHICLE AND TRAILING ATTACHMENTS");
        addToCache(new TemplateData("UTILITY VEHICLE SITE SAFETY", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("ASBESTOS RELATED HEALTH AND SAFETY LAW PROCEDURES",
                "PROPERTIES OF ASBESTOS AND ITS EFFECT ON HEALTH",
                "CONTROL MEASURES USED ON SITE FOR DUST SUPPRESSION",
                "USE OF CLASS (H) VACUUM",
                "CORRECT WASTER HANDLING ON SITE",
                "USE AND SELECTION OF PPE AND RPE",
                "DE-CONTAMINATION PROCEDURES AND TECHNIQUES");
        addToCache(new TemplateData("WORKING WITH ASBESTOS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGAL OVERVIEW REGARDING HEALTH AND SAFETY",
                "SCAFFOLD ERECTING - DISMANTLE AND STORAGE PROCEDURES",
                "BASIC SYSTEMS OF WORK TECHNIQUES AND PROCEDURES",
                "SCAFFOLD TAG IDENTIFICATION AND UNDERSTANDING",
                "INDEPENDENT SCAFFOLD INCLUDING RETURNS ASSEMBLY TECHNIQUES",
                "SCAFFOLD ERECTING PROCEDURES AND TECHNIQUES",
                "RISK ASSESSMENT AND METHOD STATEMENTS");
        addToCache(new TemplateData("SCAFFOLD SAFETY AWARENESS", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("AUTHORISED TREATMENT FACILITY REGULATIONS AND PROCEDURES",
                "ENDS OF LIFE VEHICLES (AMENDMENT) REGULATIONS 2010",
                "HAZARDOUS WASTE RULES FOR ELVS THAT HAVE NOT BEEN DE-POLLUTED",
                CONTROL_OF_SUBSISTENCE,
                "WATE DUTY OF CARE RULES FOR NONE HAZARDOUS WASTE",
                "IDENTIFICATION OF WASTE TYPES",
                "DECONTAMINATION AND CLEAN DOWN PROCEDURES");
        addToCache(new TemplateData("ELV DE-POLLUTION", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("EXTINGUISHER TYPES AND USES",
                "IDENTIFY FIRE HAZARDS IN THE WORKPLACE",
                "PRINCIPLES OF FIRES AND EXPLOSION",
                "CAUSES AND PREVENTION OF FIRE AND EXPLOSION",
                "FIRE RISK ASSESSMENTS",
                "SAFETY OF STAFF IN THE EVENT OF FIRE",
                "EMERGENCY EVACUATION PROCEDURES");
        addToCache(new TemplateData("FIRE @ WORK SAFETY TRAINING", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("ACCESS PLATFORM PRE USER INSPECTION",
                "HAZARD AND RISK ASSESSMENTS",
                "SITE SAFETY EXAMINATIONS",
                "BASIC OPERATION OF ACCESS PLATFORM",
                "LORRY LOADING AND UNLOADING PROCEDURES",
                "BAIS HANDOVER PROCEDURES",
                "VEHICLE EXAMINATION BEFORE LEAVING SITE");
        addToCache(new TemplateData("LORRY LOADING AND UNLOADING SAFETY AWARENESS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGISLATION AND CODES OF PRACTICE",
                "GAS PROPERTIES AND HAZARDS",
                "CYLINDER IDENTIFICATION AND DATA",
                "GAS EQUIPMENT IDENTIFICATION",
                "REGULATOR FITTING PROCEDURES",
                "CYLINDER STORAGE AND HANDLING",
                "EMERGENCY PROCEDURES AND TECHNIQUES");
        addToCache(new TemplateData("OXY-FUEL SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(FIRE_SAFETY,
                CHEMISTRY_OF_COMBUSTION,
                FIRE_PRECISIONS,
                CLASSES_OF_FIRE,
                "SAFETY SYSTEMS OF WORK INCLUDING PERMITS",
                "SAFE USE OF OXYACETYLENE EQUIPMENT",
                CLOSE_DOWN);
        addToCache(new TemplateData("HOT WORKS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(HELTH_SAFTY_1974,
                "WELDING AND GENERAL SHOP SAFETY",
                PPE_SELECTION,
                "BASIC WELD JOINTS AND POSITIONS",
                "MEASURING AND FABRICATION TECHNIQUES AND SKILLS",
                "SHIELDED METAL ARC WELDING BASIC SKILLS TEST",
                "STORAGE AND TRANSPORTATION OF MOBILE WELDING EQUIPMENT");
        addToCache(new TemplateData("SHIELDED ARC WELDING BASIC SKILLS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("COMPLY WITH ALL LEGAL REQUIREMENTS AND SAFE PRACTICES",
                "UNDERSTAND THE MAIN CAUSES OF ACCIDENCE AND INCIDENTS",
                "IDENTIFY THE CONTROLS AND MACHINE COMPONENTS",
                "CARRY OUT APPROPRIATE PRE USE CHECKS",
                "BASIC OPERATING SKILLS",
                "IDENTIFY LOAD AND TOWING CAPACITIES",
                "LEGAL AND PRACTICAL HIGHWAY REQUIREMENTS");
        addToCache(new TemplateData("UTILITY VEHICLE SAFETY AWARENESS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGISLATION RELATING TO PESTICIDE BOOM SPRAYING",
                "KNOWLEDGE OF THE TRACTOR MOUNTED BOOM SPRAYER",
                "PRODUCT STORAGE, LABELING AND USES",
                "ENVIRONMENTAL RISK ASSESSMENT",
                "PREPARE AND CALIBRATE THE SPRAYER ATTACHMENT",
                "SAFE OPERATION OF THE SPRAYER",
                "CLEAN DOWN AND DE-CONTAMINATION OF CHEMICALS");
        addToCache(new TemplateData("PESTICIDE BOOM SPRAYER - TRACTOR MOUNTED", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        // PEAK_update3.pdf
        elements = List.of("FIRE SAFETY AND PROCEDURES",
                CHEMISTRY_OF_COMBUSTION,
                "FIRE PRECAUTIONS IN THE WORK PLACE",
                "SAFE USE OF OXYACETYLENE EQUIPMENT",
                "SAFE SYSTEMS OF WORK",
                "EQUIPMENT LOCATION WHILST CUTTING AND BURNING",
                CLOSE_DOWN);
        addToCache(new TemplateData("OXYACETYLENE BURNING (HOT WORKS)", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("HEALTH AND SAFETY LAW RELATING TO SUPERVISORS",
                "SUPERVISORY RESPONSIBILITIES CONTROLLING SAFETY",
                "CONSTRUCTION DESIGN AND MANAGEMENT REGULATIONS (CDM)",
                "METHOD STATEMENTS, RISK ASSESSMENTS AND RECORD PROCEDURES",
                "MANAGEMENT OF OCCUPATIONAL HEALTH",
                "STATUTORY INSPECTION, ACCIDENTS AND INJURY REPORTING",
                "CONTROL OF SUBSTANCES HAZARDOUS TO HEALTH REGULATIONS");
        addToCache(new TemplateData("SITE SUPERVISOR SAFETY TRAINING", elements, 3, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("PROVISION AND USE OF WORK EQUIPMENT REGULATIONS 1998",
                "SITE SAFETY AWARENESS FOR EXCAVATOR OPERATORS",
                "MACHINE PURPOSE CAPABILITIES AND LIMITATIONS",
                "PRE OPERATIONAL CHECKS",
                "SAFE OPERATION OF THE EXCAVATOR AND ANCILLARY ATTACHMENT",
                "FITTING OF ANCILLARY ATTACHMENT",
                CLOSE_DOWN);
        addToCache(new TemplateData("TRACKED EXCAVATOR BASIC OPERATOR ABOVE 10 TON", elements, 3, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("INTRODUCTION TO THE PRINCIPLES OF PLASMA ARC CUTTING",
                "USE AND LIMITATIONS OF PLASMA ARC CUTTING",
                "THE MEANING OF PLASMA ARC CUTTING TERMINOLOGY",
                "SETTING OF THE PLASMA ARC EQUIPMENT",
                "COMMON CUTTING PROBLEMS - IDENTIFICATION AND REIFICATION",
                "PERSONAL SAFETY EQUIPMENT SELECTION",
                "SAFE USE AND SHUT DOWN OF PLASMA ARC CUTTING EQUIPMENT");
        addToCache(new TemplateData("PLASMA ARC CUTTING SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(HELTH_SAFTY_1974,
                "WINCH SELECTION AND MAINTENANCE",
                "SELECTION AND USE OF ANCHOR AND FIXING POINTS",
                "IDENTIFICATION OF WORKING POSITIONS",
                PPE_SELECTION,
                "COMMUNICATION SKILLS AND PROCEDURES",
                CLOSE_DOWN);
        addToCache(new TemplateData("FORESTRY WINCHING OPERATIONS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(HELTH_SAFTY_1974,
                "TRACTOR MOUNTED WINCH PRE USE SAFETY CHECKS",
                "WINCH MAINTENANCE AND SAFETY PROCEDURES",
                PPE_SELECTION,
                "IDENTIFICATION OF WORKING POSITIONS",
                "COMMUNICATION SKILLS AND PROCEDURES",
                CLOSE_DOWN);
        addToCache(new TemplateData("TRACTOR MOUNTED WINCH SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGISLATION OF RESPIRATORY PROTECTIVE EQUIPMENT",
                "BRIEF EXPLANATION OF THE NEED FOR RESPIRATORY EQUIPMENT",
                "EXPLANATION OF THE FIT TEST PROCEDURES",
                "ACHIEVING THE BEST FIT WITH THE RESPIRATORY EQUIPMENT",
                "SETUP OF EQUIPMENT AND MASK",
                "SENSITIVITY TEST",
                "PRACTICAL FACE FIT SEAL TEST");
        addToCache(new TemplateData("RESPIRATORY PROTECTIVE EQUIPMENT COMPETENT PERSON", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("UNDERSTANDING THE SAFE LOAD INDICATORS AND DUTIES CHART",
                "STATUARY REGULATIONS AND CODES OF PRACTICE EXPLAINED",
                "CAPACITIES OF CHAINS, SLINGS AND LIFTING GEAR",
                "GROUND CONDITIONS AND CRANE POSITIONING",
                "RADIUS CHANGE AND THE SAFE WORKING CAPACITY",
                "SAFE CRANE OPERATION",
                "ROUTINE MAINTENANCE INCLUDING CLOSE DOWN PROCEDURES");
        addToCache(new TemplateData("CRAWLER CRANE", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("AGRICULTURAL VEHICLE PRE-USE SAFETY CHECKS",
                "IDENTIFICATION AN DUSE OF INSTRUMENTS AND CONTROLS",
                "POTENTIAL WORK PLACE HAZARDS",
                "WORKING WITH PTO AND PTO TRAILED IMPLEMENTS",
                "BASIC OPERATIONS OF A TRACTOR MOUNTED LOADER",
                "BASIC DRIVING AND MANOEUVRING SKILLS",
                CLOSE_DOWN);
        addToCache(new TemplateData("TRACTOR OPERATOR SAFETY AWARENESS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("HEALTH AND SAFETY POLICY AND SAFE SYSTEM SOF WORK",
                "WORKING IN CONFINED SPACE REGULATIONS AND PROCEDURES",
                "COMMUNICATION SKILLS AND EQUIPMENT PROCEDURES",
                "MAINTENANCE AND SAFE EXAMINATIONS OF RESCUE EQUIPMENT",
                "USE OF PERMIT TO WORK AND LOCK  OFF PROCEDURES",
                "RISK ASSESSMENT FOR HAZARDS IN CONFINED SPACES",
                "EMERGENCY EVACUATION PROCEDURES AND REGULATIONS");
        addToCache(new TemplateData("CONFINED SPACE ENTRY LEVEL", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(FIRE_SAFETY,
                CHEMISTRY_OF_COMBUSTION,
                "SAFE USE OF OXYACETYLENE EQUIPMENT",
                "BURNING TECHNIQUES AND SKILLS",
                "DIFFERENT METAL CLASSES AND BURNING TECHNIQUES",
                "EXPLANATION OF FIRE PROOF SAFETY CLOTHING AND EQUIPMENT",
                "CLOSE DOWN AND STORAGE PROCEDURES OF EQUIPMENT");
        addToCache(new TemplateData("OXYACETYLENE BURNING SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGISLATION AND CODES OF PRACTICE",
                "GAS PROPERTIES AND HAZARDS",
                "CYLINDER IDENTIFICATION AND DATA",
                "GAS EQUIPMENT IDENTIFICATION",
                "REGULATOR FITTING PROCEDURES",
                "CYLINDER STORAGE AND HANDLING",
                "EMERGENCY PROCEDURES AND TECHNIQUES");
        addToCache(new TemplateData("OXY-FUEL SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("UNDERSTANDING FIRST AID REGULATIONS AND PROCEDURES",
                "PRIMARY CASUALTY ASSESSMENT",
                "DEAL AND RESPOND TO DIFFERENT TYPES OF CASUALTY",
                "EMERGENCY FIRST AID PROCEDURES AND LIMITATIONS",
                "COMMUNICATION AND CASUALTY CARE",
                "PRIORITIES OF EMERGENCY FIRST AID AND INCIDENT MANAGEMENT",
                "DATA PROTECTION INCIDENT RECORD KEEPING PROCEDURES");
        addToCache(new TemplateData("EMERGENCY FIRST AID - ENTRY LEVEL", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("LEGISLATION AND CODES OF PRACTICE RELATING TO LIFTING DUTIES",
                "SAFE SYSTEMS OF WORK INCLUDING METHOD STATEMENTS",
                "DOCUMENTATION OF LIFTING EQUIPMENT AND ACCESSORIES",
                "PLANNING A LIFTING OPERATION",
                "COMMUNICATION TYPES AND SKILLS",
                "HAZARDS AND RISK ASSESSMENT",
                "CRANE APPRECIATION, CRANE TYPES, CAPABILITIES AND LIMITATIONS");
        addToCache(new TemplateData("LIFT SUPERVISOR", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("ASBESTOS RELATED HEALTH AND SAFETY LAW PROCEDURES",
                "PROPERTIES OF ASBESTOS AND ITS EFFECT ON HEALTH",
                "ASBESTOS IDENTIFICATION AND USES",
                "CORRECT PERSONAL PROTECTIVE EQUIPMENT SELECTION",
                "RISKS AND CONTAMINATION",
                "HOW TO AVOID RISK EXPOSURE TO ASBESTOS",
                RISK_ASSESSMENTS);
        addToCache(new TemplateData("ASBESTOS AWARENESS SITE SAFETY ", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));


        // PAGE 21
        elements = List.of("INTRODUCTION TO ADR UK ROAD REGULATIONS",
                "HAZARDOUS CLASSES IDENTIFICATION",
                "DOCUMENTATION ADN COMPLETION OF THE DANGEROUS GOOD NOTE",
                "SIGNS, SYMBOLS, PACKING GROUPS AND PACKAGES",
                "LOADING, UNLOADING AND SAFE TRANSPORT",
                PPE_SELECTION,
                CONTROL_OF_SUBSISTENCE);
        addToCache(new TemplateData("DANGEROUS GOODS BY ROAD (ADR)", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("RESPONSIBILITIES OF THE ATTENDING EMERGENCY SERVICE",
                "RISK ASSESSMENT, RECORD LOGGING AT A MAJOR INCIDENT",
                "EFFECTIVE COMMUNICATION SKILLS",
                "PLANNING AND RECOVERY FOR ANY GIVEN MAJOR INCIDENT",
                "INCIDENT MANAGER'S DUTIES RESPONSIBILITIES AND LIMITS",
                "EFFECTIVELY ORGANISE AND DEPLOY THE RECOVERY EQUIPMENT",
                "RECORD AND STORE ALL INCIDENT DATA");
        addToCache(new TemplateData("RECOVERY INCIDENT MANAGER", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of(HELTH_SAFTY_1974,
                "SITE ASFETY PROCEDURES AND TECHNIQUES",
                "PERSONAL PROTECTIVE EQUIPMENT SAFETY EXAMINATIONS",
                BASIC_WINCHING,
                SIDE_BED,
                SPEC_LIGHT,
                "VEHICLE SAFETY CHECKS BEFORE LEAVING INCIDENT SITE");
        addToCache(new TemplateData("RECOVERY SAFETY AWARENESS", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("PROVISION AND USE OF WORK EQUIPMENT REGULATIONS 1998",
                "SITE SAFETY AWARENESS FOR EXCAVATOR OPERATORS",
                "MACHINE PURPOSE CAPABILITIES AND LIMITATIONS",
                "PRE OPERATIONAL CHECKS",
                "SAFE OPERATION OF THE EXCAVATOR AND ANCILLARY ATTACHMENT",
                "FITTING OF ANCILLARY ATTACHMENT",
                CLOSE_DOWN);
        addToCache(new TemplateData("TRACKED EXCAVATOR", "TRACKED EXCAVATOR BELOW 10 TON", elements, 3, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("BASIC CARE AND MAINTENANCE OF THE CHAINSAW",
                "BASICS OF A CHAINSAW",
                "BASIC FIRST AID PROCEDURES",
                "BASIC SAFETY EQUIPMENT TO BE WORN",
                "PUBLIC SAFETY AWARENESS",
                "SAFE OPERATION OF THE ABOVE EQUPMENT (FELL WORK, CROSSCAT AND STACK");
        addToCache(new TemplateData("CHAINSAW SAFETY", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("VEHICLE AND DRIVER SAFETY REGULATIONS ACT 1994",
                "RECOVER PROCEDURES AND TECHNIQUES",
                "PERSON PROTECTIVE EQUIPMENT SELECTION PROCEDURES",
                BASIC_WINCHING,
                SIDE_BED,
                SPEC_LIGHT,
                "VEHICLE SAFETY CHECKS BEFORE LEAVING INCIDENT SITE");
        addToCache(new TemplateData("VEHICLE RECOVERY MODULE", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("INTRODUCTION & PRE TEST",
                "OPERATING FUNCTIONS AND CONTROLS",
                "PRE START PROCEDURES AND VISUAL CHECKS",
                "PLATFORMS SAFETY PRECAUTIONS IN WORKING AREAS",
                "BOOM OPERATIONS",
                "STRING AND STABILITY MAINTENANCE AND INSPECTIONS",
                "WIND EFFECTS WITH BOOM");
        addToCache(new TemplateData("ARTICULATED BOOM", "GENIE SCISSOR LIFT GS 1932 - LIFT HEIGHT 7.79MTR", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("ELECTRICAL AND ELECTRONIC EQUIPMENT REGULATIONS 2013",
                "BEST AVAILABLE TREATMENT, RECOVERY AND RECYCLING TECHNIQUES",
                "ENVIRONMENT MANAGEMENT SYSTEMS",
                "CONTROL OF SUBSTANCES HAZARDOUS TO HEALTH REGULATIONS 2002",
                "WASTE HANDLING TECHNIQUES AND PROCEDURES",
                "DOCUMENT AND RECORD KEEPING (4YRS)",
                "STORAGE AND CONTAINMENT OF WASTE");
        addToCache(new TemplateData("ELECTRICAL AND ELECTRONIC EQUIPMENT WASTE HANDLING", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("ASBESTOS RELATED HEALTH AND SAFETY LAW PROCEDURES",
                "PROPERTIES OF ASBESTOS AND ITS EFFECT ON HEALTH",
                "ASBESTOS IDENTIFICATION AND USES",
                "PERSONAL PROTECTIVE EQUIPMENT SELECTION",
                "RISKS AND CONTAMINATION INCLUDING RISK ASSESMENT",
                "HOW TO AVOID RISK EXPOSURE TO ASBESTOS",
                "REMOVAL AND DE-CONTAMINATION PROCEDURES AND TECHNIQUES");
        addToCache(new TemplateData("ASBESTOS REMOVAL SITE SAFETY AWARENESS", elements, 3, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("PROVISION AND USE OF WORK EQUIPMENT REGULATIONS 1998",
                "SITE SAFETY AWARENESS FOR 3CX OPERATORS",
                "MACHIINE PURPOSE AND LIMITATIONS",
                "PRE OPERATIONAL SAFETY CHECKS",
                "SAFE OPERATION OF THE 3CX WHEELED LOADER AND BACK HOE",
                "FITTING ANCILLARY ATTACHEMENTS",
                "SAFETY CHECKS AND CLOSE DOWN PROCEDURES");
        addToCache(new TemplateData("WHEELED LOADER SITE SAFETY AWARENESS", "3CX BASIC OPERATOR", elements, 2, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

        elements = List.of("SITE SAFETY CHECKS TO BE MADE",
                "SAFETY EQUIPMENT TO BE USED",
                "COMMUNICATION SKILLS BETWEEN BANKSMAN AND VEHICLE OPERATOR",
                "VEHICLE AWARENESS INCLUDING HAZARDS",
                "BANKSMAN PROCEDURES AND TECHNIQUES FOR VEHICLE MOVEMENT",
                "BASIC EMERGENCY FIRST AID PROCEDURES");
        addToCache(new TemplateData("VEHICLE BANKSMAN", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

/*
        elements = List.of("",
                "",
                "",
                "",
                "",
                "",
                "");
        addToCache(new TemplateData("", elements, 1, CERT_TYPE_1,  currentCourseNum++, DAVE_HARDY, DAVE_HARDY));

 */
    }

    // cert type 2 Various Plant
    private void addType2Certs(){
/*
        addToCache(new TemplateData("", "", "", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
 */

        // PEAK_update1.pdf
        addToCache(new TemplateData("FALL ARRESTER", "SAFETY HARNESS TRAINING", "SITE SAFETY AWARENESS - BASIC HARNESS REGULATIONS", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("WORKING @ HEIGHTS", "WORKING @ HEIGHTS SAFTY AWARENESS", "SITE SAFETY", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("LOADING SHOVEL", "JCB436E LOADING SHOVEL", "SITE SAFETY AWARENESS - BASIC OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("MOBILE SHREDDER", "MOBILE SHREDDER", "SITE SAFETY - BASIC OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("HGV SHUNTER", "ON SITE HGV OPERATOR", "SITE SAFETY BASIC SHUNTING OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));

        // PEAK_update2.pdf
        addToCache(new TemplateData("ROAD SWEEPER", "ON SITE ROAD SWEEPER", "SITE SAFETY - BASIC OPERATOR", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("RUBBER DUCK", "JS 160W RUBBER DUCK - GRAB ATTACHMENT", "SITE SAFETY AWARENESS - BASIC OPERATOR",2, CERT_TYPE_3,  currentCourseNum++, DAVE_HARDY));

        // PEAK_update3.pdf
        addToCache(new TemplateData("POWER TOOLS", "HAND HELD POWER TOOL VIBRATION AWARENESS", "SITE SAFETY BASIC KNOWLEDGE TEST",1, CERT_TYPE_3,  currentCourseNum++, DAVE_HARDY));
        // page 24
        addToCache(new TemplateData("RECOVERY MODULE", "RECOVERY MODULE 1-2-3", "(1) HEALTH AND SAFETY LEGISLATION\n" +
                "(2) SERVICE SKILL - CUSTOMER NEEDS\n" +
                "(3) ROAD SIDE ASSESSMENT - HAZARDS AND RISKS", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
        addToCache(new TemplateData("RECOVERY MODULE", "RECOVERY MODULE 19", "(1) HEALTH AND SAFETY LEGISLATION\n" +
                "TRANSPORT AND STORE VEHICLE - PRESERVE ALL EVIDENCE", 1, CERT_TYPE_2,  currentCourseNum++, DAVE_HARDY));
    }

    private void addType3Certs(){
  //      addToCache(new TemplateData("", "", 1, CERT_TYPE_3,  currentCourseNum++, DAVE_HARDY));
    }

    private void addToCache( TemplateData data){
        cache.add(data);
        logger.info("Add to cache: {}", data);
    }

    private PreparedStatement insertedStmt = null ;
    private final static String INSERTED_SQL = "INSERT INTO TEMPLATES_INSERTED (TEMPLATE_NAME, POSTGRES_ID) VALUES (?, ?)";

    private void updateInserted(String templateName, int postgresId){
        try {
            if (insertedStmt == null) {
                insertedStmt = h2Conn.prepareStatement(INSERTED_SQL);
            }
            insertedStmt.setString(1, templateName);
            insertedStmt.setInt(2, postgresId);
            insertedStmt.executeUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean templateExists(TemplateData template){
        return dbConnection.importedTemplateExists(template);
    }

    public void writeCacheToPostgres() {
        for( TemplateData template: cache){
            if (!templateExists(template)) {
                int key = dbConnection.saveImportedTemplate(template);
                updateInserted(template.getName(), key);
                inserted++;
            }
            else{
                logger.info("Template exists - {}", template);
            }
        }
        logger.info("Templates inserted - {}", inserted);
    }


    private void updateExisting(String key, List<String> elements) {
        int id = dbConnection.getExistingKey(key);
        if (id > 0){
            dbConnection.setElements(id, elements);
        }
    }


}
