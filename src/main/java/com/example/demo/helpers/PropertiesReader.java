package com.example.demo.helpers;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@Slf4j
public class PropertiesReader {
    private static final String CONFIG_PROPERTIES = "application.properties";

    public static String getProperties(String key) {

        try (InputStream input = PropertiesReader.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            Properties properties = new Properties();
            if (input == null) {
                log.error("Unable to find application.properties = {}", (Object) null);
            }
            properties.load(input);
            return properties.getProperty(key);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return key;
    }
}
