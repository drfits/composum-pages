<%@page session="false" pageEncoding="utf-8" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><cpp:defineFrameObjects/>
<cpp:widget label="Title" property="title" type="text" i18n="true"/>
<cpp:widget label="Text" property="text" type="richtext" i18n="true" height="200px"/>
<cpp:widget label="Next" property="next" type="link" context="container"/>
