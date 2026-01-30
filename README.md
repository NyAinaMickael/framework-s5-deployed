# Framework S5 - Spring-like Web Framework

Un framework web léger inspiré de Spring, développé comme projet académique. Il offre un système de routage basé sur les annotations, du binding automatique des paramètres, de la sécurité basée sur les rôles, et la sérialisation JSON.

## ⚡ Configuration requise

### 1. Configuration du web.xml

Le `FrontServlet` **DOIT** être mappé à `/` dans votre `web.xml`. C'est le point d'entrée essentiel du framework.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <!-- Listener d'initialisation du framework -->
    <listener>
        <listener-class>mg.itu.ermite.framework.listener.FrameworkInitializer</listener-class>
    </listener>

    <!-- Configuration du framework (optionnel) -->
    <context-param>
        <param-name>uploadDirectory</param-name>
        <param-value>/uploads</param-value>
    </context-param>
    <context-param>
        <param-name>maxFileSize</param-name>
        <param-value>52428800</param-value>
    </context-param>
    <context-param>
        <param-name>userSessionId</param-name>
        <param-value>userId</param-value>
    </context-param>
    <context-param>
        <param-name>userSessionRole</param-name>
        <param-value>userRole</param-value>
    </context-param>

    <!-- ⚠️ IMPORTANT : Le FrontServlet DOIT être mappé à "/" -->
    <servlet>
        <servlet-name>frontServlet</servlet-name>
        <servlet-class>mg.itu.ermite.framework.FrontServlet</servlet-class>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>frontServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```

## 📚 Documentation du Framework

### Vue d'ensemble

Le framework S5 fonctionne selon le pattern **Front Controller** :

1. **FrontServlet** intercepte toutes les requêtes HTTP sur `/`
2. **ClasspathScanner** découvre les contrôleurs au démarrage
3. **EndPointDetails** gère le binding des paramètres et l'invocation des méthodes
4. Les réponses sont formatées en JSON ou redirigées vers des vues JSP

### Architecture

```
Requête HTTP
    ↓
FrontServlet (front controller)
    ↓
ClasspathScanner (découverte des endpoints)
    ↓
EndPointDetails (binding & invocation)
    ↓
Réponse (JSON ou JSP)
```

## 🚀 Utilisation

### 1. Créer un contrôleur

```java
import mg.itu.ermite.framework.annotation.Controller;
import mg.itu.ermite.framework.annotation.GetMapping;
import mg.itu.ermite.framework.annotation.PostMapping;
import mg.itu.ermite.framework.annotation.UrlMapping;
import mg.itu.ermite.framework.annotation.RequestParam;
import mg.itu.ermite.framework.util.ModelView;

@Controller
public class UserController {
    
    // Réponse ModelView (rendu JSP)
    @UrlMapping(url = "/users/{id}")
    @GetMapping
    public ModelView getUser(@RequestParam("id") int userId) {
        User user = new User(userId, "John Doe", "john@example.com");
        
        ModelView view = new ModelView();
        view.setView("/WEB-INF/views/user-detail.jsp");
        view.addAttribute("user", user);
        view.addAttribute("title", "Détail utilisateur");
        
        return view;
    }
    
    // Réponse JSON
    @UrlMapping(url = "/api/users/{id}")
    @GetMapping
    @JsonResponse
    public User getUserJson(@RequestParam("id") int userId) {
        return new User(userId, "Jane Doe", "jane@example.com");
    }
}
```

### 2. Binder des paramètres

#### Paramètres simples
```java
@UrlMapping(url = "/search")
@GetMapping
public ModelView search(@RequestParam("query") String q, 
                        @RequestParam("page") int page) {
    // query récupère le paramètre "query" de la requête
    // page récupère le paramètre "page" et le convertit en int
}
```

#### Paramètres d'URL
```java
@UrlMapping(url = "/users/{userId}/posts/{postId}")
@GetMapping
public ModelView getUserPost(@RequestParam("userId") int userId,
                             @RequestParam("postId") int postId) {
    // Les valeurs sont extraites de l'URL
}
```

#### Objets complexes (binding automatique)
```java
@UrlMapping(url = "/users")
@PostMapping
public ModelView createUser(User user) {
    // Les paramètres HTTP sont automatiquement bindés aux propriétés de User
    // Paramètres attendus : user.name, user.email, user.age
}
```

#### Collections et tableaux
```java
@UrlMapping(url = "/items/batch")
@PostMapping
public ModelView batchCreate(Item[] items) {
    // Paramètres : items[0].name, items[0].price, items[1].name, items[1].price...
}
```

#### Sessions
```java
@UrlMapping(url = "/profile")
@GetMapping
public ModelView profile(SessionMap session) {
    // Accès à la session HTTP comme une Map
    Integer userId = (Integer) session.get("userId");
    String role = (String) session.get("userRole");
}
```

#### Fichiers uploadés
```java
@UrlMapping(url = "/upload")
@PostMapping
public ModelView upload(Map<String, List<FileData>> files) {
    if (files.containsKey("document")) {
        FileData file = files.get("document").get(0);
        byte[] content = file.getBytes();
        String filename = file.getFileName();
        String extension = file.getExtension();
    }
}
```

### 3. Sécurité

#### Authentification
```java
@UrlMapping(url = "/admin/dashboard")
@GetMapping
@Authorized
public ModelView adminDashboard() {
    // Accessible uniquement aux utilisateurs connectés
}
```

#### Contrôle d'accès basé sur les rôles
```java
@UrlMapping(url = "/admin/users")
@GetMapping
@Authorized
@Role({"ADMIN", "SUPER_ADMIN"})
public ModelView listUsers() {
    // Accessible uniquement aux utilisateurs avec rôle ADMIN ou SUPER_ADMIN
}
```

Pour que la sécurité fonctionne, stockez l'utilisateur en session :
```java
// À la connexion
session.setAttribute("userId", 123);
session.setAttribute("userRole", "ADMIN");
```

### 4. Réponses JSON

```java
@UrlMapping(url = "/api/products")
@GetMapping
@JsonResponse
public List<Product> getProducts() {
    // Retourne automatiquement :
    // {
    //   "status": "success",
    //   "code": 200,
    //   "data": [...],
    //   "count": 5
    // }
}
```

En cas d'erreur :
```java
// {
//   "status": "error",
//   "code": 500,
//   "message": "Description de l'erreur",
//   "data": null
// }
```

## 🔧 Configuration

Via le `web.xml` :

```xml
<context-param>
    <param-name>uploadDirectory</param-name>
    <param-value>/uploads</param-value>
</context-param>
<context-param>
    <param-name>maxFileSize</param-name>
    <param-value>52428800</param-value>
</context-param>
<context-param>
    <param-name>userSessionId</param-name>
    <param-value>userId</param-value>
</context-param>
<context-param>
    <param-name>userSessionRole</param-name>
    <param-value>userRole</param-value>
</context-param>
```

Accès depuis votre code :
```java
import mg.itu.ermite.framework.config.ConfigManager;

ConfigManager config = ConfigManager.getInstance();
String uploadDir = config.get("uploadDirectory");
int maxSize = config.getInt("maxFileSize", 10485760);
```

## 📦 Dépendances requises

```xml
<!-- Jackson pour la sérialisation JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<!-- Jakarta Servlet API -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>
```

## 🏗️ Structure des fichiers

```
src/main/java/mg/itu/ermite/framework/
├── FrontServlet.java              # Contrôleur frontal
├── annotation/
│   ├── Controller.java
│   ├── GetMapping.java
│   ├── PostMapping.java
│   ├── UrlMapping.java
│   ├── RequestParam.java
│   ├── JsonResponse.java
│   └── security/
│       ├── Authorized.java
│       └── Role.java
├── config/
│   └── ConfigManager.java         # Gestion de la configuration
├── listener/
│   └── FrameworkInitializer.java # Initialisation au démarrage
├── util/
│   ├── ClasspathScanner.java     # Découverte des contrôleurs
│   ├── EndPointDetails.java      # Détails et invocation
│   ├── Reflection.java           # Utilitaires de réflexion
│   ├── ModelView.java            # Modèle-Vue
│   ├── FileData.java             # Données de fichier
│   ├── SessionMap.java           # Wrapper de session
│   └── security/
│       └── SecurityHandler.java  # Vérifications de sécurité
```

## ✨ Caractéristiques

✅ **Routage basé sur les annotations** - Mapping déclaratif des URLs  
✅ **Binding automatique** - Conversion automatique des types et création d'objets  
✅ **Support JSON** - Sérialisation/désérialisation automatique  
✅ **Sécurité** - Authentification et contrôle d'accès basé sur les rôles  
✅ **Gestion des fichiers** - Upload de fichiers avec métadonnées  
✅ **Sessions** - Accès facile à la session HTTP  
✅ **Configuration centralisée** - Gestion unique des paramètres  
✅ **Découverte automatique** - Scan du classpath pour les contrôleurs  
✅ **Gestion des erreurs** - Réponses d'erreur formatées

## 📝 Exemples d'utilisation

### Exemple complet

```java
@Controller
public class BlogController {
    
    // Liste les articles
    @UrlMapping(url = "/blog")
    @GetMapping
    public ModelView listArticles(SessionMap session) {
        Integer userId = (Integer) session.get("userId");
        
        ModelView view = new ModelView();
        view.setView("/WEB-INF/views/blog-list.jsp");
        view.addAttribute("articles", getArticles());
        view.addAttribute("userId", userId);
        
        return view;
    }
    
    // Détail d'un article
    @UrlMapping(url = "/blog/{id}")
    @GetMapping
    @JsonResponse
    public Article getArticle(@RequestParam("id") int articleId) {
        return new Article(articleId, "Titre", "Contenu...");
    }
    
    // Créer un article
    @UrlMapping(url = "/blog")
    @PostMapping
    @Authorized
    @Role("AUTHOR")
    public ModelView createArticle(Article article, SessionMap session) {
        Integer userId = (Integer) session.get("userId");
        article.setAuthorId(userId);
        article.setCreatedAt(new Date());
        
        saveArticle(article);
        
        ModelView view = new ModelView();
        view.setView("/WEB-INF/views/article-created.jsp");
        view.addAttribute("article", article);
        
        return view;
    }
}
```

## 🎓 Auteur

Projet académique Framework S5

## 📄 Licence

Ce projet est fourni à titre académique.