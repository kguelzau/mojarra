/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.application;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.faces.FacesWrapper;
import javax.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2 changed_modified_2_3">Provides</span> a simple implementation
 * of {@link Resource} that can be subclassed by developers wishing to provide specialized behavior
 * to an existing {@link Resource} instance. The default implementation of all methods is to call
 * through to the wrapped {@link Resource}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use
 * {@link #getWrapped} to access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class ResourceWrapper extends Resource implements FacesWrapper<Resource> {

    private Resource wrapped;


    /**
     * <p class="changed_added_2_3">
     * If this resource has been decorated, the implementation doing the decorating should push the
     * implementation being wrapped to this constructor. The {@link #getWrapped()} will then return
     * the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public ResourceWrapper(Resource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Resource getWrapped() {
        return wrapped;
    }

    
    // --------------------------------------------------- Methods from Resource

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getInputStream} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return getWrapped().getInputStream();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getURL} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public URL getURL() {
        return getWrapped().getURL();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getResponseHeaders} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public Map<String, String> getResponseHeaders() {
        return getWrapped().getResponseHeaders();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#getRequestPath} on the wrapped
     * {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getRequestPath() {
        return getWrapped().getRequestPath();
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link Resource#userAgentNeedsUpdate} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {
        return getWrapped().userAgentNeedsUpdate(context);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getContentType()} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getContentType() {
        return getWrapped().getContentType();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setContentType(String)} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setContentType(String contentType) {
        getWrapped().setContentType(contentType);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getLibraryName()} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getLibraryName() {
        return getWrapped().getLibraryName();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setLibraryName(String)} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setLibraryName(String libraryName) {
        getWrapped().setLibraryName(libraryName);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#getResourceName()} on the
     * wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public String getResourceName() {
        return getWrapped().getResourceName();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Resource#setResourceName(String)} on
     * the wrapped {@link ResourceHandler} object.
     * </p>
     */
    @Override
    public void setResourceName(String resourceName) {
        getWrapped().setResourceName(resourceName);
    }
    
    
    
    
    
    // --------------------------------------------------- Deprecated methods
    
    
    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ResourceWrapper() {

    }

}
