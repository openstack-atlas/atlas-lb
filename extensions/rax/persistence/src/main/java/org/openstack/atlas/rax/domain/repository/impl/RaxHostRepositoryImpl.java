package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.entity.RaxHost;
import org.openstack.atlas.rax.domain.repository.RaxHostRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional(value="core_transactionManager")
@Repository
public class RaxHostRepositoryImpl implements RaxHostRepository {

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public List<RaxHost> getAllHosts() {
        String query = "from RaxHost h where h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') ";
        List<RaxHost> raxHosts = entityManager.createQuery(query).getResultList();
        return raxHosts;
    }

    public void save(RaxHost raxHost) {
        entityManager.persist(raxHost);
    }
}
