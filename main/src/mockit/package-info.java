/*
 * JMockit Core/Annotations/Expectations
 * Copyright (c) 2006-2010 Rogério Liesenfeld
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

/**
 * Provides the main classes and annotations used when writing tests with the JMockit toolkit.
 * <p/>
 * The {@link mockit.Mockit} class contains a group of <code>setUpMock/setUpMocks</code> static
 * methods that can be used to set up <em>state-oriented</em> mock implementations.
 * They rely on the {@link mockit.Mock} and {@link mockit.MockClass} annotations.
 * <p/>
 * The {@link mockit.MockUp} class is a convenient wrapper for common uses of the
 * {@link mockit.Mockit#setUpMock(Class, Object)} and {@link mockit.Mockit#setUpMock(Object)}
 * methods, which allows the definition of in-line mocks for individual tests.
 * <p/>
 * The {@link mockit.Expectations} class provides an API for the traditional <em>record-replay</em>
 * model of recording expected invocations which are later replayed and automatically verified.
 * This API makes use of the {@link mockit.Mocked} annotation.
 * <p/>
 * The {@link mockit.Verifications} class, when combined with the use of the
 * {@link mockit.NonStrictExpectations} class, the {@link mockit.NonStrict} annotation and/or the
 * {@link mockit.Expectations#notStrict()} method, extends the record-replay model to a
 * <em>record-replay-verify</em> model, where the record phase can be empty (or not) and the
 * verifications can be done explicitly.
 */
package mockit;
