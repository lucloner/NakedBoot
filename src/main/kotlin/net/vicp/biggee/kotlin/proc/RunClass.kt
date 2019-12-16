package net.vicp.biggee.kotlin.proc

class RunClass(val classPath: String, val className: String) :
    RunJava(Runtime.getRuntime().exec("${RunJava.javaPath} -cp $classPath $className"))