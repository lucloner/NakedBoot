package net.vicp.biggee.kotlin.sys.core

import net.vicp.biggee.java.util.ClassUtils
import net.vicp.biggee.kotlin.net.servlet.UploadServlet
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.catalina.LifecycleState
import org.apache.catalina.Server
import org.apache.catalina.startup.Tomcat
import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


object BluePrint {
    var tomcat: Tomcat? = null
    lateinit var tomcatThread: Thread
    val globalSetting = HashMap<Any, Any>()
    val packages = HashSet<String>()
    val settings = HashMap<String?, Map<*, *>>()
    val logger = LoggerFactory.getLogger(javaClass)
    private const val globalSettingFile = "global.properties"
    @JvmStatic
    var uploadDir = ""

    fun start() {
        loadAllSetting()
        startTomcat()
    }

    private fun startTomcat(): Server? {
        Runtime.getRuntime().addShutdownHook(Thread { stopTomcat() })
        //设定
        val catBase = globalSetting["catBase"]?.toString() ?: "tomcat"
        val hostName = globalSetting["hostName"]?.toString() ?: "host-${this.javaClass.simpleName}"
        val port = globalSetting["port"]?.toString()?.toInt() ?: 7573

        val tomcat = Tomcat()
        tomcat.setBaseDir(FileIO.bornDir(catBase).absolutePath) // 设置工作目录
        tomcat.setHostname(hostName) // 主机名, 将生成目录: {工作目录}/work/Tomcat/{主机名}/ROOT
        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}work${File.separator}Tomcat${File.separator}${hostName}${File.separator}ROOT")

        logger.error("working dir: ${tomcat.server.catalinaBase}")
        logger.error("Hostname: $hostName")

        tomcat.setPort(port)
        val conn = tomcat.connector // Tomcat 9.0 必须调用 Tomcat#getConnector() 方法之后才会监听端口

        logger.error("connector ok: $conn")

        val urlList = HashSet<String>()

        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}root")
        tomcat.addWebapp("", "${tomcat.server.catalinaBase.absolutePath}${File.separator}root").apply {
            urlList.add("/")
        }

        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}war")
            .listFiles { _, fn -> fn.endsWith(".war") }?.iterator()?.forEach {
                tomcat.addWebapp("/war/${it.nameWithoutExtension}", it.absolutePath).apply {
                    urlList.add(this.path)
                }
            }

        // contextPath要使用的上下文映射，""表示根上下文
        // srvtest上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({主目录}/webapps/{srvtest})
        // contextPath要使用的上下文映射，""表示根上下文
        // srvtest上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({主目录}/webapps/{srvtest})
        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}webapps${File.separator}srvtest")
        val ctx = tomcat.addContext("/globalServlet",  /*{webapps}/~*/"srvtest")
        Tomcat.addServlet(ctx, "hello", object : HttpServlet() {
            private val serialVersionUID = 1L
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
        ctx.addServletMappingDecoded("/hello", "hello")
        Tomcat.addServlet(ctx, "upload", UploadServlet())
        ctx.addServletMappingDecoded("/upload", "upload")
        Tomcat.addServlet(ctx, "cmd", object : HttpServlet() {
            private val serialVersionUID = 3L
            override fun service(request: HttpServletRequest, response: HttpServletResponse) {
                //接受到表单的数据， 先使用request设置服务器应该使用的字符编码,否则字符不统一会出现乱码
                request.characterEncoding = "UTF-8"
                response.characterEncoding = "UTF-8"
                response.contentType = "text/html"
                response.setHeader("Server", "Embedded Tomcat")
                try {
                    when (request.getParameter("cmd")) {
                        "stop" -> {
                            stopTomcat()
                            response.writer.println("服务器已经停止")
                            return
                        }
                        "start" -> startTomcat()
                        "restart" -> {
                            tomcatThread = Thread {
                                restartTomcat()
                            }.apply { start() }
                            response.writer.println("完成:$tomcatThread")
                            return
                        }
                        "flush" -> {
                            val f = File(uploadDir)
                            if (uploadDir.isNullOrEmpty() || !f.exists()) {
                                response.writer.println("没有上传文件")
                                return
                            }
                            if (!f.deleteRecursively()) {
                                response.writer.println("删除失败")
                                return
                            }
                        }
                    }
                } catch (e: Exception) {
                    response.writer.use {
                        it.write("错误:$e")
                        it.flush()
                    }
                    return
                }

                response.sendRedirect("/globalServlet/hello")
            }
        })
        ctx.addServletMappingDecoded("/", "cmd")
        //集中设定
        tomcat.apply {
            host.apply {
                createDirs = true
                autoDeploy = true
                deployOnStartup = true
                undeployOldVersions = true
            }
            connector.apply {
                allowTrace = true
                enableLookups = true
                useIPVHosts = true
            }
        }

        this.tomcat = tomcat
        uploadDir =
            FileIO.bornDir("${tomcat.server?.catalinaBase?.absolutePath ?: "."}${File.separator}upload").absolutePath
        tomcat.start()
        logger.error("tomcat started")
        tomcatThread = Thread {
            logger.error("awaiting for ${tomcat.server.shutdown}")
            tomcat.server.await()
            logger.error("exiting await for ${tomcat.server.shutdown}")
        }.apply {
            start()
        }
        return tomcat.server
    }

    fun stopTomcat(serv: Tomcat? = tomcat): Boolean {
        val server = serv ?: tomcat ?: return false
        if (server.server?.state?.isAvailable == false) {
            return false
        }
        tomcat = null
        server.stop()
        server.destroy()
        return !tomcatThread.isAlive
    }

    fun restartTomcat(serv: Tomcat? = tomcat): Server? {
        val now = System.currentTimeMillis()
        val server = serv ?: tomcat ?: return null
        if (server.server?.state?.isAvailable == false) {
            return null
        }
        stopTomcat()
        while (server.server.state != LifecycleState.DESTROYED) {
            Thread.sleep(1000)
            logger.error("经过:${System.currentTimeMillis() - now}")
        }
        return startTomcat()
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