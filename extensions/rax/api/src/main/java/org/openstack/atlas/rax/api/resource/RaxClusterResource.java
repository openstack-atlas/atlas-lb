package org.openstack.atlas.rax.api.resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@Controller
@Scope("request")
public class RaxClusterResource {

    @GET
    public Response get() {
        return Response.status(200).entity("Success").build();
    }
}
