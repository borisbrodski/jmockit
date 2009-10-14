/*
 * JMockit
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

import java.lang.reflect.*;
import java.lang.annotation.*;

import mockit.internal.util.*;

/**
 * Handles invocations to all kinds of mock implementations created for interfaces and annotation
 * types through any of the mocking APIs (Core, Annotations, Expectations).
 * <p/>
 * The {@code java.lang.Object} methods {@code equals}, {@code hashCode}, and {@code toString} are
 * handled in a meaningful way, returning a value that makes sense for the proxy instance.
 * The special {@linkplain Annotation} contracts for these three methods is <em>not</em> observed,
 * though, since it would require making dynamic calls to the mocked annotation attributes.
 * <p/>
 * Any other method invocation is handled by simply returning the default value according to the
 * method's return type (as defined in {@linkplain mockit.internal.util.DefaultValues}).
 */
final class MockInvocationHandler implements InvocationHandler
{
   static final InvocationHandler INSTANCE = new MockInvocationHandler();

   public Object invoke(Object proxy, Method method, Object[] args)
   {
      Class<?> declaringClass = method.getDeclaringClass();
      String methodName = method.getName();

      if (declaringClass == Object.class) {
         if ("equals".equals(methodName)) {
            return proxy == args[0];
         }
         else if ("hashCode".equals(methodName)) {
            return System.identityHashCode(proxy);
         }
         else if ("toString".equals(methodName)) {
            return
               proxy.getClass().getName() + '@' +
               Integer.toHexString(System.identityHashCode(proxy));
         }
      }
      else if (declaringClass == Annotation.class) {
         return proxy.getClass().getInterfaces()[0];
      }

      Class<?> retType = method.getReturnType();

      return DefaultValues.computeForType(retType);
   }
}
