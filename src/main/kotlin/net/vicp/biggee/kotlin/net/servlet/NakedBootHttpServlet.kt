package net.vicp.biggee.kotlin.net.servlet

import org.slf4j.LoggerFactory
import javax.servlet.ServletContext
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class NakedBootHttpServlet : HttpServlet() {
    private val serialVersionUID = 7L
    protected var logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Writes the specified message to a servlet log file, prepended by the
     * servlet's name. See [ServletContext.log].
     *
     * @param message
     * a `String` specifying the message to be written to
     * the log file
     */
    override fun log(message: String) {
        logger.debug("$serialVersionUID\tlog\t$message")
        super.log(message)
    }

    /**
     * Writes an explanatory message and a stack trace for a given
     * `Throwable` exception to the servlet log file, prepended by
     * the servlet's name. See [ServletContext.log].
     *
     * @param message
     * a `String` that describes the error or exception
     * @param t
     * the `java.lang.Throwable` error or exception
     */
    override fun log(message: String, t: Throwable) {
        logger.error("$serialVersionUID\tlog\t$message\t${t.localizedMessage}")
        super.log(message, t)
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
        super.doPost(req, resp)
        logger.debug("$serialVersionUID\tdoPost\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     *
     * Receives an HTTP HEAD request from the protected
     * `service` method and handles the
     * request.
     * The client sends a HEAD request when it wants
     * to see only the headers of a response, such as
     * Content-Type or Content-Length. The HTTP HEAD
     * method counts the output bytes in the response
     * to set the Content-Length header accurately.
     *
     *
     * If you override this method, you can avoid computing
     * the response body and just set the response headers
     * directly to improve performance. Make sure that the
     * `doHead` method you write is both safe
     * and idempotent (that is, protects itself from being
     * called multiple times for one HTTP HEAD request).
     *
     *
     * If the HTTP HEAD request is incorrectly formatted,
     * `doHead` returns an HTTP "Bad Request"
     * message.
     *
     * @param req   the request object that is passed to the servlet
     *
     * @param resp  the response object that the servlet
     * uses to return the headers to the client
     *
     * @exception IOException   if an input or output error occurs
     *
     * @exception ServletException  if the request for the HEAD
     * could not be handled
     */
    override fun doHead(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doHead(req, resp)
        logger.debug("$serialVersionUID\tdoHead\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Called by the server (via the `service` method)
     * to allow a servlet to handle a DELETE request.
     *
     * The DELETE operation allows a client to remove a document
     * or Web page from the server.
     *
     *
     * This method does not need to be either safe
     * or idempotent. Operations requested through
     * DELETE can have side effects for which users
     * can be held accountable. When using
     * this method, it may be useful to save a copy of the
     * affected URL in temporary storage.
     *
     *
     * If the HTTP DELETE request is incorrectly formatted,
     * `doDelete` returns an HTTP "Bad Request"
     * message.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     *
     * @param resp  the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * DELETE request
     *
     * @exception ServletException  if the request for the
     * DELETE cannot be handled
     */
    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doDelete(req, resp)
        logger.debug("$serialVersionUID\tdoDelete\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Called by the server (via the `service` method)
     * to allow a servlet to handle an OPTIONS request.
     *
     * The OPTIONS request determines which HTTP methods
     * the server supports and
     * returns an appropriate header. For example, if a servlet
     * overrides `doGet`, this method returns the
     * following header:
     *
     *
     * `Allow: GET, HEAD, TRACE, OPTIONS`
     *
     *
     * There's no need to override this method unless the
     * servlet implements new HTTP methods, beyond those
     * implemented by HTTP 1.1.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     * @param resp  the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * OPTIONS request
     *
     * @exception ServletException  if the request for the
     * OPTIONS cannot be handled
     */
    override fun doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doOptions(req, resp)
        logger.debug("$serialVersionUID\tdoOptions\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Called by the server (via the `service` method)
     * to allow a servlet to handle a TRACE request.
     *
     * A TRACE returns the headers sent with the TRACE
     * request to the client, so that they can be used in
     * debugging. There's no need to override this method.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     * @param resp  the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * TRACE request
     *
     * @exception ServletException  if the request for the
     * TRACE cannot be handled
     */
    override fun doTrace(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doTrace(req, resp)
        logger.debug("$serialVersionUID\tdoTrace\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Called by the server (via the `service` method) to
     * allow a servlet to handle a GET request.
     *
     *
     * Overriding this method to support a GET request also
     * automatically supports an HTTP HEAD request. A HEAD
     * request is a GET request that returns no body in the
     * response, only the request header fields.
     *
     *
     * When overriding this method, read the request data,
     * write the response headers, get the response's writer or
     * output stream object, and finally, write the response data.
     * It's best to include content type and encoding. When using
     * a `PrintWriter` object to return the response,
     * set the content type before accessing the
     * `PrintWriter` object.
     *
     *
     * The servlet container must write the headers before
     * committing the response, because in HTTP the headers must be sent
     * before the response body.
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
     * The GET method should be safe, that is, without
     * any side effects for which users are held responsible.
     * For example, most form queries have no side effects.
     * If a client request is intended to change stored data,
     * the request should use some other HTTP method.
     *
     *
     * The GET method should also be idempotent, meaning
     * that it can be safely repeated. Sometimes making a
     * method safe also makes it idempotent. For example,
     * repeating queries is both safe and idempotent, but
     * buying a product online or modifying data is neither
     * safe nor idempotent.
     *
     *
     * If the request is incorrectly formatted, `doGet`
     * returns an HTTP "Bad Request" message.
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
     * the GET request
     *
     * @exception ServletException  if the request for the GET
     * could not be handled
     *
     * @see javax.servlet.ServletResponse.setContentType
     */
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)
        logger.debug("$serialVersionUID\tdoGet\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Returns the time the `HttpServletRequest`
     * object was last modified,
     * in milliseconds since midnight January 1, 1970 GMT.
     * If the time is unknown, this method returns a negative
     * number (the default).
     *
     *
     * Servlets that support HTTP GET requests and can quickly determine
     * their last modification time should override this method.
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * @param req   the `HttpServletRequest`
     * object that is sent to the servlet
     *
     * @return  a `long` integer specifying
     * the time the `HttpServletRequest`
     * object was last modified, in milliseconds
     * since midnight, January 1, 1970 GMT, or
     * -1 if the time is not known
     */
    override fun getLastModified(req: HttpServletRequest): Long {
        logger.debug("$serialVersionUID\tgetLastModified\t$req")
        return super.getLastModified(req)
    }

    /**
     * Called by the server (via the `service` method)
     * to allow a servlet to handle a PUT request.
     *
     * The PUT operation allows a client to
     * place a file on the server and is similar to
     * sending a file by FTP.
     *
     *
     * When overriding this method, leave intact
     * any content headers sent with the request (including
     * Content-Length, Content-Type, Content-Transfer-Encoding,
     * Content-Encoding, Content-Base, Content-Language, Content-Location,
     * Content-MD5, and Content-Range). If your method cannot
     * handle a content header, it must issue an error message
     * (HTTP 501 - Not Implemented) and discard the request.
     * For more information on HTTP 1.1, see RFC 2616
     * [](http://www.ietf.org/rfc/rfc2616.txt).
     *
     *
     * This method does not need to be either safe or idempotent.
     * Operations that `doPut` performs can have side
     * effects for which the user can be held accountable. When using
     * this method, it may be useful to save a copy of the
     * affected URL in temporary storage.
     *
     *
     * If the HTTP PUT request is incorrectly formatted,
     * `doPut` returns an HTTP "Bad Request" message.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     * @param resp  the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * PUT request
     *
     * @exception ServletException  if the request for the PUT
     * cannot be handled
     */
    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doPut(req, resp)
        logger.debug("$serialVersionUID\tdoPut\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Receives standard HTTP requests from the public
     * `service` method and dispatches
     * them to the `do`*Method* methods defined in
     * this class. This method is an HTTP-specific version of the
     * [javax.servlet.Servlet.service] method. There's no
     * need to override this method.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     * @param resp  the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * HTTP request
     *
     * @exception ServletException  if the HTTP request
     * cannot be handled
     *
     * @see javax.servlet.Servlet.service
     */
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        super.service(req, resp)
        logger.debug("$serialVersionUID\tservice\t${req.parameterMap.toMap()}\t$resp")
    }

    /**
     * Dispatches client requests to the protected
     * `service` method. There's no need to
     * override this method.
     *
     * @param req   the [HttpServletRequest] object that
     * contains the request the client made of
     * the servlet
     *
     * @param res   the [HttpServletResponse] object that
     * contains the response the servlet returns
     * to the client
     *
     * @exception IOException   if an input or output error occurs
     * while the servlet is handling the
     * HTTP request
     *
     * @exception ServletException  if the HTTP request cannot
     * be handled
     *
     * @see javax.servlet.Servlet.service
     */
    override fun service(req: ServletRequest, res: ServletResponse) {
        super.service(req, res)
        logger.debug("$serialVersionUID\tservice\t${req.parameterMap.toMap()}\t$res")
    }
}