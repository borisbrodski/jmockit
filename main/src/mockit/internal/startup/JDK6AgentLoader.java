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
package mockit.internal.startup;

import java.lang.management.*;

import com.sun.tools.attach.*;

final class JDK6AgentLoader
{
   private final String jarFilePath;

   JDK6AgentLoader(String jarFilePath)
   {
      this.jarFilePath = jarFilePath;
   }

   void loadAgent()
   {
      String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
      int p = nameOfRunningVM.indexOf('@');
      String pid = nameOfRunningVM.substring(0, p);

      try {
         VirtualMachine vm = VirtualMachine.attach(pid);
         vm.loadAgent(jarFilePath, "");
         vm.detach();
      }
      catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
