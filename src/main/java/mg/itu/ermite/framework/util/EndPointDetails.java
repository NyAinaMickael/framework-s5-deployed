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

public class EndPointDetails {
    private String className;
    private Method method;
    private String httpMethod;
    
    public EndPointDetails() {
    }

    public EndPointDetails(String className, Method method) {
        this.className = className;
        this.method = method;
    }

    /*
     * Permet d'appeler la fonction correspondant aux informations de l'instance actuelle.
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

    /*
     * Cette methode sert a la creation d'une entite non primitive a partir de donnees passes via une requete http
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


    private <T> T bindArray(HttpServletRequest request,
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

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    @Override
    public String toString()
    {
        return "Endpoint(HTTP_METHOD:"+httpMethod+" | CLASS:"+className+"[METHOD:"+method+"])";
    }

}
