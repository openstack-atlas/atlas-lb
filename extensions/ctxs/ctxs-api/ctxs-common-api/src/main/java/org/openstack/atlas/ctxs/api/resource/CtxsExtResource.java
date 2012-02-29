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
public class CtxsExtResource{

    @Autowired
    private CertificatesResource certificatesResource;

    private Integer accountId;

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Path("certificates")
    public CertificatesResource retrieveCertificatesResource() {
        certificatesResource.setAccountId(accountId);
        return certificatesResource;
    }  
}
