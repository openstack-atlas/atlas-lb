package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.OutOfVipsException;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@Transactional
public class VirtualIpRepositoryImpl implements VirtualIpRepository {
    private final Log LOG = LogFactory.getLog(VirtualIpRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    public void persist(Object obj) {
        entityManager.persist(obj);
    }


    @Override
    public VirtualIp create(VirtualIp vip) {
          return entityManager.merge(vip);
    }

    @Override
    public void update(VirtualIp vip) {
          entityManager.merge(vip);
    }


    @Override
    public List<LoadBalancerJoinVip> getJoinRecordsForVip(VirtualIp virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerJoinVip> criteria = builder.createQuery(LoadBalancerJoinVip.class);
        Root<LoadBalancerJoinVip> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip.class);

        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip_.virtualIp), virtualIp);

        criteria.select(lbJoinVipRoot);
        criteria.where(hasVip);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
    }

    @Override
    public List<VirtualIp> getVipsByAccountId(Integer accountId) {
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip j where j.loadBalancer.accountId = :accountId";
        List<VirtualIp> vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

    @Override
    public List<VirtualIp> getVipsByLoadBalancerId(Integer loadBalancerId) {
        List<VirtualIp> vips;
        String query = "select j.virtualIp from LoadBalancerJoinVip j where j.loadBalancer.id = :loadBalancerId";
        vips = entityManager.createQuery(query).setParameter("loadBalancerId", loadBalancerId).getResultList();
        return vips;
    }


    @Override
    public VirtualIp getById(Integer id) throws EntityNotFoundException {
        VirtualIp vip = entityManager.find(VirtualIp.class, id);
        if (vip == null) {
            throw new EntityNotFoundException(ErrorMessages.VIP_NOT_FOUND);
        }
        return vip;
    }



    @Override
    public void removeJoinRecord(LoadBalancerJoinVip loadBalancerJoinVip) {

        VirtualIp vip = loadBalancerJoinVip.getVirtualIp();
        Integer vipId = vip.getId();

        loadBalancerJoinVip = entityManager.find(LoadBalancerJoinVip.class, loadBalancerJoinVip.getId());
        VirtualIp virtualIp = entityManager.find(VirtualIp.class, vipId);
        virtualIp.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
        entityManager.remove(loadBalancerJoinVip);
    }


    @Override
    public void removeVirtualIp(VirtualIp vip) {
        vip = entityManager.find(VirtualIp.class, vip.getId());
        entityManager.remove(vip);
    }


    @Override
    public Map<Integer, List<LoadBalancer>> getPorts(Integer vid) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.virtualIp.id, j.loadBalancer.id, j.loadBalancer.accountId, j.loadBalancer.port " +
                "from LoadBalancerJoinVip j where j.virtualIp.id = :vid order by j.loadBalancer.port, j.loadBalancer.id";

        hResults = entityManager.createQuery(query).setParameter("vid", vid).getResultList();
        for (Object r : hResults) {
            Object[] row = (Object[]) r;
            Integer port = (Integer) row[3];
            if (!map.containsKey(port)) {
                map.put(port, new ArrayList<LoadBalancer>());
            }
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[1]);
            lb.setAccountId((Integer) row[2]);
            lb.setPort((Integer) row[3]);
            map.get(port).add(lb);
        }
        return map;
    }

    @Override
    public Account getLockedAccountRecord(Integer accountId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> accountRoot = criteria.from(Account.class);

        Predicate recordWithId = builder.equal(accountRoot.get(Account_.id), accountId);

        criteria.select(accountRoot);
        criteria.where(recordWithId);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
    }



    @Override
    public List<Integer> getAccountIdsAlreadyShaHashed() {
        return entityManager.createQuery("select a.id from Account a").getResultList();
    }

}
