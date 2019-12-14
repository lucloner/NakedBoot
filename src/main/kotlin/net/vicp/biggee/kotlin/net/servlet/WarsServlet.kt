package net.vicp.biggee.kotlin.net.servlet

import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WarsServlet(private val warDir: String, private val enabledList: MutableSet<String>) : HttpServlet() {
    private val serialVersionUID = 4L
    private val logger by lazy { LoggerFactory.getLogger(javaClass) }

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.characterEncoding = "UTF-8"
        resp.contentType = "text/html"
        resp.setHeader("Server", "Embedded Tomcat")

        try {
            val cmd = req.getParameter("cmd").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no cmd")
                }
            }
            val war = req.getParameter("war").apply {
                if (isNullOrBlank()) {
                    throw NullPointerException("no war")
                }
            }
            when (cmd) {
                "enable" -> enabledList.add(war)
                "disable" -> enabledList.remove(war)
            }
            resp.writer.println("done cmd: $cmd, target war: $war\n")
        } catch (_: Exception) {
        }

        val warList = File(warDir).listFiles { _, fn -> fn.endsWith(".war") }
        if (warList.isNullOrEmpty()) {
            resp.writer.println("no Wars!")
            return
        }
        val warEnabled = warList.filter { enabledList.contains(it.name) }.toHashSet()
        val warAvailable = warList.filter { !warEnabled.contains(it) }.toHashSet()
        resp.writer.use { writer ->
            writer.write("Wars List:<BR />")
            warEnabled.iterator().forEach {
                writer.write("${it.nameWithoutExtension}&nbsp;<a href=?war=${it.name}&cmd=disable>关闭</a>&nbsp;")
                writer.write("<BR />")
            }
            writer.write("<BR />")
            warAvailable.iterator().forEach {
                writer.write("${it.nameWithoutExtension}&nbsp;<a href=?war=${it.name}&cmd=enable>打开</a>&nbsp;")
                writer.write("<BR />")
            }
            writer.flush()
        }
    }
}