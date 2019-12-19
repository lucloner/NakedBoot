package net.vicp.biggee.kotlin.net.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class NakedRpcClient
/** Construct client for accessing RouteGuide server using the existing channel.  */
internal constructor(private val channel: ManagedChannel) {
    private val blockingStub: GreeterGrpc.GreeterBlockingStub = GreeterGrpc.newBlockingStub(channel)

    /** Construct client connecting to HelloWorld server at `host:port`.  */
    constructor(host: String, port: Int) : this(
        ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build()
    )


    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    /** Say hello to server.  */
    fun greet(name: String) {
        logger.log(Level.INFO, "Will try to greet {0}...", name)
        val request = HelloRequest.newBuilder().setName(name).build()
        val response: HelloReply = try {
            blockingStub.sayHello(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return
        }

        logger.info("Greeting: ${response.message}")
    }

    companion object {
        private val logger = Logger.getLogger(NakedRpcClient::class.java.name)

        /**
         * Greet server. If provided, the first element of `args` is the name to use in the
         * greeting.
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = NakedRpcClient("localhost", 7574)
            try {
                /* Access a service running on the local machine on port 50051 */
                val user = if (args.size > 0) "${args[0]}" else "world"
                client.greet(user)
            } finally {
                client.shutdown()
            }
        }
    }
}