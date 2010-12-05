package org.hibernate.cfg;

import org.hibernate.*;

public class Configuration
{
   static
   {
      // do nothing
   }

   public Configuration configure() { return this; }
   public SessionFactory buildSessionFactory() { return null; }
   public void reset() {}
}
