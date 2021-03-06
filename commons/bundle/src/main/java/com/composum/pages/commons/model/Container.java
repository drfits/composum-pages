package com.composum.pages.commons.model;

import com.composum.pages.commons.PagesConstants;
import com.composum.pages.commons.model.properties.AllowedTypes;
import com.composum.pages.commons.service.EditService;
import com.composum.pages.commons.util.ResolverUtil;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.filter.ResourceFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.ArrayList;
import java.util.List;

import static com.composum.pages.commons.PagesConstants.NODE_TYPE_CONTAINER;
import static com.composum.pages.commons.PagesConstants.PROP_ALLOWED_ELEMENTS;

/**
 * The Container is an Element to arrange some Elements within dynamically...
 */
public class Container extends Element {

    public static final String PROP_WITH_SPACING = "withSpacing";

    // static resource type determination

    /**
     * check the 'cpp:Container' type for a resource with an optional overloaded type
     *
     * @param resolver the resolver to use for type check (if resource is null)
     * @param resource the resource (can be 'null' if type is available)
     * @param type     the optional resource type (necessary if resource is 'null')
     */
    public static boolean isContainer(ResourceResolver resolver, Resource resource, String type) {
        return (resource != null && (resource.isResourceType(NODE_TYPE_CONTAINER) ||
                NODE_TYPE_CONTAINER.equals(ResolverUtil.getTypeProperty(
                        resource, type, PagesConstants.PROP_COMPONENT_TYPE, "")))) ||
                (StringUtils.isNotBlank(type) &&
                        NODE_TYPE_CONTAINER.equals(ResolverUtil.getTypeProperty(
                                resolver, type, PagesConstants.PROP_COMPONENT_TYPE, "")));
    }

    public Container() {
    }

    public Container(BeanContext context, String path, String resourceType) {
        super(context, path, resourceType);
    }

    public Container(BeanContext context, Resource resource) {
        super(context, resource);
    }

    // transient attributes

    private transient List<Element> elementList;
    private transient Boolean withSpacing;

    private transient AllowedTypes allowedElements;

    private transient List<String> elementTypes;

    // rendering

    /**
     * the filter to restrict the rendering of the embedded elements (if useful; defaults to ALL - no restriction)
     */
    protected ResourceFilter getRenderFilter() {
        return ResourceFilter.ALL;
    }

    /**
     * the list of elements for rendering - provided for the templates
     */
    public List<Element> getElements() {
        if (elementList == null) {
            elementList = new ArrayList<>();
            ResourceFilter filter = getRenderFilter();
            for (Resource child : resource.getChildren()) {
                if (filter.accept(child)) {
                    Element element = new Element();
                    element.initialize(context, child);
                    elementList.add(element);
                }
            }
        }
        return elementList;
    }

    /**
     * returns 'true' if spacing DOM elements should be rendered between the elements of the container
     */
    public boolean isWithSpacing() {
        if (withSpacing == null) {
            withSpacing = getProperty(PROP_WITH_SPACING, Boolean.FALSE);
        }
        return withSpacing;
    }

    // manipulation

    /**
     * returns a list of all available resource types which are allowed as container elements
     * used to offer the available component types for insertions / creation
     */
    public List<String> getElementTypes() {
        if (elementTypes == null) {
            EditService editService = context.getService(EditService.class);
            elementTypes = editService.getAllowedElementTypes(resolver,
                    new ResourceReference.List(new ResourceReference(this)), false);
        }
        return elementTypes;
    }

    // 'allowedElements' property...

    public boolean isAllowedElement(Element element) {
        return isAllowedElement(element.getType());
    }

    public boolean isAllowedElement(Resource resource) {
        return isAllowedElement(resource.getResourceType());
    }

    public boolean isAllowedElement(ResourceReference element) {
        return isAllowedElement(element.getType());
    }

    public boolean isAllowedElement(String resourceType) {
        return getAllowedElements().matches(resourceType);
    }

    /**
     * returns the 'allowedElements' rule for this container (from the configuration)
     */
    public AllowedTypes getAllowedElements() {
        if (allowedElements == null) {
            allowedElements = new AllowedTypes(new ResourceReference(this), PROP_ALLOWED_ELEMENTS);
        }
        return allowedElements;
    }
}
