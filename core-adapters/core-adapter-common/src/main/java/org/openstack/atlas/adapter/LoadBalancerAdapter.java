package org.openstack.atlas.adapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

import java.util.Set;

public interface LoadBalancerAdapter {

    void createLoadBalancer(LoadBalancer lb) throws AdapterException;

    void updateLoadBalancer(LoadBalancer lb) throws AdapterException;

    void deleteLoadBalancer(LoadBalancer lb) throws AdapterException;

    void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException;

    void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException;

    void updateNode( Integer accountId, Integer lbId, Node node) throws AdapterException;

    void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException;

    void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException;

    void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor healthMonitor) throws AdapterException;

    void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException;

    void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException;

    void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException;
}
