package net.vicp.biggee.kotlin.net.servlet

import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class UploadServlet(upload: String) : NakedBootHttpServlet() {
    private val serialVersionUID = 2L
    private var isMultipart = false
    private val maxFileSize = 1_000_000_000
    private val maxMemSize = 1_000_000_000
    private val uploadDir: String

    constructor() : this(NakedBoot.uploadDir)

    init {
        var dir = upload
        try {
            FileIO.bornDir(upload)
        } catch (_: Exception) {
            dir = createTempDir().absolutePath
        }
        logger.info("=============upload path:$dir")
        uploadDir = dir
        NakedBoot.globalSetting["uploadDir"] = uploadDir
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        // 检查是否有一个文件上传请求
        isMultipart = ServletFileUpload.isMultipartContent(req)
        logger.debug("上传开始")
        resp.contentType = "text/html;charset=utf-8"
        if (!isMultipart) {
            resp.writer.println("未获取到文件")
            return
        }
        val factory = DiskFileItemFactory()
        // 文件大小的最大值将被存储在内存中
        // 文件大小的最大值将被存储在内存中
        factory.sizeThreshold = maxMemSize
        // Location to save data that is larger than maxMemSize.
        // Location to save data that is larger than maxMemSize.

        //factory.repository=uploadDir
        // 创建一个新的文件上传处理程序
        // System.out.println(path);
        // 创建一个新的文件上传处理程序
        val upload = ServletFileUpload(factory)
        // 允许上传的文件大小的最大值
        // 允许上传的文件大小的最大值
        upload.sizeMax = maxFileSize.toLong()
        val stringBuilder = StringBuilder()
        upload.parseParameterMap(req).values.iterator().forEach { list ->
            logger.trace("获取到上传列表:$list")
            list.iterator().forEach { fItem ->
                logger.trace("获取到上传元素:$fItem")
                if (fItem.fieldName.contains("upload")) {
                    val name = fItem.name.split(File.separator).last()
                    try {
                        val f = FileIO.bornFile("${uploadDir}${File.separator}$name")
                        logger.trace("写入文件:${f.absolutePath}")
                        fItem.write(f)
                        logger.trace("完成写入文件:${f.exists()}")
                        stringBuilder.append("上传成功${f.absolutePath}\n")
                    } catch (e: Exception) {
                        stringBuilder.append("上传失败$name\n")
                    }
                }
            }
        }
        resp.writer.println(stringBuilder.append("完成").toString())
        logger.debug("上传结束")
    }
}