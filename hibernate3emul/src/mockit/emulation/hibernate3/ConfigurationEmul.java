package mockit.emulation.hibernate3;

import mockit.*;
import org.hibernate.*;
import org.hibernate.cfg.*;

@MockClass(realClass = Configuration.class)
public final class ConfigurationEmul extends Configuration
{
   @Mock
   public static void $clinit()
   {
   }

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
