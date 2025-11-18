package api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        res.getHeaders().putSingle("Access-Control-Allow-Origin", "http://localhost:5173");
        res.getHeaders().putSingle("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.getHeaders().putSingle("Access-Control-Allow-Headers", "Accept,Content-Type,If-None-Match,If-Match");
        res.getHeaders().putSingle("Access-Control-Expose-Headers", "ETag,Location,Content-Type");
    }
}
