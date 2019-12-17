package net.vicp.biggee.kotlin.net.servlet

import net.vicp.biggee.kotlin.proc.RunJar
import net.vicp.biggee.kotlin.proc.RunJava
import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JarServlet(upload: String, private val enabledList: MutableSet<String>) : NakedBootHttpServlet() {
    private val serialVersionUID = 8L
    private val jarDir: String

    constructor() : this(
        NakedBoot.globalSetting["uploadDir"]?.toString()
            ?: NakedBoot.loadAllSetting()[NakedBoot.globalSettingFile]?.get("uploadDir").toString(),
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

        logger.debug("inter Service:$req")
        try {
            val jar = req.getParameter("jar").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no jar")
                }
            }
            val port = req.getParameter("port").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no port")
                }
            }

            val war = req.getParameter("war") ?: ""

            logger.debug("receive get/post:$req")
            val jarFile = File(jarDir, "$jar.jar")
            if (!jarFile.exists()) {
                throw FileNotFoundException("no ${jarFile.absolutePath}")
            }
            if (!war.isNotBlank()) {

            }
            pool.execute {
                taskList.put(port.toInt(), RunningProcess(RunJar(jarFile.absolutePath, port, war)))
                val p = taskList.get(port.toInt())?.process ?: return@execute
                logger.debug(p.readOutPut())
                logger.debug("--BOJ(ars)--")
                logger.debug(p.readErrorOutPut())
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
        } ?: return super.service(req, resp)
        resp.writer.println("--EOJ(ars)--")
    }

    private class RunningProcess(val process: RunJava) {
        val thread by lazy { Thread.currentThread() }
        val timestamp = System.currentTimeMillis()
    }

    companion object {
        private val pool = Executors.newCachedThreadPool()
        private val taskList = HashMap<Int, RunningProcess>()
    }
}