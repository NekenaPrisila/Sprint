package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.*;
import annotations.Param;

public class Reflect {
    public static Object executeMethod(Mapping mapping, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Class<?> clazz;
        Object instance;
        Method method;

        try {
            clazz = Class.forName(mapping.getClassName());
            instance = clazz.getDeclaredConstructor().newInstance();
            System.out.println("Instance created: " + instance);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + mapping.getClassName());
            throw new Exception("Class not found: " + mapping.getClassName(), e);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println("Error creating instance of: " + mapping.getClassName());
            throw new Exception("Error creating instance of: " + mapping.getClassName(), e);
        }

        System.out.println("Available methods in class " + mapping.getClassName() + ":");
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            System.out.println(m.getName() + " with parameters:");
            for (Parameter p : m.getParameters()) {
                System.out.println("  " + p.getType().getName() + " " + p.getName());
            }
        }

        try {
            System.out.println("Attempting to get method: " + mapping.getMethodName());
            method = null;
            for (Method m : methods) {
                if (m.getName().equals(mapping.getMethodName())) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException(mapping.getMethodName());
            }
            System.out.println("Method obtained: " + method);
        } catch (NoSuchMethodException e) {
            System.err.println("Method not found: " + mapping.getMethodName() + " in class " + mapping.getClassName());
            throw new Exception("Method not found: " + mapping.getMethodName() + " in class " + mapping.getClassName(), e);
        }

        Parameter[] parameters = method.getParameters();
        System.out.println("Number of parameters: " + parameters.length);

        for (int i = 0; i < parameters.length; i++) {
            System.out.println("Parameter " + i + ": " + parameters[i]);
        }

        Object[] args = new Object[parameters.length];

        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    Param param = parameters[i].getAnnotation(Param.class);
                    String paramName = param.name();
                    String paramValue = request.getParameter(paramName);

                    System.out.println("Parameter name: " + paramName + ", value: " + paramValue);

                    if (!parameters[i].getType().isPrimitive()) {
                        args[i] = convertParameter(null, parameters[i].getType(), request, paramName);
                    }
                    else if (paramValue != null) {
                        args[i] = convertParameter(paramValue, parameters[i].getType(), request, paramName);
                    } else {
                        args[i] = null;
                    }
                } else if (parameters[i].getType().equals(SessionManager.class)) {
                    HttpSession httpSession = request.getSession();
                    args[i] = new SessionManager(httpSession);
                } else {
                    System.err.println("Parameter " + i + " is missing the @Param annotation.");
                    throw new Exception("ETU002669, add annotations to all parameters");
                }
            }
        }

        System.out.println("Arguments: ");
        for (Object arg : args) {
            System.out.println(arg);
        }

        try {
            System.out.println("Invoking method...");
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("Error invoking method: " + method);
            throw new Exception("Error invoking method: " + method, e);
        }
    }

    private static Object convertParameter(String value, Class<?> type, HttpServletRequest request, String paramName) {
        try {
            if (type.equals(String.class)) {
                return value;
            } else if (type.equals(int.class) || type.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (type.equals(long.class) || type.equals(Long.class)) {
                return Long.parseLong(value);
            } else if (type.equals(double.class) || type.equals(Double.class)) {
                return Double.parseDouble(value);
            } else if (type.equals(float.class) || type.equals(Float.class)) {
                return Float.parseFloat(value);
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            } else {
                System.out.println("ito ndray zao");
                Object instance = type.getDeclaredConstructor().newInstance();
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    String fieldValue = request.getParameter(paramName + "." + field.getName());
                    if (fieldValue != null) {
                        field.setAccessible(true);
                        field.set(instance, convertParameter(fieldValue, field.getType(), request, paramName + "." + field.getName()));
                    }
                }
                return instance;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
