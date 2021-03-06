<%@page session="false" pageEncoding="UTF-8" %><%--
--%><%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><cpp:defineObjects/>
<cpp:element var="code" type="com.composum.pages.components.model.codeblock.CodeBlock" tagName="none">
    <c:choose>
    <c:when test="${code.valid}">
        <div class="${codeCssBase}_panel panel panel-default">
            <c:if test="${code.showHeading}">
                <div class="${codeCssBase}_heading panel-heading">
                    <cpn:text tagName="span" tagClass="${codeCssBase}_title" value="${code.title}"/>
                    <c:if test="${code.showLanguage}">
                        <cpn:text tagName="span" tagClass="${codeCssBase}_language badge" value="${code.codeLanguage}"/>
                    </c:if>
                    <c:if test="${code.collapsible}">
                        <cpn:text tagName="button" tagClass="${codeCssBase}_button btn btn-xs btn-default ${codeCssBase}_expand"
                                  value="expand code"/>
                        <cpn:text tagName="button" tagClass="${codeCssBase}_button btn btn-xs btn-default ${codeCssBase}_collapse"
                                  value="collapse code"/>
                    </c:if>
                </div>
            </c:if>
            <div class="${codeCssBase}_content-block panel-body">
                <pre class="${codeCssBase}_content"><code
                        class="${code.codeLanguage}"><cpn:text value="${code.code}"/></code></pre>
            </div>
            <c:if test="${code.hasCopyright}">
                <cpn:text tagClass="${codeCssBase}_footer panel-footer" value="${code.copyright}"/>
            </c:if>
        </div>
</c:when>
    <c:otherwise>
        <cpp:include replaceSelectors="placeholder"/>
    </c:otherwise>
    </c:choose>
</cpp:element>
