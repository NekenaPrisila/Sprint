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
