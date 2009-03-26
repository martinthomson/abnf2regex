/**
 * Copyright (c) Andrew Corporation,
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Andrew Corporation. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Andrew Corporation.
 */
package net.abnf2regex.easymock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.easymock.classextension.EasyClassMock;

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
        Method[] methodArray = type.getDeclaredMethods();
        List<Method> methods = new ArrayList<Method>(Arrays.asList(methodArray));
        Iterator<Method> it = methods.iterator();
        while (it.hasNext())
        {
            Method m = it.next();
            if (!Modifier.isAbstract(m.getModifiers()))
            {
                it.remove();
            }
        }
        return EasyClassMock.createMock(type, methods.toArray(new Method[0]));
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
        Method[] methodArray = type.getDeclaredMethods();
        List<Method> methods = new ArrayList<Method>(Arrays.asList(methodArray));
        Iterator<Method> it = methods.iterator();
        while (it.hasNext())
        {
            Method m = it.next();
            if (Modifier.isPrivate(m.getModifiers()))
            {
                it.remove();
            }
        }
        return EasyClassMock.createMock(type, methods.toArray(new Method[0]));
    }
}
