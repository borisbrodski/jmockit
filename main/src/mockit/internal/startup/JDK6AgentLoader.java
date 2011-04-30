/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;
import java.lang.management.*;
import java.util.*;

import com.sun.tools.attach.*;
import com.sun.tools.attach.spi.*;
import sun.tools.attach.*;

final class JDK6AgentLoader
{
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

   @SuppressWarnings({"UseOfSunClasses"})
   private VirtualMachine getVirtualMachineImplementationFromEmbeddedOnes()
   {
      try {
         if (File.separatorChar == '\\') {
            return new WindowsVirtualMachine(ATTACH_PROVIDER, pid);
         }
         else {
            return new LinuxVirtualMachine(ATTACH_PROVIDER, pid);
         }
      }
      catch (AttachNotSupportedException e) {
         throw new RuntimeException(e);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
      catch (UnsatisfiedLinkError e) {
         throw new IllegalStateException("Native library for Attach API not available in this JRE", e);
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
