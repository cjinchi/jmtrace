package com.chenjinchi.jmtrace;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ClassAdapter extends ClassVisitor {

    public ClassAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return this.cv == null ? null : new MethodAdapter(api, this.cv.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
