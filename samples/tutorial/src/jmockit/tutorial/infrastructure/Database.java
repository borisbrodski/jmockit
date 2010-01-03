/*
 * JMockit Samples
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
package jmockit.tutorial.infrastructure;

import java.util.*;
import javax.persistence.*;

public final class Database
{
   private static final EntityManagerFactory entityManagerFactory =
      Persistence.createEntityManagerFactory("AppPersistenceUnit");
   private static final ThreadLocal<EntityManager> workUnit = new ThreadLocal<EntityManager>();

   private Database() {}

   public static <E> E find(Class<E> entityClass, Object entityId)
   {
      E entity = workUnit().find(entityClass, entityId);
      return entity;
   }

   public static <E> List<E> find(String ql, Object... args)
   {
      Query query = workUnit().createQuery(ql);
      int position = 1;

      for (Object arg : args) {
         query.setParameter(position, arg);
         position++;
      }

      List<E> result = query.getResultList();

      return result;
   }

   public static void persist(Object data)
   {
      // Persist the data of a given transient domain entity object, using JPA.
      // (In a web app, this could be scoped to the HTTP request/response cycle, which normally runs
      // entirely in a single thread - a custom javax.servlet.Filter could close the thread-bound
      // EntityManager.)
      workUnit().persist(data);
   }

   public static void remove(Object persistentEntity)
   {
      workUnit().remove(persistentEntity);
   }

   private static EntityManager workUnit()
   {
      EntityManager wu = workUnit.get();

      if (wu == null) {
         wu = entityManagerFactory.createEntityManager();
         workUnit.set(wu);
      }

      return wu;
   }
}
