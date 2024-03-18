package com.ganaye.pure

class AnnotatedFunctionInjection {

    @Pure
    @PureLog
    fun doStuff(input: Int): Int {
        return input * 2
    }

    fun doSignalStuff(input: Signal): Signal {
        return DynamicSignal(arrayOf(input), ::doStuff)
    }

}


fun main() {
    // Assert logging is invoked
    val testInstance = AnnotatedFunctionInjection()
    val result = testInstance.doStuff(5)
    println("result: $result")

    val x = VariableSignal(12)
    val result2 = testInstance.doSignalStuff(x)
    println("result2: ${result2.value}")
    x.setValue(22)
    println("result2: ${result2.value}")
}
