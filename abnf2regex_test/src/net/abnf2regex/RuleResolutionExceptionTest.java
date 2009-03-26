/**
 * Copyright (c) Andrew Corporation,
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Andrew Corporation. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Andrew Corporation.
 */
package net.abnf2regex;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that the {@link RuleResolutionException} is as dumb as it sounds.
 */
public class RuleResolutionExceptionTest
{

    /**
     * Test method for {@link net.abnf2regex.RuleResolutionException#RuleResolutionException(java.lang.String)}.
     */
    @Test
    public void testInitiation()
    {
        String name = "foo"; //$NON-NLS-1$
        RuleResolutionException rre = new RuleResolutionException(name );
        Assert.assertEquals(name, rre.getMessage());
    }
}
