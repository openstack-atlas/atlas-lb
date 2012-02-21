package org.openstack.atlas.ctxs.api.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.LoadBalancerValidator;
import org.openstack.atlas.ctxs.api.validation.validator.CertificateValidator;
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
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;


@Primary
@Controller
@Scope("request")
public class CertificatesResource extends CommonDependencyProvider  {
                                        
    public static Log LOG = LogFactory.getLog(CertificatesResource.class.getName());
    private HttpHeaders requestHeaders;
    protected Integer accountId;

    @Autowired
    protected CertificateValidator validator;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected CertificateRepository certificateRepository;

    @Autowired
    protected CertificateResource certificateResource;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createCertificates(Certificates _certificates) {
        try {
            List<Certificate> certsForAsync = new ArrayList<Certificate>();
            Certificates returnCerts = new Certificates();
            for (org.openstack.atlas.api.v1.extensions.ctxs.Certificate apiCert: _certificates.getCertificates())
            {
                ValidatorResult result = validator.validate(apiCert, HttpRequestType.POST);

                if (!result.passedValidation()) {
                    return ResponseFactory.getValidationFaultResponse(result);
                }

                Certificate domainCertificate = dozerMapper.map(apiCert, Certificate.class, "ctxs-cert-api-domain-mapping");
                domainCertificate.setAccountId(accountId);
                domainCertificate.setUserName(getUserName(requestHeaders));
                Certificate dbcert = certificateService.createCertificate(domainCertificate, apiCert);
                org.openstack.atlas.api.v1.extensions.ctxs.Certificate returnCert = dozerMapper.map(dbcert, org.openstack.atlas.api.v1.extensions.ctxs.Certificate.class, "ctxs-cert-domain-api-mapping");
                returnCert.setLinkcertificates(null);
                returnCerts.getCertificates().add(returnCert);
                certsForAsync.add(getCertForAsync(dbcert));
            }

            CtxsMessageDataContainer dataContainer = new CtxsMessageDataContainer();
            HashMap<String, Object> messageData = new HashMap<String, Object>();
            messageData.put("Certificates", certsForAsync);
            dataContainer.setHashData(messageData);

            asyncService.callAsyncLoadBalancingOperation("CREATE_CERTIFICATES", dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(returnCerts).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }

    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response list() {
    

        LOG.debug("entered list /certificates");
        
        Certificates _certificates = new Certificates();
        
        List<Certificate> certificates = certificateRepository.getByAccountId(accountId);
        
        for (Certificate certificate : certificates) {
            org.openstack.atlas.api.v1.extensions.ctxs.Certificate cert = dozerMapper.map(certificate, org.openstack.atlas.api.v1.extensions.ctxs.Certificate.class, "ctxs-cert-domain-api-mapping");
            cert.setLinkcertificates(null);
            _certificates.getCertificates().add(cert);
        }
        
        return Response.status(Response.Status.OK).entity(_certificates).build();
    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public CertificateResource retrieveCertificateResource(@PathParam("id") int id) {
        certificateResource.setAccountId(accountId);
        certificateResource.setId(id);
        return certificateResource;
    }


    private Certificate getCertForAsync(Certificate dbcert)
    {
        Certificate certForAsync = new Certificate();
        certForAsync.setAccountId(dbcert.getAccountId());
        certForAsync.setId(dbcert.getId());
        certForAsync.setUserName(dbcert.getUserName());
        return certForAsync;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }    
}
