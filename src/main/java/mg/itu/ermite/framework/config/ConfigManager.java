package mg.itu.ermite.framework.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé de la configuration de l'application.
 * 
 * ConfigManager suit le pattern Singleton et fournit un point d'accès unique
 * pour stocker et récupérer les paramètres de configuration de l'application.
 * 
 * Configuration est chargée à partir de :
 * - Paramètres d'initialisation du contexte servlet (web.xml)
 * - Appels programmatiques de set() ou setAll()
 * 
 * Fonctionnalités :
 * - Stockage de paires clé-valeur pour la configuration
 * - Conversion automatique de types (String, int, long, boolean)
 * - Valeurs par défaut si une clé n'existe pas
 * - Paramètres spécialisés (uploadDirectory, maxFileSize)
 * 
 * Exemple de configuration dans web.xml :
 * <pre>
 * {@code
 * <context-param>
 *     <param-name>uploadDirectory</param-name>
 *     <param-value>/uploads</param-value>
 * </context-param>
 * <context-param>
 *     <param-name>maxFileSize</param-name>
 *     <param-value>52428800</param-value>
 * </context-param>
 * <context-param>
 *     <param-name>userSessionId</param-name>
 *     <param-value>userId</param-value>
 * </context-param>
 * }
 * </pre>
 * 
 * Utilisation dans l'application :
 * <pre>
 * ConfigManager config = ConfigManager.getInstance();
 * String uploadDir = config.get("uploadDirectory");
 * int maxSize = config.getInt("maxFileSize", 10485760);
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see FrameworkInitializer
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, String> configData;
    
    /**
     * Constructeur privé pour empêcher l'instanciation directe (Singleton pattern).
     */
    private ConfigManager() {
        this.configData = new HashMap<>();
    }
    
    /**
     * Récupère l'instance unique du ConfigManager (Singleton).
     * Si l'instance n'existe pas encore, elle est créée.
     * 
     * @return l'instance unique du ConfigManager
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Récupère la valeur d'un paramètre de configuration.
     * 
     * @param key la clé du paramètre
     * @return la valeur du paramètre ou null si la clé n'existe pas
     */
    public String get(String key) {
        return configData.get(key);
    }
    
    /**
     * Récupère la valeur d'un paramètre de configuration avec une valeur par défaut.
     * 
     * @param key la clé du paramètre
     * @param defaultValue la valeur par défaut si la clé n'existe pas
     * @return la valeur du paramètre ou defaultValue si la clé n'existe pas
     */
    public String get(String key, String defaultValue) {
        return configData.getOrDefault(key, defaultValue);
    }
    
    /**
     * Ajoute ou met à jour un paramètre de configuration.
     * 
     * @param key la clé du paramètre
     * @param value la valeur du paramètre
     */
    public void set(String key, String value) {
        configData.put(key, value);
    }
    
    /**
     * Ajoute ou met à jour plusieurs paramètres de configuration à la fois.
     * 
     * @param configMap une map contenant les paires clé-valeur à ajouter
     */
    public void setAll(Map<String, String> configMap) {
        configData.putAll(configMap);
    }
    
    /**
     * Récupère une copie de tous les paramètres de configuration.
     * 
     * @return une map contenant tous les paramètres
     */
    public Map<String, String> getAll() {
        return new HashMap<>(configData); // Retourne une copie
    }
    
    /**
     * Récupère un paramètre de configuration converti en entier.
     * 
     * @param key la clé du paramètre
     * @param defaultValue la valeur par défaut en cas d'erreur ou clé non existante
     * @return la valeur convertie en int ou defaultValue
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }
    
    /**
     * Récupère un paramètre de configuration converti en long.
     * 
     * @param key la clé du paramètre
     * @param defaultValue la valeur par défaut en cas d'erreur ou clé non existante
     * @return la valeur convertie en long ou defaultValue
     */
    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }
    
    /**
     * Récupère un paramètre de configuration converti en booléen.
     * 
     * @param key la clé du paramètre
     * @param defaultValue la valeur par défaut si la clé n'existe pas
     * @return la valeur convertie en boolean ou defaultValue
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Récupère le répertoire d'upload des fichiers.
     * Utilise la valeur configurée ou un répertoire temporaire par défaut.
     * 
     * @return le chemin du répertoire d'upload
     */
    public String getUploadDirectory() {
        return get("uploadDirectory", 
                  System.getProperty("java.io.tmpdir") + "/uploads");
    }
    
    /**
     * Récupère la taille maximale autorisée pour les uploads de fichiers.
     * 
     * @return la taille maximale en octets (10 MB par défaut)
     */
    public long getMaxFileSize() {
        return getLong("maxFileSize", 10 * 1024 * 1024); // 10MB par défaut
    }
}