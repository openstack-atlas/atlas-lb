package org.openstack.atlas.adapter.stingray.helper;

import org.openstack.atlas.adapter.exception.BadRequestException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;


import java.util.HashSet;
import java.util.Set;

public final class StingrayNameHelper {

    public static String generateNameWithAccountIdAndLoadBalancerId(Integer accountId , Integer lbId) throws BadRequestException {
        if (lbId == null) {
            throw new BadRequestException("Missing id for load balancer.");
        }
        if (accountId == null) {
            throw new BadRequestException("Missing account id for load balancer.");
        }

        return accountId + "_" + lbId;
    }

    public static String generateNameWithAccountIdAndLoadBalancerId(LoadBalancer lb) throws BadRequestException {
        return generateNameWithAccountIdAndLoadBalancerId( lb.getAccountId(), lb.getId());
    }

    public static Set<String> generateNamesWithAccountIdAndLoadBalancerId(Set<LoadBalancer> loadBalancers) throws BadRequestException {
        Set<String> generatedNames = new HashSet<String>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            generatedNames.add(generateNameWithAccountIdAndLoadBalancerId(loadBalancer));
        }
        return generatedNames;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, Integer vipId) throws BadRequestException {
        if (vipId == null) {
            throw new BadRequestException("Missing id for virtual ip.");
        }
        if (lb.getAccountId() == null) {
            throw new BadRequestException("Missing account id for load balancer.");
        }
        return lb.getAccountId() + "_" + vipId;
    }

    public static String generateTrafficIpGroupName(LoadBalancer lb, VirtualIp vip) throws BadRequestException {
        return generateTrafficIpGroupName(lb, vip.getId());
    }



    public static Integer stripAccountIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[0]);
    }

    public static Integer stripLbIdFromName(String name) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        return Integer.valueOf(name.split("_")[1]);
    }
}
