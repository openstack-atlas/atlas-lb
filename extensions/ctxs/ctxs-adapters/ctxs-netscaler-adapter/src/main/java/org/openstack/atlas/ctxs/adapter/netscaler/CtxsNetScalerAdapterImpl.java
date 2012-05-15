package org.openstack.atlas.ctxs.adapter.netscaler;

import org.openstack.atlas.adapter.common.entity.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.ctxs.adapter.CtxsLoadBalancerAdapter;

import org.openstack.atlas.adapter.exception.*;
import org.dozer.DozerBeanMapper;

import org.openstack.atlas.adapter.netscaler.NetScalerAdapterImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.*;
import com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Certificates;
import com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Certificate;

@Primary
@Service
public class CtxsNetScalerAdapterImpl extends NetScalerAdapterImpl implements CtxsLoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(CtxsNetScalerAdapterImpl.class.getName());
    
    @Autowired
//    @Qualifier("CtxsAdapterDozerMapper")
    protected DozerBeanMapper dozerMapper;



    @Autowired
    public CtxsNetScalerAdapterImpl(Configuration configuration) {
        super(configuration);
    }

    @Override
    public List<org.openstack.atlas.ctxs.service.domain.entity.Certificate> createCertificates(List<org.openstack.atlas.ctxs.service.domain.entity.Certificate> dbCerts) throws AdapterException
    {
        LOG.debug("Reached CtxsNetScalerAdapterImpl.createCertificates");

        Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");

        Certificates certificates = new Certificates();
        Integer accountId = dbCerts.get(0).getAccountId();

        for(int index =0; index < dbCerts.size(); index++) {
            Certificate cloudCertificate = (Certificate)dozerMapper.map(dbCerts.get(index), Certificate.class, "ctxs-cert-cloud-domain-mapping");
            certificates.getCertificates().add(cloudCertificate);
        }

        String resourceType = "certificates";
        String requestBody = nsAdapterUtils.getRequestBody(certificates);
        LOG.debug("Certificate request body " + requestBody);
        String serviceUrl = host.getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType);
        String response = nsAdapterUtils.performRequest("POST", resourceUrl, requestBody);
        Certificates cloudCertificates = (Certificates) nsAdapterUtils.getResponseObject(response);
        List<Certificate> cloudListCertificates = cloudCertificates.getCertificates();
        List<org.openstack.atlas.ctxs.service.domain.entity.Certificate> returnCerts = new ArrayList<org.openstack.atlas.ctxs.service.domain.entity.Certificate>();
        for(Certificate cloudCertificate : cloudListCertificates)
        {
            org.openstack.atlas.ctxs.service.domain.entity.Certificate returnCert = (org.openstack.atlas.ctxs.service.domain.entity.Certificate)
                    dozerMapper.map(cloudCertificate, org.openstack.atlas.ctxs.service.domain.entity.Certificate.class, "ctxs-cert-cloud-domain-mapping");
            returnCerts.add(returnCert);
        }
        return returnCerts;
    }

    @Override
    public org.openstack.atlas.ctxs.service.domain.entity.Certificate getCertificate(org.openstack.atlas.ctxs.service.domain.entity.Certificate certificate) throws AdapterException
    {
        LOG.debug("Reached CtxsNetScalerAdapterImpl.getCertificate");

         Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");

        Integer accountId = certificate.getAccountId();
        String resourceType = "certificates";
        String serviceUrl = host.getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, certificate.getId());
        Certificate cert = (Certificate) nsAdapterUtils.getResponseObject(nsAdapterUtils.performRequest("GET", resourceUrl, ""));
        org.openstack.atlas.ctxs.service.domain.entity.Certificate dbcert = (org.openstack.atlas.ctxs.service.domain.entity.Certificate)
                                        dozerMapper.map(cert, org.openstack.atlas.ctxs.service.domain.entity.Certificate.class, "ctxs-cert-cloud-domain-mapping");
        return dbcert;
    }

    @Override
    public void deleteCertificate(org.openstack.atlas.ctxs.service.domain.entity.Certificate certificate) throws AdapterException
    {
        LOG.debug("Reached CtxsNetScalerAdapterImpl.deleteCertificate");

        Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");

        Integer accountId = certificate.getAccountId();
        String resourceType = "certificates";
        String serviceUrl = host.getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, certificate.getId());
        nsAdapterUtils.performRequest("DELETE", resourceUrl, "");
    }
}
