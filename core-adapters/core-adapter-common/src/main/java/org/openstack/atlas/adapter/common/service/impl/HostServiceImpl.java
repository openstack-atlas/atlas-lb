package org.openstack.atlas.adapter.common.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.service.HostService;
import org.openstack.atlas.service.domain.service.impl.HealthMonitorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HostServiceImpl implements HostService {
    private final Log LOG = LogFactory.getLog(HostServiceImpl.class);

    @Autowired
    private HostRepository hostRepository;


    @Override
    @Transactional(value="adapter_transactionManager")
    public final LoadBalancerHost createLoadBalancerHost(LoadBalancerHost lbHost) throws PersistenceServiceException {

        try {
            LoadBalancerHost dbLoadBalancerHost = hostRepository.createLoadBalancerHost(lbHost);
            return dbLoadBalancerHost;
        } catch (Exception e) {
            throw new PersistenceServiceException(e);
        }

    }

    @Override
    @Transactional(value="adapter_transactionManager")
    public final void removeLoadBalancerHost(LoadBalancerHost lbHost) throws PersistenceServiceException {

        try {
            hostRepository.removeLoadBalancerHost(lbHost);
        } catch (Exception e) {
            throw new PersistenceServiceException(e);
        }

    }

    @Override
    @Transactional(value="adapter_transactionManager")
    public final LoadBalancerHost getLoadBalancerHost(Integer loadBalancerId) {
        return hostRepository.getLBHost(loadBalancerId);
    }

    @Override
    public Host getDefaultActiveHost() {

        List<Host> hosts = hostRepository.getHosts();



        if (hosts == null || hosts.size() <= 0) {
            return null;
        }
        if (hosts.size() == 1) {
            Host host = hosts.get(0);
            return (host);
        } else {
            Host host =  hostRepository.getHostWithMinimumLoadBalancers(hosts);
            return (host);
        }
    }
}
