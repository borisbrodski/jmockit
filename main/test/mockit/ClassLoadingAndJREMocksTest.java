/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import static java.util.Arrays.*;

import org.junit.*;
import static org.junit.Assert.*;

public final class ClassLoadingAndJREMocksTest
{
   static class Foo
   {
      boolean checkFile(String filePath)
      {
         File f = new File(filePath);
         return f.exists();
      }
   }

   @Test
   public void recordExpectationForFileUsingLocalMockField()
   {
      new Expectations() {
         @Mocked File file;

         {
            new File("filePath").exists(); result = true;
         }
      };

      Foo foo = new Foo();
      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void recordExpectationForFileUsingMockParameter(@Mocked File file)
   {
      new Expectations() {{ new File("filePath").exists(); result = true; }};

      Foo foo = new Foo();
      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void mockUpFile()
   {
      Foo foo = new Foo();

      new MockUp<File>() {
         @Mock void $init(String name) {} // not necessary, except to verify non-occurrence of NPE
         @Mock boolean exists() { return true; }
      };

      assertTrue(foo.checkFile("filePath"));
   }

   @Test
   public void mockFileSafelyUsingReentrantMockMethod()
   {
      new MockUp<File>() {
         File it;
         @Mock(reentrant = true) boolean exists() { return "testFile".equals(it.getName()) || it.exists(); }
      };

      checkForTheExistenceOfSeveralFiles();
   }

   private void checkForTheExistenceOfSeveralFiles()
   {
      assertFalse(new File("someOtherFile").exists());
      assertTrue(new File("testFile").exists());
      assertFalse(new File("yet/another/file").exists());
      assertTrue(new File("testFile").exists());
   }

   @Test
   public void mockFileSafelyUsingProceed()
   {
      new MockUp<File>() {
         @Mock boolean exists(Invocation inv)
         {
            File it = inv.getInvokedInstance();
            return "testFile".equals(it.getName()) || inv.<Boolean>proceed();
         }
      };

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileSafelyUsingDynamicPartialMocking()
   {
      final File aFile = new File("");

      new NonStrictExpectations(File.class) {{
         aFile.exists();
         result = new Delegate() {
            @Mock boolean exists(Invocation inv)
            {
               File it = inv.getInvokedInstance();
               return "testFile".equals(it.getName());
            }
         };
      }};

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileSafelyUsingReplacementInstanceForMatchingConstructorInvocations()
   {
      new NonStrictExpectations() {
         File aFile;

         {
            new File("testFile"); result = aFile;
            onInstance(aFile).exists(); result = true;
         }
      };

      checkForTheExistenceOfSeveralFiles();
   }

   @Test
   public void mockFileOutputStreamInstantiation() throws Exception
   {
      new Expectations() {
         @Mocked("helperMethod") TestedUnitUsingIO tested;
         @Mocked FileOutputStream mockOS;

         {
            invoke(tested, "helperMethod", withAny(FileOutputStream.class));
         }
      };

      new TestedUnitUsingIO().doSomething();
   }

   static class TestedUnitUsingIO
   {
      void doSomething() throws FileNotFoundException
      {
         helperMethod(new FileOutputStream("test"));
      }

      private void helperMethod(OutputStream output)
      {
         // Won't happen:
         throw new IllegalStateException(output.toString());
      }
   }

   @Test
   public void mockEntireAbstractListClass()
   {
      new NonStrictExpectations() { @Mocked AbstractList<?> c; };
   }

   @Test
   public void attemptToMockNonMockableJREClass()
   {
      new NonStrictExpectations() {
         Integer mock;

         {
            assertNull(mock);
         }
      };
   }

   static class ClassWithVector
   {
      @SuppressWarnings("UseOfObsoleteCollectionType")
      final Collection<?> theVector = new Vector<Object>();
      public int getVectorSize() { return theVector.size(); }
   }

   @Test
   public void useMockedVectorDuringClassLoading()
   {
      new NonStrictExpectations() {
         @SuppressWarnings({"UseOfObsoleteCollectionType", "CollectionDeclaredAsConcreteClass"})
         Vector<?> mockedVector;

         {
            mockedVector.size(); result = 2;
         }
      };

      assertEquals(2, new ClassWithVector().getVectorSize());
   }

   @Test
   public void mockHashtable()
   {
      Properties props = new Properties();

      new Expectations() {
         Properties mock;

         {
            mock.remove(anyString); result = 123;
            mock.getProperty("test"); result = "mock";
         }
      };

      assertEquals(123, props.remove(""));
      assertEquals("mock", props.getProperty("test"));
   }

   @Test
   public void mockURLAndURLConnection(final URL mockUrl, final URLConnection mockConnection) throws Exception
   {
      new Expectations() {{ mockUrl.openConnection(); result = mockConnection; }};

      URLConnection conn = mockUrl.openConnection();
      assertSame(mockConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnection(final URL mockUrl, final HttpURLConnection mockConnection) throws Exception
   {
      new NonStrictExpectations() {{ mockUrl.openConnection(); result = mockConnection; }};

      HttpURLConnection conn = (HttpURLConnection) mockUrl.openConnection();
      assertSame(mockConnection, conn);
   }

   @Test
   public void mockURLAndHttpURLConnectionWithDynamicMockAndLocalMockField() throws Exception
   {
      final URL url = new URL("http://nowhere");

      new NonStrictExpectations(url) {
         HttpURLConnection mockHttpConnection;

         {
            url.openConnection(); result = mockHttpConnection;
            mockHttpConnection.getOutputStream(); result = new ByteArrayOutputStream();
         }
      };

      // Code under test:
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("PUT");
      OutputStream out = conn.getOutputStream();

      assertNotNull(out);

      new Verifications() {
         HttpURLConnection mockHttpConnection;

         {
            mockHttpConnection.setDoOutput(true);
            mockHttpConnection.setRequestMethod("PUT");
         }
      };
   }

   @Test
   public void mockFileInputStream() throws Exception
   {
      new Expectations(FileInputStream.class) {{ new FileInputStream("").close(); result = new IOException(); }};

      try {
         new FileInputStream("").close();
         fail();
      }
      catch (IOException ignore) {
         // OK
      }
   }

   @Test
   public void mockNonExistentZipFileSoItAppearsToExistWithTheContentsOfAnExistingFile() throws Exception
   {
      String existentZipFileName = getClass().getResource("test.zip").getPath();
      final ZipFile testZip = new ZipFile(existentZipFileName);

      new NonStrictExpectations(ZipFile.class) {{
         new ZipFile("non-existent"); result = testZip;
      }};

      assertEquals("test", readFromZipFile("non-existent"));

      try {
         new ZipFile("another-non-existent");
         fail();
      }
      catch (FileNotFoundException ignore) {}

      assertEquals("test", readFromZipFile(existentZipFileName));
   }

   private String readFromZipFile(String fileName) throws IOException
   {
      ZipFile zf = new ZipFile(fileName);
      ZipEntry firstEntry = zf.entries().nextElement();
      InputStream content = zf.getInputStream(firstEntry);
      return new BufferedReader(new InputStreamReader(content)).readLine();
   }

   @Test
   public void mockJarEntry(@Mocked final JarEntry mockEntry)
   {
      new NonStrictExpectations() {{
         mockEntry.getName(); result = "Test";
      }};

      assertEquals("Test", mockEntry.getName());
   }

   String readMainClassAndFileNamesFromJar(File file, List<String> containedFileNames) throws IOException
   {
      JarFile jarFile = new JarFile(file);

      Manifest manifest = jarFile.getManifest();
      Attributes mainAttributes = manifest.getMainAttributes();
      String mainClassName = mainAttributes.getValue(Attributes.Name.MAIN_CLASS);

      Enumeration<JarEntry> jarEntries = jarFile.entries();

      while (jarEntries.hasMoreElements()) {
         JarEntry jarEntry = jarEntries.nextElement();
         containedFileNames.add(jarEntry.getName());
      }

      return mainClassName;
   }

   @Test
   public void mockJavaUtilJarClasses() throws Exception
   {
      final File testFile = new File("test.jar");
      final String mainClassName = "test.Main";

      new Expectations() {
         @Mocked JarFile mockFile;
         @Mocked Manifest mockManifest;
         @Mocked Attributes mockAttributes;
         @Mocked Enumeration<JarEntry> mockEntries;
         @Mocked JarEntry mockEntry;

         {
            new JarFile(testFile);
            mockFile.getManifest(); result = mockManifest;
            mockManifest.getMainAttributes(); result = mockAttributes;
            mockAttributes.getValue(Attributes.Name.MAIN_CLASS); result = mainClassName;
            mockFile.entries(); result = mockEntries;

            mockEntries.hasMoreElements(); result = true;
            mockEntries.nextElement(); result = mockEntry;
            mockEntry.getName(); result = "test/Main$Inner.class";

            mockEntries.hasMoreElements(); result = true;
            mockEntries.nextElement(); result = mockEntry;
            mockEntry.getName(); result = "test/Main.class";

            mockEntries.hasMoreElements(); result = false;
         }
      };

      List<String> fileNames = new ArrayList<String>();
      String mainClassFromJar = readMainClassAndFileNamesFromJar(testFile, fileNames);

      assertEquals(mainClassName, mainClassFromJar);
      assertEquals(fileNames, asList("test/Main$Inner.class", "test/Main.class"));
   }
}
