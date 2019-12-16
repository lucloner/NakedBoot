package net.vicp.biggee.kotlin.util

import net.vicp.biggee.java.sys.BluePrint
import org.apache.log4j.Logger
import java.io.*
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.zip.*


object FileIO {
    @JvmStatic
    var logger: Logger? = null
    /** 缓冲器大小  */
    private const val BUFFER = 512
    //创建临时目录
    private val tmpDir by lazy {
        val f = createTempDir()
        logger?.info("tmpDir:${f.absolutePath}")
        return@lazy f
    }

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


    /**压缩得到的文件的后缀名 */
    //private const val SUFFIX = ".zip"

    /**
     * 得到源文件路径的所有文件
     * @param dirFile 压缩源文件路径
     */
    fun getAllFile(dirFile: File): List<File> {
        val fileList = ArrayList<File>()
        if (dirFile.isFile) {
            return listOf(dirFile)
        }
        val files = dirFile.listFiles() ?: return fileList
        for (file in files) { //文件
            if (file.isFile) {
                fileList.add(file)
                logger?.info("add file:" + file.name)
            } else { //目录
                if (!file.listFiles().isNullOrEmpty()) { //非空目录
                    fileList.addAll(getAllFile(file)) //把递归文件加到fileList中
                } else { //空目录
                    fileList.add(file)
                    logger?.info("add empty dir:" + file.name)
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
                    logger?.info("mkdirs: " + file.canonicalPath)
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
                    logger?.info("mkdirs: " + file.canonicalPath)
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
                    logger?.info("file compress:" + file.canonicalPath)
                } else { //若是空目录，则写入zip条目中
                    zipEntry = ZipEntry(getRelativePath(dirPath, file))
                    zos.putNextEntry(zipEntry)
                    logger?.info("dir compress: " + file.canonicalPath + "/")
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
            while (zis.available() > 0) {
                zipEntry = zis.nextEntry ?: break
                //zis.nextEntry.also { zipEntry = it } != null
                if (zipEntry.isDirectory) { //若是目录
                    val file = File(destPath + "/" + zipEntry.name)
                    if (!file.exists()) {
                        file.mkdirs()
                        logger?.info("mkdirs:" + file.canonicalPath)
                        continue
                    }
                } //若是文件
                val file = createFile(destPath, zipEntry.name)
                logger?.info("file created: " + file.canonicalPath)
                val os: OutputStream = FileOutputStream(file)
                while (zis.read(buffer, 0, BUFFER).also { readLength = it } != -1) {
                    os.write(buffer, 0, readLength)
                }
                os.close()
                logger?.info("file uncompressed: " + file.canonicalPath)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun tmpZip(file: String): ZipFile? {
        val origFile = File(file)
        val destFile: File = bornFile(File(tmpDir, "${origFile.name}.zip").absolutePath)
        compress(origFile.absolutePath, destFile.absolutePath)
        if (destFile.exists()) {
            return ZipFile(destFile)
        }
        return null
    }

    fun collectClz(pak: Package = BluePrint::class.java.`package`): String? {
        val clzLoader = Thread.currentThread().contextClassLoader
        val packagePath: String = pak.name.replace(".", "/")
        val url: URL = clzLoader.getResource(packagePath) ?: return null
        if (url.protocol == "file") {
            val rootPath = File(url.path.replace(packagePath, ""))
            if (rootPath.exists()) {
                return rootPath.absolutePath
            }
        } else {
            val jarFile = File((url.openConnection() as JarURLConnection).jarFileURL.toURI())
            if (jarFile.exists()) {
                return jarFile.absolutePath
            }
        }
        return null
    }

    fun xCopy(src: Path, dstPath: Path): Path = Files.walkFileTree(src, object : FileVisitor<Path> {
        val dst by lazy { Path.of(bornDir(dstPath.toFile().absolutePath + File.separator + src.fileName).absolutePath) }

        /**
         * Invoked for a directory after entries in the directory, and all of their
         * descendants, have been visited. This method is also invoked when iteration
         * of the directory completes prematurely (by a [visitFile][.visitFile]
         * method returning [SKIP_SIBLINGS][FileVisitResult.SKIP_SIBLINGS],
         * or an I/O error when iterating over the directory).
         *
         * @param   dir
         * a reference to the directory
         * @param   exc
         * `null` if the iteration of the directory completes without
         * an error; otherwise the I/O exception that caused the iteration
         * of the directory to complete prematurely
         *
         * @return  the visit result
         *
         * @throws  IOException
         * if an I/O error occurs
         */
        override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
            return FileVisitResult.CONTINUE
        }

        /**
         * Invoked for a file in a directory.
         *
         * @param   file
         * a reference to the file
         * @param   attrs
         * the file's basic attributes
         *
         * @return  the visit result
         *
         * @throws  IOException
         * if an I/O error occurs
         */
        override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
            try {
                file.toFile().copyTo(
                    File(
                        file.toFile().absolutePath.replace(
                            src.toFile().absolutePath,
                            dst.toFile().absolutePath
                        )
                    ), true
                )
            } catch (e: Exception) {
                logger?.error(e.localizedMessage)
            }
            return FileVisitResult.CONTINUE
        }

        /**
         * Invoked for a file that could not be visited. This method is invoked
         * if the file's attributes could not be read, the file is a directory
         * that could not be opened, and other reasons.
         *
         * @param   file
         * a reference to the file
         * @param   exc
         * the I/O exception that prevented the file from being visited
         *
         * @return  the visit result
         *
         * @throws  IOException
         * if an I/O error occurs
         */
        override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
            return FileVisitResult.CONTINUE
        }

        /**
         * Invoked for a directory before entries in the directory are visited.
         *
         *
         *  If this method returns [CONTINUE][FileVisitResult.CONTINUE],
         * then entries in the directory are visited. If this method returns [ ][FileVisitResult.SKIP_SUBTREE] or [ ][FileVisitResult.SKIP_SIBLINGS] then entries in the
         * directory (and any descendants) will not be visited.
         *
         * @param   dir
         * a reference to the directory
         * @param   attrs
         * the directory's basic attributes
         *
         * @return  the visit result
         *
         * @throws  IOException
         * if an I/O error occurs
         */
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
            try {
                dir.toFile().copyTo(
                    File(
                        dir.toFile().absolutePath.replace(
                            src.toFile().absolutePath,
                            dst.toFile().absolutePath
                        )
                    ), true
                )
            } catch (e: Exception) {
                logger?.error(e.localizedMessage)
            }
            return FileVisitResult.CONTINUE
        }

    })


}