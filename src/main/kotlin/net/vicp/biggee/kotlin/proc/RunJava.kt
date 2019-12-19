package net.vicp.biggee.kotlin.proc

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

open class RunJava(val process: Process) {
    private val serialVersionUID = 5L
    protected val stringBuffer by lazy { StringBuffer(1_000_000) }

    constructor(firstArg: String, vararg args: String) : this(Runtime.getRuntime().exec("$javaPath $firstArg", args))

    fun waitFor() = process.waitFor()
    fun readOutPut() =
        try {
            stringBuffer.append(String(process.inputStream.readAllBytes(), Charset.defaultCharset())).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            stringBuffer.toString()
        }

    /*@since 9*/
    fun readErrorOutPut() = try {
        String(process.inputStream.readAllBytes(), Charset.defaultCharset())
    } catch (e: Exception) {
        e.printStackTrace()
        stringBuffer.toString()
    }

    fun writeString(input: String) = process.outputStream.write(input.toByteArray(Charset.defaultCharset()))

    companion object {
        @JvmStatic
        var logger = LoggerFactory.getLogger(RunJava::class.java)

        val javaPath by lazy {
            var java: String = "java"
            var java1 = System.getenv("JAVA_HOME") + File.separator + "bin" + File.separator + "java"
            var java2 = System.getenv("JDK_HOME") + File.separator + "bin" + File.separator + "java"
            var java3: String = "java"
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
                    java3 += ".exe"
                    java = "\"$java\""
                    java1 = "\"$java1\""
                    java2 = "\"$java2\""
                    java3 = "\"$java3\""
                }
            }

            java = java.replace("\r", "").replace("\n", "")

            java = when {
                java.contains("java") && File(java).canExecute() -> {
                    java
                }
                java1.contains("java") && File(java1).canExecute() -> {
                    java1
                }
                java2.contains("java") && File(java2).canExecute() -> {
                    java2
                }
                else -> {
                    java3
                }
            }
            logger.debug("found java:[$java]")
            return@lazy java
        }

        val javaVersion by lazy {
            val p = RunJava("-version")
            val out = p.waitFor()
            val stdout = p.readOutPut()
            val stderr = p.readErrorOutPut()
            logger.debug("javaVersion:$out\tjavaVersion:$stdout\tjavaVersion:$stderr")
            return@lazy stdout + stderr + out
        }
    }
}