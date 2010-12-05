/*
 * JMockit Hibernate 3 Emulation
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
package mockit.emulation.hibernate3;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.type.*;

final class ClassicSessionEmul extends SessionEmul implements Session
{
   ClassicSessionEmul(SessionFactory sessionFactory)
   {
      super(sessionFactory, null);
   }

   public Object saveOrUpdateCopy(Object object)
   {
      return null;
   }

   public Object saveOrUpdateCopy(Object object, Serializable id)
   {
      return null;
   }

   public Object saveOrUpdateCopy(String entityName, Object object)
   {
      return null;
   }

   public Object saveOrUpdateCopy(String entityName, Object object, Serializable id)
   {
      return null;
   }

   public List<?> find(String query)
   {
      return null;
   }

   public List<?> find(String query, Object value, Type type)
   {
      return null;
   }

   public List<?> find(String query, Object[] values, Type[] types)
   {
      return null;
   }

   public Iterator<?> iterate(String query)
   {
      return null;
   }

   public Iterator<?> iterate(String query, Object value, Type type)
   {
      return null;
   }

   public Iterator<?> iterate(String query, Object[] values, Type[] types)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter, Object value, Type type)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter, Object[] values, Type[] types)
   {
      return null;
   }

   public int delete(String query)
   {
      return 0;
   }

   public int delete(String query, Object value, Type type)
   {
      return 0;
   }

   public int delete(String query, Object[] values, Type[] types)
   {
      return 0;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query createSQLQuery(String sql, String returnAlias, Class returnClass)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query createSQLQuery(String sql, String[] returnAliases, Class[] returnClasses)
   {
      return null;
   }

   public void save(Object object, Serializable id)
   {
   }

   public void save(String entityName, Object object, Serializable id)
   {
   }

   public void update(Object object, Serializable id)
   {
   }

   public void update(String entityName, Object object, Serializable id)
   {
   }
}