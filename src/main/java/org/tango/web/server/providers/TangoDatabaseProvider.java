package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.rest.entities.Failures;
import org.tango.web.server.proxy.TangoDatabase;
import org.tango.web.server.util.TangoDatabaseUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/5/16
 */
@Provider
@Priority(Priorities.USER + 100)
public class TangoDatabaseProvider implements ContainerRequestFilter {
    public static final String DEFAULT_TANGO_PORT = "10000";
    private final Logger logger = LoggerFactory.getLogger(TangoDatabaseProvider.class);

    private final TangoRestServer tangoRestServer;

    public TangoDatabaseProvider(TangoRestServer tangoRestServer) {
        this.tangoRestServer = tangoRestServer;
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        if(pathSegments.size() < 3) return;
        if (!pathSegments.get(2).getPath().equalsIgnoreCase("hosts")) return;

        if (pathSegments.size() == 3/* no host was specified*/) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(Failures.createInstance("No Tango host was specified")).build());
            return;
        }


        PathSegment tango_host = pathSegments.get(3);
        String host = tango_host.getPath();
        String port = tango_host.getMatrixParameters().getFirst("port");
        if (port == null) port = DEFAULT_TANGO_PORT;

        Optional<TangoDatabase> tangoDb = TangoDatabaseUtils.getDatabase(host, port);

        tangoDb.ifPresent(database -> {
                ResteasyProviderFactory.pushContext(TangoDatabase.class, database);
        });
    }
}
