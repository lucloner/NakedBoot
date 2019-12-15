package net.vicp.biggee.kotlin.net.servlet

import net.vicp.biggee.kotlin.sys.core.NakedBoot
import net.vicp.biggee.kotlin.util.FileIO
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload
import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class UploadServlet(upload: String) : HttpServlet() {
    private val serialVersionUID = 2L
    private var isMultipart = false
    private val maxFileSize = 1_000_000_000
    private val maxMemSize = 1_000_000_000
    private val logger by lazy { LoggerFactory.getLogger(UploadServlet::class.java) }
    private val uploadDir: String

    constructor() : this(
        NakedBoot.globalSetting["uploadDir"]?.toString()
            ?: NakedBoot.loadAllSetting()[NakedBoot.globalSettingFile]?.get("uploadDir").toString()
    )


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

    /**
     * Called by the server (via the `service` method)
     * to allow a servlet to handle a POST request.
     *
     * The HTTP POST method allows the client to send
     * data of unlimited length to the Web server a single time
     * and is useful when posting information such as
     * credit card numbers.
     *
     *
     * When overriding this method, read the request data,
     * write the response headers, get the response's writer or output
     * stream object, and finally, write the response data. It's best
     * to include content type and encoding. When using a
     * `PrintWriter` object to return the response, set the
     * content type before accessing the `PrintWriter` object.
     *
     *
     * The servlet container must write the headers before committing the
     * response, because in HTTP the headers must be sent before the
     * response body.
     *
     *
     * Where possible, set the Content-Length header (with the
     * [javax.servlet.ServletResponse.setContentLength] method),
     * to allow the servlet container to use a persistent connection
     * to return its response to the client, improving performance.
     * The content length is automatically set if the entire response fits
     * inside the response buffer.
     *
     *
     * When using HTTP 1.1 chunked encoding (which means that the response
     * has a Transfer-Encoding header), do not set the Content-Length header.
     *
     *
     * This method does not need to be either safe or idempotent.
     * Operations requested through POST can have side effects for
     * which the user can be held accountable, for example,
     * updating stored data or buying items online.
     *
     *
     * If the HTTP POST request is incorrectly formatted,
     * `doPost` returns an HTTP "Bad Request" message.
     *
     *
     * @param req   an [HttpServletRequest] object that
     * contains the request the client has made
     * of the servlet
     *
     * @param resp  an [HttpServletResponse] object that
     * contains the response the servlet sends
     * to the client
     *
     * @exception IOException   if an input or output error is
     * detected when the servlet handles
     * the request
     *
     * @exception ServletException  if the request for the POST
     * could not be handled
     *
     * @see javax.servlet.ServletOutputStream
     *
     * @see javax.servlet.ServletResponse.setContentType
     */
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
                    val name = fItem.name
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