/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import mockit.*;
import org.hibernate.*;
import org.hibernate.cfg.*;

@MockClass(realClass = Configuration.class, instantiation = Instantiation.PerMockSetup, stubs = "<clinit>")
public final class ConfigurationEmul extends Configuration
{
   @Mock @Override
   public void reset()
   {
   }

   @Mock @Override
   public Configuration configure()
   {
      return this;
   }

   @Mock @Override
   public SessionFactory buildSessionFactory()
   {
      return new SessionFactoryEmul();
   }
}
