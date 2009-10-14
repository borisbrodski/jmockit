/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package powermock.examples.dom4j;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;

import org.dom4j.*;
import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.expectLastCall;
import org.powermock.api.easymock.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.core.classloader.annotations.*;
import org.powermock.modules.junit4.*;

/**
 * Unit test for the {@link AbstractXMLRequestCreatorBase} class.
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.dom4j.tree.AbstractNode")
@PrepareForTest({DocumentHelper.class})
public class AbstractXMLRequestCreatorBaseTest
{
   private AbstractXMLRequestCreatorBase tested;

   private Document documentMock;
   private Element rootElementMock;
   private Element headerElementMock;
   private Element bodyElementMock;

   @Before
   public void setUp()
   {
      tested = new AbstractXMLRequestCreatorBase()
      {
         @Override
         protected void createBody(Element body, String... parameters) {}
      };

      PowerMock.niceReplayAndVerify();

      documentMock = createMock(Document.class);
      rootElementMock = createMock(Element.class);
      headerElementMock = createMock(Element.class);
      bodyElementMock = createMock(Element.class);
   }

   @Test
   @SuppressStaticInitializationFor
   public void testConvertDocumentToByteArray() throws Exception
   {
      // Create a fake document.
      Document document = DocumentHelper.createDocument();
      Element root = document.addElement("ListExecutionContexts");
      root.addAttribute("id", "2");
      replayAll();

      // Perform the test.
      byte[] array = tested.convertDocumentToByteArray(document);

      verifyAll();
      assertNotNull(array);
      assertEquals(70, array.length);
   }

   /**
    * Happy-flow test for the {@link AbstractXMLRequestCreatorBase#createRequest(String[])} method.
    */
   @Test
   public void testCreateRequest() throws Exception
   {
      tested = createPartialMock(AbstractXMLRequestCreatorBase.class, "convertDocumentToByteArray",
         "createBody", "generateRandomId");
      mockStatic(DocumentHelper.class);

      // Expectations:
      String[] params = {"String1", "String2"};
      byte[] expected = {42};

      expect(DocumentHelper.createDocument()).andReturn(documentMock);
      expect(documentMock.addElement(XMLProtocol.ENCODE_ELEMENT)).andReturn(rootElementMock);
      expect(rootElementMock.addElement(XMLProtocol.HEADER_ELEMENT)).andReturn(headerElementMock);
      String id = "213";
      expect(tested.generateRandomId()).andReturn(id);
      expect(headerElementMock.addAttribute(XMLProtocol.HEADER_MSG_ID_ATTRIBUTE, id))
         .andReturn(null);
      expect(rootElementMock.addElement(XMLProtocol.BODY_ELEMENT)).andReturn(bodyElementMock);
      tested.createBody(bodyElementMock, params);
      expectLastCall().times(1);
      expect(tested.convertDocumentToByteArray(documentMock)).andReturn(expected);
      replayAll();

      byte[] actual = tested.createRequest(params);

      verifyAll();
      assertArrayEquals(expected, actual);
   }
}
