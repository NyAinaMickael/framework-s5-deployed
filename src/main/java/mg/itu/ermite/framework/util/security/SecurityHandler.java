package mg.itu.ermite.framework.util.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.ermite.framework.annotation.security.Authorized;
import mg.itu.ermite.framework.annotation.security.Role;
import mg.itu.ermite.framework.config.ConfigManager;

/**
 * Gestionnaire de sécurité du framework qui applique les contrôles d'accès.
 * 
 * SecurityHandler est appelé avant chaque invocation de méthode pour vérifier :
 * 1. Si la méthode requiert une authentification (@Authorized)
 * 2. Si l'utilisateur est effectivement connecté
 * 3. Si la méthode requiert des rôles spécifiques (@Role)
 * 4. Si l'utilisateur a le rôle requis
 * 
 * Les rôles et identifiants utilisateurs sont stockés en session HTTP à l'aide de
 * clés configurables via ConfigManager :
 * - "userSessionId" : clé stockant l'identifiant utilisateur
 * - "userSessionRole" : clé stockant le rôle utilisateur
 * 
 * Configuration recommandée dans web.xml :
 * <pre>
 * {@code
 * <context-param>
 *     <param-name>userSessionId</param-name>
 *     <param-value>userId</param-value>
 * </context-param>
 * <context-param>
 *     <param-name>userSessionRole</param-name>
 *     <param-value>userRole</param-value>
 * </context-param>
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see Authorized
 * @see Role
 * @see ConfigManager
 * @see EndPointDetails
 */
public class SecurityHandler {
    
    /**
     * Vérifie les permissions de sécurité avant d'invoquer une méthode.
     * 
     * Processus de vérification :
     * 1. Si la méthode n'a pas @Authorized, elle est accessible (pas de vérification)
     * 2. Si elle a @Authorized, récupère l'ID utilisateur de la session
     * 3. Si pas d'ID utilisateur, lève une exception (utilisateur non connecté)
     * 4. Si @Role est présente, vérifie que le rôle utilisateur est dans les rôles autorisés
     * 5. Si le rôle ne correspond pas, lève une exception
     * 
     * @param method la méthode à invoquer
     * @param request la requête HTTP contenant la session
     * @throws Exception si l'utilisateur n'est pas autorisé
     */
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
