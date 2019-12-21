package net.vicp.biggee.kotlin.net.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import net.vicp.biggee.java.sys.BluePrint
import net.vicp.biggee.kotlin.sys.core.NakedBoot
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class NakedRpcServer {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {
        /* The port on which the server should run */
        val port = 7574
        server = ServerBuilder.forPort(port)
            .addService(GreeterImpl())
            .build()
            .start()
        logger.log(Level.INFO, "Server started, listening on {0}", port)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@NakedRpcServer.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server?.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server?.awaitTermination()
    }

    internal class GreeterImpl : GreeterGrpc.GreeterImplBase() {
        private val alternateLogger = LoggerFactory.getLogger(this::class.java.name)

        private fun normalRequest(): HelloReply =
            HelloReply.newBuilder().setMessage(BluePrint.Ext_Key).setSuid(suid).build()

        override fun sayHello(req: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
            if (req.key == suid) {
                try {
                    responseObserver.onNext(normalRequest())
                } catch (e: Exception) {
                    responseObserver.onError(IllegalAccessError("$suid${e.localizedMessage}"))
                }
            }
            responseObserver.onCompleted()
        }

        override fun tellLog(
            request: net.vicp.biggee.kotlin.net.grpc.Logger?,
            responseObserver: StreamObserver<HelloReply>?
        ) {
            try {
                alternateLogger.info(request!!.message)
                responseObserver!!.onNext(normalRequest())
                return
            } catch (e: Exception) {
                responseObserver?.onError(IllegalAccessError("$suid${e.localizedMessage}"))
            }
            responseObserver?.onCompleted()
        }
    }

    companion object {
        private val logger = Logger.getLogger(NakedRpcServer::class.java.name)
        @JvmStatic
        private val suid = UUID.randomUUID().toString()
        var INSTANCE: NakedRpcServer? = null

        fun deployment() {
            try {
                if (!NakedBoot.isChild) {
                    INSTANCE = NakedRpcServer()
                    INSTANCE!!.start()
                }
            } catch (_: Exception) {
            }
            NakedRpcClient.sayHello()
        }

        /**
         * Main launches the server from the command line.
         */
        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = NakedRpcServer()
            server.start()
            server.blockUntilShutdown()
        }
    }
}