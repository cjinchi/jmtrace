package com.chenjinchi.jmtrace;

import java.lang.instrument.Instrumentation;

public class JMTraceAgent {
    public static void premain(String agentArgs, Instrumentation inst)
    {
        inst.addTransformer(new Transformer());
    }
}
