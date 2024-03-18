package com.ganaye.pure

@Suppress("unused")
object PureLogger {

    /**
     * Called by [com.ganaye.pure.MetaPlugin] during IR transformation
     */
    @Suppress("unused")
    fun log() {
        println("PureLogger.log() invoked")
    }
}