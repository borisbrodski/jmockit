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

import java.io.*;
import java.lang.management.*;
import java.util.*;

import com.sun.tools.attach.*;
import com.sun.tools.attach.spi.*;
import sun.tools.attach.*;

import mockit.internal.util.*;

final class JDK6AgentLoader
{
   private static final Class<?>[] CONSTRUCTOR_PARAMS = {AttachProvider.class, String.class};
   private static final AttachProvider ATTACH_PROVIDER = new AttachProvider()
   {
      @Override
      public String name() { return null; }

      @Override
      public String type() { return null; }

      @Override
      public VirtualMachine attachVirtualMachine(String id) { return null; }

      @Override
      public List<VirtualMachineDescriptor> listVirtualMachines() { return null; }
   };

   private final String jarFilePath;
   private final String pid;

   JDK6AgentLoader(String jarFilePath)
   {
      this.jarFilePath = jarFilePath;
      pid = discoverProcessIdForRunningVM();
   }

   private String discoverProcessIdForRunningVM()
   {
      String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
      int p = nameOfRunningVM.indexOf('@');

      return nameOfRunningVM.substring(0, p);
   }

   void loadAgent()
   {
      VirtualMachine vm;

      if (AttachProvider.providers().isEmpty()) {
         vm = getVirtualMachineImplementationFromEmbeddedOnes();
      }
      else {
         vm = attachToThisVM();
      }

      loadAgentAndDetachFromThisVM(vm);
   }

   private VirtualMachine getVirtualMachineImplementationFromEmbeddedOnes()
   {
      Class<? extends VirtualMachine> vmImplClass;

      if (File.separatorChar == '\\') {
         vmImplClass = WindowsVirtualMachine.class;
      }
      else {
         vmImplClass = LinuxVirtualMachine.class;
      }

      try {
         return Utilities.newInstance(vmImplClass, CONSTRUCTOR_PARAMS, ATTACH_PROVIDER, pid);
      }
      catch (UnsatisfiedLinkError e) {
         throw new IllegalStateException(
            "Unable to load Java agent; please add lib/tools.jar from your JDK to the classpath");
      }
   }

   private VirtualMachine attachToThisVM()
   {
      try {
         return VirtualMachine.attach(pid);
      }
      catch (AttachNotSupportedException e) {
         throw new RuntimeException(e);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void loadAgentAndDetachFromThisVM(VirtualMachine vm)
   {
      try {
         vm.loadAgent(jarFilePath, null);
         vm.detach();
      }
      catch (AgentLoadException e) {
         throw new RuntimeException(e);
      }
      catch (AgentInitializationException e) {
         throw new RuntimeException(e);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
