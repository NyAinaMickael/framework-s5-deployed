package mg.itu.ermite.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation qui indique qu'une méthode doit retourner une réponse JSON au lieu d'une vue.
 * 
 * Quand @JsonResponse est présente sur une méthode :
 * - Le framework sérialise le résultat de la méthode en JSON automatiquement
 * - Le Content-Type de la réponse est défini à "application/json"
 * - Une enveloppe JSON standard est appliquée avec status, code et data
 * - Les exceptions sont transformées en réponses d'erreur JSON
 * 
 * Format de réponse en cas de succès :
 * <pre>
 * {
 *   "status": "success",
 *   "code": 200,
 *   "data": {...},
 *   "count": 5
 * }
 * </pre>
 * 
 * Format de réponse en cas d'erreur :
 * <pre>
 * {
 *   "status": "error",
 *   "code": 500,
 *   "message": "Description de l'erreur",
 *   "data": null
 * }
 * </pre>
 * 
 * Exemple d'utilisation :
 * <pre>
 * @Controller
 * public class ApiController {
 *     @UrlMapping(url = "/api/users/{id}")
 *     @GetMapping
 *     @JsonResponse
 *     public User getUser(@RequestParam("id") int userId) {
 *         // Le résultat sera automatiquement convertit en JSON
 *         return userService.findById(userId);
 *     }
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see UrlMapping
 * @see Controller
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonResponse {
    
}
