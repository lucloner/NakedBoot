package net.vicp.biggee.kotlin.util

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*

object FileIO {
    fun loadProfile(fileAndPath: String): Map<Any, Any> = Properties().apply {
        load(
            Files.newInputStream(
                File(fileAndPath).toPath()
            )
        )
    }.toMap()

    fun saveProfile(fileAndPath: String, setting: Map<Any, Any>) = Properties().apply {
        setting.iterator().forEach {
            put(it.key.toString(),it.value.toString())
        }
    }.store(
        Files.newOutputStream(
            File(fileAndPath).toPath()
        ), fileAndPath
    )

    fun bornFile(file: String): File {
        var result = File(file)
        val path = result.parent
        val origName = result.name
        var cnt = 1
        while (result.exists()) {
            result = File(path, "${cnt++}$origName")
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