package com.udsl.peaktraining.template;

import com.udsl.peaktraining.migration.MigrationUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateImport {
    private static final Logger logger = LogManager.getLogger(TemplateImport.class.getName());

    private static final List<TemplateData> cache = new ArrayList<>();

    public boolean createTemplateDataCache(){
        logger.info("Creating template data cache");

        logger.info("Cache created with {} records", cache.size());
        return true;
    }
}
