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

public class FrameworkInitializer implements ServletContextListener {

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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
