package net.vicp.biggee.kotlin.net.servlet

import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WarsServlet(private val warDir:String, private val enabledList:List<String>) : HttpServlet(){
    private val serialVersionUID = 4L
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        resp.characterEncoding = "UTF-8"
        resp.contentType = "text/html"
        resp.setHeader("Server", "Embedded Tomcat")
        val warList= File(warDir).listFiles{_,fn->fn.endsWith(".war")}
        if(warList.isNullOrEmpty()){
            resp.writer.println("no Wars!")
            return
        }
        val warEnabled=warList.filter { enabledList.contains(it.name) }.toHashSet()
        val warAvailable=warList.filter { !warEnabled.contains(it) }.toHashSet()
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