<%@ page import="net.vicp.biggee.java.sys.BluePrint" %>
<%@ page import="net.vicp.biggee.kotlin.sys.core.NakedBoot" %>
<%@ page import="java.io.File" %>
<%--
  Created by IntelliJ IDEA.
  User: Lucloner
  Date: 2019-12-03
  Time: 下午 1:06
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<script type="text/javascript">
    let warlist = [];
    let jarlist = [];
</script>
<head>
    <title>NakedBoot</title>
</head>
<body>
<form action="globalServlet/upload" method="post" enctype="multipart/form-data">
    　　　<input type="file" id="upload" name="upload"/>
    　　　<input type="submit" value="Upload"/>
</form>
<br/>
<a href="globalServlet/warManager">管理上传</a><br/>
<%
    if (BluePrint.isWar) {
%>
This war in Tomcat!<br/>
<%
} else {
%>
This jar in Java!<br/>
<a href="globalServlet/?cmd=flush">清空上传</a><br/>
<a href="globalServlet/?cmd=stop">停止服务器</a><br/>
<a href="globalServlet/?cmd=start">开始服务器</a><br/>
<a href="globalServlet/?cmd=restart">重启服务器</a><br/>
<%
    }
%>
<br/>
<label for="logger">log: </label><input type="text" id="logger"/><br/>
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
    for (final File f : NakedBoot.INSTANCE.uploadFilesJar()) {
        out.println("[" + f.getAbsolutePath() + "]");
    }
    if (NakedBoot.INSTANCE.uploadFilesJar().size() == 0) {
        out.println("无");
    }
%>
<br/>
请选择需要启动核心jar与关联的war，并填写端口：
<form action="globalServlet/JarExec" method="get">
    <label for="jar">核心jar包: </label>
    <select id="jar" name="jar">
        <%
            for (final File f : NakedBoot.INSTANCE.uploadFilesJar()) {
                out.println("<option value='" + f.getName() + "'>" + f.getName() + "</option>");
            }
        %>
    </select>
    　　　<label for="port">port: </label><input type="text" id="port" name="port"/><br/>
    <label for="war">war包: </label>
    <select id="war" name="war">
        <%
            for (final File f : NakedBoot.INSTANCE.uploadFilesWar()) {
                out.println("<option value='" + f.getName() + "'>" + f.getName() + "</option>");
            }
        %>
    </select>
    <input type="submit" value="运行"/>

    <button type="button" onclick="ajax_get('globalServlet/?cmd=flush')">点我!</button>
</form>

</body>
<script type="text/javascript">
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
</script>
</html>
