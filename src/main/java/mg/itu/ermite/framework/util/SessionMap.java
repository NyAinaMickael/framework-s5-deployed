package mg.itu.ermite.framework.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class SessionMap implements Map<String,Object> {

    private final HttpSession session;


    public SessionMap(HttpSession session) {
        this.session = session;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'size'");
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsKey'");
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    @Override
    public Object get(Object key) {
        return session.getAttribute((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        session.setAttribute(key, value);
        return session.getAttribute(key);
    }

    @Override
    public Object remove(Object key) {
        Object value = get(key);
        session.removeAttribute((String) key);
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        m.forEach((k, v) -> session.setAttribute(k, v));
    }

    @Override
    public void clear() {
        if (session == null) return;
        
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            session.removeAttribute(names.nextElement());
        }
    }

    @Override
    public Set<String> keySet() {
        if (session == null) return Collections.emptySet();
        return new HashSet<>(Collections.list(session.getAttributeNames()));
    }

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
