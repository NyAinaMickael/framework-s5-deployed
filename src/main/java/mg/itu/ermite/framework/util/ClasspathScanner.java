package mg.itu.ermite.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import mg.itu.ermite.framework.annotation.Controller;
import mg.itu.ermite.framework.annotation.GetMapping;
import mg.itu.ermite.framework.annotation.PostMapping;
import mg.itu.ermite.framework.annotation.UrlMapping;

/**
 * Scanner universel basé sur le ClassLoader.
 * Compatible WAR / JAR / EAR.
 */
public class ClasspathScanner {

    /**
     * Scanne toutes les classes disponibles sur le classpath du chargeur courant
     * et retourne celles annotées avec une annotation donnée.
     */

    public static Map<String,Object> findMappedUrls()
    {
        System.out.println("[Framework] Chargement des enpoints...");
        Map<String,Object> mappedEndpoints  = new HashMap<>(); 
        List<Class<?>> controllers = findAnnotatedClasses(Controller.class);

        for (Class<?> controller : controllers)
        {
            Method[] methods = controller.getDeclaredMethods(); 
            for(Method method : methods)
            {
                if(method.isAnnotationPresent(UrlMapping.class))
                {
                    String url = method.getAnnotation(UrlMapping.class).url();

                    EndPointDetails endpointDetails = new EndPointDetails();
                    endpointDetails.setClassName(controller.getName());
                    endpointDetails.setMethod(method);

                    if(method.isAnnotationPresent(GetMapping.class))
                    {
                        endpointDetails.setHttpMethod("GET");
                    }
                    else if(method.isAnnotationPresent(PostMapping.class))
                    {
                        endpointDetails.setHttpMethod("POST");
                    }
                    else{
                        endpointDetails.setHttpMethod("*");
                    }

                    if(mappedEndpoints.containsKey(url))
                    {
                        ((List<EndPointDetails>) mappedEndpoints.get(url)).add(endpointDetails);
                    }
                    else{
                        List<EndPointDetails> endpoints = new ArrayList<>();
                        endpoints.add(endpointDetails);
                        mappedEndpoints.put(url,endpoints);
                    }
                }
            }  
        }
        return mappedEndpoints;
    } 

    public static List<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation) {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        Set<String> allClassNames = new HashSet<>();

        try {
            allClassNames.addAll(getAllClassNames());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[Framework] Nombre total de classes détectées : " + allClassNames.size());

        for (String className : allClassNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotation)) {
                    annotatedClasses.add(clazz);
                }
            } catch (Throwable ignored) {}
        }

        return annotatedClasses;
    }

    /**
     * Récupère la liste de tous les noms de classes connus du classpath.
     */
    private static Set<String> getAllClassNames() throws IOException {
        Set<String> classNames = new HashSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = cl.getResources("");

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String path = URLDecoder.decode(url.getPath(), "UTF-8");

            // --- Classes compilées dans WEB-INF/classes ---
            if (path.contains("/WEB-INF/classes/")) {
                File classesDir = new File(path);
                if (classesDir.exists()) {
                    System.out.println("- Parcours du répertoire : " + classesDir.getAbsolutePath());
                    scanDirectory(classesDir, "", classNames);
                }
            }

            // --- Classes contenues dans les JAR du WEB-INF/lib ---
            else if (path.contains("/WEB-INF/lib/") && path.endsWith(".jar!/")) {
                String jarPath = path.substring("file:".length(), path.indexOf("!"));
                scanJarFile(jarPath, classNames);
            }
        }

        return classNames;
    }

    /**
     * Parcours récursif d’un dossier pour trouver les classes compilées.
     */
    private static void scanDirectory(File directory, String packageName, Set<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + file.getName() + ".", classNames);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * Parcourt le contenu d’un fichier JAR pour trouver les classes.
     */
    private static void scanJarFile(String jarPath, Set<String> classNames) {
        System.out.println("- Lecture du JAR : " + jarPath);
        try (InputStream is = new URL("file:" + jarPath).openStream();
             JarInputStream jarStream = new JarInputStream(is)) {

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    classNames.add(className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
