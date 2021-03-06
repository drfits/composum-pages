package com.composum.pages.commons.model;

import com.composum.pages.commons.service.PageManager;
import com.composum.pages.commons.service.SiteManager;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.composum.pages.commons.PagesConstants.DEFAULT_HOMEPAGE_PATH;
import static com.composum.pages.commons.PagesConstants.NODE_TYPE_SITE;
import static com.composum.pages.commons.PagesConstants.NODE_TYPE_SITE_CONFIGURATION;
import static com.composum.pages.commons.PagesConstants.PROP_HOMEPAGE;

public class Site extends ContentDriven<SiteConfiguration> {

    public enum PublicMode {LIVE, PUBLIC, PREVIEW}

    public static final String PROP_PUBLIC_MODE = "publicMode";
    public static final String DEFAULT_PUBLIC_MODE = PublicMode.PUBLIC.name();

    public static final String RELEASE_LABEL_PREFIX = "composum-release-";

    // static resource type determination

    /**
     * check the 'cpp:Site' type for a resource
     */
    public static boolean isSite(Resource resource) {
        return ResourceUtil.isResourceType(resource, NODE_TYPE_SITE);
    }

    /**
     * check the 'cpp:SiteConfiguration' type for a resource
     */
    public static boolean isSiteConfiguration(Resource resource) {
        return ResourceUtil.isResourceType(resource, NODE_TYPE_SITE_CONFIGURATION);
    }

    // site attributes

    private transient PublicMode publicMode;
    private transient Homepage homepage;

    public Site() {
    }

    protected Site(BeanContext context, Resource resource) {
        initialize(context, resource);
    }

    public Site(SiteManager manager, BeanContext context, Resource resource) {
        this.siteManager = manager;
        initialize(context, resource);
    }

    // initializer extensions

    @Override
    protected Resource determineResource(Resource initialResource) {
        return getSiteManager().getContainingSiteResource(initialResource);
    }

    @Override
    protected SiteConfiguration createContentModel(BeanContext context, Resource contentResource) {
        return new SiteConfiguration(context, contentResource);
    }

    // site hierarchy

    public Homepage getHomepage() {
        if (homepage == null) {
            PageManager pageManager = getPageManager();
            String homepagePath = getProperty(PROP_HOMEPAGE, null, DEFAULT_HOMEPAGE_PATH);
            Resource homepageRes = resource.getChild(homepagePath);
            if (homepageRes != null) {
                homepage = new Homepage(pageManager, context, homepageRes);
            } else {
                homepage = new Homepage(pageManager, context, resource); // use itself as homepage
            }
        }
        return homepage;
    }

    // releases

    /**
     * returns the 'publicMode' property value of this site
     */
    public PublicMode getPublicMode() {
        if (publicMode == null) {
            String propertyValue = getProperty(PROP_PUBLIC_MODE, null, DEFAULT_PUBLIC_MODE);
            publicMode = PublicMode.valueOf(propertyValue.toUpperCase());
        }
        return publicMode;
    }

    /**
     * retrieves the release label for a release category ('public', 'preview')
     * the content of this release is delivered if a public request in the category is performed
     */
    public String getReleaseLabel(String category) {
        Resource releases = content.resource.getChild("releases");
        if (releases != null) {
            category = category.toLowerCase();
            for (Resource release : releases.getChildren()) {
                ValueMap values = release.adaptTo(ValueMap.class);
                String key = values.get("key", "");
                if (StringUtils.isNotBlank(key)) {
                    if (key.equals(category)) {
                        return RELEASE_LABEL_PREFIX + key;
                    } else {
                        List<String> categories = Arrays.asList(values.get("categories", new String[0]));
                        if (categories.contains(category)) {
                            return RELEASE_LABEL_PREFIX + key;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * return the list of content releases of this site
     */
    public List<Release> getReleases() {
        List<Release> result = new ArrayList<>();
        Resource releases = content.resource.getChild("releases");
        if (releases != null) {
            for (Resource releaseResource : releases.getChildren()) {
                final Release release = new Release(context, releaseResource);
                result.add(release);
            }
        }
        return result;
    }

}
