package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.*;
import annotations.FileRequest;
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
        }catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + mapping.getClassName());
            throw new Exception("Class not found: " + mapping.getClassName(), e);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println("Error creating instance of: " + mapping.getClassName());
            throw new Exception("Error creating instance of: " + mapping.getClassName(), e);
        } catch (Exception e) {
            throw new Exception("Error creating instance of: " + mapping.getClassName(), e);
        }

        method = findMethod(clazz, mapping.getMethodName());
        System.out.println("Method obtained: " + method);
        
        Parameter[] parameters = method.getParameters();
        System.out.println("Number of parameters: " + parameters.length);
        Object[] args = new Object[parameters.length];
        FieldErrors fieldErrors = new FieldErrors();
        
        for (int i = 0; i < parameters.length; i++) {
            Object paramValue = extractParameterValue(parameters[i], request);
            ParameterValidator.validateParameter(paramValue, parameters[i], fieldErrors);
            args[i] = paramValue;
        }
        
        // if (!fieldErrors.isEmpty()) {
        //     throw new ValidationException(fieldErrors);
        // }
        
        return method.invoke(instance, args);
    }

    private static Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Method not found: " + methodName);
    }

    private static Object extractParameterValue(Parameter parameter, HttpServletRequest request) throws Exception {
        if (parameter.isAnnotationPresent(Param.class)) {
            Param param = parameter.getAnnotation(Param.class);
            String paramName = param.name();
            String paramValue = request.getParameter(paramName);
            return convertParameter(paramValue, parameter.getType(), request, paramName);
        } else if (parameter.isAnnotationPresent(FileRequest.class)) {
            FileRequest fileRequest = parameter.getAnnotation(FileRequest.class);
            return new WinterPart(request.getPart(fileRequest.name()));
        } else if (parameter.getType().equals(SessionManager.class)) {
            return new SessionManager(request.getSession());
        }
        System.err.println("Missing required annotation for parameter: " + parameter.getName());
        throw new Exception("ETU002669, add annotations to all parameters");
    }

    private static Object convertParameter(String value, Class<?> type, HttpServletRequest request, String paramName) throws Exception {
        if (type.equals(String.class)) return value;
        if (type.equals(int.class) || type.equals(Integer.class)) return Integer.parseInt(value);
        if (type.equals(long.class) || type.equals(Long.class)) return Long.parseLong(value);
        if (type.equals(double.class) || type.equals(Double.class)) return Double.parseDouble(value);
        if (type.equals(float.class) || type.equals(Float.class)) return Float.parseFloat(value);
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return Boolean.parseBoolean(value);
        
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Field field : type.getDeclaredFields()) {
            String fieldValue = request.getParameter(paramName + "." + field.getName());
            if (fieldValue != null) {
                field.setAccessible(true);
                field.set(instance, convertParameter(fieldValue, field.getType(), request, paramName + "." + field.getName()));
            }
        }
        return instance;
    }
}
