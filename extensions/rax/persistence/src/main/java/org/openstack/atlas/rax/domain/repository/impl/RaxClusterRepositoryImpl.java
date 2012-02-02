package org.openstack.atlas.rax.domain.repository.impl;

import org.openstack.atlas.rax.domain.entity.RaxCluster;
import org.openstack.atlas.rax.domain.repository.RaxClusterRepository;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class RaxClusterRepositoryImpl implements RaxClusterRepository {

    @PersistenceContext(unitName = "loadbalancing")
    private EntityManager entityManager;

    public RaxCluster getById(Integer clusterId) throws EntityNotFoundException {
        RaxCluster raxCluster = entityManager.find(RaxCluster.class, clusterId);
        if (raxCluster == null) {
            throw new EntityNotFoundException(ErrorMessages.CLUSTER_NOT_FOUND.getMessage(clusterId));
        }
        return raxCluster;
    }
}
