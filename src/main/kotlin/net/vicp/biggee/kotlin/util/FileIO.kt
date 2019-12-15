package net.vicp.biggee.kotlin.util

import java.io.*
import java.nio.file.Files
import java.util.*
import java.util.zip.*


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

    /** 缓冲器大小  */
    private const val BUFFER = 512

    /**压缩得到的文件的后缀名 */
    //private const val SUFFIX = ".zip"

    /**
     * 得到源文件路径的所有文件
     * @param dirFile 压缩源文件路径
     */
    fun getAllFile(dirFile: File): List<File> {
        val fileList = ArrayList<File>()
        val files = dirFile.listFiles() ?: return fileList
        for (file in files) { //文件
            if (file.isFile) {
                fileList.add(file)
                println("add file:" + file.name)
            } else { //目录
                if (!file.listFiles().isNullOrEmpty()) { //非空目录
                    fileList.addAll(getAllFile(file)) //把递归文件加到fileList中
                } else { //空目录
                    fileList.add(file)
                    println("add empty dir:" + file.name)
                }
            }
        }
        return fileList
    }

    /**
     * 获取相对路径
     * @param dirPath 源文件路径
     * @param file2Comp 准备压缩的单个文件
     */
    fun getRelativePath(dirPath: String, file2Comp: File): String {
        var file = file2Comp
        val dirFile = File(dirPath)
        var relativePath = file.name
        while (true) {
            file = file.parentFile
            if (file == null) break
            relativePath = if (file == dirFile) {
                break
            } else {
                file.name + File.separator + relativePath
            }
        }
        return relativePath
    }

    /**
     * @param destPath 解压目标路径
     * @param fileName 解压文件的相对路径
     */
    fun createFile(destPath: String, fileName: String): File {
        val dirs = fileName.split("/").toTypedArray() //将文件名的各级目录分解
        var file = File(destPath)
        return if (dirs.size > 1) { //文件有上级目录
            for (i in 0 until dirs.size - 1) {
                file = File(file, dirs[i]) //依次创建文件对象知道文件的上一级目录
            }
            if (!file.exists()) {
                file.mkdirs() //文件对应目录若不存在，则创建
                try {
                    println("mkdirs: " + file.canonicalPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            file = File(file, dirs[dirs.size - 1]) //创建文件
            file
        } else {
            if (!file.exists()) { //若目标路径的目录不存在，则创建
                file.mkdirs()
                try {
                    println("mkdirs: " + file.canonicalPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            file = File(file, dirs[0]) //创建文件
            file
        }
    }

    /**
     * 没有指定压缩目标路径进行压缩,用默认的路径进行压缩
     * @param dirPath 压缩源文件路径
     */
    fun compress(dirPath: String) {
        val firstIndex = dirPath.indexOf("/")
        val lastIndex = dirPath.lastIndexOf("/")
        val zipFileName = dirPath.substring(0, firstIndex + 1) + dirPath.substring(lastIndex + 1)
        compress(dirPath, zipFileName)
    }

    /**
     * 压缩文件
     * @param dirPath 压缩源文件路径
     * @param zipFile 压缩目标文件路径
     */
    fun compress(dirPath: String, zipFile: String) {
        val zipFileName = zipFile
        //zipFileName += SUFFIX //添加文件的后缀名
        val dirFile = File(dirPath)
        val fileList = getAllFile(dirFile)
        val buffer = ByteArray(BUFFER)
        var zipEntry: ZipEntry
        var readLength: Int //每次读取出来的长度
        try { // 对输出文件做CRC32校验
            val cos = CheckedOutputStream(
                FileOutputStream(
                    zipFileName
                ), CRC32()
            )
            val zos = ZipOutputStream(cos)
            for (file in fileList) {
                if (file.isFile) { //若是文件，则压缩文件
                    zipEntry = ZipEntry(getRelativePath(dirPath, file)) //
                    zipEntry.size = file.length()
                    zipEntry.time = file.lastModified()
                    zos.putNextEntry(zipEntry)
                    val `is`: InputStream = BufferedInputStream(FileInputStream(file))
                    while (`is`.read(buffer, 0, BUFFER).also { readLength = it } != -1) {
                        zos.write(buffer, 0, readLength)
                    }
                    `is`.close()
                    println("file compress:" + file.canonicalPath)
                } else { //若是空目录，则写入zip条目中
                    zipEntry = ZipEntry(getRelativePath(dirPath, file))
                    zos.putNextEntry(zipEntry)
                    println("dir compress: " + file.canonicalPath + "/")
                }
            }
            zos.close() //最后得关闭流，不然压缩最后一个文件会出错
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 解压
     */
    fun decompress(zipFile: String, destPath: String) {
        val zipFileName = zipFile
        try {
            //zipFileName += SUFFIX
            val zis = ZipInputStream(FileInputStream(zipFileName))
            var zipEntry: ZipEntry
            val buffer = ByteArray(BUFFER) //缓冲器
            var readLength: Int //每次读出来的长度
            while (zis.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry.isDirectory) { //若是目录
                    val file = File(destPath + "/" + zipEntry.name)
                    if (!file.exists()) {
                        file.mkdirs()
                        println("mkdirs:" + file.canonicalPath)
                        continue
                    }
                } //若是文件
                val file = createFile(destPath, zipEntry.name)
                println("file created: " + file.canonicalPath)
                val os: OutputStream = FileOutputStream(file)
                while (zis.read(buffer, 0, BUFFER).also { readLength = it } != -1) {
                    os.write(buffer, 0, readLength)
                }
                os.close()
                println("file uncompressed: " + file.canonicalPath)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}