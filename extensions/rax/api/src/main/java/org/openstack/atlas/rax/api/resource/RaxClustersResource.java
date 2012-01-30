package org.openstack.atlas.rax.api.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@Controller
@Scope("request")
public class RaxClustersResource {

    @Autowired
    private RaxClusterResource raxClusterResource;

    @GET
    public Response list() {
        return Response.status(200).entity("Success").build();
    }
}
