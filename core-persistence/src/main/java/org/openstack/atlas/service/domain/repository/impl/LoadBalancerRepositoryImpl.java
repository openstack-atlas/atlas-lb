package org.openstack.atlas.service.domain.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

@Repository
@Transactional(value="core_transactionManager")
public class LoadBalancerRepositoryImpl implements LoadBalancerRepository {

    final Log LOG = LogFactory.getLog(LoadBalancerRepositoryImpl.class);
    @PersistenceContext(unitName = "loadbalancing")
    protected EntityManager entityManager;

    @Override
    public LoadBalancer getById(Integer id) throws EntityNotFoundException {
        LoadBalancer loadBalancer = entityManager.find(LoadBalancer.class, id);
        if (loadBalancer == null) {
            throw new EntityNotFoundException(ErrorMessages.LB_NOT_FOUND);
        }
        return loadBalancer;
    }

    @Override
    public LoadBalancer getByIdAndAccountId(Integer id, Integer accountId) throws EntityNotFoundException {
        LoadBalancer loadBalancer = getById(id);
        if (!loadBalancer.getAccountId().equals(accountId)) {
            throw new EntityNotFoundException(ErrorMessages.LB_NOT_FOUND);
        }

        return loadBalancer;
    }

    @Override
    public List<LoadBalancer> getByAccountId(Integer accountId) {
        return entityManager.createQuery("SELECT lb FROM LoadBalancer lb WHERE lb.accountId = :accountId")
                .setParameter("accountId", accountId)
                .getResultList();
    }


    @Override
    public LoadBalancer create(LoadBalancer loadBalancer) {

        setLbIdOnChildObjects(loadBalancer);

        final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        loadBalancer.setLoadBalancerJoinVipSet(null);

        Calendar current = Calendar.getInstance();
        loadBalancer.setCreated(current);
        loadBalancer.setUpdated(current);
        loadBalancer = entityManager.merge(loadBalancer);

        Set<LoadBalancerJoinVip> newLbJoinVipSet = new HashSet<LoadBalancerJoinVip>();

        // Now attach loadbalancer to vips
        for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {

            lbJoinVipToLink.setLoadBalancer(loadBalancer);
            lbJoinVipToLink = entityManager.merge(lbJoinVipToLink);
            newLbJoinVipSet.add(lbJoinVipToLink);
        }

        loadBalancer.setLoadBalancerJoinVipSet(newLbJoinVipSet);



        return loadBalancer;
    }

    protected void setLbIdOnChildObjects(final LoadBalancer loadBalancer) {
        if (loadBalancer.getNodes() != null) {
            for (Node node : loadBalancer.getNodes()) {
                node.setLoadBalancer(loadBalancer);
            }
        }

        if (loadBalancer.getConnectionThrottle() != null) {
            loadBalancer.getConnectionThrottle().setLoadBalancer(loadBalancer);
        }
        if (loadBalancer.getHealthMonitor() != null) {
            loadBalancer.getHealthMonitor().setLoadBalancer(loadBalancer);
        }
        if (loadBalancer.getSessionPersistence() != null) {
            loadBalancer.getSessionPersistence().setLoadBalancer(loadBalancer);
        }
    }

    @Override
    public LoadBalancer update(LoadBalancer loadBalancer) {
        //final Set<LoadBalancerJoinVip> lbJoinVipsToLink = loadBalancer.getLoadBalancerJoinVipSet();
        //loadBalancer.setLoadBalancerJoinVipSet(null);

        loadBalancer.setUpdated(Calendar.getInstance());
        loadBalancer = entityManager.merge(loadBalancer);

        // Now attach loadbalancer to vips
        /*     for (LoadBalancerJoinVip lbJoinVipToLink : lbJoinVipsToLink) {
            VirtualIp virtualIp = entityManager.find(VirtualIp.class, lbJoinVipToLink.getVirtualIp().getId());
            LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, virtualIp);
            entityManager.merge(loadBalancerJoinVip);
            entityManager.merge(lbJoinVipToLink.getVirtualIp());
        }

        entityManager.flush();*/
        return loadBalancer;
    }

    @Override
    public Integer getNumNonDeletedLoadBalancersForAccount(Integer accountId) {
        Query query = entityManager.createNativeQuery(
                "select count(account_id) from load_balancer where status != 'DELETED' and account_id = :accountId").setParameter("accountId", accountId);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public void changeStatus(Integer accountId, Integer loadbalancerId, String newStatus) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        changeStatus(accountId, loadbalancerId, newStatus, false);
    }

    public void changeStatus(Integer accountId, Integer loadbalancerId, String newStatus, boolean allowConcurrentModifications) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
        // TODO: Hook up AOP logging here

        String queryString = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        Query q = entityManager.createQuery(queryString).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);

        List<LoadBalancer> lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException(ErrorMessages.LB_NOT_FOUND);
        }

        LoadBalancer lb = lbList.get(0);
        if (lb.getStatus().equals(CoreLoadBalancerStatus.DELETED)) {
            throw new UnprocessableEntityException(ErrorMessages.LB_DELETED);
        }
        final boolean isActive = lb.getStatus().equals(CoreLoadBalancerStatus.ACTIVE);
        final boolean isPendingOrActive = lb.getStatus().equals(CoreLoadBalancerStatus.PENDING_UPDATE) || isActive;
        final boolean isError = lb.getStatus().equals(CoreLoadBalancerStatus.ERROR);

        if (isError  || (allowConcurrentModifications ? isPendingOrActive : isActive)) {
            lb.setStatus(newStatus);
            lb.setUpdated(Calendar.getInstance());
            entityManager.merge(lb);
            return;
        }

        throw new ImmutableEntityException("Load Balancer can not be updated as it is currently being updated.");
    }

    public void updatePortInJoinTable(LoadBalancer lb) {
        String queryString = "from LoadBalancerJoinVip where loadBalancer.id = :lbId";
        Query query = entityManager.createQuery(queryString).setParameter("lbId", lb.getId());
        LoadBalancerJoinVip loadBalancerJoinVip = (LoadBalancerJoinVip) query.getSingleResult();
        loadBalancerJoinVip.setPort(lb.getPort());
        entityManager.merge(loadBalancerJoinVip);
    }

    public boolean canUpdateToNewPort(Integer newPort, Set<LoadBalancerJoinVip> setToCheckAgainst) {
        Set<VirtualIp> vipsToCheckAgainst = new HashSet<VirtualIp>();

        for (LoadBalancerJoinVip loadBalancerJoinVip : setToCheckAgainst) {
            vipsToCheckAgainst.add(loadBalancerJoinVip.getVirtualIp());
        }

        String queryString = "select j from LoadBalancerJoinVip j where j.virtualIp in (:vips)";
        Query query = entityManager.createQuery(queryString).setParameter("vips", vipsToCheckAgainst);

        List<LoadBalancerJoinVip> entriesWithPortsToCheckAgainst = query.getResultList();

        for (LoadBalancerJoinVip entryWithPortToCheckAgainst : entriesWithPortsToCheckAgainst) {
            if (entryWithPortToCheckAgainst.getPort().equals(newPort)) {
                return false;
            }
        }

        return true;
    }

     public boolean testAndSetStatus(Integer accountId, Integer loadbalancerId, String statusToChangeTo, boolean allowConcurrentModifications) throws EntityNotFoundException, UnprocessableEntityException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", accountId).
                setParameter("lid", loadbalancerId);
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException("");
        }

        LoadBalancer lb = lbList.get(0);
        if (lb.getStatus().equals(CoreLoadBalancerStatus.DELETED)) throw new UnprocessableEntityException(Constants.LoadBalancerDeleted);
        final boolean isActive = lb.getStatus().equals(CoreLoadBalancerStatus.ACTIVE);
        final boolean isPendingOrActive = lb.getStatus().equals(CoreLoadBalancerStatus.PENDING_UPDATE) || isActive;

        if(allowConcurrentModifications ? isPendingOrActive : isActive) {
            lb.setStatus(statusToChangeTo);
            lb.setUpdated(Calendar.getInstance());
            entityManager.merge(lb);
            return true;
        }

        return false;
    }

    public LoadBalancer changeStatus(LoadBalancer loadBalancer, String status) throws EntityNotFoundException {
        String qStr = "from LoadBalancer lb where lb.accountId=:aid and lb.id=:lid";
        List<LoadBalancer> lbList;
        Query q = entityManager.createQuery(qStr).setLockMode(LockModeType.PESSIMISTIC_WRITE).
                setParameter("aid", loadBalancer.getAccountId()).
                setParameter("lid", loadBalancer.getId());
        lbList = q.getResultList();
        if (lbList.size() < 1) {
            throw new EntityNotFoundException(ErrorMessages.LB_NOT_FOUND);
        }

        loadBalancer = lbList.get(0);
        loadBalancer.setStatus(status);
        entityManager.persist(loadBalancer);
        return loadBalancer;
    }

    /* Returns only attributes needed for performance reasons */
    public List<LoadBalancer> getLoadBalancersWithStatus(String status) {
        List<Object> loadBalancerTuples;
        List<LoadBalancer> loadBalancers = new ArrayList<LoadBalancer>();

        loadBalancerTuples = entityManager.createNativeQuery("SELECT lb.id, lb.account_id, lb.name FROM load_balancer lb where lb.status = :status")
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
}
