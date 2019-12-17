package net.vicp.biggee.kotlin.proc

class RunCmd(cmd: String) : RunJava(Runtime.getRuntime().exec(cmd)) {
    private val serialVersionUID = 6L
}