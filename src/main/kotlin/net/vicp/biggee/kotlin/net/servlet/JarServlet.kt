package net.vicp.biggee.kotlin.net.servlet

import net.vicp.biggee.kotlin.proc.RunJar
import net.vicp.biggee.kotlin.proc.RunJava
import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.juli.logging.LogFactory
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JarServlet(upload: String, private val enabledList: MutableSet<String>) : NakedBootHttpServlet() {
    private val serialVersionUID = 8L
    private val jarDir: String

    constructor() : this(
        NakedBoot.uploadDir,
        NakedBoot.enabledWars
    )

    init {
        var dir = upload
        try {
            FileIO.bornDir(upload)
        } catch (_: Exception) {
            dir = createTempDir().absolutePath
        }
        logger.info("=============upload path:$dir")
        jarDir = dir
        NakedBoot.globalSetting["uploadDir"] = jarDir
    }

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.characterEncoding = "UTF-8"
        resp.contentType = "text/html"
        resp.setHeader("Server", "Embedded Tomcat")
        var availablePort = 17573

        logger.debug("inter Service:JarServlet")
        try {
            val jar = req.getParameter("jar").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no jar")
                }
            }.toString()
            val port = req.getParameter("port").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no port")
                }
            }.toInt()

            var war = req.getParameter("war")?.toString() ?: ""

            logger.debug("receive get/post:${req.parameterMap.toMap()}")
            val jarFile = File(jarDir, "$jar.jar")
            if (!jarFile.exists()) {
                throw FileNotFoundException("no ${jarFile.absolutePath}")
            }
            if (!war.isNotBlank()) {
                val f = File(jarDir, File(war).name)
                if (f.isFile) {
                    war = f.absolutePath
                } else {
                    war = ""
                }
            }
            pool.execute {
                logger.info("get to run:$jar\t$port\t$war")
                taskList.put(port.toInt(), RunningProcess(RunJar("${jarFile.absolutePath} $port $war")))
                logger.debug("--BOJ(ars)--")
            }
            resp.writer.println("done cmd: $jar, target port: $port\n")
        } catch (_: Exception) {
        }

        File(jarDir).listFiles { _, fn ->
            return@listFiles fn.endsWith(".jar")
        }?.iterator()?.forEach {
            if (!it.isFile) {
                return@forEach
            }
            val jar = it.nameWithoutExtension
            resp.writer.println(
                """
---<br/>
[$jar]&nbsp;<form action="" method="post">
    <input type="hidden" name="jar" value="$jar"/>
    <input type="text" name="port" value="${availablePort++}"/>
    <input type="submit" value="Submit" />
</form><br/>
+++
            """.trimIndent()
            )
        } ?: return
        resp.writer.println("--EOJ(ars)--")

        return
    }

    private data class RunningProcess(val process: RunJava) {
        val thread by lazy { Thread.currentThread() }
        val timestamp = System.currentTimeMillis()
        val port by lazy {
            taskList.iterator().forEach {
                if (it.value == this) {
                    return@lazy it.key
                }
            }
            return@lazy 0
        }
        val logger by lazy { LogFactory.getLog(this::class.java) }

        init {
            var msg: String? = "STUB"
            while (!msg.isNullOrBlank()) {
                msg = process.readOutPut()
                logger.info(msg)
                msg = process.readErrorOutPut()
                logger.error(msg)
            }

            logger.debug(process.waitFor())
        }
    }

    companion object {
        private val pool = Executors.newCachedThreadPool()
        private val taskList = HashMap<Int, RunningProcess>()
    }
}