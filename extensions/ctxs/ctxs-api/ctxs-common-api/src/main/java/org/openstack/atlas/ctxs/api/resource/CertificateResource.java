package org.openstack.atlas.ctxs.api.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.service.domain.operation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;


import org.openstack.atlas.core.api.v1.LoadBalancers;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.api.v1.extensions.ctxs.Certificates;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.service.domain.service.CertificateService;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.ctxs.service.domain.repository.CertificateRepository;
import org.openstack.atlas.ctxs.service.domain.pojo.CtxsMessageDataContainer;


@Primary
@Controller
@Scope("request")
public class CertificateResource extends CommonDependencyProvider  {

    public static Log LOG = LogFactory.getLog(CertificateResource.class.getName());
    protected Integer id;
    protected Integer accountId;

    private HttpHeaders requestHeaders;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected CertificateRepository certificateRepository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response get() {
        try {
            Certificate certificate = certificateRepository.getByIdAndAccountId(id, accountId);
            org.openstack.atlas.api.v1.extensions.ctxs.Certificate apiCert = dozerMapper.map(certificate, org.openstack.atlas.api.v1.extensions.ctxs.Certificate.class, "ctxs-cert-domain-api-mapping");
            apiCert.setLinkcertificates(null);
            return Response.status(Response.Status.OK).entity(apiCert).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteCertificate() {
        try {
            Certificate certificate = new Certificate();
            certificate.setId(id);
            certificate.setAccountId(accountId);

            String deleteStatus = certificateService.delete(accountId, id);

            if(deleteStatus.equals("PENDING_DELETE"))
            {
                CtxsMessageDataContainer dataContainer = new CtxsMessageDataContainer();
                HashMap<String, Object> messageData = new HashMap<String, Object>();
                messageData.put("Certificate", certificate);
                dataContainer.setHashData(messageData);

                asyncService.callAsyncLoadBalancingOperation("DELETE_CERTIFICATE", dataContainer);
            }

            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
