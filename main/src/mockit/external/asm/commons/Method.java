/*
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package mockit.external.asm.commons;

import java.util.*;

import mockit.external.asm.*;

/**
 * A named method descriptor.
 * 
 * @author Juozas Baliuka
 * @author Chris Nokleberg
 * @author Eric Bruneton
 */
public final class Method
{
    /**
     * The method name.
     */
    private final String name;

    /**
     * The method descriptor.
     */
    private final String desc;

    /**
     * Maps primitive Java type names to their descriptors.
     */
    private static final Map<String, String> DESCRIPTORS;

    static {
        DESCRIPTORS = new HashMap<String, String>();
        DESCRIPTORS.put("void", "V");
        DESCRIPTORS.put("byte", "B");
        DESCRIPTORS.put("char", "C");
        DESCRIPTORS.put("double", "D");
        DESCRIPTORS.put("float", "F");
        DESCRIPTORS.put("int", "I");
        DESCRIPTORS.put("long", "J");
        DESCRIPTORS.put("short", "S");
        DESCRIPTORS.put("boolean", "Z");
    }

    /**
     * Creates a new Method.
     * 
     * @param name the method's name.
     * @param desc the method's descriptor.
     */
    public Method(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    /**
     * Creates a new Method.
     * 
     * @param name the method's name.
     * @param returnType the method's return type.
     * @param argumentTypes the method's argument types.
     */
    public Method(String name, Type returnType, Type[] argumentTypes)
    {
        this(name, Type.getMethodDescriptor(returnType, argumentTypes));
    }

    /**
     * Returns a Method corresponding to the given Java method declaration.
     * 
     * @param method a Java method declaration, without argument names, of the
     *        form "returnType name (argumentType1, ... argumentTypeN)", where
     *        the types are in plain Java (e.g. "int", "float",
     *        "java.util.List", ...).
     * @return a Method corresponding to the given Java method declaration.
     * @throws IllegalArgumentException if <code>method</code> could not get
     *         parsed.
     */
    public static Method getMethod(String method)
            throws IllegalArgumentException
    {
        int space = method.indexOf(' ');
        int start = method.indexOf('(', space) + 1;
        int end = method.indexOf(')', start);
        if (space == -1 || start == -1 || end == -1) {
            throw new IllegalArgumentException();
        }

        String returnType = method.substring(0, space);
        String methodName = method.substring(space + 1, start - 1).trim();
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        int p;
        do {
            p = method.indexOf(',', start);
            if (p == -1) {
                sb.append(map(method.substring(start, end).trim()));
            } else {
                sb.append(map(method.substring(start, p).trim()));
                start = p + 1;
            }
        } while (p != -1);
        sb.append(')');
        sb.append(map(returnType));
        return new Method(methodName, sb.toString());
    }

    private static String map(String type) {
        if (type.length() == 0) {
            return type;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        while ((index = type.indexOf("[]", index) + 1) > 0) {
            sb.append('[');
        }

        String t = type.substring(0, type.length() - sb.length() * 2);
        String desc = DESCRIPTORS.get(t);
        if (desc != null) {
            sb.append(desc);
        } else {
            sb.append('L');
            if (t.indexOf('.') < 0) {
                sb.append("java/lang/").append(t);
            } else {
                sb.append(t.replace('.', '/'));
            }
            sb.append(';');
        }
        return sb.toString();
    }

    /**
     * Returns the name of the method described by this object.
     * 
     * @return the name of the method described by this object.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the descriptor of the method described by this object.
     * 
     * @return the descriptor of the method described by this object.
     */
    public String getDescriptor() {
        return desc;
    }

    /**
     * Returns the return type of the method described by this object.
     * 
     * @return the return type of the method described by this object.
     */
    public Type getReturnType() {
        return Type.getReturnType(desc);
    }

    /**
     * Returns the argument types of the method described by this object.
     * 
     * @return the argument types of the method described by this object.
     */
    public Type[] getArgumentTypes() {
        return Type.getArgumentTypes(desc);
    }

    public String toString() {
        return name + desc;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Method)) {
            return false;
        }
        Method other = (Method) o;
        return name.equals(other.name) && desc.equals(other.desc);
    }

    public int hashCode() {
        return name.hashCode() ^ desc.hashCode();
    }
}
