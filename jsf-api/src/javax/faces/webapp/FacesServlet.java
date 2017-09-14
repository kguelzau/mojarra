/*
 * $Id: FacesServlet.java,v 1.34.4.2 2013/01/08 17:06:12 edburns Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.faces.webapp;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p><strong>FacesServlet</strong> is a servlet that manages the request
 * processing lifecycle for web applications that are utilizing JavaServer
 * Faces to construct the user interface.</p>
 */

public final class FacesServlet implements Servlet {

    /*
     * A white space separated list of case sensitive HTTP method names
     * that are allowed to be processed by this servlet. * means allow all
     */
    private static final String ALLOWED_HTTP_METHODS_ATTR =
            "com.sun.faces.allowedHttpMethods";
    
    // Http method names must be upper case. http://www.w3.org/Protocols/HTTP/NoteMethodCS.html
    // List of valid methods in Http 1.1 http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9

    private enum HttpMethod {
        
        OPTIONS("OPTIONS"),
        GET("GET"),
        HEAD("HEAD"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE"),
        TRACE("TRACE"),
        CONNECT("CONNECT");
        
        private String name;
        
        HttpMethod(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
    }


    private Set<String> allowedUnknownHttpMethods;
    private Set<HttpMethod> allowedKnownHttpMethods;
    final private Set<HttpMethod> defaultAllowedHttpMethods = 
            EnumSet.range(HttpMethod.OPTIONS, HttpMethod.CONNECT);
    private Set<HttpMethod> allHttpMethods;

    private boolean allowAllMethods;

    /**
     * <p>Context initialization parameter name for a comma delimited list
     * of context-relative resource paths (in addition to
     * <code>/WEB-INF/faces-config.xml</code> which is loaded automatically
     * if it exists) containing JavaServer Faces configuration information.</p>
     */
    public static final String CONFIG_FILES_ATTR =
        "javax.faces.CONFIG_FILES";


    /**
     * <p>Context initialization parameter name for the lifecycle identifier
     * of the {@link Lifecycle} instance to be utilized.</p>
     */
    public static final String LIFECYCLE_ID_ATTR =
        "javax.faces.LIFECYCLE_ID";


    /**
     * The <code>Logger</code> for this class.
     */
    private static final Logger LOGGER =
          Logger.getLogger("javax.faces.webapp", "javax.faces.LogStrings");


    /**
     * <p>Factory for {@link FacesContext} instances.</p>
     */
    private FacesContextFactory facesContextFactory = null;


    /**
     * <p>The {@link Lifecycle} instance to use for request processing.</p>
     */
    private Lifecycle lifecycle = null;


    /**
     * <p>The <code>ServletConfig</code> instance for this servlet.</p>
     */
    private ServletConfig servletConfig = null;

    /**
     * From GLASSFISH-15632.  If true, the FacesContext instance
     * left over from startup time has been released.
     */
    private boolean initFacesContextReleased = false;

    /**
     * <p>Release all resources acquired at startup time.</p>
     */
    public void destroy() {

        facesContextFactory = null;
        lifecycle = null;
        servletConfig = null;
        uninitHttpMethodValidityVerification();

    }


    /**
     * <p>Return the <code>ServletConfig</code> instance for this servlet.</p>
     */
    public ServletConfig getServletConfig() {

        return (this.servletConfig);

    }


    /**
     * <p>Return information about this Servlet.</p>
     */
    public String getServletInfo() {

        return (this.getClass().getName());

    }


    /**
     * <p>Acquire the factory instances we will require.</p>
     *
     * @throws ServletException if, for any reason, the startup of
     * this Faces application failed.  This includes errors in the
     * config file that is parsed before or during the processing of
     * this <code>init()</code> method.
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        // Save our ServletConfig instance
        this.servletConfig = servletConfig;

        // Acquire our FacesContextFactory instance
        try {
            facesContextFactory = (FacesContextFactory)
                FactoryFinder.getFactory
                (FactoryFinder.FACES_CONTEXT_FACTORY);
        } catch (FacesException e) {
            ResourceBundle rb = LOGGER.getResourceBundle();
            String msg = rb.getString("severe.webapp.facesservlet.init_failed");
            Throwable rootCause = (e.getCause() != null) ? e.getCause() : e;
            LOGGER.log(Level.SEVERE, msg, rootCause);
            throw new UnavailableException(msg);
        }

        // Acquire our Lifecycle instance
        try {
            LifecycleFactory lifecycleFactory = (LifecycleFactory)
                FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            String lifecycleId ;

            // First look in the servlet init-param set
            if (null == (lifecycleId = servletConfig.getInitParameter(LIFECYCLE_ID_ATTR))) {
                // If not found, look in the context-param set 
                lifecycleId = servletConfig.getServletContext().getInitParameter
                    (LIFECYCLE_ID_ATTR);
            }

            if (lifecycleId == null) {
                lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
            }
            lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
            initHttpMethodValidityVerification();
        } catch (FacesException e) {
            Throwable rootCause = e.getCause();
            if (rootCause == null) {
                throw e;
            } else {
                throw new ServletException(e.getMessage(), rootCause);
            }
        }

    }

    private void initHttpMethodValidityVerification() {

        assert (null == allowedUnknownHttpMethods);
        assert (null != defaultAllowedHttpMethods);
        assert (null == allHttpMethods);
        allHttpMethods = EnumSet.allOf(HttpMethod.class);

        // Configure our permitted HTTP methods

        allowedUnknownHttpMethods = Collections.emptySet();
        allowedKnownHttpMethods = defaultAllowedHttpMethods;
        
        String[] methods = {};
        String allowedHttpMethodsString = servletConfig.getServletContext().getInitParameter(ALLOWED_HTTP_METHODS_ATTR);
        if (null != allowedHttpMethodsString) {
            methods = allowedHttpMethodsString.split("\\s+");
            assert (null != methods); // assuming split always returns a non-null array result
            allowedUnknownHttpMethods = new HashSet(methods.length);
            List<String> allowedKnownHttpMethodsStringList = new ArrayList<String>();
            // validate input against allHttpMethods data structure
            for (String cur : methods) {
                if (cur.equals("*")) {
                    allowAllMethods = true;
                    allowedUnknownHttpMethods = Collections.emptySet();
                    return;
                }
                boolean isKnownHttpMethod;
                try {
                    HttpMethod.valueOf(cur);
                    isKnownHttpMethod = true;
                } catch (IllegalArgumentException e) {
                    isKnownHttpMethod = false;
                }
                
                if (!isKnownHttpMethod) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        HttpMethod [] values = HttpMethod.values();
                        Object [] arg = new Object[values.length + 1];
                        arg[0] = cur;
                        System.arraycopy(values, HttpMethod.OPTIONS.ordinal(), 
                                         arg, 1, values.length);
                        LOGGER.log(Level.WARNING,
                                "warning.webapp.facesservlet.init_invalid_http_method",
                                arg);
                    }
                    // prevent duplicates
                    if (!allowedUnknownHttpMethods.contains(cur)) {
                        allowedUnknownHttpMethods.add(cur);
                    }
                } else {
                    // prevent duplicates
                    if (!allowedKnownHttpMethodsStringList.contains(cur)) {
                        allowedKnownHttpMethodsStringList.add(cur);
                    }
                }
            }
            // Optimally initialize allowedKnownHttpMethods
            if (5 == allowedKnownHttpMethodsStringList.size()) {
                allowedKnownHttpMethods = EnumSet.of(
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(3)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(4))
                        );
            } else if (4 == allowedKnownHttpMethodsStringList.size()) {
                allowedKnownHttpMethods = EnumSet.of(
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(3))
                        );
                
            } else if (3 == allowedKnownHttpMethodsStringList.size()) {
                allowedKnownHttpMethods = EnumSet.of(
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(2))
                        );
                
            } else if (2 == allowedKnownHttpMethodsStringList.size()) {
                allowedKnownHttpMethods = EnumSet.of(
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0)),
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(1))
                        );
                
            } else if (1 == allowedKnownHttpMethodsStringList.size()) {
                allowedKnownHttpMethods = EnumSet.of(
                        HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0))
                        );
                
            } else {
                List<HttpMethod> restList = 
                        new ArrayList<HttpMethod>(allowedKnownHttpMethodsStringList.size() - 1);
                for (int i = 1; i < allowedKnownHttpMethodsStringList.size() - 1; i++) {
                    restList.add(HttpMethod.valueOf(
                            allowedKnownHttpMethodsStringList.get(i)
                            ));
                }
                HttpMethod first = HttpMethod.valueOf(allowedKnownHttpMethodsStringList.get(0));
                HttpMethod [] rest = new HttpMethod[restList.size()];
                restList.toArray(rest);
                allowedKnownHttpMethods = EnumSet.of(first, rest);
                
            } 
        }
    }

    private void uninitHttpMethodValidityVerification() {
        assert (null != allowedUnknownHttpMethods);
        assert (null != defaultAllowedHttpMethods);
        assert (null != allHttpMethods);

        allowedUnknownHttpMethods.clear();
        allowedUnknownHttpMethods = null;
        allowedKnownHttpMethods.clear();
        allowedKnownHttpMethods = null;
        allHttpMethods.clear();
        allHttpMethods = null;

    }


    /**
     * <p>Process an incoming request, and create the corresponding
     * response, by executing the request processing lifecycle.</p>
     *
     * <p>If the <code>request</code> and <code>response</code>
     * arguments to this method are not instances of
     * <code>HttpServletRequest</code> and
     * <code>HttpServletResponse</code>, respectively, the results of
     * invoking this method are undefined.</p>
     *
     * <p>This method must respond to requests that start with the
     * following strings by invoking the <code>sendError</code> method
     * on the response argument (cast to
     * <code>HttpServletResponse</code>), passing the code
     * <code>HttpServletResponse.SC_NOT_FOUND</code> as the
     * argument. </p>
     *
     * <ul>
     *
<pre><code>
/WEB-INF/
/WEB-INF
/META-INF/
/META-INF
</code></pre>
     *
     * </ul>
     *
     * 
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @throws IOException if an input/output error occurs during processing
     * @throws ServletException if a servlet error occurs during processing
     */
    public void service(ServletRequest req,
                        ServletResponse resp)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (!isHttpMethodValid(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (Thread.currentThread().isInterrupted()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINE, "Thread {0} given to FacesServlet.service() in interrupted state", 
                        Thread.currentThread().getName());
            }
        }

        // If prefix mapped, then ensure requests for /WEB-INF are
        // not processed.
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            pathInfo = pathInfo.toUpperCase();
            if (pathInfo.contains("/WEB-INF/")
                || pathInfo.contains("/WEB-INF")
                || pathInfo.contains("/META-INF/")
                || pathInfo.contains("/META-INF")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }    
        
        if (!initFacesContextReleased) {
            Object obj = servletConfig.getServletContext().getAttribute("com.sun.faces.InitFacesContext");
            if (null != obj && obj instanceof FacesContext) {
                try {
                    ((FacesContext) obj).release();
                    servletConfig.getServletContext().removeAttribute("com.sun.faces.InitFacesContext");
                } catch (Exception e) {
                    // Take no action if another thread released or removed it already.
                }
            }
            FacesContext initFacesContext = FacesContext.getCurrentInstance();
            if (null != initFacesContext) {
                initFacesContext.release();
            }
            initFacesContextReleased = true;
        }

        // Acquire the FacesContext instance for this request
        FacesContext context = facesContextFactory.getFacesContext
            (servletConfig.getServletContext(), request, response, lifecycle);

        // Execute the request processing lifecycle for this request
        try {
            lifecycle.execute(context);
            lifecycle.render(context);
        } catch (FacesException e) {
            Throwable t = e.getCause();
            if (t == null) {
                throw new ServletException(e.getMessage(), e);
            } else {
                if (t instanceof ServletException) {
                    throw ((ServletException) t);
                } else if (t instanceof IOException) {
                    throw ((IOException) t);
                } else {
                    throw new ServletException(t.getMessage(), t);
                }
            }
        }
        finally {
            // Release the FacesContext instance for this request
            context.release();
        }

    }

    private boolean isHttpMethodValid(HttpServletRequest request) {
        boolean result = false;
        if (allowAllMethods) {
            result = true;
        } else {
            String requestMethodString = request.getMethod();
            HttpMethod requestMethod = null;
            boolean isKnownHttpMethod;
            try {
                requestMethod = HttpMethod.valueOf(requestMethodString);
                isKnownHttpMethod = true;
            } catch (IllegalArgumentException e) {
                isKnownHttpMethod = false;
            }
            if (isKnownHttpMethod) {
                result = allowedKnownHttpMethods.contains(requestMethod);
            } else {
                result = allowedUnknownHttpMethods.contains(requestMethodString);
            }
            
        }

        return result;
    }


    // --------------------------------------------------------- Private Methods


}