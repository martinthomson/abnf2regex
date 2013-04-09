package net.abnf2regex.easymock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;

/**
 * A few helper methods that enable the creation of mock objects with specific criteria on the selection of mocked
 * methods.
 */
public class EasyMockHelper
{
    /**
     * Create a mock object from a class that only mocks abstract methods. Useful for getting a concrete instance of an
     * abstract class and for testing that it passes requests to extending classes correctly.
     *
     * @param <T> The type of the returned object.
     * @param type a class instance for the desired type
     * @return a new instance of the given abstract type, with {@link EasyClassMock} driving all the previously abstract
     *         methods.
     */
    public static <T> T fillAbstractWithMock(Class<T> type)
    {
        IMockBuilder<T> builder = EasyMock.createMockBuilder(type);
        for (Method m : type.getDeclaredMethods())
        {
            if (Modifier.isAbstract(m.getModifiers()))
            {
                builder.addMockedMethod(m);
            }
        }
        return builder.createMock();
    }

    /**
     * Create a mock object from a class that mocks all but private methods.
     *
     * @param <T> The type of the returned object.
     * @param type a class instance for the desired type
     * @return a new instance of the given abstract type, with {@link EasyClassMock} driving all the previously abstract
     *         methods.
     */
    public static <T> T createCompleteMock(Class<T> type)
    {
        IMockBuilder<T> builder = EasyMock.createMockBuilder(type);
        for (Method m : type.getDeclaredMethods())
        {
            if (! Modifier.isPrivate(m.getModifiers()))
            {
                builder.addMockedMethod(m);
            }
        }
        return builder.createMock();
    }
}
