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

/** 
 * This is a context object representing the current invocation.
 * When setup as a first parameter on a {@linkplain Delegate} method, the method receives and able to 
 * manipulate the context.
 * 
 */
public class Invocation {
	/** 
	 * Current invocation count. The first invocation start at 1.
	 */
	final protected int currentInvocation;
	
	/**
	 * Minimum invocation
	 */
	protected int minInvocations;
	
	/**
	 * Maximum invocation (-1 indicates unlimited)
	 */
	protected int maxInvocations;
	
	public Invocation(int currentInvocation, int minInvocations, int maxInvocations){
		this.currentInvocation=currentInvocation;
		this.minInvocations=minInvocations;
		this.maxInvocations=maxInvocations;
	}

	public int getCurrentInvocation() {
		return currentInvocation;
	}
	
	public int getMinInvocations() {
		return minInvocations;
	}

	public void setMinInvocations(int minInvocations) {
		this.minInvocations = minInvocations;
	}

	public int getMaxInvocations() {
		return maxInvocations;
	}

	public void setMaxInvocations(int maxInvocations) {
		this.maxInvocations = maxInvocations;
	}
}