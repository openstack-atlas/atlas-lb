package org.openstack.atlas.ctxs.service.domain.repository.impl;

import org.openstack.atlas.ctxs.service.domain.entity.CertificateRef;
import org.openstack.atlas.ctxs.service.domain.entity.CtxsLoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.impl.LoadBalancerRepositoryImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;

@Primary
@Repository
@Transactional
public class CtxsLoadBalancerRepositoryImpl extends LoadBalancerRepositoryImpl {

    @Override
    public LoadBalancer create(LoadBalancer loadBalancer) {
        final Set<CertificateRef> certificateRefs = ((CtxsLoadBalancer)loadBalancer).getCertificates();
        ((CtxsLoadBalancer) loadBalancer).setCertificates(null);
        loadBalancer = super.create(loadBalancer);
        ((CtxsLoadBalancer) loadBalancer).setCertificates(new HashSet<CertificateRef>());
        if(certificateRefs != null)
        {
            for(CertificateRef certificateRef: certificateRefs)
            {
                CertificateRef certificateRefWithLB = new CertificateRef(((CtxsLoadBalancer)loadBalancer), certificateRef.getIdRef());
                entityManager.merge(certificateRefWithLB);
            }
            entityManager.flush();
        }
        return loadBalancer;
    }
}
