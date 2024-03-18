package com.ganaye.pure

class AnnotatedFunctionInjection {

    @Pure
    @PureLog
    fun doStuff(input: Int): Int {
        return input * 2
    }

    fun _signal_doStuff(input: Signal<Int>): Signal<Int> {
        return DynamicSignal(arrayOf(input as Signal<Any>), ::doStuff)
    }

}


fun main() {
    // Assert logging is invoked
    val testInstance = AnnotatedFunctionInjection()
    val result = testInstance.doStuff(5)
    println("result: $result")

    val x = Variable(12)

    val result2 = testInstance._signal_doStuff(x)
    println("result2: ${result2.value}")
    x.setValue(22)
    println("result2: ${result2.value}")
}
