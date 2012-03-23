package org.openstack.atlas.adapter.common.service;

import org.openstack.atlas.adapter.common.entity.VirtualIpCluster;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface AdapterVirtualIpService {
    LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException;
    VirtualIpCluster createVirtualIpCluster(VirtualIpCluster vipCluster) throws PersistenceServiceException;
    VirtualIpCluster getVirtualIpCluster(Integer vipId);
}