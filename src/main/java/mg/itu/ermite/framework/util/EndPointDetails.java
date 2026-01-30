package mg.itu.ermite.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.itu.ermite.framework.annotation.RequestParam;
import mg.itu.ermite.framework.util.security.SecurityHandler;

/**
 * Classe contenant les détails d'un endpoint et gérant l'invocation de méthode.
 * 
 * EndPointDetails encapsule toutes les informations concernant un endpoint :
 * - La classe du contrôleur
 * - La méthode à invoquer
 * - La méthode HTTP (GET, POST, *)
 * 
 * Responsabilités principales :
 * 1. Invoquer la méthode du contrôleur avec les paramètres appropriés
 * 2. Binder (lier) les paramètres de la requête HTTP aux paramètres de la méthode
 * 3. Convertir les types (String => int, Date, etc.)
 * 4. Construire les objets complexes à partir des paramètres HTTP
 * 5. Gérer les collections et tableaux de paramètres
 * 6. Gérer l'upload de fichiers (Map<String, List<FileData>>)
 * 7. Appliquer les contrôles de sécurité
 * 
 * Le binding supporte :
 * - Les types primitifs (int, String, boolean, double, etc.)
 * - Les objets POJOs (Plain Old Java Objects)
 * - Les collections (List<T>)
 * - Les tableaux (T[])
 * - Les types génériques (List<T>, Map<String, T>, etc.)
 * - Les fichiers uploadés (FileData)
 * - La session HTTP (SessionMap)
 * - Les paramètres URL (path variables)
 * 
 * @author Framework S5
 * @version 1.0
 * @see FrontServlet
 * @see ClasspathScanner
 * @see Reflection
 */
public class EndPointDetails {
    private String className;
    private Method method;
    private String httpMethod;
    
    /**
     * Constructeur par défaut.
     */
    public EndPointDetails() {
    }

    /**
     * Constructeur avec classe et méthode.
     * 
     * @param className le nom qualifié de la classe du contrôleur
     * @param method la méthode à invoquer
     */
    public EndPointDetails(String className, Method method) {
        this.className = className;
        this.method = method;
    }

    /**
     * Invoque la méthode du contrôleur avec les paramètres extraits de la requête.
     * 
     * Processus d'invocation :
     * 1. Vérifie les permissions de sécurité
     * 2. Crée une instance du contrôleur
     * 3. Récupère les paramètres de la méthode
     * 4. Pour chaque paramètre :
     *    - Si c'est Map<String, Object> : ajoute tous les paramètres HTTP
     *    - Si c'est Map<String, List<FileData>> : ajoute les fichiers uploadés
     *    - Si c'est SessionMap : passe la session
     *    - Si c'est primitif : convertit le String en type cible
     *    - Si c'est un objet complexe : effectue un binding récursif
     * 5. Invoque la méthode avec les paramètres préparés
     * 6. Retourne le résultat ou propage l'exception
     * 
     * @param request la requête HTTP
     * @param urlParams map des paramètres extraits de l'URL (path variables)
     * @return le résultat de l'invocation de la méthode
     * @throws RuntimeException en cas d'erreur d'invocation ou de binding
     */
    public Object invokeMethod(HttpServletRequest request,Map<String,String> urlParams) {
        try {
            SecurityHandler.checkAbilityToInvoke(method, request);
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getConstructor().newInstance();
            
            Parameter[] parameters = method.getParameters();
            List<Class<?>> parameterTypes = new ArrayList<>();
            for (Parameter param : parameters) {
                parameterTypes.add(param.getType());
            }
            
            List<Object> arguments = new ArrayList<>();

            for (Parameter parameter : parameters) {
                String paramValue = null;
                String urlValue = urlParams.get(parameter.getName());
                if (parameter.getType().equals(Map.class)) {
                    try {
                        ParameterizedType mapType = (ParameterizedType) parameter.getParameterizedType();
                        Type[] typeArguments = mapType.getActualTypeArguments();
                        
                        if (typeArguments.length == 2) {
                            Type keyType = typeArguments[0];
                            Type valueType = typeArguments[1];
                            
                            // Cas 1: Map<String, Object>
                            if (keyType.equals(String.class) && valueType.equals(Object.class)) {
                                System.out.println("Nous allons remplir le map de paramètres");
                                arguments.add(request.getParameterMap());
                                continue;
                            }
                            
                            // Cas 2: Map<String, List<byte[]>>
                            if (keyType.equals(String.class) && valueType instanceof ParameterizedType) {
                                ParameterizedType listType = (ParameterizedType) valueType;
                                
                                if (listType.getRawType().equals(List.class)) {
                                    Type listElementType = listType.getActualTypeArguments()[0];
                                    
                                    if (listElementType.equals(FileData.class)) {
                                        System.out.println("Nous allons remplir le map de fichiers");
                                        
                                        // Vérifier que c'est bien une requête multipart
                                        String contentType = request.getContentType();
                                        if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
                                            throw new IllegalArgumentException(
                                                "Le paramètre " + parameter.getName() + " nécessite une requête multipart/form-data"
                                            );
                                        }
                                        
                                        Map<String, List<FileData>> fileMap = new HashMap<>();
                                        
                                        try {
                                            Collection<Part> parts = request.getParts();
                                            
                                            for (Part part : parts) {
                                                // Vérifier si c'est un fichier (pas un paramètre normal)
                                                if (part.getSubmittedFileName() != null) {
                                                    try (InputStream in = part.getInputStream()) {
                                                        byte[] fileBytes = in.readAllBytes();
                                                        
                                                        fileMap.computeIfAbsent(part.getName(), 
                                                            s -> new ArrayList<>()).add(new FileData(fileBytes,part.getSubmittedFileName()));
                                                    }
                                                }
                                            }
                                            
                                            arguments.add(fileMap);
                                            continue;
                                            
                                        } catch (IOException | ServletException e) {
                                            throw new RuntimeException("Erreur lors de la lecture des fichiers", e);
                                        }
                                    }
                                }
                            }
                            
                            // Autres types de Map non gérés
                            throw new IllegalArgumentException(
                                "Type Map non supporté: " + parameter.getParameterizedType()
                            );
                        }
                        
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException(
                            "Erreur de type pour le paramètre " + parameter.getName(), e
                        );
                    }
                }
                else if(parameter.getType() == SessionMap.class)
                {
                    arguments.add(new SessionMap(request.getSession()));
                }
                else if(Reflection.isPrimitiveType(parameter.getType())){
                    if(urlValue != null)
                    {
                        paramValue = urlValue;
                    }
                    else{
                        if(parameter.isAnnotationPresent(RequestParam.class))
                        {
                            paramValue = request.getParameter(parameter.getAnnotation(RequestParam.class).value());
                        }
                        else{
                            paramValue = request.getParameter(parameter.getName());
                        }
                        if (paramValue == null) {
                            throw new IllegalArgumentException(
                                "Parametre '" + (!parameter.isAnnotationPresent(RequestParam.class) ? parameter.getName() : parameter.getAnnotation(RequestParam.class).value()) + "' non fourni pour la methode '" + method.getName() + "'"
                            );
                        }
                    }
    
                    
                    Object convertedValue = Reflection.convertType(paramValue, parameter.getType());
                    arguments.add(convertedValue);
                }
                else{
                    //Ici,je dois trouver un moyen pour passer le genericType
                    //ici, on va faire le binding 
                    Object argument = bindObject(request, parameter.getParameterizedType(), parameter.getName());
                    arguments.add(argument);
                }
            }
            
            return method.invoke(instance, arguments.toArray());

        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Throwable targetException = e.getTargetException();
            throw new RuntimeException("Erreur lors de l'execution de la methode", targetException);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du processus de la methode de l'endpoint:"+e.getMessage(), e);
        }
    }

    /**
     * Récupère l'index maximum d'un paramètre de tableau dans la requête.
     * 
     * Utilisé pour déterminer la taille des tableaux lors du binding.
     * Exemple : Si les paramètres sont "items[0]", "items[1]", "items[2]",
     * retourne 2 (l'index maximal).
     * 
     * @param request la requête HTTP
     * @param argumentName le nom du paramètre de tableau
     * @return l'index maximal trouvé ou null s'il n'y en a pas
     */
    private Integer getMaxArrayIndex(HttpServletRequest request, String argumentName) {
        int maxIndex = -1;
        Enumeration<String> paramNames = request.getParameterNames();
        Pattern pattern = Pattern.compile(Pattern.quote(argumentName) + "(?:\\[(\\d+)])+(\\..+)?");
        System.out.println("Argument name:"+argumentName);
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            System.out.println("Actual name:"+name);
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                System.out.println("On a un matching");
                int idx = Integer.parseInt(matcher.group(1));
                if (idx > maxIndex) maxIndex = idx;
            }
        }
        return maxIndex >= 0 ? maxIndex : null;
    }

    /**
     * Effectue le binding (liaison) entre les données de requête HTTP et les objets Java.
     * 
     * Cette méthode est la clé du framework, elle transforme les paramètres HTTP
     * en objets Java typés. Elle gère :
     * - Les types primitifs (conversion String => type)
     * - Les types complexes (binding récursif de propriétés)
     * - Les collections (List<T>)
     * - Les tableaux (T[])
     * - Les types génériques
     * 
     * @param request la requête HTTP
     * @param objectType le type cible (peut être générique)
     * @param argumentName le nom du paramètre HTTP ou le préfixe pour les objets imbriqués
     * @return l'objet bindé du type spécifié
     * @throws Exception en cas d'erreur de binding
     */
    private <T> T bindObject(HttpServletRequest request,
                         Type objectType,
                         String argumentName) throws Exception {
    
        Class<?> objectClass = null;

        if(objectType instanceof Class) objectClass = (Class<?>) objectType;

        if(objectClass != null)
        {
            // 1. Si c'est primitif => bind direct
            if (Reflection.isPrimitiveType(objectClass)) {
                String raw = null;
                
                raw = request.getParameter(argumentName);
                
                if(raw == null)
                {
                    //appeler une fonction retournant un defaultValue 
                    return (T) Reflection.getDefaultValue(objectClass);
                }
                return (T) Reflection.convertType(raw, objectClass);
            }
    
            // 2. Si c'est un tableau => appeler une méthode spéciale
            if (objectClass.isArray()) {
                return (T) bindArray(request, objectType, argumentName);
            }
    
            // 3. Si c'est une liste ou autre collection => méthode spéciale
            if (Reflection.isCollectionType(objectType)) {
                return (T) bindCollection(request, objectType, argumentName);
            }

            if (objectType instanceof GenericArrayType) {
                Type componentType = ((GenericArrayType) objectType).getGenericComponentType();
                return (T) bindGenericArray(request, componentType, argumentName);
            }
    
            // 4. Si c’est un objet complexe => bind par réflexion
            return (T) bindPojo(request, objectType, argumentName);
        }
        else{
            if (Reflection.isCollectionType(objectType)) {
                System.out.println("Nous tombons dans la gestion de collections en fallback");
                return (T) bindCollection(request, objectType, argumentName);
            }
            else{
                throw new Exception("Erreur lors du bind de l'objet:Obtention de la classe generique du type '"+objectType.getTypeName()+"' impossible");
            }
        }
    }

    /**
     * Effectue le binding d'un tableau générique (T[]).
     * 
     * @param request la requête HTTP
     * @param component le type des éléments du tableau
     * @param argumentName le nom du paramètre HTTP
     * @return le tableau bindé
     * @throws Exception en cas d'erreur
     */
    private <T> T bindGenericArray(HttpServletRequest request, Type component, String argumentName) throws Exception {
        Integer maxIndex = getMaxArrayIndex(request, argumentName);

        if (maxIndex == null) {
            return (T) Array.newInstance(Reflection.resolveClass(component), 0);
        }

        Class<?> componentClass = Reflection.resolveClass(component);
        Object array = Array.newInstance(componentClass, maxIndex + 1);

        for (int i = 0; i <= maxIndex; i++) {
            Object elem = bindObject(request, component, argumentName + "[" + i + "]");
            Array.set(array, i, elem);
        }

        return (T) array;
    }

    /**
     * Effectue le binding d'un tableau typé (par exemple int[], String[]).
     * 
     * Gère les paramètres HTTP de la forme : paramName[0], paramName[1], etc.
     * Construït dynamiquement un tableau des éléments bindés.
     * 
     * @param request la requête HTTP
     * @param arrayType le type du tableau
     * @param argumentName le nom du paramètre HTTP
     * @return le tableau bindé
     * @throws Exception en cas d'erreur
     */    private <T> T bindArray(HttpServletRequest request,
                            Type arrayType,
                            String argumentName) throws Exception 
    {

        
        Class<?> arrayClass = null;

        if(arrayType instanceof Class) arrayClass = (Class<?>) arrayType;
        if(arrayClass == null)
        {
            throw new Exception("Erreur lors du bind array:Obtention de la classe generique de l'array impossible");
        }

        Class<?> elementType = arrayClass.getComponentType();
        System.out.println("Let's bind this array that contains "+elementType.getName());

        Integer maxIndex = getMaxArrayIndex(request, argumentName);

        System.out.println("MaxIndex:"+maxIndex+" for argument Name:"+argumentName);

        if (maxIndex == null) {
            return (T) Array.newInstance(elementType, 0);
        }

        Object array = Array.newInstance(elementType, maxIndex+1);

        for (int i = 0; i <= maxIndex; i++) {
            System.out.println("Je vais essayer d'obtenir le bind de la "+i+" eme valeur");
            Object elem = bindObject(request, elementType, argumentName+"["+i+"]");
            Array.set(array, i, elem);
        }
        return (T) array;
    }


    /**
     * Effectue le binding d'une collection générique (List<T>, Set<T>, etc.).
     * 
     * Gère les paramètres HTTP de la forme : paramName[0], paramName[1], etc.
     * Construit une liste contenant les éléments bindés du type générique.
     * 
     * @param request la requête HTTP
     * @param collectionType le type générique de la collection
     * @param argumentName le nom du paramètre HTTP
     * @return la collection bindée
     * @throws Exception en cas d'erreur
     */
    private <T> Collection<T> bindCollection(HttpServletRequest request, Type collectionType, String argumentName) throws Exception {
        Type elementType = ((ParameterizedType) collectionType).getActualTypeArguments()[0];
        Collection<T> result = new ArrayList<>();

        Integer maxIndex = getMaxArrayIndex(request, argumentName);
        System.out.println("MaxIndex:"+maxIndex+" for argument Name:"+argumentName);
        if(maxIndex == null) return result;

        for (int i = 0; i <= maxIndex; i++) {
            System.out.println("Indice de la ligne actuelle:"+i);
            System.out.println("ElementType:"+elementType.getTypeName());
            Object elem = bindObject(request, elementType, argumentName+"["+i+"]");
            result.add((T) elem);
        }

        return result;
    }


    /**
     * Effectue le binding d'un objet métier (POJO - Plain Old Java Object).
     * 
     * Pour chaque propriété de la classe :
     * 1. Cherche un paramètre HTTP correspondant (prefix.propertyName)
     * 2. Effectue un binding récursif pour les propriétés imbriquées
     * 3. Utilise le setter pour affecter la valeur
     * 
     * Exemple : Pour un objet User avec propriété "address" de type Address,
     * les paramètres HTTP seraient :
     * - address.street=Rue de la Paix
     * - address.city=Paris
     * 
     * @param request la requête HTTP
     * @param objectType le type de l'objet à binder
     * @param prefix le préfixe du paramètre HTTP
     * @return l'objet bindé
     * @throws Exception en cas d'erreur
     */
    //mba mampiasa terme technique kely,haha :bind old java object
    private <T> T bindPojo(HttpServletRequest request,
                       Type objectType,
                       String prefix) throws Exception {

        Class<?> objectClass = null;

        if(objectType instanceof Class) objectClass = (Class<?>) objectType;
        if(objectClass == null)
        {
            throw new Exception("Erreur lors du bind de l'objet :Obtention de la classe generique du model impossible");
        }

        T instance = (T) objectClass.getConstructor().newInstance();

        for (Field field : objectClass.getDeclaredFields()) {

            String fieldName = field.getName();
            System.out.println("FieldName:"+fieldName+"|FieldType:"+field.getType().getName());
            Class<?> fieldType = field.getType();

            String paramName = prefix + "." + fieldName;

            Object value = bindObject(request, field.getGenericType(), paramName);

            String setterName = "set" +
                Character.toUpperCase(fieldName.charAt(0)) +
                fieldName.substring(1);
            System.out.println("Setter found:"+setterName);
            System.out.println("Value to set:"+value);
            System.out.println("Length");
            if(value!=null)
            {
                Method setter = objectClass.getMethod(setterName, fieldType);
                setter.invoke(instance, value);
            }    
        }

        return instance;
    }

    /**
     * Récupère le nom qualifié de la classe du contrôleur.
     * 
     * @return le nom complet de la classe (package.NomClasse)
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Définit le nom qualifié de la classe du contrôleur.
     * 
     * @param className le nom complet de la classe
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * Récupère la méthode associée à cet endpoint.
     * 
     * @return l'objet Method représentant la méthode
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Définit la méthode associée à cet endpoint.
     * 
     * @param method l'objet Method à associer
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Récupère la méthode HTTP associée (GET, POST, *).
     * 
     * @return la méthode HTTP
     */
    public String getHttpMethod() {
        return httpMethod;
    }
    
    /**
     * Définit la méthode HTTP associée (GET, POST, ou * pour tous).
     * 
     * @param httpMethod la méthode HTTP
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    /**
     * Retourne une représentation textuelle de l'endpoint.
     * 
     * Format : Endpoint(HTTP_METHOD:XXX | CLASS:className[METHOD:methodName])
     * 
     * @return une chaîne décrivant l'endpoint
     */
    @Override
    public String toString()
    {
        return "Endpoint(HTTP_METHOD:"+httpMethod+" | CLASS:"+className+"[METHOD:"+method+"])";
    }

}
