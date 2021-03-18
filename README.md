# jmtrace

本项目实现了一个面向Java程序的工具，该工具为每一个共享内存访问输出一条日志信息。

## 编译、使用

本项目使用Maven构建。在根目录中执行

```shell
mvn package
chmod +x jmtrace
```

后即可使用。编译完成后`code/target/jmtrace-1.0-SNAPSHOT-jar-with-dependencies.jar`文件即为本工具的核心模块。

在根目录中执行

```shell
./jmtrace -jar YOUR_APP.jar
```

## 执行流程

1. 使用`java.lang.Instrument`在`premain`阶段添加一个用于插桩的`transformer`；
2. 在`transformer`中，首先排除由`Boostrap Classloader`加载的类、`java`包中的类和`sun`包中的类，然后运用适配器设计模式将传入`ClassReader`的对象修改为自定义类`ClassAdapter`的实例。
3. 在`ClassAdapter`中覆盖`visitMethod`方法，运用适配器设计模式将返回值修改为自定义类`MethodAdapter`的实例。
4. `MethodAdapter`是插桩的核心模块，它是`MethodVisitor`的子类，我们主要覆盖了`visitFieldInsn`和`visitInsn`两个方法，在处理到需要插桩的opcode时调用打印相关信息的方法。在实现该类的过程中，需要完成“判断目标类是否为库中的类”、“对栈中的元素进行交换”等操作，我们把这些操作抽象为通用的静态方法，放在`JavaVirtualMachineUtil`类中。
5. 打印相关信息的方法位于`Logger`类中，包括`accessStatic`、`accessField`和`accessArray`三个方法。

## 挑战

- 静态变量的对象标识

  - 对于成员变量、局部变量，我们可以在栈中获得对象实例，进而直接使用`System.identityHashCode()`生成对象标识符。但是在访问静态变量时，栈中不会提供对象实例，所以我们无法采用上述方法。考虑到同一个类的不同对象的同一静态域实际上是同一个实例，即“类+静态变量名”能够唯一地确定某一静态变量的实例，所以我们用以下内容标识静态变量：

    ```java
    (((long) System.identityHashCode(Class.forName(owner.replace('/', '.')))) << 32) + name.hashCode()
    ```

    即：64位整数的前32位标识类，后32位标识静态变量名（实际上也标识了该实例）。

- 手动调用`visitMethodInsn`时`descriptor`参数的生成

  - 在`MethodAdapter`类中的两个方法中，我们需要手动调用`mv.visitMethodInsn`方法以在字节码层级插入“对`Logger`类中打印信息的方法的调用”。在方法需要我们传入形如

    ```java
    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
    ```

    的`descriptor`参数，用于描述被调用方法的参数类型和返回值类型。`descriptor`比较复杂，手动生成容易出错。因此，我们采用[ASMifier](https://asm.ow2.io/faq.html#Q10)，自动生成目标方法的`descriptor`。

- 多次重复不同类型的`swap`操作

  - 在`MethodAdapter`的实现中，我们多次需要完成栈顶端两个元素的交换操作。由于这些元素的大小不一（64位和64位交换、64位和32位交换、32位和64位交换），需要分别使用`DUP`和`POP`系列指令来完成交换过程。为了便于代码维护，我们把这些常用操作抽象为`JavaVirtualMachineUtil`中的静态方法。例如，依次调用`DUP2_X1`和`POP2`指令来实现32位和64位的交换（即`JavaVirtualMachineUtil.swap32And64`方法）。

- 对`java.lang.Instrument`和ASM基本流程的了解，参考了\[1]\[2]\[3]

- 对Java字节码和栈状态的了解，参考了\[4]\[5]

- 对Java数据类型的两种类别，参考了\[6]

## 参考资料

1. https://asm.ow2.io/faq.html
2. https://cloud.tencent.com/developer/article/1609739
3. http://www.dengshenyu.com/java-asm/
4. https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
5. https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings
6. https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.11.1