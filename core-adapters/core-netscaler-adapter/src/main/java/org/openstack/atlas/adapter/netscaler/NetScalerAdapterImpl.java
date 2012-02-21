package org.openstack.atlas.adapter.netscaler;

import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.repository.NodeRepository;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.*;
import java.io.*;
import java.net.*;

@Service
public class NetScalerAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());
    private static String SOURCE_IP = "SOURCE_IP";
    private static String HTTP_COOKIE = "HTTP_COOKIE";
	protected NodeRepository nodeRepository;

    @Autowired
    protected NSAdapterUtils nsAdapterUtils;

    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void createLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";

        Integer accountId = lb.getAccountId(); 

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();

        nsAdapterUtils.populateNSLoadBalancer(lb, nsLB);

        String requestBody = nsAdapterUtils.getRequestBody(nsLB); 
        String serviceUrl = lb.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType);

        nsAdapterUtils.performRequest("POST", resourceUrl, requestBody);
    }

    @Override
    public void updateLoadBalancer(LoadBalancerEndpointConfiguration config, LoadBalancer lb) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lb.getId();

        Integer accountId = lb.getAccountId(); 
        
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();
        nsAdapterUtils.populateNSLoadBalancer(lb, nsLB);
        
		String requestBody = nsAdapterUtils.getRequestBody(nsLB);
		String serviceUrl = config.getHost().getEndpoint();
		String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId);
		nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteLoadBalancer(LoadBalancerEndpointConfiguration config,  LoadBalancer lb)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        
        Integer accountId = lb.getAccountId(); 
        Integer lbId = lb.getId();   
 
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, lbId);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void createNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) 
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
            String serviceUrl = config.getHost().getEndpoint();
            String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);
			
            nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
		}
    }
    

    @Override
    public void deleteNodes(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Set<Node> nodes) 
        throws AdapterException
    {
    	for(Node node: nodes)
    	{
			this.removeNode(config, lbId, accountId, node.getId());
    	}    
    }
    

    @Override
    public void updateNode(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, Node node) 
        throws AdapterException 
    {
        LOG.info("updateNodes");// NOP
    }

    
    @Override
    public void updateConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, ConnectionThrottle conThrottle) 
        throws AdapterException 
    {
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
    public void deleteConnectionThrottle(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void updateHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, HealthMonitor monitor) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon;

        nsMon  = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor();  

        nsAdapterUtils.populateNSHealthMonitor(monitor, nsMon);

        String requestBody = nsAdapterUtils.getRequestBody(nsMon);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteHealthMonitor(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void setSessionPersistence(LoadBalancerEndpointConfiguration config, Integer accountId, Integer lbId, SessionPersistence sessionPersistence) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";
		
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsPersistence = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence();
		nsAdapterUtils.populateSessionPersistence(sessionPersistence, nsPersistence);
        String requestBody = nsAdapterUtils.getRequestBody(nsPersistence);
        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void deleteSessionPersistence(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId) 
        throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";

        String serviceUrl = config.getHost().getEndpoint();
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl, "");
    }


    private void removeNode(LoadBalancerEndpointConfiguration config, Integer lbId, Integer accountId, Integer nodeId)
            throws AdapterException 
    {
        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";
		String serviceUrl = config.getHost().getEndpoint();
		String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId,childResourceType) + "/" + nodeId;
		
		nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }
}

