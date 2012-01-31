package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.RootResource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RaxRootResource extends RootResource {

    @Autowired
    private RaxClustersResource raxClustersResource;

    @Autowired
    private RaxHostsResource raxHostsResource;

    @Path("clusters")
    public RaxClustersResource retrieveClustersResource() {
        return raxClustersResource;
    }

    @Path("hosts")
    public RaxHostsResource retrieveHostsResource() {
        return raxHostsResource;
    }
}
