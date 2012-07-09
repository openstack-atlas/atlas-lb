package org.openstack.atlas.rax.api.integration;

import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.rax.adapter.zxtm.RaxZxtmAdapter;
import org.openstack.atlas.rax.domain.entity.RaxAccessList;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Primary
@Service
public class RaxProxyServiceImpl extends ReverseProxyLoadBalancerServiceImpl implements RaxProxyService {

    @Override
    public void addVirtualIps(Integer accountId, Integer lbId, Set<VirtualIp> vips) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).addVirtualIps(accountId, lbId, vips);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteVirtualIps(LoadBalancer dbLoadBalancer, List<Integer> vipIdsToDelete) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteVirtualIps(dbLoadBalancer, vipIdsToDelete);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void updateAccessList(Integer accountId, Integer lbId, Collection<RaxAccessList> accessListItems) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).updateAccessList(accountId, lbId, accessListItems);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteAccessList(Integer accountId, Integer lbId) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteAccessList(accountId, lbId);
        } catch (ConnectionException exc) {

            throw exc;
        }
    }

    @Override
    public void updateConnectionLogging(Integer accountId, Integer lbId, boolean isConnectionLogging, String protocol) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).updateConnectionLogging(accountId, lbId, isConnectionLogging, protocol);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void uploadDefaultErrorPage(Integer clusterId, String content) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).uploadDefaultErrorPage(content);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void setDefaultErrorPage(Integer loadBalancerId, Integer accountId) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).setDefaultErrorPage(accountId, loadBalancerId);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void setErrorPage(Integer loadBalancerId, Integer accountId, String content) throws Exception {
        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).setErrorPage(accountId, loadBalancerId, content);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteErrorPage(Integer loadBalancerId, Integer accountId) throws Exception {

        try {
            ((RaxZxtmAdapter) loadBalancerAdapter).deleteErrorPage(accountId, loadBalancerId);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }
}
