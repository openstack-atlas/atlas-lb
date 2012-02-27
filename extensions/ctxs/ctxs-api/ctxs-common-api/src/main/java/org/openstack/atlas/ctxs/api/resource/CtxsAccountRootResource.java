package org.openstack.atlas.ctxs.api.resource;

import org.springframework.beans.factory.annotation.Autowired;

import org.openstack.atlas.api.resource.AccountRootResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Path;

@Primary
@Controller
@Scope("request")
public class CtxsAccountRootResource extends AccountRootResource {

    @Autowired
    private CtxsExtResource extResource;
    
    @Path("ext/ctxs")
    public CtxsExtResource retrieveExtResource() {
        extResource.setAccountId(accountId);
        return extResource;
    }
}
