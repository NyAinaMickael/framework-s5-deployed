package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui lie un paramètre de méthode à un paramètre de requête HTTP nommé.
 * 
 * @RequestParam permet de spécifier explicitement le nom du paramètre dans la requête
 * qui doit être injecté dans le paramètre de la méthode.
 * 
 * Si @RequestParam n'est pas utilisée, le framework essaie d'utiliser le nom
 * du paramètre de la méthode comme nom de paramètre HTTP.
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class SearchController {
 *     @UrlMapping(url = "/search")
 *     @GetMapping
 *     public ModelView search(@RequestParam("query") String searchTerm,
 *                             @RequestParam("page") int pageNumber) {
 *         // Effectue une recherche avec les paramètres spécifiés
 *     }
 * }
 * </pre>
 * 
 * Requête HTTP correspondante :
 * GET /search?query=java&page=2
 * 
 * @author Framework S5
 * @version 1.0
 * @see UrlMapping
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    /**
     * Le nom du paramètre de requête HTTP à injecter.
     * 
     * @return le nom du paramètre
     */
    String value();
}
