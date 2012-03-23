package org.openstack.atlas.adapter.common.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.core.api.v1.*;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.adapter.common.entity.*;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIp_;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;


import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.VirtualIpCluster;
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
import java.util.*;

@Repository
@Transactional
public class AdapterVirtualIpRepository  {
    private final Log LOG = LogFactory.getLog(AdapterVirtualIpRepository.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;



    public VirtualIp allocateIpv4VipBeforeDate(Cluster cluster, Calendar vipReuseTime, VirtualIpType vipType) throws OutOfVipsException {
        List<VirtualIpCluster> vipCandidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpCluster> criteria = builder.createQuery(VirtualIpCluster.class);
        Root<VirtualIpCluster> vipClusterRoot = criteria.from(VirtualIpCluster.class);
        Root<VirtualIp> vipRoot = criteria.from(VirtualIp.class);

        Predicate isNotAllocated = builder.equal(vipClusterRoot.get(VirtualIpCluster_.isAllocated), false);
        Predicate lastDeallocationIsNull = builder.isNull(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation));
        Predicate isBeforeLastDeallocation = builder.lessThan(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation), vipReuseTime);



        criteria.select(vipClusterRoot);
        criteria.where(builder.and(isNotAllocated, builder.or(lastDeallocationIsNull, isBeforeLastDeallocation)));

        try {
            vipCandidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipCandidates == null) || vipCandidates.isEmpty())
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);

            Integer vipId;

            for (VirtualIpCluster vipCandidate : vipCandidates)
            {
                vipId = vipCandidate.getVirtualIpId();
                VirtualIpCluster vipCluster = getVirtualIpCluster(vipId);
                VirtualIp vip = getVirtualIp(vipId);

                if (vipCluster.equals(cluster) && (vip.getVipType().equals(vipType)))
                {
                    vipCluster.setAllocated(true);
                    vipCluster.setLastAllocation(Calendar.getInstance());
                    entityManager.merge(vipCandidate);
                    return vip;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
    }


    public void deallocateVirtualIp(VirtualIp virtualIp) {
        Integer vipId = virtualIp.getId();
        VirtualIpCluster vipCluster = getVirtualIpCluster(vipId);

        vipCluster.setAllocated(false);
        vipCluster.setLastDeallocation(Calendar.getInstance());
        entityManager.merge(virtualIp);
        LOG.info(String.format("Virtual Ip '%d' de-allocated.", virtualIp.getId()));
    }

    public VirtualIp allocateIpv4VipAfterDate(Cluster cluster, Calendar vipReuseTime, VirtualIpType vipType) throws OutOfVipsException {
        List<VirtualIpCluster> vipCandidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpCluster> criteria = builder.createQuery(VirtualIpCluster.class);
        Root<VirtualIpCluster> vipClusterRoot = criteria.from(VirtualIpCluster.class);

        Predicate isNotAllocated = builder.equal(vipClusterRoot.get(VirtualIpCluster_.isAllocated), false);
        Predicate isAfterLastDeallocation = builder.greaterThan(vipClusterRoot.get(VirtualIpCluster_.lastDeallocation), vipReuseTime);


        criteria.select(vipClusterRoot);
        criteria.where(isNotAllocated, isAfterLastDeallocation);

        try {
            vipCandidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipCandidates == null) || vipCandidates.isEmpty())
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);

            Integer vipId;

            for (VirtualIpCluster vipCandidate : vipCandidates) {

                vipId = vipCandidate.getVirtualIpId();

                VirtualIpCluster vipCluster = getVirtualIpCluster(vipId);
                VirtualIp vip = getVirtualIp(vipId);

                if ((vipCluster.equals(cluster)) && (vip.getVipType().equals(vipType)))
                {
                    vipCandidate.setAllocated(true);
                    vipCandidate.setLastAllocation(Calendar.getInstance());
                    entityManager.merge(vipCandidate);
                    return vip;
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
    }

    public VirtualIpCluster getVirtualIpCluster(Integer vipId) {
        String hqlStr = "from VirtualIpCluster vipCluster where vipCluster.virtualIpId = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpCluster> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no Cluster found for VirtualIp id %d.", vipId));
            return null;
        }
        return results.get(0);
    }

    public VirtualIp getVirtualIp(Integer vipId) {
        String hqlStr = "from VirtualIp vip where vip.id = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIp> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no VirtualIp found with id %d.", vipId));
            return null;
        }
        return results.get(0);
    }

    public VirtualIpCluster createVirtualIpCluster(VirtualIpCluster vipCluster)
    {
        LOG.info("Create/Update a VirtualIpCluster " + vipCluster.getId() + "...");
        VirtualIpCluster dbVipCluster = entityManager.merge(vipCluster);

        return dbVipCluster;
    }
}
