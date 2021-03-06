package com.composum.pages.commons.model;

import com.composum.sling.core.BeanContext;
import org.apache.sling.api.resource.Resource;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by rw on 22.01.17.
 */
public class Release extends AbstractModel implements Comparable<Release> {

    protected String key;
    protected List<String> categories;
    protected Calendar created;

    public Release() {
    }

    public Release(BeanContext context, Resource resource) {
        initialize(context, resource);
    }

    @Override
    protected void initializeWithResource(Resource resource) {
        super.initializeWithResource(resource);
        key = getProperty("key", "");
        categories = Arrays.asList(getProperty("categories", new String[0]));
        created = getProperty("jcr:created", Calendar.class);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return "composum-release-" + key;
    }

    public List<String> getCategories() {
        return categories;
    }

    @Override
    public int compareTo(@Nonnull Release o) {
        return created != null
                ? (o.created != null ? created.compareTo(o.created) : -1)
                : (o.created != null ? 1 : 0);
    }
}
