package mg.itu.ermite.framework.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class Reflection {
    public static Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return (int) Integer.parseInt(value);
        if (targetType == float.class || targetType == Float.class) return (float) Float.parseFloat(value);
        if (targetType == double.class || targetType == Double.class) return (double) Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return (boolean) Boolean.parseBoolean(value);
        // ... autres conversions
        throw new IllegalArgumentException("Parametre non pris en compte: " + targetType);
    }

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
    public static Class<?> resolveClass(Type t) {
        if (t instanceof Class) return (Class<?>) t;
        if (t instanceof GenericArrayType)
            return Array.newInstance(resolveClass(((GenericArrayType)t).getGenericComponentType()), 0).getClass();
        if (t instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType)t).getRawType();
        throw new IllegalArgumentException("Type non résoluble: " + t);
    }

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
