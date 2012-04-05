package org.openstack.atlas.adapter.common.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.core.api.v1.*;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.adapter.common.entity.*;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIp_;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;
import org.openstack.atlas.service.domain.exception.ServiceUnavailableException;


import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.VirtualIpCluster;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.PersistenceException;
import java.util.*;


@Repository
@Transactional(value="transactionManager2")
public class AdapterVirtualIpRepository  {
    private final Log LOG = LogFactory.getLog(AdapterVirtualIpRepository.class);
    @PersistenceContext(unitName = "loadbalancingadapter")
    protected EntityManager entityManager;

    @Autowired
    protected VirtualIpRepository virtualIpRepository;

    public void allocateIpv4VipBeforeDate(VirtualIp vip, Cluster cluster, Calendar vipReuseTime) throws OutOfVipsException {
        List<VirtualIpCluster> vipCandidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpCluster> criteria = builder.createQuery(VirtualIpCluster.class);
        Root<VirtualIpCluster> vipClusterRoot = criteria.from(VirtualIpCluster.class);

        Predicate isNotAllocated = builder.equal(vipClusterRoot.get(VirtualIpCluster_.isAllocated), false);
        Predicate lastDeallocationIsNull = builder.isNull(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation));
        Predicate isBeforeLastDeallocation = builder.lessThan(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation), vipReuseTime);
        Predicate sameVipType = builder.equal(vipClusterRoot.get(VirtualIpCluster_.vipType), vip.getVipType());


        criteria.select(vipClusterRoot);
        criteria.where(builder.and(isNotAllocated, sameVipType, builder.or(lastDeallocationIsNull, isBeforeLastDeallocation)));

        try {
            vipCandidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipCandidates == null) || vipCandidates.isEmpty())  {
                LOG.error(ErrorMessages.OUT_OF_VIPS);
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
            }

            for (VirtualIpCluster vipCandidate : vipCandidates)
            {
                // Is any of the candidate VIPs in the right cluster?
                if (vipCandidate.getCluster().getId().equals(cluster.getId()))  {
                    vipCandidate.setVipId(vip.getId());
                    vipCandidate.setAllocated(true);
                    vipCandidate.setLastAllocation(Calendar.getInstance());

                    entityManager.merge(vipCandidate);

                    vip.setAddress(vipCandidate.getAddress());

                    return;
                }
            }
        } catch (Exception e) {
            LOG.debug("Caught an exception");
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        LOG.error(ErrorMessages.OUT_OF_VIPS);
        throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
    }


    public void deallocateVirtualIp(VirtualIp virtualIp) {
        Integer vipId = virtualIp.getId();
        VirtualIpCluster vipCluster = getVirtualIpCluster(vipId);

        vipCluster.setAllocated(false);
        vipCluster.setLastDeallocation(Calendar.getInstance());
        vipCluster.setVipId(null);
        entityManager.merge(vipCluster);

        LOG.info(String.format("Virtual Ip '%d' de-allocated.", virtualIp.getId()));
    }

    public void allocateIpv4VipAfterDate(VirtualIp vip, Cluster cluster, Calendar vipReuseTime) throws OutOfVipsException {
        List<VirtualIpCluster> vipCandidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpCluster> criteria = builder.createQuery(VirtualIpCluster.class);
        Root<VirtualIpCluster> vipClusterRoot = criteria.from(VirtualIpCluster.class);

        Predicate isNotAllocated = builder.equal(vipClusterRoot.get(VirtualIpCluster_.isAllocated), false);
        Predicate isAfterLastDeallocation = builder.greaterThan(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation), vipReuseTime);
        Predicate sameVipType = builder.equal(vipClusterRoot.get(VirtualIpCluster_.vipType), vip.getVipType());

        criteria.select(vipClusterRoot);
        criteria.where(builder.and(isNotAllocated, sameVipType, isAfterLastDeallocation));

        try {
            vipCandidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipCandidates == null) || vipCandidates.isEmpty())   {
                LOG.error(ErrorMessages.OUT_OF_VIPS);
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
            }

            for (VirtualIpCluster vipCandidate : vipCandidates)
            {
                // Is any of the candidate VIPs in the right cluster?
                if (vipCandidate.getCluster().getId().equals(cluster.getId()))  {
                    vipCandidate.setVipId(vip.getId());
                    vipCandidate.setAllocated(true);
                    vipCandidate.setLastAllocation(Calendar.getInstance());

                    entityManager.merge(vipCandidate);

                    vip.setAddress(vipCandidate.getAddress());
                    return;
                }
            }

        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        LOG.error(ErrorMessages.OUT_OF_VIPS);
        throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
    }

    public VirtualIpCluster getVirtualIpCluster(Integer vipId) {
        String hqlStr = "from VirtualIpCluster vipCluster where vipCluster.vip_id = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpCluster> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no Cluster found for VirtualIp id %d.", vipId));
            return null;
        }
        return results.get(0);
    }

    public VirtualIp getVirtualIp(Integer vipId)  throws OutOfVipsException {

        try {
            return virtualIpRepository.getById(vipId);
        } catch(EntityNotFoundException e)
        {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }
    }

    public VirtualIpv6Octets getVirtualIpv6VipOctet(Integer vipId) {
        String hqlStr = "from VirtualIpv6Octets vip where vip.virtualIpv6Id = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpv6Octets> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no VirtualIpv6Octets found with id %d.", vipId));
            return null;
        }
        return results.get(0);
    }
    
    
    public VirtualIpCluster createVirtualIpCluster(VirtualIpCluster vipCluster)
    {
        LOG.info("Create/Update a VirtualIpCluster " + vipCluster.getAddress() + "...");
        VirtualIpCluster dbVipCluster = entityManager.merge(vipCluster);

        return dbVipCluster;
    }

 
    
    public Integer getNextVipOctet(Integer accountId) {
        List<Integer> maxList;
        Integer max;
        int retry_count = 3;

        String qStr = "SELECT max(v.vipOctets) from VirtualIpv6Octets v where v.accountId=:aid";

        while (retry_count > 0) {
            retry_count--;
            try {
                maxList = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).setParameter("aid", accountId).getResultList();
                max = maxList.get(0);
                if (max == null) {
                    max = 0;
                }
                max++; // The next VipOctet
                return max;
            } catch (PersistenceException e) {
                LOG.warn(String.format("Deadlock detected. %d retries left.", retry_count));
                if (retry_count <= 0) throw e;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        throw new ServiceUnavailableException("Too many create requests received. Please try again in a few moments.");
    }

    
}
