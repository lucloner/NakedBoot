package net.vicp.biggee.kotlin.proc

import java.io.File
import java.nio.charset.Charset

abstract class RunJava(val process: Process) {
    protected val stringBuffer by lazy { StringBuffer(1_000_000) }

    fun waitFor() = process.waitFor()

    fun readOutPut() =
        stringBuffer.append(String(process.inputStream.readAllBytes(), Charset.defaultCharset())).toString()

    fun readErrorOutPut() = String(process.inputStream.readAllBytes(), Charset.defaultCharset())
    fun writeString(input: String) = process.outputStream.write(input.toByteArray(Charset.defaultCharset()))

    companion object {
        val javaPath by lazy {
            var java1 = System.getenv("JAVA_HOME") + File.separator + "bin" + File.separator + "java"
            var java2 = System.getenv("JDK_HOME") + File.separator + "bin" + File.separator + "java"
            var java: String = "java"
            when {
                System.getProperty("os.name").toLowerCase().contains("linux") -> {
                    val p = RunCmd("which java")
                    p.waitFor()
                    java = p.readOutPut()
                }
                System.getProperty("os.name").toLowerCase().contains("windows") -> {
                    val p = RunCmd("where java")
                    p.waitFor()
                    java = p.readOutPut()
                    java1 += ".exe"
                    java2 += ".exe"
                }
            }

            java = java.replace("\r", "").replace("\n", "")

            when {
                java.contains("java") && File(java).canExecute() -> {
                    return@lazy java
                }
                java1.contains("java") && File(java1).canExecute() -> {
                    return@lazy java1
                }
                java2.contains("java") && File(java2).canExecute() -> {
                    return@lazy java2
                }
                else -> return@lazy java
            }
        }
    }
}