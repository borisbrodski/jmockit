/*
 * JMockit
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
package mockit.internal.startup;

import java.lang.instrument.*;
import java.net.*;
import java.security.*;

import mockit.internal.state.*;
import mockit.internal.util.*;

final class ProxyRegistrationTransformer implements ClassFileTransformer
{
   public byte[] transform(
      ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
   {
      if (classBeingRedefined == null) {
         registerClassIfProxy(className, classfileBuffer);
         enableAssertsIfTestClass(loader, protectionDomain, className);
      }

      return null;
   }

   private void registerClassIfProxy(String className, byte[] classfileBuffer)
   {
      int p = className.indexOf("$Proxy");

      if (p >= 0 && Utilities.isPositiveDigit(className.charAt(p + 6))) {
         if (p == 0) {
            TestRun.proxyClasses().add(className, classfileBuffer);
         }
         else if (className.charAt(p - 1) == '/') {
            TestRun.proxyClasses().add(className.replace('/', '.'), classfileBuffer);
         }
      }
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
