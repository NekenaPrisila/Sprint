package utils;
public class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        setClassName(className);
        setMethodName(methodName);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String toString() {
        return "className='" + className + '\'' +
               ", methodName='" + methodName + '\'';
    }
}

