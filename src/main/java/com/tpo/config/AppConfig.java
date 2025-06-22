package com.tpo.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("No se pudo cargar application.properties: " + e.getMessage());
        }

    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        String v = props.getProperty(key);
        if (v == null) throw new IllegalArgumentException("Propiedad no encontrada: " + key);
        return Integer.parseInt(v);
    }
}
