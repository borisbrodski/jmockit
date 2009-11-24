/*
 * JMockit Expectations
 * Copyright (c) 2009 JMockit Developers
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
package mockit;

import java.util.Collection;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.*;

import mockit.integration.junit4.JMockitTest;

public class DelegateInvocationTest extends JMockitTest {
	static class Collaborator {
		Collaborator() {
		}

		Collaborator(int i) {
		}

		int getValue() {
			return -1;
		}

		String doSomething(boolean b, int[] i, String s) {
			return s + b + i[0];
		}

		static boolean staticMethod() {
			return true;
		}

		static boolean staticMethod(int i) {
			return i > 0;
		}

		native long nativeMethod(boolean b);

		final char finalMethod() {
			return 's';
		}

		private float privateMethod() {
			return 1.2F;
		}

		void addElements(Collection<String> elements) {
			elements.add("one element");
		}
	}

	@Test
	public void testDelegateForStaticMethodWithContext() {
		new NonStrictExpectations() {
			final Collaborator unused = null;

			{
				Collaborator.staticMethod(-1);
				returns(new Delegate() {
					boolean staticMethod(Invocation context, int i) {
						return context.getCurrentInvocation() + i > 0;
					}
				});
			}
		};

		assertFalse(Collaborator.staticMethod(-1));
		assertTrue(Collaborator.staticMethod(-1));
	}

	@Test
	public void testDelegateForStaticMethodMultiWithContext() {
		new NonStrictExpectations() {
			final Collaborator unused = null;

			{
				Collaborator.staticMethod(-1);
				returns(new Delegate() {
					boolean staticMethod(Invocation context, int i) {
						return context.getCurrentInvocation() + i > 0;
					}

					boolean otherMethod(float f) {
						return f > 0;
					}
				});
			}
		};

		assertFalse(Collaborator.staticMethod(-1));
		assertTrue(Collaborator.staticMethod(-1));
	}

	static class CollaboratorNew {
		CollaboratorNew() {
		}

		static int staticMethod1(Object o, Exception e) {
			return -1;
		}
	}

	@Test
	public void testDelegateForStaticMethodMultiWithObject() {
		new NonStrictExpectations() {
			final CollaboratorNew unused = null;

			{
				CollaboratorNew.staticMethod1(any, null);
				returns(new Delegate() {
					int staticMethod1(Object o, Exception e) {
						return 1;
					}
				});
			}
		};

		assertThat(
				CollaboratorNew.staticMethod1(new Object(), new Exception()),
				equalTo(1));
		assertThat(
				CollaboratorNew.staticMethod1(new Object(), new Exception()),
				equalTo(1));
	}

	@Test
	public void testDelegateForStaticMethodMultiWithObjectSig() {
		new NonStrictExpectations() {
			final CollaboratorNew unused = null;

			{
				CollaboratorNew.staticMethod1(any, null);
	            repeatsAtMost(1);
				returns(new Delegate() {
					int staticMethod1(Invocation invocation, Object o,
							Exception e) {
						invocation.setMinInvocation(2);
						invocation.setMaxInvocation(2);
						return invocation.getCurrentInvocation();
					}
				});
			}
		};

		assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(1));
		assertThat(CollaboratorNew.staticMethod1(null, null), equalTo(2));
		
	}

}
