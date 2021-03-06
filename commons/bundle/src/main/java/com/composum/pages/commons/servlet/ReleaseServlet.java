package com.composum.pages.commons.servlet;

import com.composum.sling.core.ResourceHandle;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.core.servlet.ServletOperation;
import com.composum.sling.core.servlet.ServletOperationSet;
import com.composum.sling.platform.staging.service.ReleaseManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.composum.pages.commons.servlet.ReleaseServlet.Extension.*;

/**
 * @author Mirko Zeibig
 * @since 16.09.16.
 */
@SlingServlet(paths = "/bin/cpm/pages/release", methods = {"GET", "POST", "PUT", "DELETE"})
public class ReleaseServlet extends AbstractServiceServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ReleaseServlet.class);

    @Reference
    private ReleaseManager releaseManager;

    enum Extension {
        http
    }

    enum Operation {
        release,
        delete,
        setpublic,
        setpreview
    }

    private ReleaseOperationSet operations = new ReleaseOperationSet();

    private class ReleaseOperationSet extends ServletOperationSet<Extension, Operation> {
        ReleaseOperationSet() {
            super(http);
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();

        // POST
        operations.setOperation(ServletOperationSet.Method.POST, Extension.http, Operation.release, new ReleaseOperation());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.http, Operation.delete, new DeleteRelease());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.http, Operation.setpublic, new SetPublicRelease());
        operations.setOperation(ServletOperationSet.Method.POST, Extension.http, Operation.setpreview, new SetPreviewRelease());
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    protected ServletOperationSet getOperations() {
        return operations;
    }


    abstract private class SetReleaseCategory implements ServletOperation {

        @Override
        public void doIt(SlingHttpServletRequest request, SlingHttpServletResponse response, ResourceHandle resource) throws RepositoryException, IOException, ServletException {
            try {
                final String path = getStringParameter(request, response, "path", "site path is required");
                if (path == null) return;

                final String releaseName = getStringParameter(request, response, "releaseName", "release name is required");
                if (releaseName == null) return;

                final ResourceResolver resourceResolver = request.getResourceResolver();
                final Resource site = resourceResolver.getResource(path);
                final Resource releases = resourceResolver.getResource(site, "jcr:content/releases");
                for (Resource r: releases.getChildren()) {
                    final ValueMap valueMap = r.adaptTo(ModifiableValueMap.class);
                    final String[] categories = valueMap.get("categories", new String[0]);
                    final List<String> cs = new ArrayList<>();
                    cs.addAll(Arrays.asList(categories));
                    if (r.getName().equals("release-" + releaseName)) {
                        // set categories to release
                        if (!cs.contains(getCategoryString())) {
                            cs.add(getCategoryString());
                        }
                        valueMap.put("categories", cs.toArray(new String[cs.size()]));
                    } else {
                        //remove release if set
                        cs.remove(getCategoryString());
                        valueMap.put("categories", cs.toArray(new String[cs.size()]));
                    }
                }
                resourceResolver.commit();
            } catch (Exception e) {
                LOG.error("error setting release category: " + e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }

        @Nonnull
        protected abstract String getCategoryString();

    }

    private class SetPublicRelease extends SetReleaseCategory  {
        @Nonnull
        protected String getCategoryString() {
            return "public";
        }
    }

    private class SetPreviewRelease extends SetReleaseCategory {
        @Nonnull
        protected String getCategoryString() {
            return "preview";
        }
    }

    private class DeleteRelease implements ServletOperation {

        @Override
        public void doIt(SlingHttpServletRequest request, SlingHttpServletResponse response, ResourceHandle resource) throws RepositoryException, IOException, ServletException {
            try {
                final String path = getStringParameter(request, response, "path", "site path is required");
                if (path == null) return;
                final String sitePath = path.endsWith("/jcr:content") ? path.substring(0, path.lastIndexOf('/')) : path;

                final String releaseName = getStringParameter(request, response, "releaseName", "release name is required");
                if (releaseName == null) return;

                List<String> rootPaths = new ArrayList<>();
                rootPaths.add(sitePath);

                final ResourceResolver resourceResolver = request.getResourceResolver();
                releaseManager.removeRelease(resourceResolver, rootPaths, releaseName, false);

                final Resource site = resourceResolver.getResource(sitePath);
                final Resource releases = resourceResolver.getResource(site, "jcr:content/releases");
                final Resource release = releases.getChild("release-" + releaseName);
                if (release != null) {
                    resourceResolver.delete(release);
                }
                resourceResolver.commit();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
    }

    private class ReleaseOperation implements ServletOperation {

        @Override
        public void doIt(SlingHttpServletRequest request, SlingHttpServletResponse response, ResourceHandle resourceHandle) throws RepositoryException, IOException, ServletException {
            try {

                final String path = getStringParameter(request, response, "path", "site path is required");
                if (path == null) return;
                final String sitePath = path.endsWith("/jcr:content") ? path.substring(0, path.lastIndexOf('/')) : path;

                final String releaseName = getStringParameter(request, response, "releaseName", "release name is required");
                if (releaseName == null) return;

                final String title = getStringParameter(request, response, "title", "release title is required");
                if (title == null) return;

                final String description = getStringParameter(request, response, "description", "release description is required");
                if (description == null) return;

                final RequestParameter objectsParameter = request.getRequestParameter("objects");
                final String objectsString;
                if (objectsParameter != null) {
                    objectsString = objectsParameter.getString();
                } else {
                    objectsString = "";
                }

                List<String> rootPaths = new ArrayList<>();
                final String[] split = objectsString.split(",");
                Collections.addAll(rootPaths, split);

                final ResourceResolver resourceResolver = request.getResourceResolver();
                releaseManager.createRelease(resourceResolver, rootPaths, releaseName);
                releaseManager.updateToRelease(resourceResolver, sitePath, releaseName);

                final Resource site = resourceResolver.getResource(sitePath);
                final Resource releases = resourceResolver.getResource(site, "jcr:content/releases");
                final Node releasesNode = releases.adaptTo(Node.class);
                final Node releaseNode = releasesNode.addNode("release-" + releaseName, "nt:unstructured");
                releaseNode.setProperty("key", releaseName);
                releaseNode.setProperty("jcr:title", title);
                releaseNode.setProperty("jcr:created", Calendar.getInstance());
                releaseNode.setProperty("jcr:createdBy", resourceResolver.getUserID());
                releaseNode.setProperty("jcr:description", description);
                resourceResolver.commit();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }


    }

    @Nullable
    private String getStringParameter(SlingHttpServletRequest request, SlingHttpServletResponse response, String paramName, String errorMesage) throws IOException {
        final RequestParameter requestParameter = request.getRequestParameter(paramName);
        if (requestParameter == null || StringUtils.isEmpty(requestParameter.getString())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMesage);
            return null;
        }
        return requestParameter.getString();
    }
}
