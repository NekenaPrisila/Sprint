package controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.FieldErrors;
import utils.HttpMethod;
import utils.Mapping;
import utils.ModelView;
import utils.Reflect;
import annotations.AnnotationController;
import annotations.GET;
import annotations.POST;
import annotations.RestController;
import annotations.RestEndPoint;
import annotations.validation.ErrorUrl;

@MultipartConfig
public class FrontController extends HttpServlet {
    public static String SESSION_AUTHENTICATED, SESSION_ROLE;

    private HashMap<String, Mapping> urlMappings = new HashMap<>();

    public void init() throws ServletException {
        super.init();
        SESSION_AUTHENTICATED = getInitParameter("session_authenticated") != null
		? getInitParameter("session_authenticated")
		: "authenticated";
        SESSION_ROLE = getInitParameter("session_role") != null ? getInitParameter("session_role") : "role";
        findControllerClasses();
    }

    private void addClassIfController(String className) throws ServletException {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        String url = getAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            String errorMessage = "Error: URL " + url + " is mapped twice: " + 
                            urlMappings.get(url).getClassName() + "#" + urlMappings.get(url).getMethodName() + 
                            " and " + clazz.getName() + "#" + method.getName() + ".";      
                            throw new ServletException(errorMessage);
                        } else {
                            Mapping mapping = new Mapping(clazz.getName(), method.getName(), HttpMethod.GET);
                            urlMappings.put(url, mapping);
                        }
                    }
                    // Ajout pour POST
                    if (method.isAnnotationPresent(POST.class)) {
                        POST postAnnotation = method.getAnnotation(POST.class);
                        String url = postAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            String errorMessage = "Error: URL " + url + " is mapped twice: " + 
                            urlMappings.get(url).getClassName() + "#" + urlMappings.get(url).getMethodName() + 
                            " and " + clazz.getName() + "#" + method.getName() + ".";      
                            throw new ServletException(errorMessage);
                        } else {
                            Mapping mapping = new Mapping(clazz.getName(), method.getName(), HttpMethod.POST);
                            urlMappings.put(url, mapping);
                        }
                    }
                }
            }
            if (clazz.isAnnotationPresent(RestController.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        String url = getAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            String errorMessage = "Error: URL " + url + " is mapped twice: " +
                            urlMappings.get(url).getClassName() + "#" + urlMappings.get(url).getMethodName() +
                            " and " + clazz.getName() + "#" + method.getName() + ".";      
                            throw new ServletException(errorMessage);
                        } else {
                            Mapping mapping = new Mapping(clazz.getName(), method.getName(), HttpMethod.GET);
                            urlMappings.put(url, mapping);
                        }
                    }

                    if (method.isAnnotationPresent(POST.class)) {
                        POST postAnnotation = method.getAnnotation(POST.class);
                        String url = postAnnotation.value();
                        if (urlMappings.containsKey(url)) {
                            String errorMessage = "Error: URL " + url + " is mapped twice: " + 
                                urlMappings.get(url).getClassName() + "#" + urlMappings.get(url).getMethodName() + 
                                " and " + clazz.getName() + "#" + method.getName() + ".";      
                            throw new ServletException(errorMessage);
                        } else {
                            Mapping mapping = new Mapping(clazz.getName(), method.getName(), HttpMethod.POST);
                            urlMappings.put(url, mapping);
                        }
                    }
                    
                }
            }
        } catch (ClassNotFoundException e) {
            String errorMessage = "Class not found: " + className;
            throw new ServletException(errorMessage, e);
        }
    }

    private void findClassesInDirectory(String packageName, File directory) throws ServletException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                findClassesInDirectory(packageName + "." + file.getName(), file);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                addClassIfController(className);
            }
        }
    }

    public void findControllerClasses() throws ServletException {
        String controllerPackage = getServletConfig().getInitParameter("controller");
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            throw new ServletException("Error: Controller package not specified");
        }
    
        String path = controllerPackage.replace('.', '/');
        File directory = new File(getServletContext().getRealPath("/WEB-INF/classes/" + path));
    
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ServletException("Error: Package directory not found: " + 
                                       directory.getPath().replace(getServletContext().getRealPath(""), ""));
        }
    
        findClassesInDirectory(controllerPackage, directory);

        if (urlMappings.isEmpty()) {
            throw new ServletException("Error: No controllers found in package " + controllerPackage);
        }
    }

    public boolean isStaticFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = url.substring(contextPath.length());
    
        // Définir les extensions autorisées pour les fichiers statiques
        String[] staticExtensions = {".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".woff", ".woff2", ".ttf"};
    
        // Vérifier si l'URL correspond à un fichier statique
        for (String ext : staticExtensions) {
            if (relativePath.endsWith(ext)) {
                File staticFile = new File(getServletContext().getRealPath(relativePath));
                if (staticFile.exists() && staticFile.isFile()) {
                    // Déterminer le type MIME et renvoyer le fichier
                    String mimeType = getServletContext().getMimeType(staticFile.getName());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream"; // Par défaut
                    }
                    resp.setContentType(mimeType);
                    resp.setContentLength((int) staticFile.length());
                    
                    // Envoyer le fichier dans la réponse
                    try (var in = new java.io.FileInputStream(staticFile);
                         var out = resp.getOutputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }    

    protected void processRequested(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // Vérifier si c'est un fichier statique
        if (isStaticFile(req, resp)) {
            return; // Ne pas traiter plus loin si c'est un fichier statique
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativeUrl = url.substring(contextPath.length());

        System.out.println("chemin: " + relativeUrl);
    
        // Extraire le verbe HTTP de la requête
        HttpMethod requestMethod = HttpMethod.valueOf(req.getMethod().toUpperCase());  // GET, POST, etc.
        
        // Extraire l'URL sans les paramètres de la requête
        String method = relativeUrl.split("\\?")[0];
        
        if (method.startsWith("/")) {
            method = method.substring(1);
        }
    
        PrintWriter out = resp.getWriter();
        try {
            // Chercher le mappage pour l'URL (sans paramètres de requête)
            Mapping mapping = urlMappings.get(method);

            if (mapping != null && mapping.getHttpMethod() == requestMethod) {  // Vérifier que le verbe correspond
                // Trouver la classe et la méthode associée au mappage
                Class<?> clazz = Class.forName(mapping.getClassName());
                
                Method targetMethod = null;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(mapping.getMethodName())) {
                        targetMethod = m;
                        break;
                    }
                }
                if (targetMethod == null) {
                    throw new NoSuchMethodException("Méthode introuvable : " + mapping.getMethodName());
                }
        
                boolean isRestController = clazz.isAnnotationPresent(RestController.class);
                boolean isRestEndPoint = targetMethod.isAnnotationPresent(RestEndPoint.class);
        
                // Exécution de la méthode et récupération du résultat
                Object result = Reflect.executeMethod(mapping, req, resp, getServletContext());

                if (result instanceof FieldErrors) {
                    if (targetMethod.isAnnotationPresent(ErrorUrl.class)) {
                        // Récupérer l'annotation
                        ErrorUrl errorUrlAnnotation = targetMethod.getAnnotation(ErrorUrl.class);
                        // Récupérer la valeur de l'annotation
                        String errorUrl = errorUrlAnnotation.value();
                        HashMap<String, List<String>> errors = ((FieldErrors) result).getFieldErrors();
                        ModelView mv = new ModelView(errorUrl);
    
                        // Boucler sur les erreurs et les ajouter au ModelView
                        for (Map.Entry<String, List<String>> entry : errors.entrySet()) {
                            mv.addData("errors_" + entry.getKey(), entry.getValue());
                        }

                        mv.getData().forEach((key, value) -> req.setAttribute(key, value));
                        req.getRequestDispatcher(mv.getUrl()).forward(req, resp);
                    } 
                }
        
                if (isRestController && isRestEndPoint) {
                    // Si c'est un contrôleur REST, renvoyer la réponse en JSON
                    resp.setContentType("application/json");
                    Gson gson = new Gson();
                    if (result instanceof ModelView) {
                        ModelView mv = (ModelView) result;
                        String jsonResponse = gson.toJson(mv.getData());
                        out.println(jsonResponse);
                    } else {
                        String jsonResponse = gson.toJson(result);
                        out.println(jsonResponse);
                    }
                } else {
                    // Si c'est un retour classique avec un ModelView
                    if (result instanceof ModelView) {
                        resp.setContentType("text/html");
                        ModelView mv = (ModelView) result;
                        mv.getData().forEach((key, value) -> req.setAttribute(key, value));
                        req.getRequestDispatcher(mv.getUrl()).forward(req, resp);
                    } else {
                        // Si le résultat n'est pas un ModelView, retourner le résultat brut
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.println(result.toString());
                    }
                }
            } else {
                // Si aucun mappage trouvé ou mauvais verbe HTTP, retourner une erreur 404
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No method associated with this URL or wrong HTTP method");
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Gestion des exceptions liées à la réflexion
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Error while processing the request: " + e.getMessage());
        } catch (Exception e) {
            // Gestion d'autres exceptions générales
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Internal server error: " + e.getMessage());
            throw new Exception();            
        }
    }      
        
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequested(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            processRequested(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
