package com.ganaye.pure

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName

val Meta.processPureFunctions: CliPlugin
    get() = "Process Pure Functions" {
        meta(
            irFunction { declaration ->
                return@irFunction if (declaration.hasAnnotation(FqName("com.ganaye.pure.Pure"))) {
                    declaration.body = createReactiveFunction(pluginContext, declaration)
                    declaration
                } else {
                    declaration
                }
            }
        )
    }

fun createReactiveFunction(pluginContext: IrPluginContext, declaration: IrFunction): IrBody {
    return DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {

        val referenceClass = pluginContext.referenceClass(FqName("com.ganaye.pure.PureFunctionRunner"))
            ?: throw NoClassDefFoundError("PureFunctionRunner not found")

        val interceptionCall = referenceClass.getSimpleFunction("run")
            ?: throw NoClassDefFoundError("PureFunctionRunner.run() not found")

        // Inject logging to function top statement(will be called first)
        +irCall(interceptionCall).apply {
            dispatchReceiver = irGetObject(referenceClass)
        }

        // Apply original statements
        for (statement in declaration.body!!.statements) +statement
    }
}
