package com.tpo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static Properties props = new Properties();
    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
            // Intentar cargar application-local.properties si existe
            InputStream inLocal = AppConfig.class.getClassLoader().getResourceAsStream("application-local.properties");
            if (inLocal != null) props.load(inLocal);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar propiedades", e);
        }
    }
    public static String get(String key) {
        return props.getProperty(key);
    }
    public static int getInt(String key) {
        String v = props.getProperty(key);
        return v != null ? Integer.parseInt(v) : 0;
    }
}
