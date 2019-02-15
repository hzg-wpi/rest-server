/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server.filters;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.AccessControl;
import org.tango.web.server.exception.mapper.NoSuchCommand;
import org.tango.web.server.exception.mapper.TangoProxyExceptionMapper;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Ingvord
 * @since 01.07.14
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AccessControlFilter implements ContainerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(AccessControlFilter.class);

    private final AccessControl accessControl;

    public AccessControlFilter(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpServletRequest httpServletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String user = httpServletRequest.getRemoteUser();
        if (user == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Anonymous access is restricted. Provide username and password.").build());
            return;
        }

        try {
            UriInfo uriInfo = requestContext.getUriInfo();

            MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
            String domain = pathParams.getFirst("domain");
            String family = pathParams.getFirst("family");
            String member = pathParams.getFirst("member");

            String device = domain + "/" + family + "/" + member;
            String method = requestContext.getMethod();

            logger.debug("Validating user rights: {}, {}, {}", user, httpServletRequest.getRemoteAddr(), device);
            switch (method) {
                case "GET":
                    if (!accessControl.checkUserCanRead(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have read access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        logger.debug(msg);
                    }

                    break;
                case "PUT":
                case "POST":
                case "DELETE":
                    if (!accessControl.checkUserCanWrite(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have write access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        logger.debug(msg);
                    }
                    break;
                default:
                    requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).build());
                    logger.debug("Method is not allowed: " + method);
            }

        } catch (NoSuchCommandException e) {
            assert false;
            requestContext.abortWith(new NoSuchCommand().toResponse(e));
        } catch (TangoProxyException e) {
            requestContext.abortWith(new TangoProxyExceptionMapper().toResponse(e));
        }
    }
}
