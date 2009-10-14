/*
 * JMockit Samples
 * Copyright (c) 2009 Rogério Liesenfeld
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jdesktop.animation.timing.interpolation;

import mockit.integration.junit4.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

@RunWith(JMockit.class)
public final class SplineInterpolatorTest
{
    @Test
    public void testInterpolate()
    {
        Interpolator interp = new SplineInterpolator(0.0f, 0.0f, 1.0f, 0.0f);

        assertEquals(0.0f, interp.interpolate(0.0f), 0.0f);
        assertEquals(0.28f, interp.interpolate(0.5f), 0.001f);
        assertEquals(1.0f, interp.interpolate(1.0f), 0.0001f);
        assertEquals(1.0f, interp.interpolate(1.1f), 0.0001f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSplineWithInvalidControlPoint()
    {
        new SplineInterpolator(0.0f, 0.0f, -1.0f, 2.0f);
    }
}
