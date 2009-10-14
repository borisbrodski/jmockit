/*
 * JMockit Expectations
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
 * An empty interface to be used with the {@link mockit.Expectations#returns(Object)} method, in
 * order to allow test code to define invocation results (return values or thrown errors/exceptions)
 * based on test-specific logic.
 * <p/>
 * An implementation of this interface should define a method matching the signature of the recorded
 * method/constructor call.
 * That is, they should have the same name and parameters. In the case of delegating a constructor,
 * a delegate <em>method</em> should still be used, with name "$init".
 * <p/>
 * At replay time, when that method/constructor is called the corresponding "delegate" method will
 * be called to produce the desired result. The arguments passed to the delegate method will be the
 * same as those received by the recorded invocation during replay. The result can be any return
 * value compatible with the recorded method's return type, or a thrown error/exception.
 * <p/>
 * Even {@code static} methods in the mocked type can have delegates, which in turn can be static or
 * not. The same is true for {@code private}, {@code final}, and {@code native} methods.
 * <p/>
 * <a href="https://jmockit.dev.java.net/source/browse/jmockit/samples/easymock/test/org/easymock/samples/Example_JMockit_Test.java?view=markup">Example</a>
 */
public interface Delegate
{
}
