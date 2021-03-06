package com.composum.pages.commons.servlet;

import com.composum.pages.commons.service.TrackingService;
import com.composum.sling.core.BeanContext;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * the TokenServlet delegates each request to the 'TrackingService' to track the usage of the current
 * content; the TokenServlet is delivering a simple 1x1 transparent image after the request tracking
 */
@SlingServlet(
        selectors = "token",
        extensions = {"png"},
        methods = {"GET"},
        resourceTypes = "sling/servlet/default"
)
public class TokenServlet extends SlingSafeMethodsServlet {

    protected BundleContext bundleContext;

    @Activate
    private void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TokenServlet.class);

    public static final byte[] TRACKING_GIF = {
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x1, 0x0, 0x1, 0x0, (byte) 0x80, 0x0, 0x0, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, 0x0, 0x0, 0x0, 0x2c, 0x0, 0x0, 0x0, 0x0, 0x1, 0x0, 0x1, 0x0, 0x0, 0x2, 0x2, 0x44, 0x1, 0x0,
            0x3b
    };
    public static final byte[] TRACKING_PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0B, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0xDA, 0x63, 0x60, 0x00, 0x02,
            0x00, 0x00, 0x05, 0x00, 0x01, (byte) 0xE9, (byte) 0xFA, (byte) 0xDC, (byte) 0xD8, 0x00, 0x00, 0x00, 0x00,
            0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Reference
    protected TrackingService trackingService;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request,
                         @Nonnull SlingHttpServletResponse response) throws ServletException,
            IOException {
        String uri = request.getRequestURI();
        Resource resource = request.getResource();
        if (!ResourceUtil.isNonExistingResource(resource)) {
            try {
                String path = resource.getPath();
                String suffix = request.getRequestPathInfo().getSuffix();
                String referer = null;
                if (StringUtils.isNotBlank(suffix)) {
                    referer = suffix.substring(1);
                    // the base64 encoded referer is NOT decoded here (the encoded string is used as a node name)
                }
                trackingService.trackToken(
                        new BeanContext.Servlet(getServletContext(), bundleContext, request, response),
                        path,
                        referer);
            } catch (RepositoryException | LoginException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } else {
            LOG.warn("resource not found: " + uri);
        }
        AbstractServiceServlet.setNoCacheHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/png");
        response.setContentLength(TRACKING_PNG.length);
        IOUtils.copy(new ByteArrayInputStream(TRACKING_PNG), response.getOutputStream());
    }
}
