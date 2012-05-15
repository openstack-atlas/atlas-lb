package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;

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
public class NullAdapterImpl implements LoadBalancerAdapter {

    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());

    @Autowired
    protected HostService hostService;


    @Autowired
    protected HostRepository hostRepository;


    @Autowired
    protected AdapterVirtualIpService virtualIpService;


    protected String logFileLocation;

    protected String adapterConfigFileLocation;


    @Autowired
    public NullAdapterImpl(Configuration configuration) {

        logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        adapterConfigFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.adapter_config_file_location);

        //Read settings from our adapter config file.
    }


    private LoadBalancerEndpointConfiguration getConfig(Integer loadBalancerId)  throws AdapterException
    {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        if (config == null)
            throw new AdapterException("Adapter error: Cannot fetch information about LB devices");

        return config;
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) {

        if (hostService == null) {
            LOG.debug("hostService is null !");
        }

        LoadBalancerHost lbHost = hostService.getLoadBalancerHost(lbId);
        Host host = lbHost.getHost();
        return getConfigbyHost(host);
    }

    private LoadBalancerEndpointConfiguration getConfigbyHost(Host host) {
        try {
            Cluster cluster = host.getCluster();
            Host endpointHost = hostRepository.getEndPointHost(cluster.getId());
            List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
            return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
        } catch(DecryptException except)
        {
            LOG.error(String.format("Decryption exception: ", except.getMessage()));
            return null;
        }
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

        // Now we can create the load balancer on the remote device


        try {
            LOG.info("createLoadBalancer"); // NOP
        } catch (Exception e) {
            // Undo creation on adapter of this loadbalancer if there is an error.
            undoCreateLoadBalancer(lb, lbHost);
            throw new AdapterException("Error occurred while creating request or connecting to device : " + e.getMessage());
        }
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb) throws AdapterException {


        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());
		String serviceUrl = config.getHost().getEndpoint();

        LOG.info("updateLoadBalancer");// NOP

    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("deleteLoadBalancer");// NOP


        // Cleanup the state of the adapter for this load balancer
        removeLoadBalancerAdapterResources(lb);
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("createNodes");// NOP
    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("deleteNodes");// NOP
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("updateNodes");// NOP
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("updateConnectionThrottle");// NOP
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();


        LOG.info("deleteConnectionThrottle");// NOP
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("updateHealthMonitor");// NOP
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();


        LOG.info("deleteHealthMonitor");// NOP
    }

    @Override
    public void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();

        LOG.info("setSessionPersistence");// NOP
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        String serviceUrl = config.getHost().getEndpoint();


        LOG.info("deleteSessionPersistence");// NOP
    }
}
