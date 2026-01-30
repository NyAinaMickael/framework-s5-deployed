package mg.itu.ermite.framework.util.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.ermite.framework.annotation.security.Authorized;
import mg.itu.ermite.framework.annotation.security.Role;
import mg.itu.ermite.framework.config.ConfigManager;

public class SecurityHandler {
    public static void checkAbilityToInvoke(Method method, HttpServletRequest request) throws Exception
    {
        
        if(method.isAnnotationPresent(Authorized.class))
        {
            
            Object userSessionId = request.getSession().getAttribute(ConfigManager.getInstance().get("userSessionId"));
            if(userSessionId == null) throw new Exception("Vous devez etre connecte pour pouvoir acceder a la methode:"+method.getName());
            if(method.isAnnotationPresent(Role.class))
            {
                String userSessionRole = (String) request.getSession().getAttribute(ConfigManager.getInstance().get("userSessionRole"));
                if(userSessionRole == null) throw new Exception("Votre utilisateur actuel n'a defini aucun role");
                if(!new ArrayList<>(Arrays.asList(method.getAnnotation(Role.class).value())).contains(userSessionRole))
                {
                    throw new Exception("Le role ["+userSessionRole+"] est insuffisant pour acceder a la methode :"+method.getName());
                }
            }
            
            //aveo verifier-na ato hoe manana acces amle methode ve ilay role connecte amzao
            //raha tsy manana acces dia on throw une exception
        }
        else{
            return;
        }
    }
}
