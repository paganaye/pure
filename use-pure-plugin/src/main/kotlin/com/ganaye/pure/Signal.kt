package com.ganaye.pure

import kotlin.reflect.KFunction

abstract class Signal {
    abstract val value: Any
    private var observers: MutableList<SignalObserver>? = mutableListOf()

    protected fun signalValueChanged() {
        observers?.forEach { it.onValueChanged(this) }
    }

    open fun dispose() {
        observers?.forEach { it.onSignalDisposed(this) }
        observers?.clear()
        observers = null
    }

    open fun addObserver(observer: SignalObserver) {
        if (observers == null) {
            observers = mutableListOf()
        }
        observers?.add(observer)
    }

    open fun removeObserver(listener: SignalObserver) {
        observers?.remove(listener)
    }
}

interface SignalObserver {
    fun onValueChanged(signal: Signal)
    fun onSignalDisposed(signal: Signal)
}

class ConstSignal<T : Any>(private val constValue: T) : Signal() {
    override val value: T get() = constValue
    override fun removeObserver(listener: SignalObserver) {}
    override fun addObserver(observer: SignalObserver) {}
}

class DynamicSignal(
    private val dependencies: Array<out Signal>,
    private val calcFunction: KFunction<Any>
) : Signal() {
    var _value: Any? = null
    var modified = true

    init {
        val observer = object : SignalObserver {
            override fun onValueChanged(signal: Signal) {
                if (modified) return
                modified = true
                signalValueChanged()
            }

            override fun onSignalDisposed(signal: Signal) {
                // Well
            }
        }
        dependencies.forEach { it.addObserver(observer) }
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

class VariableSignal<T : Any>(initialValue: T) : Signal() {
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