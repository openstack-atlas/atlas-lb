package org.openstack.atlas.adapter.fake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;

import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NullAdapterImpl implements LoadBalancerAdapter {
    public static Log LOG = LogFactory.getLog(NullAdapterImpl.class.getName());


    @Override
    public void createLoadBalancer(LoadBalancer lb) throws AdapterException {



        LOG.info("createLoadBalancer"); // NOP
    }

    @Override
    public void updateLoadBalancer(LoadBalancer lb) throws AdapterException {


        LOG.info("updateLoadBalancer");// NOP
    }

    @Override
    public void deleteLoadBalancer(LoadBalancer lb) throws AdapterException {



        LOG.info("deleteLoadBalancer");// NOP
    }

    @Override
    public void createNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {



        LOG.info("createNodes");// NOP
    }

    @Override
    public void deleteNodes(Integer accountId, Integer lbId, Set<Node> nodes) throws AdapterException {



        LOG.info("deleteNodes");// NOP
    }

    @Override
    public void updateNode(Integer accountId, Integer lbId, Node node) throws AdapterException {



        LOG.info("updateNodes");// NOP
    }

    @Override
    public void updateConnectionThrottle(Integer accountId, Integer lbId, ConnectionThrottle connectionThrottle) throws AdapterException {



        LOG.info("updateConnectionThrottle");// NOP
    }

    @Override
    public void deleteConnectionThrottle(Integer accountId, Integer lbId) throws AdapterException {




        LOG.info("deleteConnectionThrottle");// NOP
    }

    @Override
    public void updateHealthMonitor(Integer accountId, Integer lbId, HealthMonitor monitor) throws AdapterException {



        LOG.info("updateHealthMonitor");// NOP
    }

    @Override
    public void deleteHealthMonitor(Integer accountId, Integer lbId) throws AdapterException {




        LOG.info("deleteHealthMonitor");// NOP
    }

    @Override
    public void setSessionPersistence(Integer accountId, Integer lbId, SessionPersistence sessionPersistence) throws AdapterException {



        LOG.info("setSessionPersistence");// NOP
    }

    @Override
    public void deleteSessionPersistence(Integer accountId, Integer lbId) throws AdapterException {



        LOG.info("deleteSessionPersistence");// NOP
    }
}
