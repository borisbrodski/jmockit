/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
      if (protectionDomain == null) {
         return false;
      }

      CodeSource codeSource = protectionDomain.getCodeSource();

      if (codeSource == null) {
         return false;
      }

      if (classesToInclude != null) {
         return
            classesToInclude.reset(className).matches() &&
            (classesToExclude == null || !classesToExclude.reset(className).matches());
      }
      else if (classesToExclude != null && classesToExclude.reset(className).matches()) {
         return false;
      }

      String codeLocation = codeSource.getLocation().getPath();

      return !codeLocation.endsWith(".jar") && !codeLocation.endsWith("/test-classes/");
   }
}
