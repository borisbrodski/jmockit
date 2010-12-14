package mockit;

import java.io.*;

public final class FileIO
{
   public void writeToFile(String fileName) throws IOException
   {
      FileWriter writer = new FileWriter(fileName);
      BufferedWriter out = new BufferedWriter(writer);
      out.write("Test FileIO");
      out.close();

      System.out.println("File written");
   }
}
