/*
 * JMockit Coverage
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
package mockit.coverage.reporting.parsing;

import java.util.*;

public final class FileParser
{
   private static final class PendingClass
   {
      final String className;
      int braceBalance;

      PendingClass(String className)
      {
         this.className = className;
      }
   }

   public final LineParser lineParser = new LineParser();
   public final List<PendingClass> currentClasses = new ArrayList<PendingClass>(2);

   private PendingClass currentClass;
   private boolean openingBraceForClassFound;
   private int currentBraceBalance;

   public boolean parseCurrentLine(String line)
   {
      if (!lineParser.parse(line)) {
         return false;
      }

      LineElement firstElement = lineParser.getInitialElement();
      LineElement classDeclaration = findClassNameInNewClassDeclaration();

      if (classDeclaration != null) {
         firstElement = classDeclaration;
         registerStartOfClassDeclaration(classDeclaration);
      }

      if (currentClass != null) {
         detectPotentialEndOfClassDeclaration(firstElement);
      }

      return true;
   }

   private LineElement findClassNameInNewClassDeclaration()
   {
      LineElement previous = null;

      for (LineElement element : lineParser.getInitialElement()) {
         if (element.isKeyword("class") && (previous == null || !previous.isDotSeparator())) {
            return element.getNextCodeElement();
         }

         previous = element;
      }

      return null;
   }

   private void registerStartOfClassDeclaration(LineElement elementWithClassName)
   {
      String className = elementWithClassName.getText();

      if (currentClass != null) {
         currentClass.braceBalance = currentBraceBalance;
      }

      currentClass = new PendingClass(className);
      currentClasses.add(currentClass);
      currentBraceBalance = 0;
   }

   private void detectPotentialEndOfClassDeclaration(LineElement firstElement)
   {
      // TODO: how to deal with classes defined entirely in one line?
      currentBraceBalance += firstElement.getBraceBalanceUntilEndOfLine();

      if (!openingBraceForClassFound && currentBraceBalance > 0) {
         openingBraceForClassFound = true;
      }
      else if (openingBraceForClassFound && currentBraceBalance == 0) {
         restorePreviousPendingClassIfAny();
      }
   }

   private void restorePreviousPendingClassIfAny()
   {
      currentClasses.remove(currentClass);

      if (currentClasses.isEmpty()) {
         currentClass = null;
      }
      else {
         currentClass = currentClasses.get(currentClasses.size() - 1);
         currentBraceBalance = currentClass.braceBalance;
      }
   }

   public String getCurrentlyPendingClass()
   {
      return currentClass == null ? null : currentClass.className;
   }
}
