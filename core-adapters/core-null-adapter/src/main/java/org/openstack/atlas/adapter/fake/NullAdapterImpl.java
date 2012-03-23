package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.adapter.common.service.AdapterVirtualIpService;
import org.openstack.atlas.adapter.common.service.HostService;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.common.config.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.common.EndpointUtils;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NullAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());


    @Autowired
    protected HostService hostService;

    @Autowired
    protected AdapterVirtualIpService virtualIpService;

    private LoadBalancerEndpointConfiguration getConfig(Integer loadBalancerId)  throws AdapterException
    {
        LoadBalancerEndpointConfiguration config = EndpointUtils.getConfigbyLoadBalancerId(loadBalancerId);

        if (config == null)
            throw new AdapterException("Adapter error: Cannot fetch information about LB devices");

        return config;
    }

    @Override
    public void createLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());

        // Choose a host and place the loadbalancer on it
        Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");

        LoadBalancerHost lbHost = new LoadBalancerHost(lb.getId(), host);

        try {
            hostService.createLoadBalancerHost(lbHost);
            virtualIpService.assignVipsToLoadBalancer(lb);
        } catch (PersistenceServiceException e) {
            throw new AdapterException("Cannot assign Vips to the loadBalancer");
        }

        LOG.info("createLoadBalancer"); // NOP
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());

        LOG.info("updateLoadBalancer");// NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());

        LOG.info("deleteLoadBalancer");// NOP
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("createNodes");// NOP
    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("deleteNodes");// NOP
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("updateNodes");// NOP
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("updateConnectionThrottle");// NOP
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);


        LOG.info("deleteConnectionThrottle");// NOP
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("updateHealthMonitor");// NOP
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);


        LOG.info("deleteHealthMonitor");// NOP
    }

    @Override
    public void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("setSessionPersistence");// NOP
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        LOG.info("deleteSessionPersistence");// NOP
    }
}
