package mg.itu.ermite.framework.util;

import java.util.HashMap;

public class ModelView {
    private String view;    

    private HashMap<String,Object> attributes = new HashMap<>();

    public ModelView() {
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key,Object value)
    {
        attributes.put(key,value);
    }
    
}
