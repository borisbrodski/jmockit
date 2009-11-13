package integrationTests;

import java.io.*;
import java.util.concurrent.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public class ProcessTest
{
   public static class CommandTask implements Callable<String>
   {
      private final String[] args;

      public CommandTask(String... args) { this.args = args; }

      public String call() throws IOException
      {
         ProcessBuilder pb = new ProcessBuilder(args);
         Process p = pb.start();
         InputStream outputFromProcess = p.getInputStream();
         String output = readProcessOutput(outputFromProcess);
         return output;
      }

      private String readProcessOutput(InputStream outputFromProcess) throws IOException
      {
         StringBuilder processOutput = new StringBuilder();
         Reader r = new BufferedReader(new InputStreamReader(outputFromProcess));
         char[] buffer = new char[4096];
         int n;

         while ((n = r.read(buffer)) > -1) {
            processOutput.append(buffer, 0, n);
         }

         return processOutput.toString();
      }
   }

   @Test
   public void test() throws Exception
   {
      final String expectedTaskOutput = "blah";

      new Expectations()
      {
         @Capturing Process p;

         {
            p.getInputStream();
            returns(new ByteArrayInputStream(expectedTaskOutput.getBytes()));
         }
      };

      String javaHome = System.getProperty("java.home");
      String actualOutput = new CommandTask(javaHome + "/bin/java").call();

      assertEquals(expectedTaskOutput, actualOutput);
   }
}