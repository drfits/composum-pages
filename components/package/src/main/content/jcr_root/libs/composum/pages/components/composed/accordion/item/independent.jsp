<%@page session="false" pageEncoding="UTF-8" %><%--
--%><%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><cpp:defineObjects/><%-- the 'independent' selector is used to enable multiple open items --%>
<cpp:container var="item" type="com.composum.pages.components.model.accordion.AccordionItem"
               cssBase="composum-pages-components-accordion-item" cssAdd="panel panel-default">
    <div id="${itemId}_head" class="panel-heading" role="tab">
        <h4 class="panel-title">
            <a role="button" data-toggle="collapse" href="#${itemId}_body"
               aria-expanded="${item.initialOpen||item.editMode}" aria-controls="${itemId}_body">
                <cpn:text value="${item.title}"/>
            </a>
        </h4>
    </div>
    <div id="${itemId}_body" class="panel-collapse collapse ${item.initialOpen||item.editMode?'in':''}"
         role="tabpanel" aria-labelledby="${itemId}_head">
        <div class="panel-body">
            <c:forEach items="${item.elements}" var="element" varStatus="loop">
                <div class="${itemCssBase}_element">
                    <cpp:include path="${element.path}"/>
                </div>
            </c:forEach>
        </div>
    </div>
</cpp:container>
