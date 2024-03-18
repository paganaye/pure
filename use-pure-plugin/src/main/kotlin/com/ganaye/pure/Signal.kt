package com.ganaye.pure

import kotlin.reflect.KFunction

abstract class Signal<T> {
    abstract val value: Any
    private var observers: MutableList<SignalObserver<Any>>? = mutableListOf()

    protected fun signalValueChanged() {
        observers?.forEach { it.onValueChanged(this as Signal<Any>) }
    }

    open fun dispose() {
        observers?.forEach { it.onSignalDisposed(this as Signal<Any>) }
        observers?.clear()
        observers = null
    }

    open fun addObserver(observer: SignalObserver<T>) {
        if (observers == null) {
            observers = mutableListOf()
        }
        observers?.add(observer as SignalObserver<Any>)
    }

    open fun removeObserver(observer: SignalObserver<T>) {
        observers?.remove(observer as SignalObserver<Any>)
    }
}

interface SignalObserver<T> {
    fun onValueChanged(signal: Signal<T>)
    fun onSignalDisposed(signal: Signal<T>)
    fun onInnerValueChanged(signalEntry: SignalEntry<T>)
}

sealed class SignalEntry<T>(val subEntry: SignalEntry<Any>? = null) {

    class ArrayEntry<T>(val signal: Signal<T>, val member: Int, subEntry: SignalEntry<Any>?) :
        SignalEntry<T>(subEntry)

    class MemberEntry<T>(val signal: Signal<T>, val member: String, subEntry: SignalEntry<Any>?) :
        SignalEntry<T>(subEntry)
}


var v = Variable(1)

class ConstSignal<T : Any>(private val constValue: T) : Signal<T>() {
    override val value: T get() = constValue
    override fun removeObserver(observer: SignalObserver<T>) {}
    override fun addObserver(observer: SignalObserver<T>) {}
}

class DynamicSignal<T>(
    private val dependencies: Array<out Signal<Any>>,
    private val calcFunction: KFunction<Any>
) : Signal<T>() {
    var _value: Any? = null
    var modified = true

    init {
        val observer = object : SignalObserver<T> {
            override fun onValueChanged(signal: Signal<T>) {
                if (modified) return
                modified = true
                signalValueChanged()
            }

            override fun onSignalDisposed(signal: Signal<T>) {
                // Well
            }

            override fun onInnerValueChanged(signalEntry: SignalEntry<T>) {
                // TODO("Not yet implemented")
            }
        }
        dependencies.forEach { it.addObserver(observer as SignalObserver<Any>) }
    }

    override val value: Any
        get() {
            if (modified) {
                val args = dependencies.map {
                    it.value
                }.toTypedArray()
                return calcFunction.call(*args).also {
                    modified = false
                }
            }
            return _value!!
        }


}

class Variable<T : Any>(initialValue: T) : Signal<T>() {
    private var _value: T = initialValue
        set(value) {
            field = value
            signalValueChanged() // Notify observers about the value change
        }

    override val value: T
        get() = _value

    fun setValue(value: T) {
        _value = value
    }
}