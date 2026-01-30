package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui mappe une méthode de contrôleur à une URL HTTP.
 * 
 * @UrlMapping est obligatoire sur chaque méthode de contrôleur qui doit être accessible
 * en tant qu'endpoint de l'application. Elle définit l'URL (path) sur laquelle la méthode
 * sera accessible.
 * 
 * L'URL peut contenir des paramètres de chemin (path variables) entre accolades.
 * Ces paramètres seront extraits de l'URL et passés en arguments à la méthode.
 * 
 * Formats d'URL supportés :
 * - URL statique : "/users"
 * - URL avec paramètres : "/users/{id}", "/posts/{id}/comments/{commentId}"
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class PostController {
 *     // URL statique
 *     @UrlMapping(url = "/posts")
 *     @GetMapping
 *     public ModelView listPosts() { ... }
 *     
 *     // URL avec un paramètre
 *     @UrlMapping(url = "/posts/{id}")
 *     @GetMapping
 *     public ModelView getPost(@RequestParam("id") int postId) { ... }
 *     
 *     // URL avec plusieurs paramètres
 *     @UrlMapping(url = "/users/{userId}/posts/{postId}")
 *     @GetMapping
 *     public ModelView getUserPost(@RequestParam("userId") int userId,
 *                                  @RequestParam("postId") int postId) { ... }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see Controller
 * @see GetMapping
 * @see PostMapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    /**
     * L'URL (path) sur laquelle la méthode est accessible.
     * Les paramètres de chemin doivent être entre accolades : {nomParam}
     * 
     * @return l'URL mappée
     */
    String url ();
}
