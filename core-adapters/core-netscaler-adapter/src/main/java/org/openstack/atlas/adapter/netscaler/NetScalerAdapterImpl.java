package org.openstack.atlas.adapter.netscaler;

import org.openstack.atlas.adapter.common.config.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.common.EndpointUtils;

import org.openstack.atlas.adapter.common.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.service.AdapterVirtualIpService;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.common.crypto.exception.EncryptException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.repository.NodeRepository;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;

import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.exception.*;
import org.openstack.atlas.service.domain.exception.*;

import org.openstack.atlas.adapter.common.service.HostService;

import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetScalerAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());
    private static String SOURCE_IP = "SOURCE_IP";
    private static String HTTP_COOKIE = "HTTP_COOKIE";


    @Autowired
    protected Configuration configuration;

    @Autowired
    protected NSAdapterUtils nsAdapterUtils;


    @Autowired
    protected HostService hostService;


    @Autowired
    protected HostRepository hostRepository;

    @Autowired
    protected AdapterVirtualIpService virtualIpService;

    /*
    public NetScalerAdapterImpl() {

        String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.adapter_config_file_location);

        //Read adapter config file.
    }
    */

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
            String logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
            return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
        } catch(DecryptException except)
        {
            LOG.error(String.format("Decryption exception: ", except.getMessage()));
            return null;
        }
    }

    @Override
    public void createLoadBalancer(LoadBalancer lb)
            throws AdapterException {

        Host host = hostService.getDefaultActiveHost();

        if (host == null)
            throw new AdapterException("Cannot retrieve default active host from persistence layer");


        LoadBalancerHost lbHost = new LoadBalancerHost(lb.getId(), host);

        try {
            LOG.debug("Before calling hostService.createLoadBalancerHost()");
            hostService.createLoadBalancerHost(lbHost);
            virtualIpService.assignVipsToLoadBalancer(lb);
        } catch (PersistenceServiceException e) {
            throw new AdapterException("Cannot assign Vips to the loadBalancer : " + e.getMessage());
        }


        try {
            String resourceType = "loadbalancers";

            Integer accountId = lb.getAccountId();

            com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();

            nsAdapterUtils.populateNSLoadBalancerForCreate(lb, nsLB);

            String requestBody = nsAdapterUtils.getRequestBody(nsLB);

            String serviceUrl = host.getEndpoint();

            String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType);

            nsAdapterUtils.performRequest("POST", resourceUrl, requestBody);
        } catch(Exception e) {
            undoCreateLoadBalancer(lb, lbHost);
            throw new AdapterException("Error occurred while creating request or connecting to device : " + e.getMessage());
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

    @Override
    public void updateLoadBalancer(LoadBalancer lb)
        throws AdapterException 
    {

        LoadBalancerEndpointConfiguration config = getConfig(lb.getId());
		String serviceUrl = config.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lb.getId();

        Integer accountId = lb.getAccountId(); 
        
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();
        nsAdapterUtils.populateNSLoadBalancerForUpdate(lb, nsLB);
        
		String requestBody = nsAdapterUtils.getRequestBody(nsLB);

		String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId);
		nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
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
    public void deleteLoadBalancer(LoadBalancer lb)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        
        Integer accountId = lb.getAccountId(); 
        Integer lbId = lb.getId();   

        LOG.debug("NetScaler adapter preparing to delete loadbalancer with id " + lbId);

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, lbId);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);

        removeLoadBalancerAdapterResources(lb);
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes)
        throws AdapterException
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";

        if(nodes.size() > 0)
        {
            com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes nsNodes = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes();
            nsAdapterUtils.populateNSNodes(nodes, nsNodes.getNodes());
            String requestBody = nsAdapterUtils.getRequestBody(nsNodes);

            LoadBalancerEndpointConfiguration config = getConfig(lbId);
            String serviceUrl = config.getHost().getEndpoint();
            String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

            nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
        }
    }
    

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes)
        throws AdapterException
    {
        LoadBalancerEndpointConfiguration config = getConfig(lbId);

    	for(Node node: nodes)
    	{
			this.removeNode(lbId, accountId, node.getId());
    	}    
    }
    

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node)
        throws AdapterException 
    {
        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        LOG.info("updateNodes");// NOP
    }

    
    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle conThrottle)
        throws AdapterException 
    {
        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";
		
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle nsThrottle = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
		nsAdapterUtils.populateConnectionThrottle(conThrottle, nsThrottle);
        String requestBody = nsAdapterUtils.getRequestBody(nsThrottle);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId)
        throws AdapterException 
    {

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor)
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        LoadBalancerEndpointConfiguration config = getConfig(lbId);
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon;

        nsMon  = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor();  

        nsAdapterUtils.populateNSHealthMonitor(monitor, nsMon);

        String requestBody = nsAdapterUtils.getRequestBody(nsMon);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId)
        throws AdapterException 
    {

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence)
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsPersistence = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence();
		nsAdapterUtils.populateSessionPersistence(sessionPersistence, nsPersistence);
        String requestBody = nsAdapterUtils.getRequestBody(nsPersistence);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteSessionPersistence(Integer lbId, Integer accountId)
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl, "");
    }


    private void removeNode(Integer lbId, Integer accountId, Integer nodeId)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";

        LoadBalancerEndpointConfiguration config = getConfig(lbId);

		String serviceUrl = config.getHost().getEndpoint();
		String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId,childResourceType) + "/" + nodeId;
		
		nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }
}

