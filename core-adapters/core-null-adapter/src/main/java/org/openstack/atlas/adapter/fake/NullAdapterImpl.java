package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapterBase;


import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.service.domain.entity.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NullAdapterImpl extends LoadBalancerAdapterBase {

    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());


    @Autowired
    public NullAdapterImpl(Configuration configuration) {

        super(configuration);
    }


    @Override
    public void doCreateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost) throws AdapterException {

        // Now we can create the load balancer on the remote device

       String serviceUrl = lbHost.getHost().getEndpoint();

       LOG.info("createLoadBalancer"); // NOP

    }

    @Override
    public void doUpdateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost) throws AdapterException {


        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("updateLoadBalancer");// NOP

    }

    @Override
    public void doDeleteLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("deleteLoadBalancer");// NOP

    }

    @Override
    public void doCreateNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("createNodes");// NOP
    }

    @Override
    public void doDeleteNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("deleteNodes");// NOP
    }

    @Override
    public void doUpdateNode(Integer accountId, Integer lbId, Node node, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("updateNodes");// NOP
    }

    @Override
    public void doUpdateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("updateConnectionThrottle");// NOP
    }

    @Override
    public void doDeleteConnectionThrottle(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("deleteConnectionThrottle");// NOP
    }

    @Override
    public void doUpdateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("updateHealthMonitor");// NOP
    }

    @Override
    public void doDeleteHealthMonitor(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("deleteHealthMonitor");// NOP
    }

    @Override
    public void doSetSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence, LoadBalancerHost lbHost) throws AdapterException {

        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("setSessionPersistence");// NOP
    }

    @Override
    public void doDeleteSessionPersistence(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException {
        String serviceUrl = lbHost.getHost().getEndpoint();

        LOG.info("deleteSessionPersistence");// NOP
    }
}
