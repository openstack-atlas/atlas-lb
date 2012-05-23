package org.openstack.atlas.rax.adapter.zxtm;

import org.apache.axis.AxisFault;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;


import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RaxZxtmAdapter {
    void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> vips) throws AdapterException;

    void deleteVirtualIps(LoadBalancer lb, List<Integer> vipIdsToDelete) throws AdapterException;

    void updateAccessList(Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws AdapterException;

    void deleteAccessList(Integer accountId, Integer lbId) throws AdapterException, AxisFault;

    void updateConnectionLogging(Integer accountId, Integer lbId, boolean isConnectionLogging, String protocol) throws AdapterException;

    void uploadDefaultErrorPage(String content) throws AdapterException;

    void setDefaultErrorPage(Integer loadBalancerId, Integer accountId) throws AdapterException;

    void setErrorPage(Integer loadBalancerId, Integer accountId, String content) throws AdapterException;

    void deleteErrorPage(Integer loadBalancerId, Integer accountId) throws AdapterException;
}
