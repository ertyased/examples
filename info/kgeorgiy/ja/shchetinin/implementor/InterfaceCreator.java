package info.kgeorgiy.ja.shchetinin.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * InterfaceCreator is a concrete subclass of {@link AbstractCreator} responsible for generating
 * implementations of interfaces by implementing them.
 */
public class InterfaceCreator extends AbstractCreator {
    /**
     * Constructs a new InterfaceCreator object for the specified interface.
     *
     * @param clazz the interface for which an implementation is being generated
     */
    public InterfaceCreator(Class<?> clazz) {
        super(clazz);
    }

    /**
     * Returns the type of the implemented interface, which is "implements" for InterfaceCreator.
     *
     * @return the type of the implemented interface
     */
    @Override
    protected String getType() {
        return "implements";
    }

    /**
     * Retrieves an empty list of constructors since interfaces cannot have constructors.
     *
     * @return an empty list of constructors
     */
    @Override
    protected List<Constructor<?>> getConstructors() {
        return new ArrayList<>();
    }

    /**
     * Retrieves all methods declared by the interface.
     *
     * @return a list of all methods declared by the interface
     */
    protected List<Method> getMethods() {
        return methods.values().stream().toList();
    }
}
