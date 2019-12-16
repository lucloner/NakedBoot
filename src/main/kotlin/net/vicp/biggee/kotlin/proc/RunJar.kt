package net.vicp.biggee.kotlin.proc

import java.io.File

class RunJar(val jarFile: String) :
    RunJava(Runtime.getRuntime().exec("${RunJava.javaPath} -jar ${File(jarFile).absolutePath}"))