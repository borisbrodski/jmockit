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
package orderMngr.service.persistence;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;

public final class Persistence
{
   private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
   private static final SessionFactory sessionFactory;

   static {
      //noinspection CatchGenericClass,OverlyBroadCatchBlock
      try {
         sessionFactory = new Configuration().configure().buildSessionFactory();
      }
      catch (Error e) {
         System.err.println(e);
         throw e;
      }
      catch (RuntimeException e) {
         System.err.println(e);
         throw e;
      }
   }

   // Entity life-cycle methods -------------------------------------------------------------------

   public static <T> T load(Class<T> entityClass, Serializable entityId)
   {
      return (T) session().load(entityClass, entityId);
   }

   public static boolean exists(Class<?> entityClass, Serializable entityId)
   {
      return session().get(entityClass, entityId) != null;
   }

   public static void unload(Object entity)
   {
      session().evict(entity);
   }

   public static void persist(Object entityData)
   {
      session().save(entityData);
   }

   public static void delete(Object entity)
   {
      session().delete(entity);
   }

   private static Session session()
   {
      Session session = currentSession.get();

      if (session == null) {
         session = sessionFactory.openSession();
         currentSession.set(session);
      }

      return session;
   }

   // Query methods -------------------------------------------------------------------------------

   public static <E> List<E> find(String ql, Object... args)
   {
      Query query = newQuery(ql, args);
      return query.list();
   }

   private static Query newQuery(String ql, Object... args)
   {
      Query query = session().createQuery(ql);

      for (int i = 0; i < args.length; i++) {
         query.setParameter(i, args[i]);
      }

      return query;
   }

   public static boolean exists(String ql, Object... args)
   {
      Query query = newQuery(ql, args);
      query.setMaxResults(1);
      return query.iterate().hasNext();
   }

   // Methods related to the Unit of Work and the Transaction -------------------------------------

   public static void persistPendingChanges()
   {
      session().flush();
      session().beginTransaction();
   }

   public static void clearUnitOfWork()
   {
      session().clear();
   }

   public static void closeUnitOfWork()
   {
      Session session = currentSession.get();

      if (session != null) {
         session.close();
         currentSession.set(null);
      }
   }

   public static void beginTransaction()
   {
      session().beginTransaction();
   }

   public static void rollbackTransaction()
   {
      session().getTransaction().rollback();
   }

   // Miscelaneous methods ------------------------------------------------------------------------

   public static Serializable entityId(Object entity)
   {
      return session().getIdentifier(entity);
   }
}
