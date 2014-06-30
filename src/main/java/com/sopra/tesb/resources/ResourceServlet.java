package com.sopra.tesb.resources;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Resource servlet to wrap a filesystem folder to HTTP.
 */
public class ResourceServlet extends HttpServlet {

    private BundleContext bundleContext;

    public void init(ServletConfig servletConfig) throws ServletException {
        ServletContext context = servletConfig.getServletContext();
        bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doIt(request, response);
    }

    public void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get config admin service
        String base = "/resources";

        ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (ref != null) {
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(ref);
            Configuration configuration = configurationAdmin.getConfiguration("com.sopra.tesb.resources");
            base = (String) configuration.getProperties().get("resources.location");
            bundleContext.ungetService(ref);
        }

        String uri = request.getPathInfo();

        // remove the starting /
        uri = uri.substring(1);

        File file = new File(base + "/" + uri);
        if (!file.exists()) {
            throw new ServletException(uri + " doesn't exist in the resources " + base + " storage");
        }

        InputStream inputStream = new FileInputStream(file);
        OutputStream outputStream = response.getOutputStream();
        int c;
        while ((c = inputStream.read()) >= 0) {
            outputStream.write(c);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }
}
