package com.composum.pages.commons.model;

import com.composum.pages.commons.util.PagesUtil;
import org.apache.jackrabbit.api.JackrabbitSession;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;
import java.util.Calendar;

import static com.composum.pages.commons.PagesConstants.PROP_CREATION_DATE;
import static com.composum.pages.commons.PagesConstants.PROP_LAST_MODIFIED;

/**
 * the abstract base class for all 'jcr:content' child element model implementations
 * such a resource is almost the versioned part of an element in the Pages content
 *
 * @param <ParentModel> the model class of the parent resource
 */
public abstract class ContentModel<ParentModel extends ContentDriven> extends AbstractModel {

    public static final String CSS_BASE_ASPECT = "_content";

    protected ParentModel parent;

    private transient Calendar creationDate;
    private transient Calendar lastModified;

    private transient VersionManager versionManager;

    public ParentModel getParent() {
        return parent;
    }

    @Override
    protected String buildCssBase() {
        String cssBase = super.buildCssBase();
        return cssBase + CSS_BASE_ASPECT;
    }

    public Calendar getCreationDate() {
        if (creationDate == null) {
            creationDate = getProperty(PROP_CREATION_DATE, null, Calendar.class);
        }
        return creationDate;
    }

    public String getCreationDateString() {
        return PagesUtil.getTimestampString(getCreationDate());
    }

    public Calendar getLastModified() {
        if (lastModified == null) {
            lastModified = getProperty(PROP_LAST_MODIFIED, null, Calendar.class);
        }
        return lastModified;
    }

    public String getLastModifiedString() {
        return PagesUtil.getTimestampString(getLastModified());
    }

    /** returns 'true' if the content is modified after the creation of the last stored version */
    public boolean isModified() throws RepositoryException {
        Calendar lastModified = getLastModified();
        if (lastModified != null) {
            VersionManager versionManager = getVersionManager();
            Version currentVersion = versionManager.getBaseVersion(getPath());
            if (currentVersion != null) {
                Calendar currentVersionCreated = currentVersion.getCreated();
                return (lastModified.after(currentVersionCreated));
            } else {
                return false;
            }
        }
        return true;
    }

    public VersionManager getVersionManager() throws RepositoryException {
        if (versionManager == null) {
            final JackrabbitSession session = (JackrabbitSession) resolver.adaptTo(Session.class);
            versionManager = session.getWorkspace().getVersionManager();
        }
        return versionManager;
    }
}
