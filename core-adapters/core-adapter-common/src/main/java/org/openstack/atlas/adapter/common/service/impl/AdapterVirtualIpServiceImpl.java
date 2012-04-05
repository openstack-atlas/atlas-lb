package org.openstack.atlas.adapter.common.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.entity.*;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entity.LoadBalancerJoinVip6;
import org.openstack.atlas.service.domain.entity.VirtualIp;
import org.openstack.atlas.service.domain.entity.VirtualIpType;
import org.openstack.atlas.service.domain.entity.VirtualIpv6;
import org.openstack.atlas.service.domain.exception.*;

import org.openstack.atlas.adapter.common.service.AdapterVirtualIpService;

import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.repository.ClusterRepository;
import org.openstack.atlas.adapter.common.repository.AdapterVirtualIpRepository;

import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: youcef
 * Date: 3/1/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class AdapterVirtualIpServiceImpl implements AdapterVirtualIpService {

    private final Log LOG = LogFactory.getLog(AdapterVirtualIpServiceImpl.class);

    @Autowired
    protected VirtualIpRepository virtualIpRepository;


    @Autowired
    protected AdapterVirtualIpRepository adapterVirtualIpRepository;

    @Autowired
    protected VirtualIpv6Repository virtualIpv6Repository;

    @Autowired
    protected ClusterRepository clusterRepository;

    @Autowired
    protected HostRepository hostRepository;


    @Override
    @Transactional(value="transactionManager2")
    public LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException {

        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {
            Set<LoadBalancerJoinVip> newVipConfig = new HashSet<LoadBalancerJoinVip>();
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
                // Add a new vip to set
                LoadBalancerHost lbHost = hostRepository.getLBHost(loadBalancer.getId());
                if (lbHost == null)
                    throw new PersistenceServiceException(new Exception(String.format("Cannot find host of loadbalancer %d", loadBalancer.getId())));

                Host host = lbHost.getHost();
                LOG.debug(String.format("The loadBalancer %d has the Host endpoint %s", lbHost.getLoadBalancerId(), lbHost.getHost().getEndpoint()));
                allocateIpv4VirtualIp(loadBalancerJoinVip.getVirtualIp(), host.getCluster());
            }
        }

        if (!loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
            loadBalancer.setLoadBalancerJoinVip6Set(null);
            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                VirtualIpv6 ipv6 = allocateIpv6VirtualIp(loadBalancer);
                loadBalancerJoinVip6.setVirtualIp(ipv6);
            }

        }

        return loadBalancer;
    }

    @Transactional(value="transactionManager2")
    public void allocateIpv4VirtualIp(VirtualIp virtualIp, Cluster cluster) throws OutOfVipsException {
        Calendar timeConstraintForVipReuse = Calendar.getInstance();
        timeConstraintForVipReuse.add(Calendar.DATE, -Constants.NUM_DAYS_BEFORE_VIP_REUSE);

        if (virtualIp.getVipType() == null) {
            virtualIp.setVipType(VirtualIpType.PUBLIC);
        }

        try {
            adapterVirtualIpRepository.allocateIpv4VipBeforeDate(virtualIp, cluster, timeConstraintForVipReuse);
        } catch (OutOfVipsException e) {
            LOG.warn(String.format("Out of IPv4 virtual ips that were de-allocated before '%s'.", timeConstraintForVipReuse.getTime()));
            try {
                adapterVirtualIpRepository.allocateIpv4VipAfterDate(virtualIp, cluster, timeConstraintForVipReuse);
            } catch (OutOfVipsException e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
    }

    @Transactional(value="transactionManager2")
    public VirtualIpv6 allocateIpv6VirtualIp(LoadBalancer loadBalancer) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpv6Repository.getLockedAccountRecord(loadBalancer.getAccountId());

        Integer vipOctets = adapterVirtualIpRepository.getNextVipOctet(loadBalancer.getAccountId());

        LoadBalancerHost lbHost = hostRepository.getLBHost(loadBalancer.getId());
        Host host = lbHost.getHost();
        Cluster c = clusterRepository.getById(host.getCluster().getId());

        VirtualIpv6 ipv6 = new VirtualIpv6();
        ipv6.setAccountId(loadBalancer.getAccountId());  
        virtualIpRepository.persist(ipv6);

        VirtualIpv6Octets ipv6octets = new VirtualIpv6Octets();

        ipv6octets.setAccountId(loadBalancer.getAccountId());
        ipv6octets.setVipOctets(vipOctets);
        ipv6octets.setVirtualIpv6Id(ipv6.getId());
        virtualIpRepository.persist(ipv6octets);
                
        VirtualIpCluster vipCluster = new VirtualIpCluster(ipv6.getAddress(), VirtualIpType.PUBLIC, c);


        adapterVirtualIpRepository.createVirtualIpCluster(vipCluster);

        return ipv6;
    }





    @Transactional(value="transactionManager2")
    public void removeAllVipsFromLoadBalancer(LoadBalancer lb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            LOG.debug("Removing loadBalancerJoinVip for vip id " + loadBalancerJoinVip.getVirtualIp().getId());
            virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
            adapterVirtualIpRepository.deallocateVirtualIp(loadBalancerJoinVip.getVirtualIp());
        }
        LOG.debug("Reclaimed all IPv4 VIPs");

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            LOG.debug("Removing loadBalancerJoinVip for vip id " + loadBalancerJoinVip6.getVirtualIp().getId());
            virtualIpv6Repository.removeJoinRecord(loadBalancerJoinVip6);
            virtualIpv6Repository.deleteVirtualIp(loadBalancerJoinVip6.getVirtualIp());
        }

        LOG.debug("Reclaimed all IPv6 VIPs");
    }




    @Transactional(value="transactionManager2")
    public boolean isVipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIp virtualIp) {
        List<LoadBalancerJoinVip> joinRecords = virtualIpRepository.getJoinRecordsForVip(virtualIp);

        for (LoadBalancerJoinVip joinRecord : joinRecords) {
            if (!joinRecord.getLoadBalancer().getId().equals(lb.getId())) {
                LOG.debug(String.format("Virtual ip '%d' is used by a load balancer other than load balancer '%d'.", virtualIp.getId(), lb.getId()));
                return true;
            }
        }

        return false;
    }



    @Transactional(value="transactionManager2")
    public boolean isIpv6VipAllocatedToAnotherLoadBalancer(LoadBalancer lb, VirtualIpv6 virtualIp) {
        List<LoadBalancerJoinVip6> joinRecords = virtualIpv6Repository.getJoinRecordsForVip(virtualIp);

        for (LoadBalancerJoinVip6 joinRecord : joinRecords) {
            if (!joinRecord.getLoadBalancer().getId().equals(lb.getId())) {
                LOG.debug(String.format("IPv6 virtual ip '%d' is used by a load balancer other than load balancer '%d'.", virtualIp.getId(), lb.getId()));
                return true;
            }
        }

        return false;
    }


    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort) {
        return virtualIpv6Repository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Override
    @Transactional(value="transactionManager2")
    public final VirtualIpCluster createVirtualIpCluster(VirtualIpCluster vipCluster) throws PersistenceServiceException {

        VirtualIpCluster dbVipCluster = adapterVirtualIpRepository.createVirtualIpCluster(vipCluster);

        return dbVipCluster;
    }

    @Override
    @Transactional(value="transactionManager2")
    public final VirtualIpCluster getVirtualIpCluster(Integer vipId) {

        VirtualIpCluster dbVipCluster = adapterVirtualIpRepository.getVirtualIpCluster(vipId);

        return dbVipCluster;
    }


}
