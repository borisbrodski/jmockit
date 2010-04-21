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
package mockit.coverage;

import java.security.*;
import java.util.regex.*;

final class ClassSelection
{
   private final Matcher classesToInclude;
   private final Matcher classesToExclude;

   ClassSelection(String[] args)
   {
      classesToInclude = getClassNameRegexForClassesToInclude(args);
      classesToExclude = getClassNameRegexForClassesToExclude();
   }

   private Matcher getClassNameRegexForClassesToInclude(String[] args)
   {
      String regex = args.length == 0 ? "" : args[0];

      if (regex.length() == 0) {
         regex = System.getProperty("jmockit-coverage-classes", "");
      }

      return getClassNameRegex(regex);
   }

   private Matcher getClassNameRegex(String regex)
   {
      return regex.length() == 0 ? null : Pattern.compile(regex).matcher("");
   }

   private Matcher getClassNameRegexForClassesToExclude()
   {
      String defaultExclusions = "mockit\\..+|.+Test(\\$.+)?|junit\\..+";
      String regex = System.getProperty("jmockit-coverage-excludes", defaultExclusions);
      return getClassNameRegex(regex);
   }

   boolean isSelected(String className, ProtectionDomain protectionDomain)
   {
      if (classesToInclude != null) {
         return
            classesToInclude.reset(className).matches() &&
            (classesToExclude == null || !classesToExclude.reset(className).matches());
      }
      else if (classesToExclude != null && classesToExclude.reset(className).matches()) {
         return false;
      }

      if (protectionDomain == null) {
         return false;
      }

      CodeSource codeSource = protectionDomain.getCodeSource();

      if (codeSource == null) {
         return false;
      }

      String codeLocation = codeSource.getLocation().getPath();

      return !codeLocation.endsWith(".jar") && !codeLocation.endsWith("/test-classes/");
   }
}
