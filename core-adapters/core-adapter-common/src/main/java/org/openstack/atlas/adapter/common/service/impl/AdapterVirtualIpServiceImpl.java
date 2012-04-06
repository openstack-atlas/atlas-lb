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
    protected VirtualIpRepository virtualIpRepository;


    @Autowired
    protected AdapterVirtualIpRepository adapterVirtualIpRepository;

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

    @Transactional(value="transactionManager2")
    public String allocateIpv4VirtualIp(VirtualIp virtualIp, Integer accountId, Cluster cluster) throws OutOfVipsException {
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

    @Transactional(value="transactionManager2")
    public String allocateIpv6VirtualIp(VirtualIp vip, Integer accountId, Cluster c) throws EntityNotFoundException {

        Integer vipOctets = adapterVirtualIpRepository.getNextVipOctet(accountId);


        VirtualIpv6 ipv6octets = new VirtualIpv6();

        ipv6octets.setAccountId(accountId);
        ipv6octets.setVipOctets(vipOctets);
        ipv6octets.setVirtualIpId(vip.getId());
        ipv6octets = adapterVirtualIpRepository.create(ipv6octets);

        try {
            return ipv6octets.getDerivedIpString(c);
        } catch (IPStringConversionException1 e) {
            LOG.error("Caught an exception while trying to convert IPv6 octets into a string");
            return null;
        }
    }

    @Transactional(value="transactionManager2")
    public void removeAllVipsFromLoadBalancer(LoadBalancer loadBalancer) {

        if (!loadBalancer.getLoadBalancerJoinVipSet().isEmpty()) {

            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();
                deallocateVirtualIp(vip);
            }
        }
    }

    @Transactional(value="transactionManager2")
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


    @Override
    @Transactional(value="transactionManager2")
    public final VirtualIpv4 createVirtualIpCluster(VirtualIpv4 vipCluster) throws PersistenceServiceException {

        VirtualIpv4 dbVipCluster = adapterVirtualIpRepository.createVirtualIpCluster(vipCluster);

        return dbVipCluster;
    }

    @Override
    @Transactional(value="transactionManager2")
    public final VirtualIpv4 getVirtualIpCluster(Integer vipId) {

        VirtualIpv4 dbVipCluster = adapterVirtualIpRepository.getVirtualIpCluster(vipId);

        return dbVipCluster;
    }


}
