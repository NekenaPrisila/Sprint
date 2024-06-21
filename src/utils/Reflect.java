package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.*;
import annotations.Param;

public class Reflect {
    public static Object executeMethod(Mapping mapping, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Class<?> clazz = Class.forName(mapping.getClassName());
        Object instance = clazz.getDeclaredConstructor().newInstance();
        
        System.out.println("Instance created: " + instance);

        Method method = clazz.getMethod(mapping.getMethodName());
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    Param param = parameters[i].getAnnotation(Param.class);
                    String paramName = param.name();
                    String paramValue = request.getParameter(paramName);

                    if (paramValue != null) {
                        args[i] = convertParameter(parameters[i].getType(), paramValue);
                    } else {
                        args[i] = null;
                    }
                } else if (parameters[i].getType().equals(HttpServletRequest.class)) {
                    args[i] = request;
                } else if (parameters[i].getType().equals(HttpServletResponse.class)) {
                    args[i] = response;
                }
            }
        }

        System.out.println("Arguments: ");
        for (Object arg : args) {
            System.out.println(arg);
        }

        return method.invoke(instance, args);
    }

    private static Object convertParameter(Class<?> type, String value) {
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
        }
        // Ajoutez d'autres conversions si nécessaire
        return null;
    }
}

