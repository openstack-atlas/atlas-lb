package org.openstack.atlas.ctxs.service.domain.service.impl;

import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.service.domain.entity.CertificateRef;
import org.openstack.atlas.ctxs.service.domain.entity.CtxsLoadBalancer;
import org.openstack.atlas.ctxs.service.domain.repository.CertificateRepository;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.service.impl.LoadBalancerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Primary
public class CtxsLoadBalancerServiceImpl extends LoadBalancerServiceImpl {

    @Autowired
    CertificateRepository certificateRepository;

    @Override
    protected void validateCreate(LoadBalancer loadBalancer) throws BadRequestException, EntityNotFoundException, LimitReachedException {
        super.validateCreate(loadBalancer);    //To change body of overridden methods use File | Settings | File Templates.


        CtxsLoadBalancer ctxsLoadBalancer = ((CtxsLoadBalancer)loadBalancer);
        Set<CertificateRef> certificateRefs = ctxsLoadBalancer.getCertificates();
        if(certificateRefs != null)
        {
            for(CertificateRef ref : certificateRefs)
            {

                Certificate certificate = certificateRepository.getById(ref.getIdRef()); //EntityNotFoundException is thrown if id specified in idRef's is not found in certifiate repository
                if(!certificate.getStatus().equals("ACTIVE"))
                    throw new BadRequestException(String.format("Only certificates with ACTIVE status can be supplied. The status of the certificate with id %d is %s ", certificate.getId(), certificate.getStatus()));
            }
        }
    }
}
