package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import annotations.FileRequest;
import annotations.Param;
import annotations.authentication.Authenticated;
import annotations.authentication.Public;

public class Reflect {
    public static Object executeMethod(Mapping mapping, HttpServletRequest request, HttpServletResponse response, ServletContext context) throws Exception {
        Class<?> clazz;
        Object instance;
        Method method;

        try {
            clazz = Class.forName(mapping.getClassName());
            instance = clazz.getDeclaredConstructor().newInstance();
            System.out.println("Instance created: " + instance);
        }catch (ClassNotFoundException e) {
            throw new Exception("Class not found: " + mapping.getClassName(), e);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new Exception("Error creating instance of: " + mapping.getClassName(), e);
        } catch (Exception e) {
            throw new Exception("Error creating instance of: " + mapping.getClassName(), e);
        }

        method = findMethod(clazz, mapping.getMethodName());
        System.out.println("Method obtained: " + method);

        
        if (
            clazz.isAnnotationPresent(Authenticated.class)
                && !method.isAnnotationPresent(Public.class)
                && !method.isAnnotationPresent(Authenticated.class)
        ) {
            Authenticated authenticated = clazz.getAnnotation(Authenticated.class);
            if (!Authenticator.isAuthorised(request, authenticated)) throw new Exception("You are not allowed to access this URL");
        }
        if(method.isAnnotationPresent(Authenticated.class)) {
            Authenticated authenticated = method.getAnnotation(Authenticated.class);
            if (!Authenticator.isAuthorised(request, authenticated)) throw new Exception("You are not allowed to access this URL");
        }
        
        Parameter[] parameters = method.getParameters();
        System.out.println("Number of parameters: " + parameters.length);
        Object[] args = new Object[parameters.length];
        FieldErrors fieldErrors = new FieldErrors();
        
        for (int i = 0; i < parameters.length; i++) {
            Object paramValue = extractParameterValue(parameters[i], request, context);
            ParameterValidator.validateParameter(paramValue, parameters[i], fieldErrors);
            args[i] = paramValue;
        }
        
        if (fieldErrors.hasErrors()) {
            return fieldErrors;
        }
        
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

    private static Object extractParameterValue(Parameter parameter, HttpServletRequest request, ServletContext context) throws Exception {
        if (parameter.isAnnotationPresent(Param.class)) {
            Param param = parameter.getAnnotation(Param.class);
            String paramName = param.name();
            String paramValue = request.getParameter(paramName);
            return convertParameter(paramValue, parameter.getType(), request, paramName);
        } else if (parameter.isAnnotationPresent(FileRequest.class)) {
            FileRequest fileRequest = parameter.getAnnotation(FileRequest.class);
            return new WinterPart(request.getPart(fileRequest.name()),context);
        } else if (parameter.getType().equals(SessionManager.class)) {
            return new SessionManager(request.getSession());
        }
        System.err.println("Missing required annotation for parameter: " + parameter.getName());
        throw new Exception("ETU002669, add annotations to all parameters");
    }

    public static Object convertParameter(String value, Class<?> type, HttpServletRequest request, String paramName) throws Exception {
        if (value != null) {
            // Décoder la valeur si elle contient des caractères encodés (comme %3A)
            String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    
            if (type.equals(String.class)) {
                return decodedValue;
            }
            if (type.equals(int.class) || type.equals(Integer.class)) {
                return Integer.parseInt(decodedValue);
            }
            if (type.equals(long.class) || type.equals(Long.class)) {
                return Long.parseLong(decodedValue);
            }
            if (type.equals(double.class) || type.equals(Double.class)) {
                return Double.parseDouble(decodedValue);
            }
            if (type.equals(float.class) || type.equals(Float.class)) {
                return Float.parseFloat(decodedValue);
            }
            if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                return Boolean.parseBoolean(decodedValue);
            }
    
            // Gestion des dates et heures
            if (type.equals(Timestamp.class)) {
                System.out.println("yeess");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime localDateTime = LocalDateTime.parse(decodedValue, formatter);
                Timestamp timestamp = Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC));
                return timestamp;
            }
            if (type.equals(LocalDate.class)) {
                return LocalDate.parse(decodedValue, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            if (type.equals(LocalDateTime.class)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime localDateTime = LocalDateTime.parse(decodedValue, formatter);
                return localDateTime;
            }
            if (type.equals(LocalTime.class)) {
                return parseLocalTime(decodedValue);
            }

        }

        // Gestion des objets complexes
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Field field : type.getDeclaredFields()) {
            String fieldValue = request.getParameter(paramName + "." + field.getName());
            if (fieldValue != null) {
                field.setAccessible(true);
                System.out.println("fieldType : " + field.getType());
                field.set(instance, convertParameter(fieldValue, field.getType(), request, paramName + "." + field.getName()));
            }
        }
        return instance;
    }

    // Méthode pour gérer LocalTime avec différents formats
    private static LocalTime parseLocalTime(String value) {
        DateTimeFormatter formatter;
        if (value.matches("\\d{2}:\\d{2}")) { // Format HH:mm sans secondes
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        } else if (value.matches("\\d{2}:\\d{2}:\\d{2}")) { // Format HH:mm:ss avec secondes
            formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        } else { // Format ISO HH:mm:ss
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }
        return LocalTime.parse(value, formatter);
    }

}
