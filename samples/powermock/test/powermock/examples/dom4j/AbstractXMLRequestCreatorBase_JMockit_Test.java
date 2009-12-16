/*
 * JMockit Samples
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
package powermock.examples.dom4j;

import org.junit.*;

import mockit.*;

import org.dom4j.*;
import org.dom4j.tree.*;
import static org.junit.Assert.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/dom4j/src/test/java/org/powermock/examples/dom4j/AbstractXMLRequestCreatorBaseTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(AbstractNode.class)
public final class AbstractXMLRequestCreatorBase_JMockit_Test
{
   private AbstractXMLRequestCreatorBase tested;

   @Mocked private Document documentMock;
   @Mocked private Element rootElementMock;
   @Mocked private Element headerElementMock;
   @Mocked private Element bodyElementMock;

   @Before
   public void setUp()
   {
      tested = new AbstractXMLRequestCreatorBase()
      {
         @Override
         protected void createBody(Element body, String... parameters) {}
      };
   }

   @Test
   public void testConvertDocumentToByteArray() throws Exception
   {
      // Create a fake document.
      Document document = DocumentHelper.createDocument();
      Element root = document.addElement("ListExecutionContexts");
      root.addAttribute("id", "2");

      // Perform the test.
      byte[] array = tested.convertDocumentToByteArray(document);

      assertNotNull(array);
      assertEquals(70, array.length);
   }

   @Test
   public void testCreateRequest() throws Exception
   {
      final String[] params = {"String1", "String2"};
      final byte[] expected = {42};

      new Expectations(tested)
      {
         final DocumentHelper unused = null;

         {
            DocumentHelper.createDocument(); result = documentMock;
            documentMock.addElement(XMLProtocol.ENCODE_ELEMENT); result = rootElementMock;

            rootElementMock.addElement(XMLProtocol.HEADER_ELEMENT); result = headerElementMock;
            tested.generateRandomId();
            String id = "213"; result = id;
            headerElementMock.addAttribute(XMLProtocol.HEADER_MSG_ID_ATTRIBUTE, id);

            rootElementMock.addElement(XMLProtocol.BODY_ELEMENT); result = bodyElementMock;
            tested.createBody(bodyElementMock, params);

            tested.convertDocumentToByteArray(documentMock); result = expected;
         }
      };

      byte[] actual = tested.createRequest(params);

      assertArrayEquals(expected, actual);
   }
}
