package com.chenjinchi.util;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JavaVirtualMachineUtil {
    public static boolean isCategoryTwoType(String descriptor) {
        // Category 2 computational type includes Long and Double
        return "J".equals(descriptor) || "D".equals(descriptor);
    }

    public static boolean isCategoryTwoType(int opcode) {
        // Category 2 computational type includes Long and Double
        return opcode == Opcodes.LALOAD || opcode == Opcodes.DALOAD || opcode == Opcodes.LASTORE || opcode == Opcodes.DASTORE;
    }

    public static boolean isLibClass(String owner) {
        return owner == null || owner.startsWith("java/") || owner.startsWith("sun/");
    }

    public static void swap32And64(MethodVisitor mv) {
        // Stack: ..., v1(32), v2(64) →
        mv.visitInsn(Opcodes.DUP2_X1);
        // Stack: ..., v2(64), v1(32), v2(64) →
        mv.visitInsn(Opcodes.POP2);
        // Stack: ..., v2(64), v1(32) →
    }

    public static void swap64And32(MethodVisitor mv) {
        // Stack: ..., v1(64), v2(32) →
        mv.visitInsn(Opcodes.DUP_X2);
        // Stack: ..., v2(32), v1(64), v2(32) →
        mv.visitInsn(Opcodes.POP);
        // Stack: ..., v2(32), v1(64) →
    }

    public static void swap64And64(MethodVisitor mv){
        // Stack: ..., v1(64), v2(64) →
        mv.visitInsn(Opcodes.DUP2_X2);
        // Stack: ..., v2(64), v1(64), v2(64) →
        mv.visitInsn(Opcodes.POP2);
        // Stack: ..., v2(64), v1(64) →
    }

}
