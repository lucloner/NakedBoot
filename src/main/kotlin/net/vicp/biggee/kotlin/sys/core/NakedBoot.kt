package net.vicp.biggee.kotlin.sys.core

import net.vicp.biggee.java.util.ClassUtils
import net.vicp.biggee.kotlin.net.servlet.JarServlet
import net.vicp.biggee.kotlin.net.servlet.NakedBootHttpServlet
import net.vicp.biggee.kotlin.net.servlet.UploadServlet
import net.vicp.biggee.kotlin.net.servlet.WarsServlet
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.catalina.LifecycleState
import org.apache.catalina.Server
import org.apache.catalina.startup.Tomcat
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.system.exitProcess

object NakedBoot : NakedBootHttpServlet() {
    private val serialVersionUID = 3L
    var tomcat: Tomcat? = null
    lateinit var tomcatThread: Thread
    @JvmStatic
    val globalSetting = ConcurrentHashMap<Any, Any>()
    @JvmStatic
    val packages = HashSet<String>()
    @JvmStatic
    val settings = ConcurrentHashMap<String?, Map<Any, Any>>()
    @JvmStatic
    val globalSettingFile = "global.properties"
    @JvmStatic
    @Volatile
    var uploadDir = ""
    @JvmStatic
    val enabledWars = HashSet<String>()
    val fingerPrint by lazy { hashCode() }
    @JvmStatic
    var isChild = false
    @JvmStatic
    val urlList = HashSet<String>()

    init {
        loadAllSetting()
        uploadDir = globalSetting["uploadDir"]?.toString() ?: uploadDir
        logger = LoggerFactory.getLogger(NakedBoot::class.java)
    }

    fun uploadFiles() = File(uploadDir).listFiles() ?: emptyArray<File>()
    fun uploadFilesWar() = uploadFiles().filter { it.name.endsWith(".war") }
    fun uploadFilesJar() = uploadFiles().filter { it.name.endsWith(".jar") }

    fun start(port: Int? = null, war: String? = null) {
        globalDefault()
        startTomcat(port, war)
    }

    private fun globalDefault() = globalSetting.apply {
        if (!containsKey("catBase")) put("catBase", "tomcat")
        if (!containsKey("hostName")) put("hostName", "host-${this.javaClass.simpleName}")
        if (!containsKey("port")) put("port", 7573)
        if (!containsKey("uploadDir")) {
            put("uploadDir", "${get("catBase")}${File.separator}upload")
        }
    }

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        //接受到表单的数据， 先使用request设置服务器应该使用的字符编码,否则字符不统一会出现乱码
        req.characterEncoding = "UTF-8"
        resp.characterEncoding = "UTF-8"
        resp.contentType = "text/html"
        resp.setHeader("Server", "Embedded Tomcat")
        try {
            when (req.getParameter("cmd")) {
                "stop" -> {
                    stopTomcat()
                    resp.writer.println("服务器已经停止")
                    if (isChild) {
                        exitProcess(0)
                    }
                    return
                }
                "start" -> start()
                "restart" -> {
                    tomcatThread = Thread {
                        restartTomcat()
                    }.apply { start() }
                    resp.writer.println("完成:$tomcatThread")
                    return
                }
                "flush" -> {
                    val f = File(uploadDir)
                    if (uploadDir.isNullOrEmpty() || !f.exists()) {
                        resp.writer.println("没有上传文件")
                        return
                    }
                    if (!f.deleteRecursively()) {
                        resp.writer.println("删除失败")
                        return
                    }
                }
                "halt" -> {
                    exitProcess(0)
                }
                else -> logger.info("echo:[${req.getParameter("cmd")}]")
            }
        } catch (e: Exception) {
            resp.writer.use {
                it.write("错误:${e.localizedMessage}\t${e.stackTrace?.contentToString()}")
                it.flush()
            }
            return
        }

        resp.writer.println("please post/get command")
    }

    private fun startTomcat(tcpPort: Int? = null, war: String? = null): Server? {
        Runtime.getRuntime().addShutdownHook(Thread { stopTomcat() })
        //设定
        val catBase = FileIO.bornDir(globalSetting["catBase"]?.toString() ?: "tomcat")
        val hostName = globalSetting["hostName"]?.toString() ?: "host-${this.javaClass.simpleName}"
        val port = tcpPort ?: globalSetting["port"]?.toString()?.toInt() ?: 7573

        val tomcat = Tomcat()
        tomcat.setBaseDir(catBase.absolutePath) // 设置工作目录
        tomcat.setHostname(hostName) // 主机名, 将生成目录: {工作目录}/work/Tomcat/{主机名}/ROOT
        //FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}work${File.separator}Tomcat${File.separator}${hostName}${File.separator}ROOT")

        logger.info("working dir: ${catBase}")
        logger.info("Hostname: $hostName")

        tomcat.setPort(port)
        val conn = tomcat.connector // Tomcat 9.0 必须调用 Tomcat#getConnector() 方法之后才会监听端口

        logger.info("connector ok: $conn")

        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}war")
            .listFiles { _, fn -> fn.endsWith(".war") }?.iterator()?.forEach {
                tomcat.addWebapp("/war/${it.nameWithoutExtension}", it.absolutePath).apply {
                    urlList.add(this.path)
                }
            }

        // contextPath要使用的上下文映射，""表示根上下文
        // srvtest上下文的基础目录，用于静态文件。相对于服务器主目录必须存在 ({主目录}/webapps/{srvtest})
        FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}webapps${File.separator}srvtest")
        val ctx = tomcat.addContext("/globalServlet",  /*{webapps}/~*/"srvtest")

        Tomcat.addServlet(ctx, "cmd", this)
        ctx.addServletMappingDecoded("/", "cmd")
        urlList.add("globalServlet/")

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
        uploadDir = FileIO.bornDir(globalSetting["uploadDir"]?.toString() ?: uploadDir).absolutePath


        //upload files
        Tomcat.addServlet(ctx, "upload", UploadServlet(uploadDir))
        ctx.addServletMappingDecoded("/upload", "upload")
        //load uploaded wars
        enabledWars.apply {
            (globalSetting["enabledWars"]?.toString() ?: "[]").split(",").iterator().forEach {
                var warFile = it
                while (warFile.startsWith("[")) {
                    warFile = warFile.removeRange(0, 1)
                }
                while (warFile.endsWith("]")) {
                    warFile = warFile.removeRange(warFile.length - 1, warFile.length)
                }
                add(warFile.trim())
            }
        }.filter {
            val f = File(uploadDir, it)
            logger.debug("filter: $f")
            return@filter f.isFile && f.exists()
        }

        logger.debug("enabledWars: $enabledWars")
        FileIO.bornDir(uploadDir)
            .listFiles { _, fn -> fn.endsWith(".war") && enabledWars.contains(fn) }?.iterator()?.forEach {
                tomcat.addWebapp("/upload/${it.nameWithoutExtension}", it.absolutePath).apply {
                    urlList.add(this.path)
                }
            }
        //war manager
        Tomcat.addServlet(ctx, "warManager", WarsServlet(uploadDir, enabledWars))
        ctx.addServletMappingDecoded("/warManager", "warManager")
        urlList.add("globalServlet/warManager")
        //jar manager
        Tomcat.addServlet(ctx, "JarExec", JarServlet(uploadDir, enabledWars))
        ctx.addServletMappingDecoded("/JarExec", "JarExec")
        urlList.add("globalServlet/JarExec")
        //catalogue
        Tomcat.addServlet(ctx, "hello", object : HttpServlet() {
            private val serialVersionUID = 1L
            override fun service(request: HttpServletRequest, response: HttpServletResponse) {
                response.characterEncoding = "UTF-8"
                response.contentType = "text/html"
                response.setHeader("Server", "Embedded Tomcat")
                response.writer.use { writer ->
                    writer.write("Hello, Embedded Tomcat!")
                    urlList.iterator().forEach {
                        writer.write("<p><a href='$it'>linke to: [$it]</a></p>")
                    }
                    writer.flush()
                }
            }
        })
        ctx.addServletMappingDecoded("/hello", "hello")
        urlList.add("globalServlet/hello")

        tomcat.start()
        logger.info("tomcat started")
        tomcatThread = Thread {
            globalSetting["catBase"] = catBase
            globalSetting["hostName"] = hostName
            globalSetting["uploadDir"] = uploadDir
            globalSetting["enabledWars"] = enabledWars
            if (war.isNullOrBlank()) {
                globalSetting["port"] = port
                saveSetting(globalSettingFile)
            }
            logger.info("awaiting for ${tomcat.server.shutdown}")
            tomcat.server.await()
            logger.info("exiting await for ${tomcat.server.shutdown}")
        }.apply {
            start()
        }

        if (!war.isNullOrBlank() && File(war).exists() && war.endsWith(".war")) {
            tomcat.addWebapp("", File(war).absolutePath).apply {
                urlList.add("/")
            }
        } else {
            val rootDir = FileIO.bornDir("${tomcat.server.catalinaBase.absolutePath}${File.separator}root")
            val indexJsp = File(rootDir, "index.jsp")
            if (!indexJsp.exists()) {
                FileIO.getRootIndexJsp(indexJsp.toPath())
            }
            tomcat.addWebapp("", "${tomcat.server.catalinaBase.absolutePath}${File.separator}root").apply {
                urlList.add("/")
            }
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
            logger.debug("经过:${System.currentTimeMillis() - now}")
        }
        start()
        return tomcat?.server
    }

    private fun loadAllSetting(): MutableMap<String?, Map<Any, Any>> {
        try {
            globalSetting.apply {
                putAll(loadSetting(globalSettingFile))
                put("uptime", Date())
            }
            settings.put(globalSettingFile, globalSetting)
        } catch (_: Exception) {
        }

        Package.getPackages().iterator().forEach {
            val name = it.name
            packages.add(name)
            try {
                val classNames = ClassUtils.getClassName(name, true)
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

        logger.info("载入配置:${globalDefault()}")
        return settings
    }

    fun loadSetting(fileAndPath: String): Map<Any, Any> {
        val f = File(fileAndPath)
        if (!f.exists()) {
            logger.error("profile not found: $f")
            return HashMap()
        }
        val result = FileIO.loadProfile(fileAndPath)
        logger.info("profile($fileAndPath) loaded: $result")
        settings.put(fileAndPath, result)
        return result
    }

    fun saveSetting(fileAndPath: String) {
        if (isChild) {
            logger.error("children is no rights")
            return
        }
        FileIO.saveProfile(
            fileAndPath, HashMap<Any, Any>().apply {
                putAll(settings[fileAndPath] ?: HashMap())
                logger.info("profile($fileAndPath) stored: $this")
            })
    }

    fun saveSetting(any: Any) = saveSetting(any.javaClass.name)
    fun loadSetting(any: Any) = loadSetting(any.javaClass.name)
}