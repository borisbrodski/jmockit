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
package org.jmock.samples.cache;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class Cache_JMockit_Test
{
   private static final String KEY = "cachedItem";
   private static final Object VALUE = new Object();

   @Mocked private Clock clock;
   @Mocked private Loader loader;
   @NonStrict private CacheReloadPolicy reloadPolicy;

   @Test
   public void returnsCachedObjectWithinTimeout()
   {
      // Note that with JMockit, the Cache class could obtain any or all of its dependencies
      // internally instead of using "constructor injection" as shown here. Examples of such
      // alternative mechanisms include the use of the design patterns Abstract Factory, Factory
      // Method, Singleton, and Service Locator. However, using such a pattern only makes sense when
      // there is a good reason for separating interface and implementation, such as when multiple
      // implementations can realistically exist for a given abstraction (separated interface), AND
      // client code is expected to be able to select or switch between such implementations at
      // runtime. If that is not the case (and it often isn't), then it may be better to have no
      // separation between interface and implementation, and/or simply use the "new" operator to
      // obtain dependencies wherever they are needed. (Consider how an application typically uses
      // List and ArrayList: declaring all variables/fields/parameters of the interface type "List"
      // -- the "program to an interface, not an implementation" principle -- while referencing the
      // implementation type in "new ArrayList()" expressions which "obtain the dependency".)
      Cache cache = new Cache(clock, reloadPolicy, loader);

      final long loadTime = 10;

      new Expectations() // (this is "JMockit Expectations", not the jMock version)
      {
         {
            clock.time(); result = loadTime;
            loader.load(KEY); result = VALUE;
         }
      };

      Object actualValueFromFirstLookup = cache.lookup(KEY);

      new Expectations()
      {
         final long fetchTime = 200;

         {
            clock.time(); result = fetchTime;
            reloadPolicy.shouldReload(loadTime, fetchTime); result = false;
         }
      };

      Object actualValueFromSecondLookup = cache.lookup(KEY);

      assertSame("should be loaded object", VALUE, actualValueFromFirstLookup);
      assertSame("should be cached object", VALUE, actualValueFromSecondLookup);
   }

   @Test
   public void returnsCachedObjectWithinTimeout_usingTwoExpectationsBlocksForOneReplayPhase()
   {
      Cache cache = new Cache(clock, reloadPolicy, loader);
      final long loadTime = 10;

      new Expectations()
      {
         {
            clock.time(); result = loadTime;
            loader.load(KEY); result = VALUE;
         }
      };

      new Expectations()
      {
         final long fetchTime = 200;

         {
            clock.time(); result = fetchTime;
            reloadPolicy.shouldReload(loadTime, fetchTime); result = false;
         }
      };

      // Replay phase:
      Object actualValueFromFirstLookup = cache.lookup(KEY);
      Object actualValueFromSecondLookup = cache.lookup(KEY);

      assertSame("should be loaded object", VALUE, actualValueFromFirstLookup);
      assertSame("should be cached object", VALUE, actualValueFromSecondLookup);
   }

   @Test
   public void returnsCachedObjectWithinTimeout_usingSingleExpectationsBlock()
   {
      Cache cache = new Cache(clock, reloadPolicy, loader);
      final long loadTime = 10;

      new Expectations()
      {
         // For first cache lookup:
         {
            clock.time(); result = loadTime;
            loader.load(KEY); result = VALUE;
         }

         // For second cache lookup:
         final long fetchTime = 200;

         {
            clock.time(); result = fetchTime;
            reloadPolicy.shouldReload(loadTime, fetchTime); result = false;
         }
      };

      Object actualValueFromFirstLookup = cache.lookup(KEY);
      Object actualValueFromSecondLookup = cache.lookup(KEY);

      assertSame("should be loaded object", VALUE, actualValueFromFirstLookup);
      assertSame("should be cached object", VALUE, actualValueFromSecondLookup);
   }
}
