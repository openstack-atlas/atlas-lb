package org.openstack.atlas.adapter.common.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.adapter.common.entity.*;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;
import org.openstack.atlas.service.domain.exception.ServiceUnavailableException;


import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.VirtualIpv4;
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




    public String allocateIpv4VipBeforeDate(VirtualIp vip, Cluster cluster, Calendar vipReuseTime) throws OutOfVipsException,EntityNotFoundException {

        // If we are using a VIP that has already been allocated before, we simply increase the ref count.
        if (vip.getAddress() != null) {

            VirtualIpv4 vipIpv4 = getVirtualIpv4(vip.getId());

            if (vipIpv4 == null) {
                throw new EntityNotFoundException("Adapter cannot find a previously allocated IPv4 vip with id: " + vip.getId());
            }

            Integer refCount = vipIpv4.getRefCount();

            refCount++;
            vipIpv4.setRefCount(refCount);
            entityManager.merge(vipIpv4);
            LOG.info(String.format("Virtual Ip '%d' refcount set to %d", vip.getId(), refCount));
            return vip.getAddress();
        }

        List<VirtualIpv4> vipCandidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpv4> criteria = builder.createQuery(VirtualIpv4.class);
        Root<VirtualIpv4> vipClusterRoot = criteria.from(VirtualIpv4.class);

        Predicate isNotAllocated = builder.equal(vipClusterRoot.get(VirtualIpv4_.isAllocated), false);
        Predicate lastDeallocationIsNull = builder.isNull(vipClusterRoot.get(VirtualIpv4_.lastDeallocation));
        Predicate isBeforeLastDeallocation = builder.lessThan(vipClusterRoot.get(VirtualIpv4_.lastDeallocation), vipReuseTime);
        Predicate sameVipType = builder.equal(vipClusterRoot.get(VirtualIpv4_.vipType), vip.getVipType());


        criteria.select(vipClusterRoot);
        criteria.where(builder.and(isNotAllocated, sameVipType, builder.or(lastDeallocationIsNull, isBeforeLastDeallocation)));

        try {
            vipCandidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipCandidates == null) || vipCandidates.isEmpty())  {
                LOG.error(ErrorMessages.OUT_OF_VIPS);
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
            }

            for (VirtualIpv4 vipCandidate : vipCandidates)
            {
                // Is any of the candidate VIPs in the right cluster?
                if (vipCandidate.getCluster().getId().equals(cluster.getId()))  {
                    vipCandidate.setVipId(vip.getId());
                    vipCandidate.setAllocated(true);
                    vipCandidate.setLastAllocation(Calendar.getInstance());
                    vipCandidate.setRefCount(1);

                    entityManager.merge(vipCandidate);
                    LOG.info(String.format("Virtual Ip '%d' refcount set to 1", vip.getId()));
                    return vipCandidate.getAddress();
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

    public String allocateIpv4VipAfterDate(VirtualIp vip, Cluster cluster, Calendar vipReuseTime) throws OutOfVipsException, EntityNotFoundException {

        // If we are using a VIP that has already been allocated before, we simply increase the ref count.
        if (vip.getAddress() != null) {

            VirtualIpv4 vipIpv4 = getVirtualIpv4(vip.getId());

            if (vipIpv4 == null) {
                throw new EntityNotFoundException("Adapter cannot find a previously allocated IPv4 vip with id: " + vip.getId());
            }

            Integer refCount = vipIpv4.getRefCount();

            refCount++;
            vipIpv4.setRefCount(refCount);
            entityManager.merge(vipIpv4);
            LOG.info(String.format("Virtual Ip '%d' refcount set to %d", vip.getId(), refCount));
            return vip.getAddress();
        }


        List<VirtualIpv4> vipIpv4Candidates;
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<VirtualIpv4> criteria = builder.createQuery(VirtualIpv4.class);
        Root<VirtualIpv4> vipIpv4Root = criteria.from(VirtualIpv4.class);

        Predicate isNotAllocated = builder.equal(vipIpv4Root.get(VirtualIpv4_.isAllocated), false);
        Predicate isAfterLastDeallocation = builder.greaterThan(vipIpv4Root.get(VirtualIpv4_.lastDeallocation), vipReuseTime);
        Predicate sameVipType = builder.equal(vipIpv4Root.get(VirtualIpv4_.vipType), vip.getVipType());

        criteria.select(vipIpv4Root);
        criteria.where(builder.and(isNotAllocated, sameVipType, isAfterLastDeallocation));

        try {
            vipIpv4Candidates = entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

            if ((vipIpv4Candidates == null) || vipIpv4Candidates.isEmpty())   {
                LOG.error(ErrorMessages.OUT_OF_VIPS);
                throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
            }

            for (VirtualIpv4 vipIpv4Candidate : vipIpv4Candidates)
            {
                // Is any of the candidate VIPs in the right cluster?
                if (vipIpv4Candidate.getCluster().getId().equals(cluster.getId()))  {
                    vipIpv4Candidate.setVipId(vip.getId());
                    vipIpv4Candidate.setAllocated(true);
                    vipIpv4Candidate.setLastAllocation(Calendar.getInstance());
                    vipIpv4Candidate.setRefCount(1);
                    LOG.info(String.format("Virtual Ip '%d' refcount set to 1", vip.getId()));
                    entityManager.merge(vipIpv4Candidate);

                    return vipIpv4Candidate.getAddress();
                }
            }

        } catch (Exception e) {
            LOG.error(e);
            throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
        }

        LOG.error(ErrorMessages.OUT_OF_VIPS);
        throw new OutOfVipsException(ErrorMessages.OUT_OF_VIPS);
    }


    public String allocateIpv6Vip(VirtualIp vip, Integer accountId, Cluster c) throws EntityNotFoundException {



        // If we are using a VIP that has already been allocated before, we simply increase the ref count.
        if (vip.getAddress() != null) {

            VirtualIpv6 vipIpv6 = getVirtualIpv6(vip.getId());

            if (vipIpv6 == null) {
                throw new EntityNotFoundException("Adapter cannot find a previously allocated IPv6 vip with id: " + vip.getId());
            }

            Integer refCount = vipIpv6.getRefCount();
            refCount++;
            vipIpv6.setRefCount(refCount);
            entityManager.merge(vipIpv6);

            LOG.info(String.format("Virtual Ip '%d' refcount set to %d", vip.getId(), refCount));
            return vip.getAddress();
        }

        Integer vipOctets = getNextVipOctet(accountId);
        VirtualIpv6 vipIpv6 = new VirtualIpv6();

        vipIpv6.setAccountId(accountId);
        vipIpv6.setVipOctets(vipOctets);
        vipIpv6.setVirtualIpId(vip.getId());
        vipIpv6.setRefCount(1);

        vipIpv6 = entityManager.merge(vipIpv6);

        try {
            LOG.info(String.format("Virtual Ip '%d' refcount set to 1", vip.getId()));
            return vipIpv6.getDerivedIpString(c);
        } catch (IPStringConversionException1 e) {
            LOG.error("Caught an exception while trying to convert IPv6 octets into a string");
            return null;
        }
    }

    public void deallocateVirtualIp(VirtualIp vip) {

        IpVersion ipVersion = vip.getIpVersion();
        Integer vipId = vip.getId();

        if (ipVersion == IpVersion.IPV6) {
            deallocateIpv6VirtualIp(vipId);
        } else {
            deallocateIpv4VirtualIp(vipId);
        }
    }

    public void resetVirtualIp(VirtualIp vip) {

        IpVersion ipVersion = vip.getIpVersion();
        Integer vipId = vip.getId();

        if (ipVersion == IpVersion.IPV6) {
            resetIpv6VirtualIp(vipId);
        } else {
            resetIpv4VirtualIp(vipId);
        }
    }


    private void deallocateIpv4VirtualIp(Integer vipId) {

        VirtualIpv4 vipIpv4 = getVirtualIpv4(vipId);
        Integer refCount = vipIpv4.getRefCount();

        if (refCount == 1) {
            vipIpv4.setAllocated(false);
            vipIpv4.setLastDeallocation(Calendar.getInstance());
            vipIpv4.setVipId(null);

        }

        refCount--;
        vipIpv4.setRefCount(refCount);
        entityManager.merge(vipIpv4);

        LOG.info(String.format("Virtual Ip '%d' refcount set to %d", vipId, refCount));
    }

    private void resetIpv4VirtualIp(Integer vipId) {

        VirtualIpv4 vipIpv4 = getVirtualIpv4(vipId);
        Integer refCount = vipIpv4.getRefCount();

        if (refCount == 1) {
            vipIpv4.setAllocated(false);
            vipIpv4.setLastAllocation(null);
            vipIpv4.setLastDeallocation(null);
            vipIpv4.setVipId(null);
        }
        refCount--;
        vipIpv4.setRefCount(refCount);
        entityManager.merge(vipIpv4);

        LOG.info(String.format("Virtual Ip '%d' refcount set to %d", vipId, refCount));
    }

    private void deallocateIpv6VirtualIp(Integer vipId) {

        VirtualIpv6 vip6 = getVirtualIpv6(vipId);
        Integer refCount = vip6.getRefCount();

        if (refCount == 1) {
        entityManager.remove(vip6);
        } else {
            vip6.setRefCount(--refCount);
            entityManager.merge(vip6);
        }

        LOG.info(String.format("Virtual Ip '%d' de-allocated.", vipId));
    }

    private void resetIpv6VirtualIp(Integer vipId) {

        deallocateIpv6VirtualIp(vipId);

        LOG.info(String.format("Virtual Ip '%d' de-allocated.", vipId));
    }

    public VirtualIpv4 getVirtualIpv4(Integer vipId) {
        String hqlStr = "from VirtualIpv4 vipCluster where vipCluster.vip_id = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpv4> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no Cluster found for VirtualIp id %d.", vipId));
            return null;
        }
        return results.get(0);
    }

    public VirtualIpv6 getVirtualIpv6(Integer vipId) {
        String hqlStr = "from VirtualIpv6 vip6Octets where vip6Octets.virtualIpId = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpv6> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no VirtualIpv6 found for VirtualIp id %d.", vipId));
            return null;
        }
        return results.get(0);
    }


    public VirtualIpv6 getVirtualIpv6VipOctet(Integer vipId) {
        String hqlStr = "from VirtualIpv6 vip where vip.virtualIpv6Id = :vipId";
        Query query = entityManager.createQuery(hqlStr).setParameter("vipId", vipId).setMaxResults(1);
        List<VirtualIpv6> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no VirtualIpv6 found with id %d.", vipId));
            return null;
        }
        return results.get(0);
    }
    
    
    public VirtualIpv4 createVirtualIpv4(VirtualIpv4 vipCluster)
    {
        LOG.info("Create/Update a VirtualIpv4 " + vipCluster.getAddress() + "...");
        VirtualIpv4 dbVipIpv4 = entityManager.merge(vipCluster);

        return dbVipIpv4;
    }

 
    
    public Integer getNextVipOctet(Integer accountId) {
        List<Integer> maxList;
        Integer max;
        int retry_count = 3;

        String qStr = "SELECT max(v.vipOctets) from VirtualIpv6 v where v.accountId=:aid";

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
