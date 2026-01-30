package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui indique qu'une méthode répond à des requêtes HTTP GET.
 * 
 * @GetMapping doit être appliquée sur une méthode déjà annotée avec @UrlMapping.
 * Une méthode annotée avec @GetMapping ne sera accessible que via les requêtes GET.
 * Si la même URL a plusieurs méthodes avec des méthodes HTTP différentes,
 * le framework aiguille vers la bonne méthode selon la méthode HTTP utilisée.
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class ProductController {
 *     @UrlMapping(url = "/products/{id}")
 *     @GetMapping
 *     public ModelView getProduct(@RequestParam("id") int productId) {
 *         // Récupération et affichage du produit
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see UrlMapping
 * @see PostMapping
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetMapping {
    
}
