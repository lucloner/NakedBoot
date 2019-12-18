<%@ page import="net.vicp.biggee.java.sys.BluePrint" %>
<%@ page import="net.vicp.biggee.kotlin.sys.core.NakedBoot" %>
<%@ page import="net.vicp.biggee.kotlin.util.FileIO" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.jar.JarFile" %>
<%--
  Created by IntelliJ IDEA.
  User: Lucloner
  Date: 2019-12-03
  Time: 下午 1:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>NakedBoot(<%=BluePrint.Ext_Key%>)</title>
</head>
<body>
<p>本程序tomcat混合多进程容器，容器旨在管理多个站点时可以互相独立，并且运作在不同的进程中。</p>
<p>名称:NakedBoot</p>
<p>识别参数:<%=BluePrint.Ext_Key%><br/>此参数在调试时为notJar在打包运行时会出现，用于识别自有核心jar包。</p>
<p>是否副本（子容器）:<%=NakedBoot.isChild()%>
</p>
<p>上传目录:<%=NakedBoot.getUploadDir()%>
</p>
<p>内嵌tomcat运行:<%=NakedBoot.INSTANCE.getTomcat() != null%>
</p>
<p>主体为war:<%=BluePrint.isWar%>
</p>
<hr/>
<label for="logger">log: </label><input type="text" id="logger" width="100%"/><br/>
<hr/>
<form action="globalServlet/upload" method="post" enctype="multipart/form-data">
    请选择要上传的文件：
    　　　<input type="file" id="upload" name="upload"/>
    　　　<input type="submit" value="Upload"/>
</form>
<br/>
<a href="globalServlet/warManager">管理上传</a><br/>
<a href="globalServlet/?cmd=flush">清空上传</a><br/>
<%
    if (BluePrint.isWar) {
%>
This war in Tomcat!<br/>
<%
} else {
%>
This jar in Java!<br/>
<hr/>
<p>服务网址列表:</p>
<div id="hello"></div>
<br/>
<hr/>
<a href="globalServlet/?cmd=stop">停止服务器</a><br/>
<a href="globalServlet/?cmd=start">开始服务器</a><br/>
<a href="globalServlet/?cmd=restart">重启服务器</a><br/>
<a href="globalServlet/?cmd=halt">退出应用</a><br/>
<%
    }
%>
<br/>
上传的文件有：
<%
    for (final File f : NakedBoot.INSTANCE.uploadFiles()) {
        out.println("[" + f.getAbsolutePath() + "]");
    }
    if (NakedBoot.INSTANCE.uploadFiles().length == 0) {
        out.println("无");
    }
%>
<br/>
其中war:
<%
    for (final File f : NakedBoot.INSTANCE.uploadFilesWar()) {
        out.println("[" + f.getAbsolutePath() + "]");
    }
    if (NakedBoot.INSTANCE.uploadFilesWar().size() == 0) {
        out.println("无");
    }
%>
<br/>
其中jar：
<%
    final ArrayList<String> coreJars = new ArrayList<>();
    for (final File f : NakedBoot.INSTANCE.uploadFilesJar()) {
        out.println("[" + f.getAbsolutePath() + "]");
        try {
            if (FileIO.INSTANCE.isCoreJar(new JarFile(f), BluePrint.Ext_Key)) {
                coreJars.add(FileIO.INSTANCE.getMajorName(f.getName()));
                out.println("[&lt;&lt;&lt;CORE!!!]");
            }
            out.println("&nbsp;");
        } catch (Exception ignored) {
        }
    }
    if (NakedBoot.INSTANCE.uploadFilesJar().size() == 0) {
        out.println("无");
    }
%>
<br/>
<hr/>
请选择需要启动核心jar与关联的war，并填写端口：
<form action="globalServlet/JarExec" method="get">
    <label for="jar">核心jar包: </label>
    <select id="jar" name="jar">
        <%
            for (final String f : coreJars) {
                out.println("<option value='" + f + "'>" + f + "</option>");
            }
        %>
    </select>
    　　　<label for="port">port: </label><input type="text" id="port" name="port"/><br/>
    <label for="war">war包: </label>
    <select id="war" name="war">
        <%
            for (final File f : NakedBoot.INSTANCE.uploadFilesWar()) {
                if (f.isFile() && f.getName().endsWith(".war")) {
                    out.println("<option value='" + f.getAbsolutePath() + "'>" + FileIO.INSTANCE.getMajorName(f.getAbsolutePath()) + "</option>");
                }
            }
        %>
    </select>
    <input type="submit" value="运行"/>
</form>

</body>
<script type="text/javascript">
    //此处没有甬到
    const ajax = new XMLHttpRequest();
    const logger = document.getElementById("logger");
    let result = "无";
    let loop = true;
    ajax.onload = function () {
        result = "无";
        if (ajax.status === 200) {
            result = ajax.responseText;
        } else {
            result = "error:" + ajax.status;
        }
        logger.value = ("玩命加载完成，反馈:" + result);
    };
    ajax.onloadend = function () {
        loop = false;
    };

    function ajax_get(url) {
        let met = 'get';
        url = document.URL + url;
        ajax.open(met, url);
        ajax.send();
        logger.value = ("玩命加载中:" + url);
        return result;
    }

    <%
        if (!BluePrint.isWar) {
    %>
    const hello = document.getElementById("hello");
    ajax_get("globalServlet/hello");
    ajax.onload = function () {
        if (ajax.status === 200) {
            hello.innerHTML += ajax.responseText;
        }
    };
    <%
        }
    %>

</script>
</html>
