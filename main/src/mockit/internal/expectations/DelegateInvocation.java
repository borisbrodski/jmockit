package mockit.internal.expectations;

import mockit.Invocation;

public class DelegateInvocation extends Invocation {

   public DelegateInvocation(int invocationCount, int minInvocations, int maxInvocations) {
      super();
      this.invocationCount = invocationCount;
      this.minInvocations = minInvocations;
      this.maxInvocations = maxInvocations;
   }

}
