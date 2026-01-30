package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui marque une classe comme un contrôleur de l'application.
 * 
 * Une classe annotée avec @Controller sera automatiquement scannée au démarrage
 * de l'application pour découvrir ses méthodes annotées avec @UrlMapping,
 * qui définissent les endpoints de l'application.
 * 
 * Les classes contrôleurs doivent posséder un constructeur par défaut (sans paramètres)
 * car le framework les instancie via réflexion pour invoque les méthodes.
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class UserController {
 *     @UrlMapping(url = "/user/{id}")
 *     @GetMapping
 *     public ModelView getUser(@RequestParam("id") int userId) {
 *         // Logique métier
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see UrlMapping
 * @see GetMapping
 * @see PostMapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    
}
