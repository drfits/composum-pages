<%@page session="false" pageEncoding="UTF-8" %><%--
--%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><cpp:defineFrameObjects/>
<cpp:element var="release" type="com.composum.pages.commons.model.Release" mode="none">
    <li class="${releaseCssBase}_listentry">
        <input type="radio" class="${releaseCssBase}_select" name="${releaseCssBase}_select" value="${release.key}" />
        <div class="${releaseCssBase}_entry" data-path="${release.path}">
            <div class="${releaseCssBase}_head">
                <span class="${releaseCssBase}_key">${release.key}</span>
                <c:forEach items="${release.categories}" var="category">
                    <span class="label label-primary ${releaseCssBase}_category">${category}</span>
                </c:forEach>
                <span class="${releaseCssBase}_title">${release.title}</span>
            </div>
            <div class="${releaseCssBase}_path">${release.description}</div>
        </div>
    </li>
</cpp:element>
