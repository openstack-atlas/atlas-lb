package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.rax.domain.entity.RaxCluster;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

public interface RaxClusterRepository {
    RaxCluster getById(Integer clusterId) throws EntityNotFoundException;

}
