package com.chenjinchi.jmtrace;

import com.chenjinchi.util.JavaVirtualMachineUtil;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodAdapter extends MethodVisitor {
    public MethodAdapter(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (this.mv == null) {
            return;
        }

        if (!JavaVirtualMachineUtil.isLibClass(owner)) {
            //do something here
            if (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.GETSTATIC) {
                mv.visitLdcInsn(opcode == Opcodes.PUTSTATIC ? "W" : "R");
                mv.visitLdcInsn(owner);
                mv.visitLdcInsn(name);
                mv.visitLdcInsn(descriptor);
                // Stack: ..., ioType, owner, name, descriptor
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Logger.class), "accessStatic", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
            } else if (opcode == Opcodes.PUTFIELD ) {
                // Stack: ..., objectref, value → (here value can be 32bit or 64bit)
                if (JavaVirtualMachineUtil.isCategoryTwoType(descriptor)) {
                    // Stack: ..., objectref(32), value(64) →
                    JavaVirtualMachineUtil.swap32And64(this.mv);
                    // Stack: ..., value(64), objectref(32) →
                    mv.visitInsn(Opcodes.DUP);
                    // Stack: ..., value(64), objectref(32), objectref(32) →
                } else {
                    // Stack: ..., objectref(32), value(32) →
                    mv.visitInsn(Opcodes.SWAP);
                    // Stack: ..., value(32), objectref(32) →
                    mv.visitInsn(Opcodes.DUP);
                    // Stack: ..., value(32), objectref(32), objectref(32) →
                }

                // Stack: ..., value, objectref, objectref →
                mv.visitLdcInsn( "W" );
                mv.visitLdcInsn(owner);
                mv.visitLdcInsn(name);
                mv.visitLdcInsn(descriptor);
                // Stack: ..., value, objectref, objectref, ioType, owner, name, descriptor →
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Logger.class), "accessField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                // Stack: ..., value, objectref →

                // stack rollback
                if (JavaVirtualMachineUtil.isCategoryTwoType(descriptor)) {
                    // Stack: ..., objectref(32), value(64) →
                    JavaVirtualMachineUtil.swap64And32(this.mv);
                    // Stack: ..., value(64), objectref(32) →
                } else {
                    // Stack: ..., objectref(32), value(32) →
                    mv.visitInsn(Opcodes.SWAP);
                    // Stack: ..., value(32), objectref(32) →
                }
                // Stack: ..., value, objectref →
            }else if (opcode == Opcodes.GETFIELD){
                // Stack: ..., objectref →
                mv.visitInsn(Opcodes.DUP);
                // Stack: ..., objectref, objectref →
                mv.visitLdcInsn( "R" );
                mv.visitLdcInsn(owner);
                mv.visitLdcInsn(name);
                mv.visitLdcInsn(descriptor);
                // Stack: ...,objectref, objectref, ioType, owner, name, descriptor →
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Logger.class), "accessField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
                // Stack: ..., objectref →
            }
        }

        this.mv.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitInsn(int opcode) {
        if (this.mv == null) {
            return;
        }


        if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD) {
            // IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD

            // Stack: ..., arrayref, index →
            mv.visitInsn(Opcodes.DUP2);
            // Stack: ..., arrayref, index, arrayref, index →
            mv.visitLdcInsn("R");
            // Stack: ..., arrayref, index, arrayref, index, ioType →
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Logger.class), "accessArray", "(Ljava/lang/Object;ILjava/lang/String;)V", false);
            // Stack: ..., arrayref, index →
        } else if (opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE) {
            // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE

            // Stack: ..., arrayref, index, value →
            if (JavaVirtualMachineUtil.isCategoryTwoType(opcode)){
                // Stack: ..., arrayref(32), index(32), value(64) →
                JavaVirtualMachineUtil.swap64And64(mv);
                // Stack: ..., value(64), arrayref(32), index(32) →
                mv.visitInsn(Opcodes.DUP2);
                // Stack: ..., value(64), arrayref(32), index(32), arrayref(32), index(32) →
            }else{
                // Stack: ..., arrayref(32), index(32), value(32) →
                JavaVirtualMachineUtil.swap64And32(mv);
                // Stack: ..., value(32), arrayref(32), index(32) →
                mv.visitInsn(Opcodes.DUP2);
                // Stack: ..., value(32), arrayref(32), index(32), arrayref(32), index(32) →
            }
            // Stack: ..., value, arrayref, index, arrayref, index →
            mv.visitLdcInsn("W");
            // Stack: ..., value, arrayref, index, arrayref, index, ioType →
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Logger.class), "accessArray", "(Ljava/lang/Object;ILjava/lang/String;)V", false);
            // Stack: ..., value, arrayref, index →
            if(JavaVirtualMachineUtil.isCategoryTwoType(opcode)){
                // Stack: ..., value(64), arrayref(32), index(32) →
                JavaVirtualMachineUtil.swap64And64(mv);
                // Stack: ..., arrayref(32), index(32), value(64) →
            }else{
                // Stack: ..., value(32), arrayref(32), index(32) →
                JavaVirtualMachineUtil.swap32And64(mv);
                // Stack: ..., arrayref(32), index(32), value(32) →
            }
            // Stack: ..., arrayref, index, value →
        }


        this.mv.visitInsn(opcode);
    }


}
