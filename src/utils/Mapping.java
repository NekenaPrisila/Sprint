package utils;

public class Mapping {
    private String className;
    private String methodName;
    private HttpMethod httpMethod;  // Ajout du verbe HTTP

    public Mapping(String className, String methodName, HttpMethod httpMethod) {
        this.className = className;
        this.methodName = methodName;
        this.httpMethod = httpMethod;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String toString() {
        return "className='" + className + '\'' +
               ", methodName='" + methodName + '\'';
    }
}
