package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import java.util.List;
import java.util.Set;

public interface RaxVirtualIpRepository extends VirtualIpRepository {

    List<Integer> getAccountIds(VirtualIp virtualIp);

    Long getNumVipsForLoadBalancer(LoadBalancer lb);

}
