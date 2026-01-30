package mg.itu.ermite.framework.annotation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation de sécurité qui contrôle l'accès à une méthode en fonction du rôle de l'utilisateur.
 * 
 * @Role doit être combinée avec @Authorized pour fonctionner correctement.
 * Elle définit une liste de rôles autorisés à accéder à la méthode.
 * 
 * Le framework vérifie que :
 * 1. L'utilisateur est authentifié (session valide)
 * 2. L'utilisateur a un rôle affecté
 * 3. Le rôle de l'utilisateur fait partie de la liste des rôles autorisés
 * 
 * Le rôle de l'utilisateur est récupéré depuis la session HTTP à l'aide de la clé
 * définie dans ConfigManager via "userSessionRole".
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class AdminController {
 *     @UrlMapping(url = "/admin/users")
 *     @GetMapping
 *     @Authorized
 *     @Role({"ADMIN", "SUPER_ADMIN"})
 *     public ModelView listUsers() {
 *         // Accessible uniquement aux utilisateurs avec rôle ADMIN ou SUPER_ADMIN
 *     }
 *     
 *     @UrlMapping(url = "/admin/config")
 *     @GetMapping
 *     @Authorized
 *     @Role("SUPER_ADMIN")
 *     public ModelView adminConfig() {
 *         // Accessible uniquement aux SUPER_ADMIN
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see Authorized
 * @see SecurityHandler
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    /**
     * Tableau des rôles autorisés à accéder à la méthode.
     * Au moins un des rôles spécifiés doit correspondre au rôle de l'utilisateur.
     * 
     * @return les rôles autorisés
     */
    String[] value();
}
