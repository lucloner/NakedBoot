package net.vicp.biggee.kotlin.net.servlet

import org.slf4j.LoggerFactory
import javax.servlet.ServletContext
import javax.servlet.http.HttpServlet

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
        logger.debug(message)
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
        logger.error("$message\n${t.localizedMessage}")
        super.log(message, t)
    }

}