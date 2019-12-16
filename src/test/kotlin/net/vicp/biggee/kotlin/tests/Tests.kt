package net.vicp.biggee.kotlin.tests

import net.vicp.biggee.kotlin.proc.RunJar
import org.junit.Test
import java.io.File

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

}