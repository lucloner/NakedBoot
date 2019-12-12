package net.vicp.biggee.kotlin.sys.core

import net.vicp.biggee.java.util.ClassUtils
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.catalina.Server
import org.apache.catalina.startup.Tomcat
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


object BluePrint {
    lateinit var tomcat: Tomcat
    lateinit var tomcatThread: Thread
    val globalSetting = HashMap<Any, Any>()
    val packages = HashSet<String>()
    val settings = HashMap<String?, Map<*, *>>()
    val logger = LoggerFactory.getLogger(javaClass)
    private const val globalSettingFile = "global.properties"

    fun start() {
        loadAllSetting()
        startTomcat()
    }

    fun startTomcat(): Server? {
        Runtime.getRuntime().addShutdownHook(Thread { tomcat.destroy() })
        //设定
        val catBase = globalSetting["catBase"]?.toString() ?: "tomcat"
        val hostName = globalSetting["hostName"]?.toString() ?: "host-${this.javaClass.simpleName}"
        val port = globalSetting["port"]?.toString()?.toInt() ?: 7573

        tomcat = Tomcat()
        tomcat.setBaseDir(FileIO.bornDir(catBase).absolutePath) // 设置工作目录
        tomcat.setHostname(hostName) // 主机名, 将生成目录: {工作目录}/work/Tomcat/{主机名}/ROOT
        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}work${File.separator}Tomcat${File.separator}${hostName}${File.separator}ROOT")

        println("working dir: ${tomcat.server.catalinaBase}")
        println("Hostname: $hostName")

        tomcat.setPort(port)
        val conn = tomcat.connector // Tomcat 9.0 必须调用 Tomcat#getConnector() 方法之后才会监听端口

        println("connector ok: $conn")

        val urlList = ArrayList<String>()

        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}root")
        tomcat.addWebapp("", "${tomcat.server.catalinaBase.absolutePath}${File.separator}root").apply {
            urlList.add("/")
        }

        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}war")
            .listFiles { _, fn -> fn.endsWith(".war") }?.iterator()?.forEach {
            tomcat.addWebapp("/war/${it.nameWithoutExtension}", "${it.absolutePath}").apply {
                urlList.add(this.path)
            }
        }

        // contextPath要使用的上下文映射，""表示根上下文
        // srvtest上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({主目录}/webapps/{srvtest})
        // contextPath要使用的上下文映射，""表示根上下文
        // srvtest上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({主目录}/webapps/{srvtest})
        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}webapps${File.separator}srvtest")
        val ctx = tomcat.addContext("/globalServlet",  /*{webapps}/~*/"srvtest")
        Tomcat.addServlet(ctx, "globalServlet", object : HttpServlet() {
            private val serialVersionUID = 1L
            @Throws(ServletException::class, IOException::class)
            override fun service(request: HttpServletRequest, response: HttpServletResponse) {
                response.characterEncoding = "UTF-8"
                response.contentType = "text/html"
                response.setHeader("Server", "Embedded Tomcat")
                response.writer.use { writer ->
                    writer.write("Hello, Embedded Tomcat!")
                    urlList.iterator().forEach {
                        writer.write("<BR /><a href=${it}>${it}</a><BR />")
                    }
                    writer.flush()
                }
            }
        })
        ctx.addServletMappingDecoded("/hello", "globalServlet")

        tomcat.start()
        println("tomcat started")
        tomcatThread = Thread {
            println("awaiting for ${tomcat.server.shutdown}")
            tomcat.server.await()
            println("exiting await for ${tomcat.server.shutdown}")
        }.apply {
            start()
        }
        return tomcat.server
    }

    fun stopTomcat(serv: Tomcat? = tomcat): Boolean {
        val server = serv ?: tomcat
        server.stop()
        server.destroy()
        return !tomcatThread.isAlive
    }

    fun loadAllSetting() {
        try {
            globalSetting.putAll(FileIO.loadProfile(globalSettingFile))
            settings.put(null, globalSetting)
        } catch (_: Exception) {
        }

        Package.getPackages().iterator().forEach {
            val name = it.name
            packages.add(name)
            try {
                val classNames = ClassUtils.getClassName(name, false)
                packages.addAll(classNames)
            } catch (_: Exception) {
            }
        }

        packages.iterator().forEach {
            try {
                loadSetting(it)
            } catch (_: Exception) {
            }
        }
    }

    fun loadSetting(fileAndPath: String) {
        val f = File(fileAndPath)
        if (!f.exists()) {
            logger.error("profile not found: $f")
            return
        }
        settings.put(fileAndPath, FileIO.loadProfile(fileAndPath))
    }


}