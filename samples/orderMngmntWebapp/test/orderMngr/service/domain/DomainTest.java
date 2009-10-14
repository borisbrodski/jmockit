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
package orderMngr.service.domain;

import java.io.*;

import orderMngr.service.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;

public class DomainTest
{
   @Before
   public final void setUp()
   {
      Persistence.beginTransaction();
   }

   @After
   public final void tearDown()
   {
      Persistence.persistPendingChanges();
      Persistence.rollbackTransaction();
      Persistence.closeUnitOfWork();
   }

   protected final void assertPersisted(Serializable entityId, Object entity)
   {
      Persistence.unload(entity);
      Object loadedEntity = Persistence.load(entity.getClass(), entityId);
      assertEquals(entity, loadedEntity);
   }

   protected final void assertUpdated(Object entity)
   {
      Persistence.persistPendingChanges();
      assertPersisted(Persistence.entityId(entity), entity);
   }

   protected final void assertDeleted(Object entity)
   {
      assertFalse(
         "Entity " + entity + " not actually deleted from persistent store", 
         Persistence.exists(entity.getClass(), Persistence.entityId(entity)));
   }
}
