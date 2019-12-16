package net.vicp.biggee.kotlin.tests

import net.vicp.biggee.java.sys.BluePrint
import net.vicp.biggee.kotlin.proc.RunJar
import net.vicp.biggee.kotlin.proc.RunJava
import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.util.jar.JarFile
import javax.servlet.ServletContextListener

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
    fun testFindClz() {
        val p = FileIO.collectClz(NakedBoot::class.java.`package`)
        println(p)
        FileIO.xCopy(Path.of(p), Path.of("/tmp/abc"))
        val pk = FileIO.collectClz(BluePrint::class.java.`package`)
        println(pk)
        FileIO.xCopy(Path.of(pk), Path.of("/tmp/abc"))
        val pk1 = FileIO.collectClz(ServletContextListener::class.java.`package`)
        println(pk1)
        FileIO.xCopy(Path.of(pk1), Path.of("/tmp/abc"))
    }

    @Test
    fun testClz() {
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