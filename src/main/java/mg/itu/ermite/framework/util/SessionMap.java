package mg.itu.ermite.framework.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

/**
 * Wrapper de la session HTTP implémentant l'interface Map.
 * 
 * SessionMap permet de manipuler la session HTTP de manière identique à une Map.
 * Cela offre une interface uniforme et facilite le passage de la session aux
 * méthodes de contrôleur sans exposure directe de l'API servlet.
 * 
 * Utilisation dans un contrôleur :
 * <pre>
 * @Controller
 * public class AuthController {
 *     @UrlMapping(url = "/login")
 *     @PostMapping
 *     public ModelView login(SessionMap session, String username) {
 *         // Stocker des données en session
 *         session.put("userId", 123);
 *         session.put("userName", username);
 *         session.put("role", "USER");
 *         
 *         // Récupérer des données
 *         Integer userId = (Integer) session.get("userId");
 *         
 *         // Supprimer des données
 *         session.remove("tempData");
 *         
 *         // Nettoyer la session
 *         session.clear();
 *     }
 * }
 * </pre>
 * 
 * SessionMap est injectée automatiquement dans les paramètres de méthode
 * annotés d'un contrôleur si leur type est SessionMap.
 * 
 * @author Framework S5
 * @version 1.0
 * @see EndPointDetails
 */
public class SessionMap implements Map<String,Object> {

    private final HttpSession session;

    /**
     * Crée un SessionMap wrappant une session HTTP.
     * 
     * @param session la session HTTP à wrapper
     */
    public SessionMap(HttpSession session) {
        this.session = session;
    }

    /**
     * Retourne la taille de la session (nombre d'attributs).
     * 
     * @return le nombre d'attributs
     * @throws UnsupportedOperationException si non implémenté
     */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    /**
     * Vérifie si la session est vide.
     * 
     * @return true si la session n'a pas d'attributs
     * @throws UnsupportedOperationException si non implémenté
     */
    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    /**
     * Vérifie si un attribut existe en session.
     * 
     * @param key le nom de l'attribut
     * @return true si l'attribut existe
     * @throws UnsupportedOperationException si non implémenté
     */
    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsKey'");
    }

    /**
     * Vérifie si une valeur existe en session.
     * 
     * @param value la valeur à chercher
     * @return true si la valeur existe
     * @throws UnsupportedOperationException si non implémenté
     */
    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    /**
     * Récupère un attribut de la session.
     * 
     * @param key le nom de l'attribut
     * @return la valeur de l'attribut ou null
     */
    @Override
    public Object get(Object key) {
        return session.getAttribute((String) key);
    }

    /**
     * Ajoute ou met à jour un attribut dans la session.
     * 
     * @param key le nom de l'attribut
     * @param value la valeur à stocker
     * @return la valeur précédente ou null
     */
    @Override
    public Object put(String key, Object value) {
        session.setAttribute(key, value);
        return session.getAttribute(key);
    }

    /**
     * Supprime un attribut de la session.
     * 
     * @param key le nom de l'attribut à supprimer
     * @return la valeur de l'attribut supprimé
     */
    @Override
    public Object remove(Object key) {
        Object value = get(key);
        session.removeAttribute((String) key);
        return value;
    }

    /**
     * Ajoute plusieurs attributs à la session en une seule opération.
     * 
     * @param m une map contenant les attributs à ajouter
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        m.forEach((k, v) -> session.setAttribute(k, v));
    }

    /**
     * Vide la session (supprime tous les attributs).
     */
    @Override
    public void clear() {
        if (session == null) return;
        
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            session.removeAttribute(names.nextElement());
        }
    }

    /**
     * Récupère l'ensemble des noms d'attributs de la session.
     * 
     * @return un Set contenant les noms des attributs
     */
    @Override
    public Set<String> keySet() {
        if (session == null) return Collections.emptySet();
        return new HashSet<>(Collections.list(session.getAttributeNames()));
    }

    /**
     * Récupère la collection des valeurs d'attributs de la session.
     * 
     * @return une Collection contenant les valeurs
     */
    @Override
    public Collection<Object> values() {
        if (session == null) return Collections.emptyList();
        
        Collection<Object> values = new java.util.ArrayList<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            values.add(session.getAttribute(names.nextElement()));
        }
        return values;
    }

    /**
     * Récupère l'ensemble des paires clé-valeur de la session.
     * 
     * @return un Set contenant les entries (paires clé-valeur)
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        if (session == null) return Collections.emptySet();
        
        Map<String, Object> entries = new HashMap<>();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            entries.put(name, session.getAttribute(name));
        }
        return entries.entrySet();
    }
    
}
