package com.composum.pages.components.model.navigation;

import com.composum.pages.commons.model.Page;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.composum.pages.commons.PagesConstants.PROP_NAV_TITLE;

public class Menuitem extends Page {

    private static final Logger LOG = LoggerFactory.getLogger(Menuitem.class);

    public static final String[] PROP_TITLE_KEYS = new String[]{PROP_NAV_TITLE, ResourceUtil.PROP_TITLE, PROP_TITLE};

    public static final String MENU_ITEM_CSS_BASE_TYPE = "composum/pages/components/navigation/menuitem";

    private transient Boolean redirect;
    private transient Boolean menuOnly;
    private transient Menu submenu;

    public Menuitem() {
    }

    public Menuitem(BeanContext context, Resource resource) {
        super(context, resource);
    }

    @Override
    protected String[] getTitleKeys() {
        return PROP_TITLE_KEYS;
    }

    @Override
    protected String getCssBaseType() {
        return MENU_ITEM_CSS_BASE_TYPE;
    }

    public String getCssClasses() {
        String currentPagePath = getCurrentPage().getPath();
        String menuitemPath = resource.getPath();
        StringBuilder cssClasses = new StringBuilder();
        if (menuitemPath.equals(currentPagePath)) {
            cssClasses.append(" current");
        }
        if (isActive()) {
            cssClasses.append(" active");
        }
        return cssClasses.toString();
    }

    public boolean isActive() {
        String currentPagePath = getCurrentPage().getPath();
        String menuitemPath = resource.getPath();
        String homepagePath = getHomepage().getPath();
        return currentPagePath.startsWith(menuitemPath)
                && (!homepagePath.equals(menuitemPath) || menuitemPath.equals(currentPagePath));
    }

    public boolean isMenuOnly() {
        if (menuOnly == null) {
            String target = getTarget();
            menuOnly = target != null && StringUtils.isBlank(target);
        }
        return menuOnly;
    }

    public boolean isRedirect() {
        if (redirect == null) {
            String target = getTarget();
            redirect = StringUtils.isNotBlank(target);
        }
        return redirect;
    }

    protected String getTarget() {
        return getProperty("sling:target", null, (String) null);
    }

    public boolean isSubmenu() {
        return !getSubmenu().isEmpty();
    }

    public Menu getSubmenu() {
        if (submenu == null) {
            submenu = new Menu(context, resource);
        }
        return submenu;
    }
}
