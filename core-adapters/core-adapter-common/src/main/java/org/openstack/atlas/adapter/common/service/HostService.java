package org.openstack.atlas.adapter.common.service;

import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface HostService {
    Host getDefaultActiveHost();
    LoadBalancerHost createLoadBalancerHost(LoadBalancerHost lbHost) throws PersistenceServiceException;
}
