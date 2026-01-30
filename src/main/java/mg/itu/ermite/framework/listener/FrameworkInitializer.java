package mg.itu.ermite.framework.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mg.itu.ermite.framework.config.ConfigManager;
import mg.itu.ermite.framework.util.ClasspathScanner;
import mg.itu.ermite.framework.util.EndPointDetails;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Écouteur de contexte servlet qui initialise le framework au démarrage de l'application.
 * 
 * FrameworkInitializer est automatiquement invoqué au démarrage du conteneur servlet
 * et effectue les opérations suivantes :
 * 
 * 1. Vérifie la disponibilité de la dépendance Jackson (sérialisation JSON)
 * 2. Charge les paramètres de configuration depuis web.xml
 * 3. Scanne le classpath pour découvrir les contrôleurs et les endpoints
 * 4. Enregistre tous les endpoints dans le contexte servlet pour accès depuis FrontServlet
 * 5. Affiche un rapport d'initialisation sur la console
 * 
 * Les endpoints découverts sont stockés dans le contexte servlet sous la clé "endpoints"
 * comme une Map<String, List<EndPointDetails>>.
 * 
 * Configuration requise dans web.xml :
 * <pre>
 * {@code
 * <listener>
 *     <listener-class>
 *         mg.itu.ermite.framework.listener.FrameworkInitializer
 *     </listener-class>
 * </listener>
 * <context-param>
 *     <param-name>userSessionId</param-name>
 *     <param-value>userId</param-value>
 * </context-param>
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see ConfigManager
 * @see ClasspathScanner
 * @see EndPointDetails
 */
public class FrameworkInitializer implements ServletContextListener {

    /**
     * Appelée lors de l'initialisation du contexte servlet (démarrage de l'application).
     * 
     * Étapes de l'initialisation :
     * 1. Vérifie que Jackson est disponible dans le classpath
     * 2. Charge les paramètres de configuration
     * 3. Scanne les contrôleurs et endpoints
     * 4. Enregistre les endpoints dans le contexte
     * 5. Affiche un résumé d'initialisation
     * 
     * @param sce l'événement d'initialisation du contexte servlet
     * @throws IllegalStateException si Jackson n'est pas disponible
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        } catch (Throwable e) {
            throw new IllegalStateException("La librairie Jackson est requise pour utiliser le framework. Veuillez l'ajouter aux dependances du projet.", e);
        }
        
        System.out.println("[Framework] Initialisation du projet...");

        ConfigManager config = ConfigManager.getInstance();
        ServletContext context = sce.getServletContext();
        
        Enumeration<String> paramNames = context.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = context.getInitParameter(paramName);
            config.set(paramName, paramValue);
            
            System.out.println("[Config] " + paramName + " = " + paramValue);
        }
        

        Map<String,Object> endpoints = ClasspathScanner.findMappedUrls();

        System.out.println("NOMBRE D'ENDPOINTS:"+endpoints.size());

        context.setAttribute("endpoints", endpoints);

        for (Map.Entry<String,Object> endpoint : endpoints.entrySet()) {
            String url = endpoint.getKey();
            System.out.println("-----------");
            System.out.println("URL["+url+"]");
            List<EndPointDetails> detailsList = (List<EndPointDetails>) endpoint.getValue();
            for (EndPointDetails endPoint : detailsList) {
                System.out.println("\t"+endPoint);
            }
        }

    }

    /**
     * Appelée lors de la destruction du contexte servlet (arrêt de l'application).
     * 
     * @param sce l'événement de destruction du contexte servlet
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
