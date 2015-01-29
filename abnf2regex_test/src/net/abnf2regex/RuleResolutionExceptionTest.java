package net.abnf2regex;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that the {@link RuleResolutionException} is as dumb as it sounds.
 */
public class RuleResolutionExceptionTest
{

    /**
     * Test method for
     * {@link net.abnf2regex.RuleResolutionException#RuleResolutionException(java.lang.String)}
     * .
     */
    @Test
    public void testInitiation()
    {
        String name = "foo"; //$NON-NLS-1$
        RuleResolutionException rre = new RuleResolutionException(name);
        Assert.assertEquals(name, rre.getMessage());
    }
}
