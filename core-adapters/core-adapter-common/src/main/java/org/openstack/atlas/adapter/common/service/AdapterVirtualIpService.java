package org.openstack.atlas.adapter.common.service;

import org.openstack.atlas.adapter.common.entity.VirtualIpv4;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;

public interface AdapterVirtualIpService {
    LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException;
    VirtualIpv4 createVirtualIpCluster(VirtualIpv4 vipCluster) throws PersistenceServiceException;
    VirtualIpv4 getVirtualIpCluster(Integer vipId);
    void removeAllVipsFromLoadBalancer(LoadBalancer lb);
}