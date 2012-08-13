package org.openstack.atlas.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.config.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.common.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.service.AdapterVirtualIpService;
import org.openstack.atlas.adapter.common.service.HostService;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;




@Service
public abstract class LoadBalancerAdapterBase implements LoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(LoadBalancerAdapterBase.class.getName());

    @Autowired
    protected HostService hostService;


    @Autowired
    protected HostRepository hostRepository;


    @Autowired
    protected AdapterVirtualIpService virtualIpService;


    protected String logFileLocation;

    protected String adapterConfigFileLocation;

    // Derived classes must implement the following methods
    protected abstract void doCreateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)  throws AdapterException;
    protected abstract void doUpdateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)  throws AdapterException;
    protected abstract void doDeleteLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)  throws AdapterException;
    protected abstract void doCreateNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doDeleteNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doUpdateNode(Integer accountId, Integer lbId, Node node, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doUpdateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle, LoadBalancerHost lbHost)  throws AdapterException;;
    protected abstract void doDeleteConnectionThrottle(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doUpdateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doDeleteHealthMonitor(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doSetSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence, LoadBalancerHost lbHost) throws AdapterException;
    protected abstract void doDeleteSessionPersistence(Integer accountId, Integer lbId, LoadBalancerHost lbHost) throws AdapterException;

    public LoadBalancerAdapterBase(Configuration configuration) {

        logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        adapterConfigFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.adapter_config_file_location);

        //Read settings from our adapter config file.
    }



    private LoadBalancerHost  getLoadBalancerHost(Integer  lbId)
    {
        if (hostService == null) {
            LOG.debug("hostService is null !");
        }

        LoadBalancerHost lbHost = hostService.getLoadBalancerHost(lbId);

        return lbHost;
    }



    private void undoCreateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost) {

        try {
            hostService.removeLoadBalancerHost(lbHost);
            virtualIpService.undoAllVipsFromLoadBalancer(lb);
        } catch (PersistenceServiceException e) {
            LOG.error(String.format("Failed to remove LoadBalancerHost for lbId %d: %s", lb.getId(), e.getMessage()));
        }
    }

    private void removeLoadBalancerAdapterResources(LoadBalancer lb) {

        try {
            LoadBalancerHost lbHost = hostService.getLoadBalancerHost(lb.getId());
            hostService.removeLoadBalancerHost(lbHost);
            virtualIpService.removeAllVipsFromLoadBalancer(lb);
        } catch (PersistenceServiceException e) {
            LOG.error(String.format("Failed to remove LoadBalancerHost for lbId %d: %s", lb.getId(), e.getMessage()));
        }
    }



    @Override
    public void createLoadBalancer(LoadBalancer lb) throws AdapterException {

        // Choose a host for this new load Balancer
        Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");

        String serviceUrl = host.getEndpoint();

        LoadBalancerHost lbHost = new LoadBalancerHost(lb.getId(), host);


        try {
            LOG.debug("Before calling hostService.createLoadBalancerHost()");
            hostService.createLoadBalancerHost(lbHost);
            // Also assign the Virtual IP for this load balancer
            virtualIpService.assignVipsToLoadBalancer(lb);
        } catch (PersistenceServiceException e) {
            throw new AdapterException("Cannot assign Vips to the loadBalancer : " + e.getMessage());
        }


         try {
             // call derived class to do the real job
            doCreateLoadBalancer(lb, lbHost);
        } catch (Exception e) {
            // Undo creation on adapter of this loadbalancer if there is an error.
            undoCreateLoadBalancer(lb, lbHost);
            throw new AdapterException("Error occurred while creating request or connecting to device : " + e.getMessage());
        }
    }




    @Override
    public void updateLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lb.getId());

        // call derived class to do the real job
        doUpdateLoadBalancer(lb, lbHost);
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lb.getId());

        // call derived class to do the real job
        doDeleteLoadBalancer(lb, lbHost);

        // Cleanup the state of the adapter for this load balancer
        removeLoadBalancerAdapterResources(lb);
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doCreateNodes(accountId, lbId, nodes, lbHost);

    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doDeleteNodes(accountId, lbId, nodes, lbHost);
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doUpdateNode(accountId, lbId, node, lbHost);
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doUpdateConnectionThrottle(accountId, lbId, connectionThrottle, lbHost);
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doDeleteConnectionThrottle(accountId, lbId, lbHost);
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doUpdateHealthMonitor(accountId, lbId, monitor, lbHost);
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doDeleteHealthMonitor(accountId, lbId, lbHost);
    }

    @Override
    public void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {

        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doSetSessionPersistence(accountId, lbId, sessionPersistence, lbHost);
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException {


        LoadBalancerHost lbHost = getLoadBalancerHost(lbId);

        // call derived class to do the real job
        doDeleteSessionPersistence(accountId, lbId, lbHost);
    }
}
