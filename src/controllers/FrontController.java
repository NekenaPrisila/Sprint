package controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Mapping;
import utils.ModelView;
import utils.Reflect;
import annotations.AnnotationController;
import annotations.GET;
import annotations.RestController;
import annotations.RestEndPoint;

public class FrontController extends HttpServlet {
    private HashMap<String, Mapping> urlMappings = new HashMap<>();

    public void init() throws ServletException {
        super.init();
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
                            Mapping mapping = new Mapping(clazz.getName(), method.getName());
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

    protected void processRequested(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativeUrl = url.substring(contextPath.length());

        // Extract the URL without query parameters
        String method = relativeUrl.split("\\?")[0];
        
        if (method.startsWith("/")) {
            method = method.substring(1);
        }
        PrintWriter out = resp.getWriter();
        try {
            Mapping mapping = urlMappings.get(method);
            if (mapping != null) {
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method targetMethod = clazz.getDeclaredMethod(mapping.getMethodName());

                boolean isRestController = clazz.isAnnotationPresent(RestController.class);
                boolean isRestEndPoint = targetMethod.isAnnotationPresent(RestEndPoint.class);

                // Execute the method and get the result
                Object result = Reflect.executeMethod(mapping, req, resp);

                if (isRestController && isRestEndPoint) {
                    // Set response type to JSON
                    resp.setContentType("application/json");
                    Gson gson = new Gson();
                    // If the result is a ModelView, serialize its data attribute
                    if (result instanceof ModelView) {
                        ModelView mv = (ModelView) result;
                        String jsonResponse = gson.toJson(mv.getData());
                        out.println(jsonResponse);
                    } else {
                        // Serialize the result directly
                        String jsonResponse = gson.toJson(result);
                        out.println(jsonResponse);
                    }
                } else {
                    // Handle as regular view rendering
                    if (result instanceof ModelView) {
                        resp.setContentType("text/html");
                        ModelView mv = (ModelView) result;
                        mv.getData().forEach((key, value) -> req.setAttribute(key, value));
                        req.getRequestDispatcher(mv.getUrl()).forward(req, resp);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.println(result.toString());
                    }
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No method associated with this URL");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println(e.getMessage());
        }
    }
        
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequested(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequested(req, resp);
    }
}
