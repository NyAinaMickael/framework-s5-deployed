package mg.itu.ermite.framework.annotation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation de sécurité qui exige qu'un utilisateur soit authentifié pour accéder à une méthode.
 * 
 * Une méthode annotée avec @Authorized ne sera accessible que si un utilisateur est connecté.
 * Le framework vérifie la présence d'une session utilisateur avant d'invoquer la méthode.
 * L'identifiant de session utilisateur est défini dans le ConfigManager via la clé "userSessionId".
 * 
 * Si un utilisateur non authentifié tente d'accéder à une méthode @Authorized,
 * le framework lève une exception qui est propagée en tant qu'erreur HTTP.
 * 
 * @Authorized peut être combinée avec @Role pour une sécurité supplémentaire basée sur les rôles.
 * 
 * Configuration requise dans web.xml :
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
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class AdminController {
 *     @UrlMapping(url = "/admin/dashboard")
 *     @GetMapping
 *     @Authorized
 *     public ModelView adminDashboard() {
 *         // Accessible uniquement aux utilisateurs connectés
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see Role
 * @see SecurityHandler
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorized {
    
}
