package info.kgeorgiy.ja.shchetinin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * ClassCreator is a concrete subclass of {@link AbstractCreator} responsible for generating
 * implementations of abstract classes by extending them.
 */
public class ClassCreator extends AbstractCreator {
    /**
     * Constructs a new ClassCreator object for the specified class.
     *
     * @param clazz the class for which an implementation is being generated
     */
    public ClassCreator(Class<?> clazz) {
        super(clazz);
    }
    /**
     * Returns the type of the implemented class, which is "extends" for ClassCreator.
     *
     * @return the type of the implemented class
     */
    protected String getType() {
        return "extends";
    }

    /**
     * Retrieves the constructors of the implemented class.
     *
     * @return a list of constructors of the implemented class
     * @throws ImplerException if no non-private constructors are found
     */
    @Override
    protected List<Constructor<?>> getConstructors() throws ImplerException {
        List<Constructor<?>> constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toList();
        if (constructors.isEmpty()) {
            throw new ImplerException("No constructors was created");
        }
        return constructors;
    }

    /**
     * Retrieves the abstract methods of the implemented class.
     *
     * @return a list of abstract methods of the implemented class
     */
    protected List<Method> getMethods() {
        return methods.values().stream()
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .toList();
    }
}
