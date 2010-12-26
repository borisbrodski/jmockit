/*
 * JMockit Samples
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
package unitils.wiki;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class PartialMock_JMockit_Test
{
   // Note that JMockit does not require the use of dependency injection.
   final MyService myService = new MyService();

   @Test
   public void outputText()
   {
      // Nothing is mocked for this test.
      assertEquals("the text", myService.outputText()); // executes the original behavior
   }

   @Test
   public void outputOtherText()
   {
      // Creates an instance normally, to use when recording expectations on instance methods.
      final TextService textService = new TextService();

      new NonStrictExpectations(TextService.class) // "dynamic" partial mocking, for all instances of the class
      {
         {
            // Will match calls to the method on any instance:
            textService.getText(); result = "some other text"; // overrides the original behavior
         }
      };

      assertEquals("some other text", myService.outputText()); // executes this new behavior
   }
}