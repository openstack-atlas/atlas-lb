package org.openstack.atlas.adapter.common.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Repository
@Transactional(value="adapter_transactionManager")
public class ClusterRepository {

    final Log LOG = LogFactory.getLog(ClusterRepository.class);

    @PersistenceContext(unitName = "loadbalancingadapter")
    private EntityManager entityManager;

    public Cluster getById(Integer id) throws EntityNotFoundException {
        Cluster cl = entityManager.find(Cluster.class, id);
        if (cl == null) {
            String errMsg = String.format("Cannot access cluster {id=%d}", id);
            LOG.warn(errMsg);
            throw new EntityNotFoundException(errMsg);
        }
        return cl;
    }
}
