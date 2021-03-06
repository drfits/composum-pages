package com.composum.pages.commons.model;

import com.composum.pages.commons.PagesConstants;
import com.composum.pages.commons.util.ResolverUtil;
import com.composum.pages.commons.util.ResourceTypeUtil;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.composum.pages.commons.servlet.EditServlet.EDIT_RESOURCE_TYPE_KEY;

/**
 * the model class for a component itself (the implementation)
 */
public class Component extends AbstractModel {

    public static final String TYPE_HINT_PARAM = "type";

    public static final Pattern PRIMARY_TYPE_PATTERN = Pattern.compile("^[^:/]+:.+");
    public static final Pattern EDIT_SUBTYPE_PATTERN = Pattern.compile(
            "^(.+)/edit(/(default|actions)/[^/]+)?/(dialog|toolbar|tree|tile)$"
    );

    /**
     * the model of the components dialog implemented as a 'subcomponent'
     */
    public class EditDialog extends AbstractModel {

        private transient Resource thumbnailImage;

        public EditDialog() {
            super();
            Resource subtypeResource = ResourceTypeUtil.getSubtype(Component.this.resolver,
                    null, Component.this.getPath(), ResourceTypeUtil.EDIT_DIALOG_PATH);
            // a component mustn't have a dialog implementation...
            initialize(Component.this.context, subtypeResource != null
                    ? subtypeResource
                    : new NonExistingResource(Component.this.resolver,
                    Component.this.getPath() + "/" + ResourceTypeUtil.EDIT_DIALOG_PATH));
        }

        /** returns false if no dialog is configured */
        public boolean isValid() {
            return !ResourceUtil.isNonExistingResource(resource);
        }

        /** returns true if the dialog of a resource supertype is used */
        public boolean isInherited() {
            String path = getPath();
            return StringUtils.isNotBlank(path) && !path.startsWith(Component.this.getPath());
        }

        public boolean getHasThumbnailImage() {
            return getThumbnailImage() != null;
        }

        public Resource getThumbnailImage() {
            if (thumbnailImage == null) {
                thumbnailImage = resource.getChild("thumbnail.png");
                if (thumbnailImage == null) {
                    thumbnailImage = resource.getChild("thumbnail.jpg");
                }
            }
            return thumbnailImage;
        }
    }

    /**
     * the model of the components tile implemented as a 'subcomponent'
     */
    public class EditTile extends AbstractModel {

        public EditTile() {
            super();
            Resource subtypeResource = ResourceTypeUtil.getSubtype(Component.this.resolver,
                    null, Component.this.getPath(), ResourceTypeUtil.EDIT_TILE_PATH);
            initialize(Component.this.context, subtypeResource);
        }
    }

    /** transient (lazy loaded) attributes */

    private transient EditDialog editDialog;
    private transient EditTile editTile;

    /** model initialization */

    public Component() {
    }

    public Component(BeanContext context, Resource resource) {
        initialize(context, resource);
    }

    /**
     * determine the components resource even if the initial resource is an instance of the component
     */
    @Override
    protected Resource determineResource(Resource initialResource) {
        // ignore all resource types modified by resource wrappers
        Resource typeResource = getTypeResource(initialResource);
        return typeResource != null ? typeResource : initialResource;
    }

    /**
     * determines the resource of the component (of the 'implementation') even
     * if the resource is an instance of the component (content resource)
     */
    protected Resource getTypeResource(Resource resource) {
        Resource typeResource = null;
        if (!ResourceUtil.isNonExistingResource(resource)) {
            // ignore all resource types modified by resource wrappers
            typeResource = resolver.getResource(resource.getPath());
            if (typeResource != null &&
                    !typeResource.isResourceType(PagesConstants.NODE_TYPE_COMPONENT)) {
                // the initialResource is probably an instance of a component not a component itself
                // in this case we have to switch to the resource of the resource type
                String resourceType = typeResource.getResourceType();
                if (StringUtils.isBlank(resourceType) || PRIMARY_TYPE_PATTERN.matcher(resourceType).matches()) {
                    // check a probably present content child if no resource type property found
                    Resource contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
                    if (contentResource != null) {
                        resourceType = contentResource.getResourceType();
                    } else {
                        // is there a hint in the request...
                        // (used if context tools are rendered for the current selection)
                        resourceType = context.getRequest().getParameter(TYPE_HINT_PARAM);
                        if (StringUtils.isBlank(resourceType)) {
                            resourceType = resource.getResourceType();
                        }
                        Matcher matcher = EDIT_SUBTYPE_PATTERN.matcher(resourceType);
                        if (matcher.matches()) {
                            // if type is a subtype use the component type instead
                            resourceType = matcher.group(1);
                        }
                    }
                }
                if (StringUtils.isNotBlank(resourceType)) {
                    typeResource = ResolverUtil.getResourceType(typeResource, resourceType);
                }
            }
        } else {
            // probably a static include of a non existing resource - search for a type hint...
            SlingHttpServletRequest request = context.getRequest();
            String resourceType = (String) request.getAttribute(EDIT_RESOURCE_TYPE_KEY);
            if (StringUtils.isNotBlank(resourceType)) {
                typeResource = ResolverUtil.getResourceType(resolver, resourceType);
            }
        }
        return typeResource;
    }

    /** the type of a component is the the components absolute resource path */
    public String getType() {
        return getPath();
    }

    public EditDialog getEditDialog() {
        if (editDialog == null) {
            editDialog = new EditDialog();
        }
        return editDialog;
    }

    public EditTile getEditTile() {
        if (editTile == null) {
            editTile = new EditTile();
        }
        return editTile;
    }
}
