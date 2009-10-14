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
package powermock.examples.mockpolicy.policy;

import java.lang.reflect.Method;

import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.mockpolicies.MockPolicyClassLoadingSettings;
import org.powermock.mockpolicies.MockPolicyInterceptionSettings;
import org.powermock.reflect.Whitebox;

import powermock.examples.mockpolicy.nontest.Dependency;
import powermock.examples.mockpolicy.nontest.domain.DataObject;

/**
 * A simple mock policy whose purpose is to intercept calls to {@link Dependency#getData()} and
 * return a mock.
 */
public class MyCustomMockPolicy implements PowerMockPolicy
{
   /**
    * Add the {@link Dependency} to the list of classes that should be loaded by the mock
    * class-loader.
    */
   public void applyClassLoadingPolicy(MockPolicyClassLoadingSettings settings)
   {
      settings.addFullyQualifiedNamesOfClassesToLoadByMockClassloader(Dependency.class.getName());
   }

   /**
    * Every time the {@link Dependency#getData()} method is invoked we return a custom instance of a
    * {@link DataObject}.
    */
   public void applyInterceptionPolicy(MockPolicyInterceptionSettings settings)
   {
      Method getDataMethod = Whitebox.getMethod(Dependency.class);
      DataObject dataObject = new DataObject("Policy generated data object");
      settings.addSubtituteReturnValue(getDataMethod, dataObject);
   }
}
