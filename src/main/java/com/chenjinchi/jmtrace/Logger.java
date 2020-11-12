package com.chenjinchi.jmtrace;

public class Logger {
    public static void accessStatic(String ioType, String owner, String name, String descriptor) {
        // System.out.println(descriptor);
        try {
            final long threadId = Thread.currentThread().getId();
            final long objectId = (((long) System.identityHashCode(Class.forName(owner.replace('/', '.')))) << 32) + name.hashCode();
            final String fieldName = owner.replace('/', '.') + "." + name;

            System.out.printf("%s %d %x %s\n", ioType, threadId, objectId, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void accessField(Object object,String ioType,String owner, String name, String descriptor){
        final long threadId = Thread.currentThread().getId();
        final long objectId = System.identityHashCode(object);
        final String fieldName = owner.replace('/', '.') + "." + name;

        System.out.printf("%s %d %x %s\n", ioType, threadId, objectId, fieldName);
    }

    public static void accessArray(Object object, int index, String ioType){
        final long threadId = Thread.currentThread().getId();
        final long objectId = System.identityHashCode(object);
        final String fieldName = object.getClass().getComponentType().getCanonicalName();

        System.out.printf("%s %d %x %s[%d]\n", ioType, threadId, objectId, fieldName,index);

    }


}
