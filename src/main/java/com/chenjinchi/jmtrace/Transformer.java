package com.chenjinchi.jmtrace;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // Ignore class loaded by Boostrap ClassLoader
        if(loader == null || className.startsWith("java/") || className.startsWith("sun/")){
            return null;
        }

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES);
        ClassAdapter adapter = new ClassAdapter(Opcodes.ASM9,writer);
        reader.accept(adapter,0);

        return writer.toByteArray();

    }
}
