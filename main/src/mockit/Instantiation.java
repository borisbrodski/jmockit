/*
 * JMockit Annotations
 * Copyright (c) 2006-2009 Rogério Liesenfeld
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
package mockit;

/**
 * Defines the lifetime of the instances automatically created for a
 * {@linkplain MockClass mock class}.
 * <p/>
 * <a href="http://jmockit.googlecode.com/svn/trunk/www/tutorial/StateBasedTesting.html#instantiation">Tutorial</a>
 */
public enum Instantiation
{
   /**
    * The mock class is instantiated once every time it's set up through a method like
    * {@link mockit.Mockit#setUpMocks(Object...)}, or for each test class to which the mock class is
    * applied through the {@link UsingMocksAndStubs} annotation.
    * <p/>
    * In the first case, the instance lifetime will be limited to the scope in which that set up
    * applies: a single test method (if called directly from the test method or from a
    * {@code setUp/@Before/@BeforeMethod} method), the whole test class (if called from a
    * {@code @BeforeClass} method), or the whole test suite (if called from
    * {@code @BeforeClass} method in a JUnit 4 {@code @Suite} class, for example).
    * <p/>
    * This same instance of the mock class is then used to redirect each call to a mocked method
    * or constructor (provided the mock method is not static, of course).
    */
   PerMockSetup,

   /**
    * The mock class is instantiated every time an instance mock method is about to be called
    * from a mocked method in the real class; that is, each time a mocked method or constructor
    * is executed, if it has a corresponding instance mock method.
    * <p/>
    * This new instance will be used only for that particular execution of the mocked method or
    * constructor, being discarded as soon as the mock method returns from the call.
    */
   PerMockInvocation,

   /**
    * A new instance of the mock class is created for each instance of the real class that was
    * mocked, but only when the first instance mock method needs to be called from a mocked
    * method or constructor.
    * <p/>
    * This same instance of the mock class is then used to redirect each call to a mocked
    * instance method on the same instance of the real class (static mock methods are never
    * called on any instance of the mock class, of course).
    */
   PerMockedInstance
}
