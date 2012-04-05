package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.ServiceUnavailableException;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
@Transactional
public class VirtualIpv6RepositoryImpl implements VirtualIpv6Repository {

    private final Log LOG = LogFactory.getLog(VirtualIpv6RepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    @Override
    public VirtualIpv6 create(VirtualIpv6 vip6) {
          return entityManager.merge(vip6);
    }

    @Override
    public void update(VirtualIpv6 vip6) {
          entityManager.merge(vip6);
    }


    @Override
    public List<VirtualIpv6> getVipsByAccountId(Integer accountId) {
        List<VirtualIpv6> vips;
        String query = "select distinct(j.virtualIp) from LoadBalancerJoinVip6 j where j.loadBalancer.accountId = :accountId";
        //String query = "select distinct l.virtualIps from LoadBalancer l where l.accountId = :accountId";
        vips = entityManager.createQuery(query).setParameter("accountId", accountId).getResultList();
        return vips;
    }

    @Override
    public Set<VirtualIpv6> getVipsByLoadBalancerId(Integer loadBalancerId) {
        String qStr = "SELECT j.virtualIp from LoadBalancerJoinVip6 j where j.loadBalancer.id = :loadBalancerId";
        Query q = entityManager.createQuery(qStr).setParameter("loadBalancerId", loadBalancerId);
        return new HashSet<VirtualIpv6>(q.getResultList());
    }

    @Override
    public List<Integer> getAccountIdsAlreadyShaHashed() {
        return entityManager.createQuery("select a.id from Account a").getResultList();
    }

    @Override
    public void deleteVirtualIp(VirtualIpv6 virtualIpv6) {
        virtualIpv6 = entityManager.find(VirtualIpv6.class, virtualIpv6.getId());
        entityManager.remove(virtualIpv6);
        LOG.info(String.format("IPv6 virtual Ip '%d' deleted.", virtualIpv6.getId()));
    }

        public void removeJoinRecord(LoadBalancerJoinVip6 loadBalancerJoinVip6) {
        loadBalancerJoinVip6 = entityManager.find(LoadBalancerJoinVip6.class, loadBalancerJoinVip6.getId());
        VirtualIpv6 virtualIpv6 = entityManager.find(VirtualIpv6.class, loadBalancerJoinVip6.getVirtualIp().getId());
        virtualIpv6.getLoadBalancerJoinVip6Set().remove(loadBalancerJoinVip6);
        entityManager.remove(loadBalancerJoinVip6);
    }


    @Override
    public void removeVirtualIpv6(VirtualIpv6 ipv6) {

        entityManager.remove(ipv6);
    }

    @Override
     public List<LoadBalancerJoinVip6> getJoinRecordsForVip(VirtualIpv6 virtualIp) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LoadBalancerJoinVip6> criteria = builder.createQuery(LoadBalancerJoinVip6.class);
        Root<LoadBalancerJoinVip6> lbJoinVipRoot = criteria.from(LoadBalancerJoinVip6.class);

        Predicate hasVip = builder.equal(lbJoinVipRoot.get(LoadBalancerJoinVip6_.virtualIp), virtualIp);

        criteria.select(lbJoinVipRoot);
        criteria.where(hasVip);
        return entityManager.createQuery(criteria).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
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
    public Map<Integer, List<LoadBalancer>> getPorts(Integer vid) {
        Map<Integer, List<LoadBalancer>> map = new TreeMap<Integer, List<LoadBalancer>>();
        List<Object> hResults;

        String query = "select j.virtualIp.id, j.loadBalancer.id, j.loadBalancer.accountId, j.loadBalancer.port " +
                "from LoadBalancerJoinVip6 j where j.virtualIp.id = :vid order by j.loadBalancer.port, j.loadBalancer.id";

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

}
