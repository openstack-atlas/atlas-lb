package org.openstack.atlas.adapter.netscaler;


import org.openstack.atlas.common.config.Configuration;

import org.openstack.atlas.service.domain.entity.*;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;

import org.openstack.atlas.adapter.LoadBalancerAdapterBase;
import org.openstack.atlas.adapter.exception.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetScalerAdapterImpl extends LoadBalancerAdapterBase {
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());



    @Autowired
    protected NSAdapterUtils nsAdapterUtils;



    @Autowired
    public NetScalerAdapterImpl(Configuration configuration) {

        super(configuration);

        //Read settings from our adapter config file.
    }


    @Override
    public void doCreateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)
            throws AdapterException {


        String resourceType = "loadbalancers";

        Integer accountId = lb.getAccountId();

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();

        nsAdapterUtils.populateNSLoadBalancerForCreate(lb, nsLB);

        String requestBody = nsAdapterUtils.getRequestBody(nsLB);

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType);

        nsAdapterUtils.performRequest("POST", resourceUrl, requestBody);


    }


    @Override
    public void doUpdateLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lb.getId();

        Integer accountId = lb.getAccountId();

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer nsLB = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.LoadBalancer();
        nsAdapterUtils.populateNSLoadBalancerForUpdate(lb, nsLB);

        String requestBody = nsAdapterUtils.getRequestBody(nsLB);

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }


    @Override
    public void doDeleteLoadBalancer(LoadBalancer lb, LoadBalancerHost lbHost)
            throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        
        Integer accountId = lb.getAccountId(); 
        Integer lbId = lb.getId();   

        LOG.debug("NetScaler adapter preparing to delete loadbalancer with id " + lbId);

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, lbId);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);

    }

    @Override
    public void doCreateNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost)
        throws AdapterException
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";

        if(nodes.size() > 0)
        {
            com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes nsNodes = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.Nodes();
            nsAdapterUtils.populateNSNodes(nodes, nsNodes.getNodes());
            String requestBody = nsAdapterUtils.getRequestBody(nsNodes);

            String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

            nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
        }
    }
    

    @Override
    public void doDeleteNodes(Integer accountId, Integer lbId, Set<Node> nodes, LoadBalancerHost lbHost)
        throws AdapterException
    {

    	for(Node node: nodes)
    	{
			this.doRemoveNode(lbId, accountId, node.getId(), lbHost);
    	}    
    }
    

    @Override
    public void doUpdateNode(Integer accountId, Integer lbId, Node node, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        LOG.info("updateNodes");// NOP
    }

    
    @Override
    public void doUpdateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle conThrottle, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";
		
        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle nsThrottle = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
		nsAdapterUtils.populateConnectionThrottle(conThrottle, nsThrottle);
        String requestBody = nsAdapterUtils.getRequestBody(nsThrottle);
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void doDeleteConnectionThrottle(Integer accountId, Integer lbId, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "connectionthrottle";

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void doUpdateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor nsMon;

        nsMon  = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.HealthMonitor();  

        nsAdapterUtils.populateNSHealthMonitor(monitor, nsMon);

        String requestBody = nsAdapterUtils.getRequestBody(nsMon);
        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void doDeleteHealthMonitor(Integer accountId, Integer lbId, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "healthmonitor";

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }

    @Override
    public void doSetSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence, LoadBalancerHost lbHost)
        throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";


        com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence nsPersistence = new com.citrix.cloud.netscaler.atlas.docs.loadbalancers.api.v1.SessionPersistence();
		nsAdapterUtils.populateSessionPersistence(sessionPersistence, nsPersistence);
        String requestBody = nsAdapterUtils.getRequestBody(nsPersistence);

        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("PUT", resourceUrl, requestBody);
    }

    @Override
    public void doDeleteSessionPersistence(Integer lbId, Integer accountId, LoadBalancerHost lbHost)
        throws AdapterException 
    {
        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "sessionpersistence";


        String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId, childResourceType);

        nsAdapterUtils.performRequest("DELETE", resourceUrl, "");
    }


    private void doRemoveNode(Integer lbId, Integer accountId, Integer nodeId, LoadBalancerHost lbHost)
            throws AdapterException 
    {

        String serviceUrl = lbHost.getHost().getEndpoint();

        String resourceType = "loadbalancers";
        Integer resourceId = lbId;
        String childResourceType = "nodes";

		String resourceUrl = nsAdapterUtils.getLBURLStr(serviceUrl, accountId, resourceType, resourceId,childResourceType) + "/" + nodeId;
		
		nsAdapterUtils.performRequest("DELETE", resourceUrl);
    }
}

