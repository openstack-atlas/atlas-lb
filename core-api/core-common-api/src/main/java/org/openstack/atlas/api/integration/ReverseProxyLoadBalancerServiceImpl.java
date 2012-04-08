package org.openstack.atlas.api.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReverseProxyLoadBalancerServiceImpl implements ReverseProxyLoadBalancerService {
    private final Log LOG = LogFactory.getLog(ReverseProxyLoadBalancerServiceImpl.class);

    @Autowired
    protected Configuration configuration;

    @Autowired
    protected LoadBalancerAdapter loadBalancerAdapter;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Override
    public void createLoadBalancer(Integer accountId, LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {

        if (configuration != null) {
            LOG.debug("Configuration is not null");
        } else {
            LOG.debug("Configuration is null");
        }

        try {
            loadBalancerAdapter.createLoadBalancer(lb);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void updateLoadBalancer(Integer accountId, LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.updateLoadBalancer(lb);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.deleteLoadBalancer(lb);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.createNodes(accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.deleteNodes(accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.updateNode(accountId, lbId, node);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteNode(Integer accountId, Integer lbId, Node node) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            Set<Node> nodes = new HashSet<Node>();
            nodes.add(node);
            loadBalancerAdapter.deleteNodes(accountId, lbId, nodes);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException, DecryptException, MalformedURLException, Exception {
 
        try {
            loadBalancerAdapter.updateConnectionThrottle(accountId, lbId, connectionThrottle);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.deleteConnectionThrottle(accountId, lbId);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.updateHealthMonitor(accountId, lbId, monitor);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.deleteHealthMonitor(accountId, lbId);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void setSessionPersistence(Integer lbId, Integer accountId, SessionPersistence sessionPersistence) throws Exception {

        try {
            loadBalancerAdapter.setSessionPersistence(accountId, lbId, sessionPersistence);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException, DecryptException, MalformedURLException, Exception {

        try {
            loadBalancerAdapter.deleteSessionPersistence(accountId, lbId);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    private boolean isConnectionExcept(Exception exc) {
        String faultString = exc.getMessage();
        if (faultString == null) {
            return false;
        }
        if (faultString.split(":")[0].equals("java.net.ConnectException")) {
            return true;
        }
        return false;
    }
}
