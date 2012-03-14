package org.openstack.atlas.service.domain.common;

import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CoreNodeStatus;
import org.openstack.atlas.service.domain.entity.*;

public class LoadBalancerDefaultBuilder {


    private LoadBalancerDefaultBuilder() {
    }

    public static LoadBalancer addDefaultValues(final LoadBalancer loadBalancer) {
        loadBalancer.setStatus(CoreLoadBalancerStatus.QUEUED);
        NodesHelper.setNodesToStatus(loadBalancer, CoreNodeStatus.ONLINE);

        return loadBalancer;
    }
}
