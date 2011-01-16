/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.lang.instrument.*;
import java.net.*;
import java.security.*;

import mockit.internal.state.*;
import mockit.internal.util.*;

final class JMockitTransformer implements ClassFileTransformer
{
   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
   {
      if (classBeingRedefined == null) {
         if (!registerClassIfProxy(className, classfileBuffer)) {
            enableAssertsIfTestClass(loader, protectionDomain, className);
         }
      }

      return null;
   }

   private boolean registerClassIfProxy(String className, byte[] classfileBuffer)
   {
      String proxyClassName = null;
      int p = className.indexOf("$Proxy");

      if (p >= 0 && Utilities.hasPositiveDigit(className, p + 5)) {
         if (p == 0) {
            proxyClassName = className;
         }
         else if (className.charAt(p - 1) == '/') {
            proxyClassName = className.replace('/', '.');
         }
      }

      if (proxyClassName != null) {
         TestRun.proxyClasses().add(proxyClassName, classfileBuffer);
         return true;
      }

      return false;
   }

   private void enableAssertsIfTestClass(ClassLoader loader, ProtectionDomain protectionDomain, String className)
   {
      if (
         loader != null && protectionDomain != null &&
         (className.endsWith("Test") || isFromTestClassesDirectory(protectionDomain))
      ) {
         loader.setClassAssertionStatus(className.replace('/', '.'), true);
      }
   }

   private boolean isFromTestClassesDirectory(ProtectionDomain protectionDomain)
   {
      CodeSource codeSource = protectionDomain.getCodeSource();

      if (codeSource == null) {
         return false;
      }

      URL location = codeSource.getLocation();

      return location != null && location.getPath().endsWith("/test-classes/");
   }
}
