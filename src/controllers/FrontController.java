package controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import annotations.AnnotationController;
import annotations.GET;

public class FrontController extends HttpServlet {
    private HashMap<String, Mapping> urlMappings = new HashMap<>();

    public void init() throws ServletException {
        super.init();
        findControllerClasses();
    }

    private void addClassIfController(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(AnnotationController.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        String url = getAnnotation.value();
                        System.out.println(clazz.getName());
                        Mapping mapping = new Mapping(clazz.getName(), method.getName());
                        urlMappings.put(url, mapping);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
        }
    }

    private void findClassesInDirectory(String packageName, File directory) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                findClassesInDirectory(packageName + "." + file.getName(), file);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                addClassIfController(className);
            }
        }
    }

    public void findControllerClasses() {
        String controllerPackage = getServletConfig().getInitParameter("controller");
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            System.err.println("Controller package not specified");
            return;
        }

        String path = controllerPackage.replace('.', '/');
        File directory = new File(getServletContext().getRealPath("/WEB-INF/classes/" + path));

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Package directory not found: " + directory.getAbsolutePath());
            return;
        }

        findClassesInDirectory(controllerPackage, directory);
    }

    protected void processRequested(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        
        String relativeUrl = url.substring(contextPath.length());

        String[] parts = relativeUrl.split("/");
        String methode = "";
        if (parts.length >= 2) {
            methode = parts[1];
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println("URL: " + url);

            Mapping mapping = urlMappings.get(methode);
            if (mapping != null) {
                out.println("Method found: " + mapping);
            } else {
                out.println("No method associated with this URL");
            }
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequested(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequested(req, resp);
    }
}
