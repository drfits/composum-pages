<%@page session="false" pageEncoding="UTF-8" %><%--
--%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %><%--
--%><%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %><%--
--%><%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %><%--
--%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%--
--%><cpp:defineFrameObjects/>
<div class="${widgetCssBase}_${widget.widgetType} ${widgetCssBase}_${widget.cssName} form-group">
    <sling:call script="label.jsp"/>
    <textarea name="${widget.name}" data-label="${widget.label}" data-i18n="${widget.i18n}" ${widget.attributes}
              class="${widgetCssBase}_input ${widgetCssBase}_text-area widget text-area-widget form-control"
              rows="${widget.model.rows}" placeholder="${cpn:text(widget.placeholder)}"
              <c:if test="${widget.disabled}">disabled</c:if>>${cpn:text(widget.model.text)}</textarea>
</div>

