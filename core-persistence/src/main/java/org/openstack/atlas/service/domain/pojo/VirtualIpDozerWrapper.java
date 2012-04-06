package org.openstack.atlas.service.domain.pojo;

import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public final class VirtualIpDozerWrapper implements Serializable {
    private final static long serialVersionUID = 532512316L;

    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();

    public VirtualIpDozerWrapper() {
    }

    public VirtualIpDozerWrapper(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }

    public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
    }
}