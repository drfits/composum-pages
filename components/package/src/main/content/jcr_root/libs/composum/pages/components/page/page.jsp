<%@page session="false" pageEncoding="utf-8" %><%--
--%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><cpp:defineObjects/>
<cpp:model var="pageModel" type="com.composum.pages.commons.model.Page" scope="request">
    <html ${pageModel.htmlLangAttribute} ${pageModel.htmlDirAttribute}
            class="${pageModel.htmlClasses}" data-context-path="${slingRequest.contextPath}">
    <cpp:head>
        <sling:call script="head.jsp"/>
    </cpp:head>
    <cpp:body>
        <sling:call script="navbar.jsp"/>
        <sling:call script="main.jsp"/>
        <sling:call script="token.jsp"/>
        <sling:call script="script.jsp"/>
    </cpp:body>
    </html>
</cpp:model>
