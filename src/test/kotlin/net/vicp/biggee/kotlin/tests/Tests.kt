package net.vicp.biggee.kotlin.tests

import net.vicp.biggee.kotlin.proc.RunJar
import net.vicp.biggee.kotlin.proc.RunJava
import net.vicp.biggee.kotlin.util.FileIO
import org.junit.Test
import java.io.File
import java.util.jar.JarFile

class Tests {
    @Test
    fun testFile() {
        val f = File("gradle.properties")
        println(f.exists())
        println(f.listFiles())
        val r = RunJar("D:\\src\\testMain.jar")
        //println(r.waitFor())
        println(r.readOutPut())
        println(r.readErrorOutPut())

        println(System.getenv("JAVA_HOME"))
    }

    @Test
    fun testClz() {
        p
        val p = FileIO.collectClz() ?: return
        println()
        val c = ProcessBuilder(RunJava.javaPath, "net.vicp.biggee.java.sys.BluePrint")
        c.directory(File(p))
        val d = RunJava(c.start())
        println(d.waitFor())
        println(d.readOutPut())
        println(d.readErrorOutPut())

    }

    @Test
    fun testJar() {
        val jar = JarFile("D:\\src\\testMain.jar")
        jar.entries().asIterator().forEach {
            println(it.realName)
        }
    }
}