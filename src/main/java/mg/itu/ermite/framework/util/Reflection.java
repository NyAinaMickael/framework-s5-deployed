package mg.itu.ermite.framework.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Classe utilitaire fournissant des méthodes de réflexion pour le framework.
 * 
 * Reflection offre des fonctionnalités essentielles pour :
 * - La conversion de types (String vers int, boolean, double, etc.)
 * - L'identification des types primitifs et simples
 * - La détection des collections et types génériques
 * - L'obtention de valeurs par défaut pour les types
 * 
 * Ces méthodes sont utilisées par :
 * - EndPointDetails pour binder les paramètres de requête
 * - FrontServlet pour traiter les réponses
 * - L'injection de dépendances et le data binding
 * 
 * @author Framework S5
 * @version 1.0
 * @see EndPointDetails
 * @see FrontServlet
 */
public class Reflection {
    
    /**
     * Convertit une valeur String vers le type cible spécifié.
     * 
     * Supporte les conversions vers :
     * - String
     * - Types numériques primitifs et wrappers (int, Integer, float, Float, etc.)
     * - Types booléens (boolean, Boolean)
     * - Et potentiellement d'autres types...
     * 
     * @param value la chaîne de caractères à convertir
     * @param targetType le type cible
     * @return la valeur convertie
     * @throws IllegalArgumentException si le type n'est pas supporté
     */
    public static Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return (int) Integer.parseInt(value);
        if (targetType == float.class || targetType == Float.class) return (float) Float.parseFloat(value);
        if (targetType == double.class || targetType == Double.class) return (double) Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return (boolean) Boolean.parseBoolean(value);
        // ... autres conversions
        throw new IllegalArgumentException("Parametre non pris en compte: " + targetType);
    }

    /**
     * Vérifie si un type est un type "primitif" au sens du framework.
     * 
     * Les types primitifs incluent :
     * - Types Java primitifs (int, boolean, double, etc.)
     * - String
     * - Enums
     * - UUID
     * - Wrappers numériques (Integer, Long, Double, Boolean, etc.)
     * - Grands nombres (BigDecimal, BigInteger)
     * - Types date/heure (LocalDate, Date, Timestamp, etc.)
     * 
     * Ces types sont considérés comme "simples" et peuvent être convertis
     * directement à partir de String lors du binding des paramètres HTTP.
     * 
     * @param clazz la classe à vérifier
     * @return true si c'est un type primitif au sens du framework
     */
    public static boolean isPrimitiveType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        // 1. Types de base Java
        if (clazz.isPrimitive() || 
            clazz == String.class ||
            clazz.isEnum() ||
            clazz == java.util.UUID.class) {
            return true;
        }
        
        // 2. Wrappers numériques et booléens
        if (clazz == Integer.class || clazz == Long.class || 
            clazz == Double.class || clazz == Float.class ||
            clazz == Boolean.class || clazz == Character.class ||
            clazz == Byte.class || clazz == Short.class) {
            return true;
        }
        
        // 3. Grands nombres
        if (clazz == java.math.BigDecimal.class || 
            clazz == java.math.BigInteger.class) {
            return true;
        }
        
        // 4. Dates et heures
        if (java.time.temporal.Temporal.class.isAssignableFrom(clazz) ||
            clazz == java.util.Date.class ||
            clazz == java.sql.Date.class ||
            clazz == java.sql.Time.class ||
            clazz == java.sql.Timestamp.class ||
            clazz == java.util.Calendar.class) {
            return true;
        }
        
        return false;
    }

    /**
     * Détermine si un type est une collection (List, Set, Collection, etc.).
     * 
     * Supporte les collections génériques paramétrées :
     * - List<T>
     * - Set<T>
     * - Collection<T>
     * - Et autres implémentations de Collection
     * 
     * @param type le type à vérifier (peut être un Type générique)
     * @return true si c'est un type collection
     */
    public static boolean isCollectionType(Type type) {
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return raw instanceof Class && Collection.class.isAssignableFrom((Class<?>) raw);
        }
        if (type instanceof Class) {
            return Collection.class.isAssignableFrom((Class<?>) type);
        }
        return false;
    }
    
    /**
     * Résout un Type générique et retourne la Class correspondante.
     * 
     * Supporte :
     * - Les classes simples (Class<T>)
     * - Les types génériques paramétrés (ParameterizedType)
     * - Les types de tableaux génériques (GenericArrayType)
     * 
     * @param t le type générique à résoudre
     * @return la Class résolue
     * @throws IllegalArgumentException si le type ne peut pas être résolu
     */
    public static Class<?> resolveClass(Type t) {
        if (t instanceof Class) return (Class<?>) t;
        if (t instanceof GenericArrayType)
            return Array.newInstance(resolveClass(((GenericArrayType)t).getGenericComponentType()), 0).getClass();
        if (t instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType)t).getRawType();
        throw new IllegalArgumentException("Type non résoluble: " + t);
    }

    /**
     * Retourne la valeur par défaut pour un type donné.
     * 
     * Comportement :
     * - Pour les primitifs : false, 0, 0L, 0.0, etc.
     * - Pour les wrappers et objets : null
     * - Pour void : null
     * 
     * Utilisé lors du binding pour initialiser les paramètres non fournis.
     * 
     * @param clazz le type pour lequel obtenir la valeur par défaut
     * @return la valeur par défaut du type
     */
    public static <T> T getDefaultValue(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        
        // Pour les types primitifs, retourner leur valeur par défaut
        if (clazz == boolean.class) {
            return (T) Boolean.FALSE;
        } else if (clazz == byte.class) {
            return (T) Byte.valueOf((byte) 0);
        } else if (clazz == char.class) {
            return (T) Character.valueOf('\0');
        } else if (clazz == short.class) {
            return (T) Short.valueOf((short) 0);
        } else if (clazz == int.class) {
            return (T) Integer.valueOf(0);
        } else if (clazz == long.class) {
            return (T) Long.valueOf(0L);
        } else if (clazz == float.class) {
            return (T) Float.valueOf(0.0f);
        } else if (clazz == double.class) {
            return (T) Double.valueOf(0.0);
        } else if (clazz == void.class) {
            return null; // void n'a pas de valeur
        }
        
        // Pour les types wrappers et objets, retourner null
        // car ils peuvent accepter null
        return null;
    }
}
