# Guide d'Utilisation du Framework MVC Nekena 2669

## Introduction
Ce guide explique comment utiliser le framework MVC pour développer des applications web en Java. Il couvre l'installation, la configuration et l'utilisation des principales fonctionnalités du framework.

---

## 1. Installation et Configuration

### 1.1. Ajout du Framework au Projet
- Télécharger le fichier **JAR** du framework.
- Ajouter le JAR dans le **classpath** de votre projet.

### 1.2. Configuration de `web.xml`
Ajouter le `FrontController` dans `web.xml` en précisant le package où se trouvent vos contrôleurs :

```xml
<servlet>
    <servlet-name>FrontController</servlet-name>
    <servlet-class>controllers.FrontController</servlet-class>
    <init-param>
        <param-name>controller</param-name>
        <param-value>nom_de_votre_package</param-value>
    </init-param>
</servlet>
```

---

## 2. Définition des Contrôleurs et Routes

### 2.1. Création d'un Contrôleur
Un contrôleur est une classe annotée avec `@Controller`.

```java
@Controller
public class MonController {
    
    @GetMapping("/accueil")
    public ModelView accueil() {
        ModelView mv = new ModelView("index.jsp");
        return mv;
    }
}
```

### 2.2. Passage de Données à la Vue
Utilisez `ModelView` pour envoyer des données à la vue :

```java
@Controller
public class UserController {
    
    @GetMapping("/utilisateur")
    public ModelView afficherUtilisateur() {
        ModelView mv = new ModelView("user.jsp");
        mv.addData("nom", "Alice");
        mv.addData("age", 30);
        return mv;
    }
}
```

Dans la page `user.jsp`, affichez les données comme suit :

```jsp
Nom: ${nom}<br>
Âge: ${age}
```

---

## 3. Gestion des Paramètres de Requête

### 3.1. Récupération de Paramètres Simples
Ajoutez l'annotation `@Param` pour récupérer des paramètres dans l'URL :

```java
@Controller
public class TestController {
    
    @GetMapping("/test")
    public ModelView test(@Param("nom") String nom, @Param("age") int age) {
        ModelView mv = new ModelView("result.jsp");
        mv.addData("nom", nom);
        mv.addData("age", age);
        return mv;
    }
}
```

Accès via : `http://localhost:8080/test?nom=Alice&age=30`

### 3.2. Passage d'un Objet Complet
Vous pouvez directement passer un objet en paramètre :

```java
public class Utilisateur {
    private String nom;
    private int age;
    
    // Getters et Setters
}
```

```java
@Controller
public class UserController {
    
    @GetMapping("/ajouterUtilisateur")
    public ModelView ajouterUtilisateur(@Param ObjetUtilisateur user) {
        ModelView mv = new ModelView("userDetails.jsp");
        mv.addData("utilisateur", user);
        return mv;
    }
}
```

---

## 4. Gestion des Sessions

### 4.1. Stocker et Récupérer des Données de Session

```java
@Controller
public class SessionController {
    
    @GetMapping("/session")
    public ModelView gestionSession(SessionManager session) {
        session.setAttribute("user", "Admin");
        ModelView mv = new ModelView("dashboard.jsp");
        mv.addData("user", session.getAttribute("user"));
        return mv;
    }
}
```

---

## 5. Gestion des API REST

### 5.1. Création d'un Contrôleur REST
Utilisez `@RestController` pour renvoyer des réponses JSON :

```java
@RestController
public class APIController {
    
    @GetMapping("/api/utilisateur")
    public Utilisateur getUtilisateur() {
        return new Utilisateur("Alice", 30);
    }
}
```

---

## 6. Gestion des Fichiers Uploadés

### 6.1. Upload d'un Fichier
Utilisez `WinterPart` pour récupérer un fichier uploadé :

```java
@Controller
public class UploadController {
    
    @PostMapping("/upload")
    public ModelView uploadFile(WinterPart file) {
        file.save("/uploads/");
        ModelView mv = new ModelView("uploadSuccess.jsp");
        mv.addData("fileName", file.getFileName());
        return mv;
    }
}
```

---


