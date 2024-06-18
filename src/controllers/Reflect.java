package controllers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import annotations.Param;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Reflect {

    public static Object executeMethod(Mapping m, HttpServletRequest req, HttpServletResponse resp) throws Exception{
        Class<?> clazz = Class.forName(m.getClassName());
        Method method = Reflect.getMethode(clazz, m.getMethodName());
        Object obj = clazz.newInstance();
        List<String> paramValues = new ArrayList<>();
        for (Parameter methodParam : method.getParameters()) {
            // par annotation  
            if  (methodParam.isAnnotationPresent(Param.class) && Reflect.isAmoungRequestParameter(req.getParameterNames(), methodParam.getAnnotation(Param.class).value()) ) {
                Param p = methodParam.getAnnotation(Param.class);
                paramValues.add(req.getParameter(p.value()));
            }
            else {   
                // par convention methodParamName = Name of req.getPramater
                if (Reflect.isAmoungRequestParameter(req.getParameterNames(), methodParam.getName())) {    
                    paramValues.add(req.getParameter(methodParam.getName()));                      
                }
                else{
                    paramValues.add(null);
                }
            }
        }
        for (String string : paramValues) {
            System.out.println(string);
        }
        // excecute Methode
        return method.invoke(obj, paramValues.toArray( new Object[0]));
    }

    public static Method getMethode(Class<?> clazz, String methodeName) throws Exception{
        Method m = null;
        Method[] methods =  clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodeName)) {
                m =  method;
                break;
            }
        }
        return m;
    }

    public static boolean isAmoungRequestParameter(Enumeration<String> parameters, String name){
        while (parameters.hasMoreElements()) {
            if (parameters.nextElement().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
