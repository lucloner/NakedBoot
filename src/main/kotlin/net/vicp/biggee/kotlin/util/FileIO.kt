package net.vicp.biggee.kotlin.util

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*

object FileIO {
    fun loadProfile(fileAndPath: String): Map<Any, Any> {
        val f = File(fileAndPath)
        val stream = Files.newInputStream(f.toPath())
        val p = Properties().apply {
            load(stream)
        }
        return p.toMap()
    }

    fun bornFile(file: String): File {
        var result = File(file)
        var cnt = 1
        while (result.exists()) {
            if (result.isDirectory) {
                result = File("${cnt++}$file")
            }
            result.delete()
        }
        return result
    }

    fun bornDir(dir: String): File {
        val file = checkDir(dir)
        var result = file
        var cnt = 1
        while (!result.isDirectory) {
            result = checkDir(dir + cnt++)
        }
        return result
    }

    fun checkDir(dir: String) = File(dir).apply {
        if (!exists()) {
            val result = mkdirs()
            if (!result) {
                throw IOException("Unable to create directory, could be a file with same name in path!")
            }
        }
    }
}