/*
 * JMockit Core
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
package mockit.internal.core;

import java.util.*;

/**
 * A container for the mock methods and constructors "collected" from a mock class, separated in two
 * sets: one with all the mock methods, and another with just the subset of static methods.
 */
public class MockMethods
{
   protected String mockClassInternalName;
   private boolean isInnerMockClass;
   private boolean withItField;

   /**
    * The set of public mock methods and constructors in a mock class. Each one is represented by
    * the concatenation of its name ("&lt;init>" in the case of a constructor) with the internal JVM
    * description of its parameters and return type.
    */
   protected final List<String> methods = new ArrayList<String>(20);

   /**
    * The subset of static methods between the {@link #methods mock methods} in a mock class. This
    * is needed when generating calls for the mock methods.
    */
   private final Collection<String> staticMethods = new ArrayList<String>(20);

   public MockMethods() {}

   public final String addMethod(String name, String desc, boolean isStatic)
   {
      String nameAndDesc = name + desc;

      if (isStatic) {
         staticMethods.add(nameAndDesc);
      }

      methods.add(nameAndDesc);
      return nameAndDesc;
   }

   /**
    * Verifies if a mock method with the same signature of a given real method was previously
    * collected from the mock class. This operation can be performed only once for any given mock
    * method in this container, so that after the last real method is processed there should be no
    * mock methods left in the container.
    */
   protected boolean containsMethod(String name, String desc)
   {
      String nameAndDesc = name + desc;

      for (int i = 0; i < methods.size(); i++) {
         if (nameAndDesc.equals(methods.get(i))) {
            methods.remove(i);
            return true;
         }
      }

      return false;
   }

   final boolean containsStaticMethod(String name, String desc)
   {
      return staticMethods.remove(name + desc);
   }

   public final String getMockClassInternalName()
   {
      return mockClassInternalName;
   }

   public final void setMockClassInternalName(String mockClassInternalName)
   {
      this.mockClassInternalName = mockClassInternalName;
   }

   final boolean isInnerMockClass()
   {
      return isInnerMockClass;
   }

   public final void setInnerMockClass(boolean innerMockClass)
   {
      isInnerMockClass = innerMockClass;
   }

   final boolean isWithItField()
   {
      return withItField;
   }

   public final void setWithItField(boolean withItField)
   {
      this.withItField = withItField;
   }

   public final int getMethodCount()
   {
      return methods.size();
   }

   public final List<String> getMethods()
   {
      return methods;
   }
}
