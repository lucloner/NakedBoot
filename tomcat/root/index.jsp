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
  <title>$Title$</title>
</head>
<body>
<%=getClass()%>
$END$
<form action="globalServlet/upload" method="post" enctype="multipart/form-data">
  　　　<input type="file" id="upload" name="upload"/>
  　　　<input type="submit" value="Upload"/>
</form>
<br/>
<a href="globalServlet/?cmd=commitUpload">应用上传</a><br/>
<a href="globalServlet/?cmd=flush">清空上传</a><br/>
<a href="globalServlet/?cmd=stop">停止服务器</a><br/>
<a href="globalServlet/?cmd=start">开始服务器</a><br/>
<a href="globalServlet/?cmd=restart">重启服务器</a><br/>
</body>

</html>
