package net.vicp.biggee.java.util;

/**
 * @program: BluePrint
 * @description:
 * @author: Biggee
 * @create: 2019-12-03 16:34
 **/

import net.vicp.biggee.kotlin.util.FileIO;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class ClassUtils {
    public static void main(String[] args) throws Exception {
        String packageName = "net.vicp.biggee.kotlin.sys.core";
        Set<String> classNames = getClassName(packageName, false);
        if (classNames != null) {
            for (String className : classNames) {
                System.out.println(className);
            }
        }
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    public static Set<String> getClassName(String packageName, boolean isRecursion) {
        return getClassName(packageName, isRecursion, null);
    }

    public static Set<String> getClassName(String packageName, boolean isRecursion, HashSet<ZipFile> clzSet) {
        Set<String> classNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");

        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
                if (clzSet != null) {
                    try {
                        clzSet.add(FileIO.INSTANCE.tmpZip(url.getPath()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    if (clzSet != null) {
                        clzSet.add(jarFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (jarFile != null) {
                    classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            /*从所有的jar包中查找包名*/
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }

        return classNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath    文件路径
     * @param packageName 类名集合
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
        Set<String> className = new HashSet<String>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (files == null) {
            return className;
        }
        for (File childFile : files) {
            if (childFile.isDirectory()) {
                if (isRecursion) {
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
                }
            } else {
                String fileName = childFile.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    className.add(packageName + "." + fileName.replace(".class", ""));
                }
            }
        }

        return className;
    }

    /**
     * @param jarEntries
     * @param packageName
     * @param isRecursion
     * @return
     */
    private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<String>();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                /*
                 * 这里是为了方便，先把"/" 转成 "." 再判断 ".class" 的做法可能会有bug
                 * (FIXME: 先把"/" 转成 "." 再判断 ".class" 的做法可能会有bug)
                 */
                String entryName = jarEntry.getName().replace("/", ".");
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    entryName = entryName.replace(".class", "");
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else if (!entryName.replace(packageName + ".", "").contains(".")) {
                        classNames.add(entryName);
                    }
                }
            }
        }

        return classNames;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls        URL集合
     * @param packageName 包路径
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<String>();

        for (int i = 0; i < urls.length; i++) {
            String classPath = urls[i].getPath();

            //不必搜索classes文件夹
            if (classPath.endsWith("classes"+File.separator)) {
                continue;
            }

            JarFile jarFile = null;
            try {
                String jarPath=classPath.substring(classPath.indexOf(File.separator));
                if(!new File(jarPath).isFile()){
                    continue;
                }
                jarFile = new JarFile(jarPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }

        return classNames;
    }
}