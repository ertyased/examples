package info.kgeorgiy.ja.shchetinin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


/**
 * AbstractCreator is an abstract class providing common functionality for generating implementations
 * of abstract classes or interfaces. It contains methods for processing constructors and methods,
 * as well as utilities for generating code strings.
 */
public abstract class AbstractCreator {
    /**
     * Set of primitive classes of integer types.
     */
    static Set<Class<?>> integerClass = Set.of(int.class, short.class, long.class, byte.class, double.class,
            float.class, char.class);
    /**
     * The StringBuilder used for constructing the implementation code.
     */
    final protected StringBuilder builder;
    /**
     * The class for which an implementation is being generated.
     */
    final protected Class<?> clazz;
    /**
     * The simple name of the class being implemented with the "Impl" suffix.
     */
    final String className;

    /**
     * Map storing method information as MethodInfo-Method pairs.
     */
    final protected Map<MethodInfo, Method> methods;

    /**
     * Constructs a new AbstractCreator object for the specified class.
     *
     * @param _clazz the class for which an implementation is being generated
     */
    public AbstractCreator(Class<?> _clazz) {
        className = _clazz.getSimpleName() + "Impl";
        builder = new StringBuilder();
        clazz = _clazz;
        methods = getAllMethodsRecursive(clazz);
    }

    /**
     * Checks if the provided class is suitable for implementation.
     *
     * @return true if the class is suitable for implementation; false otherwise
     */
    public boolean isCorrectClass() {
        return !clazz.isArray() && !clazz.isEnum() && !clazz.isPrimitive() && !Modifier.isFinal(clazz.getModifiers())
                && !Modifier.isPrivate(clazz.getModifiers()) && !clazz.equals(Enum.class) && !clazz.equals(Record.class);
    }

    /**
     * Convert a constructor to its string representation.
     *
     * @param constructor the constructor to convert
     * @return the string representation of the constructor
     */
    private String constructorToString(Constructor<?> constructor) {
        StringBuilder cons = new StringBuilder();
        cons.append("    ").append(AbstractCreator.getStringModifiers(constructor.getModifiers()))
                .append(" ")
                .append(className);
        cons.append("(").append(listClassString(constructor.getParameterTypes())).append(") ");
        Class<?>[] exceptions = constructor.getExceptionTypes();
        if (exceptions.length != 0) {
            cons.append("throws ");
            cons.append(Arrays.stream(exceptions)
                    .map(Class::getCanonicalName)
                    .collect(Collectors.joining(", ")));

        }
        cons.append(" {\n");
        cons.append("         super(");
        for (int i = 1; i <= constructor.getParameterCount(); ++i) {
            if (i != 1) {
                cons.append(", ");
            }
            cons.append("var").append(i);
        }
        cons.append(");\n    }\n");

        return cons.toString();
    }
    /**
     * Converts a method to its string representation.
     *
     * @param method the method to be converted.
     * @return the string representation of the method.
     */
    public String methodToString(Method method) {
        StringBuilder stringBuilder = new StringBuilder().append("    ")
                .append(getStringModifiers(method.getModifiers()));
        stringBuilder.append(method.getReturnType().getCanonicalName()).append(" ").append(method.getName());
        stringBuilder.append("(");
        stringBuilder.append(listClassString(method.getParameterTypes()));
        stringBuilder.append(") {\n        return");
        stringBuilder.append(getDefaultValue(method.getReturnType()));
        stringBuilder.append(";\n    }\n");
        return stringBuilder.toString();
    }
    /**
     * Check if a method is suitable for implementation.
     *
     * @param method the method to check
     * @return true if the method is suitable for implementation; false otherwise
     */
    private boolean checkMethod(Method method) {
        for (Class<?> clazz : method.getParameterTypes()) {
            if (Modifier.isPrivate(clazz.getModifiers())) {
                return false;
            }
        }
        return !Modifier.isPrivate(method.getReturnType().getModifiers());
    }

    /**
     * Abstract method to get the type of the implemented class (class or interface).
     *
     * @return the type of the implemented class
     */
    abstract protected String getType();

    /**
     * Abstract method to get the constructors of the implemented class.
     *
     * @return a list of constructors of the implemented class
     * @throws ImplerException if an error occurs while retrieving constructors
     */
    abstract protected List<Constructor<?>> getConstructors() throws ImplerException;

    /**
     * Abstract method to get the methods of the implemented class.
     *
     * @return a list of methods of the implemented class
     * @throws ImplerException if an error occurs while retrieving methods
     */
    abstract protected List<Method> getMethods() throws ImplerException;

    /**
     * Generates the implementation code for the class.
     *
     * @return the implementation code for the class
     * @throws ImplerException if an error occurs during code generation
     */
    public String create() throws ImplerException {
        builder.append("package ").append(clazz.getPackageName()).append(";\n\n");
        builder.append("public class ").append(className).append(" ").append(getType()).append(" ").append(clazz.getCanonicalName()).append(" {\n");
        List<Constructor<?>> constructors = getConstructors();
        for (Constructor<?> constructor : constructors) {
            builder.append(constructorToString(constructor));
        }
        List<Method> methods = getMethods();
        for (Method mi : methods) {
            if (!checkMethod(mi)) {
                throw new ImplerException("can't implement method:" + mi + "because one of parametrs or return value is private");
            }
            builder.append("\n").append(methodToString(mi));
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Get the string representation of default value for a given class.
     *
     * @param clazz the class to get the default value for
     * @return the default value for the class
     */
    protected static String getDefaultValue(Class<?> clazz) {
        if (integerClass.contains(clazz)) {
            return " (" + clazz.getSimpleName() + ") 0";
        }
        if (clazz == void.class) {
            return "";
        }
        if (clazz == boolean.class) {
            return " false";
        }
        return " null";
    }

    /**
     * Add methods to the method signatures map.
     *
     * @param methods the methods to add
     * @param md      the map of method signatures
     */
    protected void addMethods(Method[] methods, Map<MethodInfo, Method> md) {
        for (Method method : methods) {
            List<Class<?>> args = Arrays.asList(method.getParameterTypes());
            MethodInfo mi = new MethodInfo(args, method.getName());
            if (md.containsKey(mi)) {
                if (md.get(mi).getReturnType().equals(method.getReturnType()) && Modifier.isAbstract(md.get(mi).getModifiers())) {
                    continue;
                }
                if (!Modifier.isFinal(md.get(mi).getModifiers())
                        && md.get(mi).getReturnType().isAssignableFrom(method.getReturnType())) {
                    md.put(mi, method);
                }
            } else {
                md.put(mi, method);
            }

        }
    }

    /**
     * Get all methods of a class and its superclasses recursively.
     *
     * @param clazz the class to get the methods for
     * @return a map of method information to methods
     */
    protected Map<MethodInfo, Method> getAllMethodsRecursive(Class<?> clazz) {
        Map<MethodInfo, Method> methodSignatures = new HashMap<>();

        addMethods(clazz.getMethods(), methodSignatures);
        while (clazz.getSuperclass() != null) {
            addMethods(clazz.getDeclaredMethods(), methodSignatures);
            clazz = clazz.getSuperclass();
        }
        return methodSignatures;
    }

    /**
     * Record class to store method information.
     */
    protected record MethodInfo(List<Class<?>> args, String name) {
    }

    /**
     * Get string representation of method modifiers.
     *
     * @param modifiers the modifiers to convert
     * @return the string representation of the modifiers
     */
    static protected String getStringModifiers(int modifiers) {
        if (Modifier.isPublic(modifiers)) {
            return "public ";
        }
        if (Modifier.isPrivate(modifiers)) {
            return "private ";
        }
        if (Modifier.isProtected(modifiers)) {
            return "protected ";
        }
        return "";
    }

    /**
     * Get string representation of a list of classes.
     *
     * @param classes the list of classes to convert
     * @return the string representation of the list of classes
     */
    protected String listClassString(Class<?>[] classes) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Class<?> clazz : classes) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            i++;
            stringBuilder.append(clazz.getCanonicalName()).append(" ").append("var").append(i);
        }
        return stringBuilder.toString();
    }
}
