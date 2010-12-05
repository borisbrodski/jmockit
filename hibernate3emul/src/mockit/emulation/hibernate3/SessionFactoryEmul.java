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
import java.sql.*;
import java.util.*;
import javax.naming.*;

import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.engine.*;
import org.hibernate.metadata.*;
import org.hibernate.stat.*;

final class SessionFactoryEmul implements SessionFactory
{
   public Session openSession(Connection connection)
   {
      return openSession();
   }

   public Session openSession(Interceptor interceptor)
   {
      return openSession();
   }

   public Session openSession(Connection connection, Interceptor interceptor)
   {
      return null;
   }

   public Session openSession()
   {
      return new ClassicSessionEmul(this);
   }

   public Session getCurrentSession()
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public ClassMetadata getClassMetadata(Class persistentClass)
   {
      return null;
   }

   public ClassMetadata getClassMetadata(String entityName)
   {
      return null;
   }

   public CollectionMetadata getCollectionMetadata(String roleName)
   {
      return null;
   }

   public Map<?, ?> getAllClassMetadata()
   {
      return null;
   }

   public Map<?, ?> getAllCollectionMetadata()
   {
      return null;
   }

   public Statistics getStatistics()
   {
      return null;
   }

   public void close()
   {
   }

   public boolean isClosed()
   {
      return false;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public void evict(Class persistentClass)
   {
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public void evict(Class persistentClass, Serializable id)
   {
   }

   public void evictEntity(String entityName)
   {
   }

   public void evictEntity(String entityName, Serializable id)
   {
   }

   public void evictCollection(String roleName)
   {
   }

   public void evictCollection(String roleName, Serializable id)
   {
   }

   public void evictQueries()
   {
   }

   public void evictQueries(String cacheRegion)
   {
   }

   public StatelessSession openStatelessSession()
   {
      return null;
   }

   public StatelessSession openStatelessSession(Connection connection)
   {
      return null;
   }

   public Set<?> getDefinedFilterNames()
   {
      return null;
   }

   public FilterDefinition getFilterDefinition(String filterName)
   {
      return null;
   }

   public Reference getReference()
   {
      return null;
   }
}
