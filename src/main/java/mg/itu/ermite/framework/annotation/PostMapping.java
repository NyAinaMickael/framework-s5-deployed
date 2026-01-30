package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui indique qu'une méthode répond à des requêtes HTTP POST.
 * 
 * @PostMapping doit être appliquée sur une méthode déjà annotée avec @UrlMapping.
 * Une méthode annotée avec @PostMapping ne sera accessible que via les requêtes POST.
 * Comme pour @GetMapping, le framework gère automatiquement l'aiguillage entre
 * plusieurs méthodes sur la même URL avec différentes méthodes HTTP.
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class UserController {
 *     @UrlMapping(url = "/users")
 *     @PostMapping
 *     public ModelView createUser(User user) {
 *         // Création d'un nouvel utilisateur
 *         // Le framework lie automatiquement les paramètres POST à l'objet User
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see UrlMapping
 * @see GetMapping
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostMapping {
    
}
