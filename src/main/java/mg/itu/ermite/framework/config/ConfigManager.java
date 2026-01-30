package mg.itu.ermite.framework.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, String> configData;
    
    // Constructeur privé pour Singleton
    private ConfigManager() {
        this.configData = new HashMap<>();
    }
    
    // Singleton pattern
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    // Méthodes d'accès
    public String get(String key) {
        return configData.get(key);
    }
    
    public String get(String key, String defaultValue) {
        return configData.getOrDefault(key, defaultValue);
    }
    
    public void set(String key, String value) {
        configData.put(key, value);
    }
    
    public void setAll(Map<String, String> configMap) {
        configData.putAll(configMap);
    }
    
    public Map<String, String> getAll() {
        return new HashMap<>(configData); // Retourne une copie
    }
    
    // Méthodes utilitaires avec conversion
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }
    
    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    // Pour les chemins d'upload spécifiques
    public String getUploadDirectory() {
        return get("uploadDirectory", 
                  System.getProperty("java.io.tmpdir") + "/uploads");
    }
    
    public long getMaxFileSize() {
        return getLong("maxFileSize", 10 * 1024 * 1024); // 10MB par défaut
    }
}