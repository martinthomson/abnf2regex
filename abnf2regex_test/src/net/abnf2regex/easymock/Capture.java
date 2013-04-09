package net.abnf2regex.easymock;

import java.io.Serializable;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;

/**
 * This class provides an implementation of the Capture class provided by EasyMock 2.4, with some extras. This ensures
 * that successive calls are all made with the same object. On the first attempt at a match, this class captures the
 * argument and it then checks on subsequent calls to check if the argument is the same. An accessor is provided so that
 * the value can be extracted for more checking later.
 *
 * <p>
 * Create and use as follows:
 * </p>
 *
 * <pre>
 *   Capture&lt;String&gt; c = new Capture&lt;String&gt;(String.class);
 *   mock.method(c.capture(), EasyMock.eq(othervalue));
 *   // EasyMock 2.4 uses the following instead:
 *   //   mock.method(EasyMock.capture(c));
 *   ...
 *   Assert.assertEqual(&quot;expected value&quot;, c.getValue());
 * </pre>
 *
 * <p>
 * This class implements {@link IAnswer} so that it can be provided as a response, as well as a matcher. If the answer
 * is requested before a capture is made, an {@link AssertionError} is thrown (this class is not nice). Use as follows:
 * </p>
 *
 * <pre>
 *   EasyMock.expect(mock.method(c.capture(), EasyMock.eq(othervalue))).andAnswer(c);
 * </pre>
 *
 * @param <T> matches must all provide the same type
 */
public class Capture<T> implements IAnswer<T>, Serializable
{
    /** For serialization. */
    private static final long serialVersionUID = 6845948292227446678L;
    /** Whether a value has been captured or not. */
    private boolean captured = false;
    /** The captured value. */
    private T value = null;

    /**
     * Return the captured value.
     *
     * @return the captured value.
     * @throws AssertionError if no value has been captured yet.
     * @see org.easymock.IAnswer#answer()
     */
    @Override
    public T answer() throws Throwable
    {
        if (!this.captured)
        {
            throw new AssertionError("Answer from uninitialized capture: " + this.toString()); //$NON-NLS-1$
        }
        return this.value;
    }

    /**
     * Get the captured value.
     *
     * @return the value captured in the first call
     */
    public T getValue()
    {
        return this.value;
    }

    /**
     * Determine if this has been used.
     *
     * @return whether a parameter has been captured
     */
    public boolean hasCaptured()
    {
        return this.captured;
    }

    /**
     * Resets the object to a pristine state, no captured state
     */
    public void reset()
    {
        this.captured = false;
        this.value = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.getClass().getName();
    }

    /**
     * Use this in an argument list to report this matcher.
     *
     * @return null - easymock doesn't need anything from this method
     */
    public T capture()
    {
        EasyMock.reportMatcher(new CaptureMatcher());
        return null;
    }

    /**
     * Check for a match. The match fails if the type of the argument isn't correct. If a value has already been
     * captured, the argument must be the same object (== rather than .equals).
     *
     * @param argument the object to check
     * @return true if the match is successful
     * @see org.easymock.IArgumentMatcher#matches(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    boolean matches(Object argument)
    {
        if (!this.captured)
        {
            this.captured = true;
            this.value = (T) argument;
            return true;
        }
        return this.value == argument;
    }

    /** Implement {@link IArgumentMatcher} privately. */
    class CaptureMatcher implements IArgumentMatcher
    {
        /*
         * @see org.easymock.IArgumentMatcher#appendTo(java.lang.StringBuffer)
         */
        @Override
        public void appendTo(StringBuffer buffer)
        {
            buffer.append(Capture.this.toString());
        }

        /*
         * @see org.easymock.IArgumentMatcher#matches(java.lang.Object)
         */
        @Override
        public boolean matches(Object argument)
        {
            return Capture.this.matches(argument);
        }
    }
}
