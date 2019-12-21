package net.vicp.biggee.kotlin.tests

import net.vicp.biggee.java.sys.BluePrint
import net.vicp.biggee.kotlin.proc.RunCmd
import net.vicp.biggee.kotlin.proc.RunJar
import net.vicp.biggee.kotlin.proc.RunJava
import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.log4j.LogManager
import org.junit.Test
import org.slf4j.LoggerFactory
import org.slf4j.log4j12.Log4jLoggerAdapter
import org.slf4j.log4j12.Log4jLoggerFactory
import java.io.File
import java.nio.file.Path
import java.util.*
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


    }

    @Test
    fun testJar() {
        val jar = JarFile("D:\\src\\testMain.jar")
        jar.entries().asIterator().forEach {
            println(it.realName)
        }
    }

    @Test
    fun testRes() {
        println(this.javaClass.getResource("/index.jsp").toString())
    }

    @Test
    fun testJarFile() {
        val j = JarFile("build/libs/NakedBoot-0.1-SNAPSHOT-all.jar")
        println(j.manifest.mainAttributes.entries)
        j.manifest.entries.iterator().forEach {
            val s = "Extension-Name"
            print(it.key)
            print(String(it.key.toString().toByteArray()).equals("Extension-Name"))
            print(String(BluePrint.Ext_Key.toByteArray()).equals(it.key))
            print(BluePrint.Ext_Key == it.key)
            print(s == it.key)
            println(it.value)
        }
    }

    @Test
    fun testCore() {
        val j = JarFile("build/libs/NakedBoot-0.2-SNAPSHOT-all.jar")
        println(FileIO.isCoreJar(j, "notJar"))
    }

    @Test
    fun testJava() {
        var j = RunJava("-version")
        println(j.waitFor())
        println(j.readOutPut())
        println(j.readErrorOutPut())
        j = RunCmd("where.exe java")
        println(j.waitFor())
        println(j.readOutPut())
        println(j.readErrorOutPut())
        j = RunCmd("java -jar build/libs/NakedBoot-0.2-SNAPSHOT-all.jar")
        println(j.waitFor())
        println(j.readOutPut())
        println(j.readErrorOutPut())
    }

    @Test
    fun testLog() {
        val f = LoggerFactory.getILoggerFactory() as Log4jLoggerFactory
        val l = f.getLogger(this::class.java.name) as Log4jLoggerAdapter
        l.info(Date().toString())
        l.atInfo().log(System.currentTimeMillis().toString())
        LogManager.getRootLogger().allAppenders.iterator().forEach {
            println(it)
        }
    }
}