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
    @Transactional(value="transactionManager2")
    public final LoadBalancerHost createLoadBalancerHost(LoadBalancerHost lbHost) throws PersistenceServiceException {

        try {
            LoadBalancerHost dbLoadBalancerHost = hostRepository.createLoadBalancerHost(lbHost);
            return dbLoadBalancerHost;
        } catch (Exception e) {
            throw new PersistenceServiceException(e);
        }

    }

    @Override
    @Transactional(value="transactionManager2")
    public final void removeLoadBalancerHost(LoadBalancerHost lbHost) throws PersistenceServiceException {

        try {
            hostRepository.removeLoadBalancerHost(lbHost);
        } catch (Exception e) {
            throw new PersistenceServiceException(e);
        }

    }


    @Override
    public Host getDefaultActiveHost() {

        LOG.debug("Before calling hostRepository.getHosts()");
        List<Host> hosts = hostRepository.getHosts();

        LOG.debug(String.format("HostRepository.getHosts() retuned %d hosts", hosts.size()));

        if (hosts == null || hosts.size() <= 0) {
            LOG.error("ACTIVE_TARGET host not found");
            return null;
        }
        if (hosts.size() == 1) {
            Host host = hosts.get(0);
            LOG.debug("Host endpoint is " + host.getEndpoint());
            return (host);
        } else {
            Host host =  hostRepository.getHostWithMinimumLoadBalancers(hosts);
            LOG.debug("Host endpoint is " + host.getEndpoint());
            return (host);

        }
    }
}
