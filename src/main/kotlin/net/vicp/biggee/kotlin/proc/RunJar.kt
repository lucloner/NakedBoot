package net.vicp.biggee.kotlin.proc

import java.io.File

class RunJar(val jarFile: String, vararg args: String) :
    RunJava(Runtime.getRuntime().exec("${RunJava.javaPath} -jar ${File(jarFile).absolutePath}", args)) {
    private val serialVersionUID = 6L
}