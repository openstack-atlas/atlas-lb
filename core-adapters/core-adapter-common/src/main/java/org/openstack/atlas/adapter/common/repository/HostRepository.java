package org.openstack.atlas.adapter.common.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.adapter.common.entity.HostStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(value="transactionManager2")
public class HostRepository {

    final Log LOG = LogFactory.getLog(HostRepository.class);
    @PersistenceContext(unitName = "loadbalancingadapter")
    private EntityManager entityManager2;

//    public Host getById(Integer id) throws EntityNotFoundException {
//        Host host = entityManager.find(Host.class, id);
//        if (host == null) {
//            String errMsg = String.format("Cannot access host {id=%d}", id);
//            LOG.warn(errMsg);
//            throw new EntityNotFoundException(errMsg);
//        }
//        return host;
//    }

    public Host getEndPointHost(Integer clusterId) {
        String hqlStr = "from Host h where h.endpointActive = 1 "
                + "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') "
                + "and h.cluster.id = :clusterId "
                + "order by h.hostStatus desc, h.id asc";
        Query query = entityManager2.createQuery(hqlStr).setParameter("clusterId", clusterId).setMaxResults(1);
        List<Host> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no more endpoints left for ClusterId %d.", clusterId));
            return null;
        }
        return results.get(0);
    }

    public LoadBalancerHost getLBHost(Integer lbId) {
        String hqlStr = "from LoadBalancerHost lbHost where lbHost.id.loadBalancerId = :loadBalancerId";
        Query query = entityManager2.createQuery(hqlStr).setParameter("loadBalancerId", lbId).setMaxResults(1);
        List<LoadBalancerHost> results = query.getResultList();
        if (results.size() < 1) {
            LOG.error(String.format("Error no Host found for LoadBalancer id %d.", lbId));
            return null;
        }
        return results.get(0);
    }

    public LoadBalancerHost createLoadBalancerHost(LoadBalancerHost lbHost) throws Exception
    {
        LOG.debug("before merge");

        entityManager2.persist(lbHost);
        LOG.debug("after persist");

        LOG.info(String.format("Create/Update a LoadBalancerHost for loadbalancer id %d and host id %d", lbHost.getLoadBalancerId(), lbHost.getHost().getId()));

        return lbHost;
    }

    public Host update(Host host) {
        LOG.info("Updating Host " + host.getId() + "...");
        host = entityManager2.merge(host);
        entityManager2.flush();
        return host;
    }

    public List<String> getFailoverHostNames(Integer clusterId) {
        String hql = "select h.name from Host h where h.hostStatus = 'FAILOVER' and h.cluster.id = :clusterId";

        Query query = entityManager2.createQuery(hql).setParameter("clusterId", clusterId);
        List<String> results = query.getResultList();
        return results;
    }

    public List<Host> getHosts() {
        String sql = "SELECT h from Host h";

        Query query = entityManager2.createQuery(sql);
        List<Host> hosts = query.getResultList();
        return hosts;
    }

    public long countLoadBalancersInHost(Host host) {
        String query = "select count(*) from LoadBalancerHost lbHost where lbHost.host.id = :id";

        List<Long> lbsInHost = entityManager2.createQuery(query).setParameter("id", host.getId()).getResultList();
        long count = lbsInHost.get(0).longValue();
        return count;

    }

    public Host getHostWithMinimumLoadBalancers(List<Host> hosts) {

        long mincount = 0;
        Host hostWithMinimumLoadBalancers = null;
        for (Host host : hosts) {
            long count = countLoadBalancersInHost(host);
            if (count == 0) {
                return host;
            } else {
                if (mincount == 0) {
                    mincount = count;
                    hostWithMinimumLoadBalancers = host;
                } else if (mincount <= count) {
                    //do nothing
                } else {
                    mincount = count;
                    hostWithMinimumLoadBalancers = host;
                }
            }
        }
        return hostWithMinimumLoadBalancers;
    }

    /* Returns only attributes needed for performance reasons */
    public List<LoadBalancer> getUsageLoadBalancersWithStatus(Integer hostId, String status) {
        List<Object> loadBalancerTuples;
        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();

        loadBalancerTuples = entityManager2.createNativeQuery("SELECT lb.id, lb.account_id, lb.name FROM load_balancer lb where lb.host_id = :hostId and lb.status = :status")
                .setParameter("hostId", hostId)
                .setParameter("status", status)
                .getResultList();

        for (Object loadBalancerTuple : loadBalancerTuples) {
            Object[] row = (Object[]) loadBalancerTuple;
            LoadBalancer lb = new LoadBalancer();
            lb.setId((Integer) row[0]);
            lb.setAccountId((Integer) row[1]);
            lb.setName((String) row[2]);
            loadBalancers.add(lb);
        }

        return loadBalancers;
    }

    public List<Host> getActiveHosts() {
        List<Host> allHosts = getHosts();
        List<Host> activeHosts = new ArrayList<Host>();

        for (Host host : allHosts) {
            if (host.getHostStatus().equals(HostStatus.ACTIVE) || host.getHostStatus().equals(HostStatus.ACTIVE_TARGET)) {
                activeHosts.add(host);
            }
        }

        return activeHosts;
    }
}
