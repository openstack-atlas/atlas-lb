package org.openstack.atlas.ctxs.api.integration;

import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerService;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Primary
@Service
public interface CtxsReverseProxyLoadBalancerService extends ReverseProxyLoadBalancerService{

    List<Certificate> createCertificates(List<Certificate> dbCerts) throws Exception;
    Certificate getCertificate(Certificate certificate) throws Exception;
    void deleteCertificate(Certificate certificate) throws Exception;
}
