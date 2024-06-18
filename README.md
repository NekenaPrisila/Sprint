# Sprint
sprint

A modifier dans web.xml projet test:
 - mettre controllers.FrontController comme servlet initial
 - importer annotations.* pour chaque controller
 - mettre tous les controlleurs dans un meme package et les annoter 
 <!-- @AnnotationController -->
 - ajouter en utilisant init-param avec le servlet initial le package contenant les controlleurs et mettre comme param-name 'controller'
    <!-- <init-param>
      <param-name>controller</param-name>
      <param-value>nom_de_votre_package</param-value>
    </init-param> -->
 - annoter vos methodes dans vos controllers de la maniere suivante
    <!-- @GET("votre_nom_de_methode") -->
 - mettre mes fichiers jsp dans:
      webapps/mon_projet/mes_fichiers.jsp
 - pour direger vers un view utiliser ModelView
   et ajouter les donnees a envoyer vers le ficher jsp a l'aide de addData dans la class ModelView
   exemple:
   <!-- @GET("listeEmp")
    public ModelView listerData() {
        ModelView mv = new ModelView("test.jsp");
        String anarana = "Jean";
        int age = 20;
        mv.addData("nom", anarana);
        mv.addData("nbr", age);
        return mv;
    } -->
 - si il y a des parametre annoter les paramettre comme suite:
     <!-- @GET("/testParam")
    public ModelView doSomething(@Param("Nom") String param1, @Param("Age") String param2) {
        ModelView mv = new ModelView("test.jsp");
        mv.addData("nom", param1);
        mv.addData("nbr", param2);
        return mv;
    } -->
