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

import org.openstack.atlas.adapter.common.repository.AdapterHostRepository;
import org.openstack.atlas.adapter.common.repository.AdapterClusterRepository;
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
    protected AdapterClusterRepository clusterRepository;

    @Autowired
    protected AdapterHostRepository hostRepository;


    @Override
    @Transactional
    public LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException {
        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {
            Set<LoadBalancerJoinVip> newVipConfig = new HashSet<LoadBalancerJoinVip>();
            List<VirtualIp> vipsOnAccount = virtualIpRepository.getVipsByAccountId(loadBalancer.getAccountId());
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
                if (loadBalancerJoinVip.getVirtualIp().getId() == null) {
                    // Add a new vip to set
                    LoadBalancerHost lbHost = hostRepository.getLBHost(loadBalancer.getId());
                    Host host = lbHost.getHost();
                    VirtualIp newVip = allocateIpv4VirtualIp(loadBalancerJoinVip.getVirtualIp(), host.getCluster());
                    LoadBalancerJoinVip newJoinRecord = new LoadBalancerJoinVip();
                    newJoinRecord.setVirtualIp(newVip);
                    newVipConfig.add(newJoinRecord);
                } else {
                    // Add shared vip to set
                    newVipConfig.addAll(getSharedIpv4Vips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, loadBalancer.getPort()));
                }
            }
            loadBalancer.setLoadBalancerJoinVipSet(newVipConfig);
        }

        if (!loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            List<VirtualIpv6> vips6OnAccount = virtualIpv6Repository.getVipsByAccountId(loadBalancer.getAccountId());
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
            loadBalancer.setLoadBalancerJoinVip6Set(null);
            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                if (loadBalancerJoinVip6.getVirtualIp().getId() == null) {
                    VirtualIpv6 ipv6 = allocateIpv6VirtualIp(loadBalancer);
                    LoadBalancerJoinVip6 jbjv6 = new LoadBalancerJoinVip6();
                    jbjv6.setVirtualIp(ipv6);
                    newVip6Config.add(jbjv6);
                } else {
                    //share ipv6 vip here..
                    newVip6Config.addAll(getSharedIpv6Vips(loadBalancerJoinVip6.getVirtualIp(), vips6OnAccount, loadBalancer.getPort()));
                }
                loadBalancer.setLoadBalancerJoinVip6Set(newVip6Config);
            }
        }
        return loadBalancer;
    }

    @Transactional
    public VirtualIp allocateIpv4VirtualIp(VirtualIp virtualIp, Cluster cluster) throws OutOfVipsException {
        Calendar timeConstraintForVipReuse = Calendar.getInstance();
        timeConstraintForVipReuse.add(Calendar.DATE, -Constants.NUM_DAYS_BEFORE_VIP_REUSE);

        if (virtualIp.getVipType() == null) {
            virtualIp.setVipType(VirtualIpType.PUBLIC);
        }

        try {
            return adapterVirtualIpRepository.allocateIpv4VipBeforeDate(cluster, timeConstraintForVipReuse, virtualIp.getVipType());
        } catch (OutOfVipsException e) {
            LOG.warn(String.format("Out of IPv4 virtual ips that were de-allocated before '%s'.", timeConstraintForVipReuse.getTime()));
            try {
                return adapterVirtualIpRepository.allocateIpv4VipAfterDate(cluster, timeConstraintForVipReuse, virtualIp.getVipType());
            } catch (OutOfVipsException e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
    }

    @Transactional
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
                
        VirtualIpCluster vipCluster = new VirtualIpCluster(ipv6.getId(), c);

        adapterVirtualIpRepository.createVirtualIpCluster(vipCluster);

        return ipv6;
    }

    private Set<LoadBalancerJoinVip> getSharedIpv4Vips(VirtualIp vipConfig, List<VirtualIp> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip> sharedVips = new HashSet<LoadBalancerJoinVip>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIp vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (this.isIpv4VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                loadBalancerJoinVip.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }

    private Set<LoadBalancerJoinVip6> getSharedIpv6Vips(VirtualIpv6 vipConfig, List<VirtualIpv6> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip6> sharedVips = new HashSet<LoadBalancerJoinVip6>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIpv6 vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (this.isIpv6VipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip6 loadBalancerJoinVip6 = new LoadBalancerJoinVip6();
                loadBalancerJoinVip6.setVirtualIp(vipOnAccount);
                sharedVips.add(loadBalancerJoinVip6);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }




    @Transactional
    public void removeAllVipsFromLoadBalancer(LoadBalancer lb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
            reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
        }

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            virtualIpv6Repository.removeJoinRecord(loadBalancerJoinVip6);
            reclaimIpv6VirtualIp(lb, loadBalancerJoinVip6.getVirtualIp());
        }
    }

    private void reclaimVirtualIp(LoadBalancer lb, VirtualIp virtualIp) {
        if (!isVipAllocatedToAnotherLoadBalancer(lb, virtualIp)) {
            adapterVirtualIpRepository.deallocateVirtualIp(virtualIp);
        }
    }


    @Transactional
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



    @Transactional
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


    private void reclaimIpv6VirtualIp(LoadBalancer lb, VirtualIpv6 virtualIpv6) {
        if (!isIpv6VipAllocatedToAnotherLoadBalancer(lb, virtualIpv6)) {
            virtualIpv6Repository.deleteVirtualIp(virtualIpv6);
        }
    }


    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort) {
        return virtualIpv6Repository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Override
    @Transactional
    public final VirtualIpCluster createVirtualIpCluster(VirtualIpCluster vipCluster) throws PersistenceServiceException {

        VirtualIpCluster dbVipCluster = adapterVirtualIpRepository.createVirtualIpCluster(vipCluster);

        return dbVipCluster;
    }

    @Override
    @Transactional
    public final VirtualIpCluster getVirtualIpCluster(Integer vipId) {

        VirtualIpCluster dbVipCluster = adapterVirtualIpRepository.getVirtualIpCluster(vipId);

        return dbVipCluster;
    }


}
