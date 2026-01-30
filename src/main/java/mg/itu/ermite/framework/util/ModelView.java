package mg.itu.ermite.framework.util;

import java.util.HashMap;

/**
 * Classe représentant un modèle-vue (Model-View) pour le rendu de pages JSP.
 * 
 * ModelView est retournée par les méthodes de contrôleur pour :
 * - Spécifier le chemin du fichier JSP à afficher
 * - Passer des données (attributs) à la vue
 * 
 * Le FrontServlet récupère le ModelView, ajoute les attributs à la requête,
 * puis fait un forward vers la page JSP spécifiée.
 * 
 * Exemple d'utilisation dans un contrôleur :
 * <pre>
 * @Controller
 * public class UserController {
 *     @UrlMapping(url = "/users/{id}")
 *     @GetMapping
 *     public ModelView getUser(@RequestParam("id") int userId) {
 *         User user = userService.findById(userId);
 *         
 *         ModelView modelView = new ModelView();
 *         modelView.setView("/WEB-INF/views/user-detail.jsp");
 *         modelView.addAttribute("user", user);
 *         modelView.addAttribute("title", "Détail utilisateur");
 *         
 *         return modelView;
 *     }
 * }
 * </pre>
 * 
 * Dans la JSP, les attributs sont accessibles comme des variables EL :
 * <pre>
 * {@code
 * <h1>${title}</h1>
 * <p>Nom : ${user.name}</p>
 * <p>Email : ${user.email}</p>
 * }
 * </pre>
 * 
 * @author Framework S5
 * @version 1.0
 * @see FrontServlet
 * @see Controller
 */
public class ModelView {
    
    /** Le chemin vers la page JSP à afficher */
    private String view;    

    /** Les attributs à passer à la vue */
    private HashMap<String,Object> attributes = new HashMap<>();

    /**
     * Constructeur par défaut.
     */
    public ModelView() {
    }

    /**
     * Récupère le chemin de la vue (fichier JSP).
     * 
     * @return le chemin vers la JSP (ex: "/WEB-INF/views/index.jsp")
     */
    public String getView() {
        return view;
    }

    /**
     * Définit le chemin de la vue (fichier JSP) à afficher.
     * 
     * @param view le chemin vers la JSP
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * Récupère tous les attributs de la vue.
     * 
     * @return une map des attributs
     */
    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Ajoute un attribut qui sera disponible dans la vue (JSP).
     * 
     * L'attribut est stocké dans la requête HTTP et sera accessible
     * via Expression Language (EL) dans la page JSP.
     * 
     * @param key le nom de l'attribut
     * @param value la valeur de l'attribut
     */
    public void addAttribute(String key,Object value)
    {
        attributes.put(key,value);
    }
    
}
