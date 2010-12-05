/*
 * JMockit Hibernate 3 Emulation
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
package mockit.emulation.hibernate3.ast;

import java.lang.reflect.*;
import java.util.*;

public final class QueryEval
{
   public final Map<?, ?> parameters;
   public final Map<String, Object> tuple;

   // Just for tests.
   public QueryEval()
   {
      parameters = null;
      tuple = null;
   }

   // Just for tests.
   public QueryEval(Map<?, ?> parameters)
   {
      this.parameters = parameters;
      tuple = null;
   }

   public QueryEval(Map<?, ?> parameters, Map<String, Object> tuple)
   {
      this.parameters = parameters;
      this.tuple = tuple;
   }

   public static Object executeStaticMethod(String name, Object[] args)
   {
      Class<?>[] argClasses = new Class<?>[args.length];

      for (int i = 0; i < args.length; i++) {
         argClasses[i] = args[i].getClass();
      }

      return executeMethod(HQLFunctions.class, name, argClasses, null, args);
   }

   public static Object executeGetter(Object instance, String methodName)
   {
      return executeMethod(instance.getClass(), methodName, new Class<?>[0], instance);
   }

   private static Object executeMethod(
      Class<?> theClass, String name, Class<?>[] paramTypes, Object instance, Object... args)
   {
      try {
         Method getter = theClass.getMethod(name, paramTypes);
         return getter.invoke(instance, args);
      }
      catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
         Throwable cause = e.getCause();

         if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
         }
         else {
            throw new RuntimeException(cause);
         }
      }
   }
}