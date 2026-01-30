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
 * Scanner de classpath qui découvre automatiquement les contrôleurs et endpoints.
 * 
 * ClasspathScanner utilise le ClassLoader pour parcourir le classpath et identifier :
 * 1. Toutes les classes disponibles (compilées et dans les JAR)
 * 2. Celles annotées avec @Controller
 * 3. Les méthodes annotées avec @UrlMapping
 * 4. Les méthodes annotées avec @GetMapping ou @PostMapping
 * 
 * Ce scanner est universel et compatible avec :
 * - Les applications WAR
 * - Les applications JAR
 * - Les applications EAR
 * 
 * Le scanner parcourt :
 * - Les classes compilées dans WEB-INF/classes/ (en environnement web)
 * - Les JAR dans WEB-INF/lib/
 * 
 * Résultat : une Map<String, List<EndPointDetails>> où :
 * - La clé est l'URL mappée (ex: "/users/{id}")
 * - La valeur est une liste des endpoints (différentes méthodes HTTP sur même URL)
 * 
 * @author Framework S5
 * @version 1.0
 * @see Controller
 * @see UrlMapping
 * @see GetMapping
 * @see PostMapping
 * @see EndPointDetails
 */
public class ClasspathScanner {

    /**
     * Scanne le classpath pour découvrir tous les endpoints annotés.
     * 
     * Processus :
     * 1. Trouve toutes les classes avec @Controller
     * 2. Pour chaque classe, cherche les méthodes avec @UrlMapping
     * 3. Identifie la méthode HTTP (GET, POST, ou *)
     * 4. Enregistre l'endpoint dans la map
     * 
     * @return une Map où les clés sont les URLs et les valeurs sont les listes d'EndPointDetails
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

    /**
     * Trouve toutes les classes du classpath annotées avec une annotation spécifiée.
     * 
     * @param annotation l'annotation à chercher
     * @return une liste de classes annotées
     */
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
     * Récupère l'ensemble de tous les noms de classes disponibles sur le classpath.
     * 
     * Parcourt les répertoires WEB-INF/classes et les JAR du WEB-INF/lib.
     * 
     * @return un ensemble contenant tous les noms de classes
     * @throws IOException en cas d'erreur lors de la lecture des ressources
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
     * Parcourt récursivement un répertoire pour trouver les fichiers .class.
     * 
     * @param directory le répertoire à parcourir
     * @param packageName le nom du package en cours
     * @param classNames l'ensemble où ajouter les noms de classes trouvées
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
     * Parcourt un fichier JAR pour extraire les noms de classes.
     * 
     * @param jarPath le chemin complet du fichier JAR
     * @param classNames l'ensemble où ajouter les noms de classes trouvées
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
