package org.openstack.atlas.adapter.common.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.entity.*;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;

import org.openstack.atlas.service.domain.exception.*;

import org.openstack.atlas.adapter.common.service.AdapterVirtualIpService;

import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.repository.ClusterRepository;
import org.openstack.atlas.adapter.common.repository.AdapterVirtualIpRepository;

import org.openstack.atlas.service.domain.repository.VirtualIpRepository;


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
    protected AdapterVirtualIpRepository adapterVirtualIpRepository;


    @Autowired
    protected HostRepository hostRepository;


    @Override
    @Transactional(value="adapter_transactionManager")
    public LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException, EntityNotFoundException {

        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {

            Set<LoadBalancerJoinVip> newVipConfig = new HashSet<LoadBalancerJoinVip>();

            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
                // Add a new vip to set

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();

                LoadBalancerHost lbHost = hostRepository.getLBHost(loadBalancer.getId());

                if (lbHost == null)
                    throw new PersistenceServiceException(new Exception(String.format("Cannot find host of loadbalancer %d", loadBalancer.getId())));

                Host host = lbHost.getHost();

                Cluster cluster = host.getCluster();

                String address;

                if (vip.getIpVersion() == IpVersion.IPV4) {
                    address = allocateIpv4VirtualIp(vip, loadBalancer.getAccountId(), cluster);
                } else {
                    address = allocateIpv6VirtualIp(vip, loadBalancer.getAccountId(), cluster);
                }

                vip.setAddress(address);

            }
        }

        return loadBalancer;
    }

    @Transactional(value="adapter_transactionManager")
    public String allocateIpv4VirtualIp(VirtualIp virtualIp, Integer accountId, Cluster cluster) throws OutOfVipsException, EntityNotFoundException {
        Calendar timeConstraintForVipReuse = Calendar.getInstance();
        timeConstraintForVipReuse.add(Calendar.DATE, -Constants.NUM_DAYS_BEFORE_VIP_REUSE);

        if (virtualIp.getVipType() == null) {
            virtualIp.setVipType(VirtualIpType.PUBLIC);
        }

        try {
            return adapterVirtualIpRepository.allocateIpv4VipBeforeDate(virtualIp, cluster, timeConstraintForVipReuse);
        } catch (OutOfVipsException e) {
            LOG.warn(String.format("Out of IPv4 virtual ips that were de-allocated before '%s'.", timeConstraintForVipReuse.getTime()));
            try {
                return adapterVirtualIpRepository.allocateIpv4VipAfterDate(virtualIp, cluster, timeConstraintForVipReuse);
            } catch (OutOfVipsException e2) {
                e2.printStackTrace();
                throw e2;
            }
        }
    }

    @Transactional(value="adapter_transactionManager")
    public String allocateIpv6VirtualIp(VirtualIp vip, Integer accountId, Cluster c) throws EntityNotFoundException {
         return adapterVirtualIpRepository.allocateIpv6Vip(vip, accountId, c);
    }

    @Transactional(value="adapter_transactionManager")
    public void removeAllVipsFromLoadBalancer(LoadBalancer loadBalancer) {

        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {

            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();
                deallocateVirtualIp(vip);
            }
        }
    }

    @Transactional(value="adapter_transactionManager")
    public void undoAllVipsFromLoadBalancer(LoadBalancer loadBalancer) {

        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {

            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();
                resetVirtualIp(vip);
            }
        }
    }

    private void deallocateVirtualIp(VirtualIp vip)
    {
        adapterVirtualIpRepository.deallocateVirtualIp(vip);
        vip.setAddress(null);
    }

    private void resetVirtualIp(VirtualIp vip)
    {
        adapterVirtualIpRepository.resetVirtualIp(vip);
        vip.setAddress(null);
    }


    @Override
    @Transactional(value="adapter_transactionManager")
    public final VirtualIpv4 createVirtualIpCluster(VirtualIpv4 vipCluster) throws PersistenceServiceException {

        VirtualIpv4 dbVipCluster = adapterVirtualIpRepository.createVirtualIpv4(vipCluster);

        return dbVipCluster;
    }

    @Override
    @Transactional(value="adapter_transactionManager")
    public final VirtualIpv4 getVirtualIpCluster(Integer vipId) {

        VirtualIpv4 dbVipCluster = adapterVirtualIpRepository.getVirtualIpv4(vipId);

        return dbVipCluster;
    }


}
