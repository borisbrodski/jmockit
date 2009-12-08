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
 * A MethodAdapter with convenient methods to generate code. For example, using this adapter, the
 * class below
 * <pre>
 * public class Example {
 *     public static void main(String[] args) {
 *         System.out.println(&quot;Hello world!&quot;);
 *     }
 * }
 * </pre>
 * 
 * can be generated as follows:
 * 
 * <pre>
 * ClassWriter cw = new ClassWriter(true);
 * cw.visit(V1_1, ACC_PUBLIC, &quot;Example&quot;, null, &quot;java/lang/Object&quot;, null);
 * 
 * Method m = Method.getMethod(&quot;void &lt;init&gt; ()&quot;);
 * GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
 * mg.loadThis();
 * mg.invokeConstructor(Type.getType(Object.class), m);
 * mg.returnValue();
 * mg.endMethod();
 * 
 * m = Method.getMethod(&quot;void main (String[])&quot;);
 * mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
 * mg.getStatic(Type.getType(System.class), &quot;out&quot;, Type.getType(PrintStream.class));
 * mg.push(&quot;Hello world!&quot;);
 * mg.invokeVirtual(Type.getType(PrintStream.class), Method.getMethod(&quot;void println (String)&quot;));
 * mg.returnValue();
 * mg.endMethod();
 * 
 * cw.visitEnd();
 * </pre>
 * 
 * @author Juozas Baliuka
 * @author Chris Nokleberg
 * @author Eric Bruneton
 */
public final class GeneratorAdapter extends LocalVariablesSorter
{
    private static final Type BYTE_TYPE = Type.getType("Ljava/lang/Byte;");
    private static final Type BOOLEAN_TYPE = Type.getType("Ljava/lang/Boolean;");
    private static final Type SHORT_TYPE = Type.getType("Ljava/lang/Short;");
    private static final Type CHARACTER_TYPE = Type.getType("Ljava/lang/Character;");
    private static final Type INTEGER_TYPE = Type.getType("Ljava/lang/Integer;");
    private static final Type FLOAT_TYPE = Type.getType("Ljava/lang/Float;");
    private static final Type LONG_TYPE = Type.getType("Ljava/lang/Long;");
    private static final Type DOUBLE_TYPE = Type.getType("Ljava/lang/Double;");
    private static final Type NUMBER_TYPE = Type.getType("Ljava/lang/Number;");
    private static final Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");
    private static final Method BOOLEAN_VALUE = Method.getMethod("boolean booleanValue()");
    private static final Method CHAR_VALUE = Method.getMethod("char charValue()");
    private static final Method INT_VALUE = Method.getMethod("int intValue()");
    private static final Method FLOAT_VALUE = Method.getMethod("float floatValue()");
    private static final Method LONG_VALUE = Method.getMethod("long longValue()");
    private static final Method DOUBLE_VALUE = Method.getMethod("double doubleValue()");

    /**
     * Constant for the {@link #math math} method.
     */
    public static final int AND = Opcodes.IAND;

    /**
     * Constant for the {@link #math math} method.
     */
    public static final int OR = Opcodes.IOR;

    /**
     * Return type of the method visited by this adapter.
     */
    private final Type returnType;

    /**
     * Types of the local variables of the method visited by this adapter.
     */
    private final List<Type> localTypes;

    /**
     * Creates a new instance.
     * 
     * @param mv the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param desc the method's descriptor (see {@link Type Type}).
     */
    public GeneratorAdapter(MethodVisitor mv, int access, String desc)
    {
        super(access, desc, mv);
        returnType = Type.getReturnType(desc);
        localTypes = new ArrayList<Type>();
    }

   // ------------------------------------------------------------------------
    // Instructions to push constants on the stack
    // ------------------------------------------------------------------------

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(boolean value) {
        push(value ? 1 : 0);
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(int value) {
        if (value >= -1 && value <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(long value) {
        if (value == 0L || value == 1L) {
            mv.visitInsn(Opcodes.LCONST_0 + (int) value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(float value) {
        int bits = Float.floatToIntBits(value);
        if (bits == 0L || bits == 0x3f800000 || bits == 0x40000000) { // 0..2
            mv.visitInsn(Opcodes.FCONST_0 + (int) value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(double value) {
        long bits = Double.doubleToLongBits(value);
        if (bits == 0L || bits == 0x3ff0000000000000L) { // +0.0d and 1.0d
            mv.visitInsn(Opcodes.DCONST_0 + (int) value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack. May be <tt>null</tt>.
     */
    public void push(String value) {
        if (value == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    /**
     * Generates the instruction to push the given value on the stack.
     * 
     * @param value the value to be pushed on the stack.
     */
    public void push(Type value) {
        if (value == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    // ------------------------------------------------------------------------
    // Instructions to load and store method arguments
    // ------------------------------------------------------------------------

   // ------------------------------------------------------------------------
    // Instructions to load and store local variables
    // ------------------------------------------------------------------------

    /**
     * Creates a new local variable of the given type.
     * 
     * @param type the type of the local variable to be created.
     * @return the identifier of the newly created local variable.
     */
    public int newLocal(Type type) {
        int local = newLocal(type.getSize());
        setLocalType(local, type);
        return local;
    }

   /**
     * Sets the current type of the given local variable.
     * 
     * @param local a local variable identifier, as returned by {@link #newLocal
     *        newLocal}.
     * @param type the type of the value being stored in the local variable
     */
    private void setLocalType(int local, Type type) {
        int index = local - firstLocal;

        while (localTypes.size() < index + 1) {
           localTypes.add(null);
        }

        localTypes.set(index, type);
    }

   /**
     * Generates the instruction to load an element from an array.
     * 
     * @param type the type of the array element to be loaded.
     */
    public void arrayLoad(Type type) {
        mv.visitInsn(type.getOpcode(Opcodes.IALOAD));
    }

   // ------------------------------------------------------------------------
    // Instructions to manage the stack
    // ------------------------------------------------------------------------

    /**
     * Generates a POP instruction.
     */
    public void pop() {
        mv.visitInsn(Opcodes.POP);
    }

    /**
     * Generates a POP2 instruction.
     */
    public void pop2() {
        mv.visitInsn(Opcodes.POP2);
    }

   /**
     * Generates a DUP_X1 instruction.
     */
    public void dupX1() {
        mv.visitInsn(Opcodes.DUP_X1);
    }

    /**
     * Generates a DUP_X2 instruction.
     */
    public void dupX2() {
        mv.visitInsn(Opcodes.DUP_X2);
    }

    /**
     * Generates a DUP2_X1 instruction.
     */
    public void dup2X1() {
        mv.visitInsn(Opcodes.DUP2_X1);
    }

    /**
     * Generates a DUP2_X2 instruction.
     */
    public void dup2X2() {
        mv.visitInsn(Opcodes.DUP2_X2);
    }

    /**
     * Generates a SWAP instruction.
     */
    public void swap() {
        mv.visitInsn(Opcodes.SWAP);
    }

    /**
     * Generates the instructions to swap the top two stack values.
     * 
     * @param prev type of the top - 1 stack value.
     * @param type type of the top stack value.
     */
    public void swap(Type prev, Type type) {
        if (type.getSize() == 1) {
            if (prev.getSize() == 1) {
                swap(); // same as dupX1(), pop();
            } else {
                dupX2();
                pop();
            }
        } else {
            if (prev.getSize() == 1) {
                dup2X1();
                pop2();
            } else {
                dup2X2();
                pop2();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Instructions to do mathematical and logical operations
    // ------------------------------------------------------------------------

    /**
     * Generates the instruction to do the specified mathematical or logical
     * operation.
     * 
     * @param op a mathematical or logical operation. Must be one of ADD, SUB,
     *        MUL, DIV, REM, NEG, SHL, SHR, USHR, AND, OR, XOR.
     * @param type the type of the operand(s) for this operation.
     */
    public void math(int op, Type type) {
        mv.visitInsn(type.getOpcode(op));
    }

    /**
     * Generates the instructions to compute the bitwise negation of the top
     * stack value.
     */
    public void not() {
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IXOR);
    }

   /**
     * Generates the instructions to cast a numerical value from one type to
     * another.
     * 
     * @param from the type of the top stack value
     * @param to the type into which this value must be cast.
     */
    public void cast(Type from, Type to) {
        if (from != to) {
            if (from == Type.DOUBLE_TYPE) {
                if (to == Type.FLOAT_TYPE) {
                    mv.visitInsn(Opcodes.D2F);
                } else if (to == Type.LONG_TYPE) {
                    mv.visitInsn(Opcodes.D2L);
                } else {
                    mv.visitInsn(Opcodes.D2I);
                    cast(Type.INT_TYPE, to);
                }
            } else if (from == Type.FLOAT_TYPE) {
                if (to == Type.DOUBLE_TYPE) {
                    mv.visitInsn(Opcodes.F2D);
                } else if (to == Type.LONG_TYPE) {
                    mv.visitInsn(Opcodes.F2L);
                } else {
                    mv.visitInsn(Opcodes.F2I);
                    cast(Type.INT_TYPE, to);
                }
            } else if (from == Type.LONG_TYPE) {
                if (to == Type.DOUBLE_TYPE) {
                    mv.visitInsn(Opcodes.L2D);
                } else if (to == Type.FLOAT_TYPE) {
                    mv.visitInsn(Opcodes.L2F);
                } else {
                    mv.visitInsn(Opcodes.L2I);
                    cast(Type.INT_TYPE, to);
                }
            } else {
                if (to == Type.BYTE_TYPE) {
                    mv.visitInsn(Opcodes.I2B);
                } else if (to == Type.CHAR_TYPE) {
                    mv.visitInsn(Opcodes.I2C);
                } else if (to == Type.DOUBLE_TYPE) {
                    mv.visitInsn(Opcodes.I2D);
                } else if (to == Type.FLOAT_TYPE) {
                    mv.visitInsn(Opcodes.I2F);
                } else if (to == Type.LONG_TYPE) {
                    mv.visitInsn(Opcodes.I2L);
                } else if (to == Type.SHORT_TYPE) {
                    mv.visitInsn(Opcodes.I2S);
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Instructions to do boxing and unboxing operations
    // ------------------------------------------------------------------------

    /**
     * Generates the instructions to box the top stack value. This value is
     * replaced by its boxed equivalent on top of the stack.
     * 
     * @param type the type of the top stack value.
     */
    public void box(Type type) {
        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            return;
        }
        if (type == Type.VOID_TYPE) {
            push((String) null);
        } else {
            Type boxed = type;
            switch (type.getSort()) {
                case Type.BYTE:
                    boxed = BYTE_TYPE;
                    break;
                case Type.BOOLEAN:
                    boxed = BOOLEAN_TYPE;
                    break;
                case Type.SHORT:
                    boxed = SHORT_TYPE;
                    break;
                case Type.CHAR:
                    boxed = CHARACTER_TYPE;
                    break;
                case Type.INT:
                    boxed = INTEGER_TYPE;
                    break;
                case Type.FLOAT:
                    boxed = FLOAT_TYPE;
                    break;
                case Type.LONG:
                    boxed = LONG_TYPE;
                    break;
                case Type.DOUBLE:
                    boxed = DOUBLE_TYPE;
                    break;
            }
            newInstance(boxed);
            if (type.getSize() == 2) {
                // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
                dupX2();
                dupX2();
                pop();
            } else {
                // p -> po -> opo -> oop -> o
                dupX1();
                swap();
            }
            invokeConstructor(boxed, new Method("<init>",
                    Type.VOID_TYPE,
                    new Type[] { type }));
        }
    }

    /**
     * Generates the instructions to unbox the top stack value. This value is
     * replaced by its unboxed equivalent on top of the stack.
     * 
     * @param type the type of the top stack value.
     */
    public void unbox(Type type) {
        Type t = NUMBER_TYPE;
        Method sig = null;
        switch (type.getSort()) {
            case Type.VOID:
                return;
            case Type.CHAR:
                t = CHARACTER_TYPE;
                sig = CHAR_VALUE;
                break;
            case Type.BOOLEAN:
                t = BOOLEAN_TYPE;
                sig = BOOLEAN_VALUE;
                break;
            case Type.DOUBLE:
                sig = DOUBLE_VALUE;
                break;
            case Type.FLOAT:
                sig = FLOAT_VALUE;
                break;
            case Type.LONG:
                sig = LONG_VALUE;
                break;
            case Type.INT:
            case Type.SHORT:
            case Type.BYTE:
                sig = INT_VALUE;
        }
        if (sig == null) {
            checkCast(type);
        } else {
            checkCast(t);
            invokeVirtual(t, sig);
        }
    }

    // ------------------------------------------------------------------------
    // Instructions to jump to other instructions
    // ------------------------------------------------------------------------

   /**
     * Generates the instruction to return the top stack value to the caller.
     */
    public void returnValue() {
        mv.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
    }

    // ------------------------------------------------------------------------
    // Instructions to load and store fields
    // ------------------------------------------------------------------------

   // ------------------------------------------------------------------------
    // Instructions to invoke methods
    // ------------------------------------------------------------------------

    /**
     * Generates an invoke method instruction.
     * 
     * @param opcode the instruction's opcode.
     * @param type the class in which the method is defined.
     * @param method the method to be invoked.
     */
    private void invokeInsn(
        int opcode,
        Type type,
        Method method)
    {
        String owner = type.getSort() == Type.ARRAY
                ? type.getDescriptor()
                : type.getInternalName();
        mv.visitMethodInsn(opcode,
                owner,
                method.getName(),
                method.getDescriptor());
    }

    /**
     * Generates the instruction to invoke a normal method.
     * 
     * @param owner the class in which the method is defined.
     * @param method the method to be invoked.
     */
    public void invokeVirtual(Type owner, Method method) {
        invokeInsn(Opcodes.INVOKEVIRTUAL, owner, method);
    }

    /**
     * Generates the instruction to invoke a constructor.
     * 
     * @param type the class in which the constructor is defined.
     * @param method the constructor to be invoked.
     */
    public void invokeConstructor(Type type, Method method) {
        invokeInsn(Opcodes.INVOKESPECIAL, type, method);
    }

   // ------------------------------------------------------------------------
    // Instructions to create objects and arrays
    // ------------------------------------------------------------------------

    /**
     * Generates a type dependent instruction.
     * 
     * @param opcode the instruction's opcode.
     * @param type the instruction's operand.
     */
    private void typeInsn(int opcode, Type type) {
        String desc;
        if (type.getSort() == Type.ARRAY) {
            desc = type.getDescriptor();
        } else {
            desc = type.getInternalName();
        }
        mv.visitTypeInsn(opcode, desc);
    }

    /**
     * Generates the instruction to create a new object.
     * 
     * @param type the class of the object to be created.
     */
    public void newInstance(Type type) {
        typeInsn(Opcodes.NEW, type);
    }

   // ------------------------------------------------------------------------
    // Miscellaneous instructions
    // ------------------------------------------------------------------------

   /**
     * Generates the instruction to check that the top stack value is of the
     * given type.
     * 
     * @param type a class or interface type.
     */
    public void checkCast(Type type) {
        if (!type.equals(OBJECT_TYPE)) {
            typeInsn(Opcodes.CHECKCAST, type);
        }
    }
}
