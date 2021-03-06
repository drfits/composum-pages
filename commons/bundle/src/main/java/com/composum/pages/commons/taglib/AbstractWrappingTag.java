package com.composum.pages.commons.taglib;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.composum.pages.commons.model.AbstractModel.addCssClass;
import static com.composum.pages.commons.model.AbstractModel.cssOfType;
import static com.composum.pages.commons.servlet.EditServlet.EDIT_RESOURCE_KEY;

/**
 * the base class for all content wrapping tags: prepare - start tag - end tag - finish
 */
public abstract class AbstractWrappingTag extends ModelTag {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractWrappingTag.class);

    public static final String TAG_ID = "id";

    protected String cssSet;
    protected String cssAdd;

    protected Object editResource;

    private transient List<String> cssClassesList;
    private transient String attributes;

    @Override
    protected void clear() {
        attributes = null;
        cssClassesList = null;
        editResource = null;
        cssAdd = null;
        cssSet = null;
        super.clear();
    }

    /**
     * a string with the complete set of CSS classes (prevents from the generation of the default classes)
     */
    public void setCssSet(String classes) {
        cssSet = classes;
    }

    /**
     * a string with additional css classes (optional)
     */
    public String getCssAdd() {
        return cssAdd;
    }

    public void setCssAdd(String classes) {
        cssAdd = classes;
    }

    @Deprecated
    public String getCssClasses() {
        return getCssAdd();
    }

    @Deprecated
    public void setCssClasses(String classes) {
        setCssAdd(classes);
    }

    /**
     * transforms the 'cssAdd' atribute into a list of classes as base for the CSS class collection
     */
    protected List<String> getCssClassesList() {
        if (cssClassesList == null) {
            if (StringUtils.isNotBlank(cssSet)) {
                cssClassesList = new ArrayList<>(Arrays.asList(cssSet.split(" +")));
            } else {
                cssClassesList = new ArrayList<>();
            }
        }
        return cssClassesList;
    }

    /**
     * builds the complete CSS classes string with the given classes and all collected classes
     */
    protected String buildCssClasses() {
        List<String> cssClassList = getCssClassesList();
        collectCssClasses(cssClassList);
        return StringUtils.join(cssClassList, " ");
    }

    /**
     * collects the set of CSS classes (extension hook)
     * adds the 'cssBase' itself as CSS class and the transformed resource super type if available
     */
    protected void collectCssClasses(List<String> collection) {
        if (StringUtils.isBlank(cssSet)) {
            addCssClass(collection, StringUtils.isNotBlank(cssBase) ? cssBase : buildCssBase());
            addCssClass(collection, cssOfType(resource.getResourceSuperType()));
        }
        if (StringUtils.isNotBlank(cssAdd)) {
            collection.addAll(Arrays.asList(cssAdd.split(" +")));
        }
    }

    /**
     * collects the set of tag attributes classes (extension hook)
     * adds the 'class' attribute with all collected CSS classes to the set build by the superclass
     */
    protected void collectAttributes(Map<String, String> attributeSet) {
        String cssClasses = buildCssClasses();
        if (StringUtils.isNotBlank(cssClasses)) {
            attributeSet.put("class", cssClasses);
        }
        super.collectAttributes(attributeSet);
    }

    /**
     * returns the complete set of attributes as one string value with a leading space
     * provided to embed all attributes in a template (JSP or something else)
     */
    public String getAttributes() {
        if (attributes == null) {
            StringBuilder builder = new StringBuilder();
            Map<String, String> attributeSet = new LinkedHashMap<>();
            collectAttributes(attributeSet);
            for (Map.Entry<String, String> attribute : attributeSet.entrySet()) {
                builder.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
            }
            attributes = builder.toString();
        }
        return attributes;
    }

    //
    // the render steps for all superclasses
    //

    /**
     * if this returns 'false' nothing is rendered, no wrapping tag and no content within
     * (extension hook; returns 'true')
     */
    protected boolean renderTag() {
        return true;
    }

    protected abstract void prepareTagStart();

    protected abstract void renderTagStart() throws JspException, IOException;

    protected abstract void renderTagEnd() throws JspException, IOException;

    protected abstract void finishTagEnd();

    /**
     * collects all CSS classes and attributes, prepares the rendering (prepareTagStart())
     * and performs the rendering of the tag start (renderTagStart()) if 'renderTag()' returns 'true'
     * stores the resource to edit as request attribute for further use if not always present
     * if 'cssBase' is specified the '{var}CssBase' attribute is set in the page context
     */
    @Override
    public int doStartTag() throws JspException {
        super.doStartTag(); // necessary to initialize the tag for the following 'render test'
        if (renderTag()) {
            if (request.getAttribute(EDIT_RESOURCE_KEY) == null) {
                request.setAttribute(EDIT_RESOURCE_KEY, editResource = resource);
            }
            if (StringUtils.isNotBlank(cssSet)) {
                cssSet = eval(cssSet, cssSet);
            }
            if (StringUtils.isNotBlank(cssAdd)) {
                cssAdd = eval(cssAdd, cssAdd);
            }
            prepareTagStart();
            try {
                out.flush();
                renderTagStart();
                out.flush();
            } catch (IOException ioex) {
                LOG.error(ioex.getMessage(), ioex);
            }
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * performs the rendering of the tag end (renderTagEnd()) followed by the cleanup (finishTagEnd())
     * removes the resource to edit attribute if set by this tag during tag start
     * if '{var}CssBase' was set on start tag this attribute will be removed from the page context
     */
    @Override
    public int doEndTag() throws JspException {
        if (renderTag()) {
            try {
                out.flush();
                renderTagEnd();
                out.flush();
            } catch (IOException ioex) {
                LOG.error(ioex.getMessage(), ioex);
            }
            finishTagEnd();
            if (editResource != null) {
                request.removeAttribute(EDIT_RESOURCE_KEY);
                editResource = null;
            }
        }
        super.doEndTag();
        return EVAL_PAGE;
    }

    //
    // helpers
    //

    protected boolean includeSnippet(String resourceType, String selector) throws JspException, IOException {
        out.flush();
        if (StringUtils.isNotBlank(resourceType)) {

            RequestDispatcherOptions options = new RequestDispatcherOptions();
            options.setForceResourceType(resourceType);
            if (StringUtils.isNotBlank(selector)) {
                options.setReplaceSelectors(selector);
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(getResource(), options);

            if (dispatcher != null) {
                try {
                    dispatcher.include(request, pageContext.getResponse());
                    return true;

                } catch (ServletException | IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        return false;
    }
}
