package org.openstack.atlas.api.resource;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class RootResource {

    @Autowired
    private AccountRootResource accountRootResource;

    @Path("/{id: [1-9][0-9]*}")
    public AccountRootResource retrieveAccountResource(@PathParam("id") int id) {
        accountRootResource.setAccountId(id);
        return accountRootResource;
    }
}
