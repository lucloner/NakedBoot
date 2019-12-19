package net.vicp.biggee.kotlin.proc

class RunClass(val classPath: String, val className: String) :
    RunJava(Runtime.getRuntime().exec("$javaPath -cp $classPath $className")) {
    private val serialVersionUID = 6L
}