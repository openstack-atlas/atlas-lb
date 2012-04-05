package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.crypto.HashUtil;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;
import org.openstack.atlas.service.domain.repository.VirtualIpv6Repository;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VirtualIpServiceImpl implements VirtualIpService {
    private final Log LOG = LogFactory.getLog(VirtualIpServiceImpl.class);

    @Autowired
    protected VirtualIpRepository virtualIpRepository;
    @Autowired
    protected VirtualIpv6Repository virtualIpv6Repository;


    @Override
    @Transactional
    public LoadBalancer assignVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException {

        Set<LoadBalancerJoinVip> loadBalancerJoinVipSetConfig = loadBalancer.getLoadBalancerJoinVipSet();

        if (!loadBalancerJoinVipSetConfig.isEmpty()) {

            loadBalancer.setLoadBalancerJoinVipSet(null);

            Set<LoadBalancerJoinVip> newJoinVipSetConfig = new HashSet<LoadBalancerJoinVip>();

            List<VirtualIp> vipsOnAccount = virtualIpRepository.getVipsByAccountId(loadBalancer.getAccountId());


            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancerJoinVipSetConfig) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();

                if (vip.getId() == null) {
                    // Update vip
                    updateIpv4VirtualIp(loadBalancer, vip);
                    vip = virtualIpRepository.create(vip);
                    LoadBalancerJoinVip newJoinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, vip);
                    newJoinVipSetConfig.add(newJoinVip);
                } else {
                    // Add shared vip to set
                    newJoinVipSetConfig.addAll(getSharedIpv4Vips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, loadBalancer.getPort()));
                }
            }
            loadBalancer.setLoadBalancerJoinVipSet(newJoinVipSetConfig);
        }

        if (!loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {
            Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();
            List<VirtualIpv6> vips6OnAccount = virtualIpv6Repository.getVipsByAccountId(loadBalancer.getAccountId());
            Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();
            loadBalancer.setLoadBalancerJoinVip6Set(null);

            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                VirtualIpv6 vip6 = loadBalancerJoinVip6.getVirtualIp();

                if (vip6.getId() == null) {
                    // Update vip
                    updateIpv6VirtualIp(loadBalancer,vip6);
                    vip6 = virtualIpv6Repository.create(vip6);
                    LoadBalancerJoinVip6 newJoinVip6 = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, vip6);
                    newVip6Config.add(newJoinVip6);
                } else {
                    //share ipv6 vip here..
                    newVip6Config.addAll(getSharedIpv6Vips(loadBalancerJoinVip6.getVirtualIp(), vips6OnAccount, loadBalancer.getPort()));
                }

                loadBalancer.setLoadBalancerJoinVip6Set(newVip6Config);
            }
        }

        assignExtraVipsToLoadBalancer(loadBalancer);

        // By default, we always allocate at least an IPv6 address if none is specified by the user or added by extensions
        if (loadBalancer.getLoadBalancerJoinVipSet().isEmpty() && loadBalancer.getLoadBalancerJoinVip6Set().isEmpty())
        {
            LOG.debug("Assigning the default IPV6 VIP to the loadbalancer");
            assignDefaultIPv6ToLoadBalancer(loadBalancer);
        }


        return loadBalancer;
    }

    @Override
    @Transactional
    public void updateLoadBalancerVips(LoadBalancer loadBalancer) throws PersistenceServiceException {

        Set<LoadBalancerJoinVip> loadBalancerJoinVipSetConfig = loadBalancer.getLoadBalancerJoinVipSet();

        if (!loadBalancerJoinVipSetConfig.isEmpty()) {
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancerJoinVipSetConfig) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();

                virtualIpRepository.update(vip);

            }

        }

        Set<LoadBalancerJoinVip6> loadBalancerJoinVip6SetConfig = loadBalancer.getLoadBalancerJoinVip6Set();

        if (!loadBalancer.getLoadBalancerJoinVip6Set().isEmpty()) {


            for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : loadBalancerJoinVip6SetConfig) {
                VirtualIpv6 vip6 = loadBalancerJoinVip6.getVirtualIp();

                virtualIpv6Repository.update(vip6);
            }
        }
    }


    protected LoadBalancer assignDefaultIPv6ToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException
    {

        Set<LoadBalancerJoinVip6> newVip6Config = new HashSet<LoadBalancerJoinVip6>();

        VirtualIpv6 ipv6 = allocateIpv6VirtualIp(loadBalancer);
        LoadBalancerJoinVip6 jbjv6 = new LoadBalancerJoinVip6(loadBalancer.getPort(), loadBalancer, ipv6);
        jbjv6.setVirtualIp(ipv6);
        newVip6Config.add(jbjv6);
        loadBalancer.setLoadBalancerJoinVip6Set(newVip6Config);

        return loadBalancer;
    }




    protected LoadBalancer assignExtraVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException
    {
        // Extensions can override this method and add extra VIPs to loadBalancer.
        return loadBalancer;
    }



    @Transactional
    public void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException {
        Set<Integer> accountsInAccount = new HashSet<Integer>(virtualIpv6Repository.getAccountIdsAlreadyShaHashed());

        if (accountsInAccount.contains(accountId)) return;

        Account account = new Account();
        String accountIdStr = String.format("%d", accountId);
        account.setId(accountId);
        account.setSha1SumForIpv6(HashUtil.sha1sumHex(accountIdStr.getBytes(), 0, 4));
        try {
            virtualIpRepository.persist(account);
        } catch (Exception e) {
            LOG.warn("High concurrency detected. Ignoring...");
        }
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
    public void updateIpv4VirtualIp(LoadBalancer loadBalancer, VirtualIp vip) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpRepository.getLockedAccountRecord(loadBalancer.getAccountId());
        vip.setAccountId(loadBalancer.getAccountId());

    }

    @Transactional
    public void updateIpv6VirtualIp(LoadBalancer loadBalancer, VirtualIpv6 vip) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpRepository.getLockedAccountRecord(loadBalancer.getAccountId());
        vip.setAccountId(loadBalancer.getAccountId());
    }
        
    @Transactional
    public VirtualIpv6 allocateIpv6VirtualIp(LoadBalancer loadBalancer) throws EntityNotFoundException {


        VirtualIpv6 ipv6 = new VirtualIpv6();
        updateIpv6VirtualIp(loadBalancer, ipv6);
        virtualIpv6Repository.create(ipv6);
        return ipv6;
    }


    public boolean isIpv4VipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    public boolean isIpv6VipPortCombinationInUse(VirtualIpv6 virtualIp, Integer loadBalancerPort) {
        return virtualIpv6Repository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }

    @Transactional
    public void removeAllVipsFromLoadBalancer(LoadBalancer lb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            LOG.debug("Removing loadBalancerJoinVip for vip id " + loadBalancerJoinVip.getVirtualIp().getId());
            virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
            reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
        }

        lb.setLoadBalancerJoinVipSet(null);
        LOG.debug("Reclaimed all IPv4 VIPs");

        for (LoadBalancerJoinVip6 loadBalancerJoinVip6 : lb.getLoadBalancerJoinVip6Set()) {
            LOG.debug("Removing loadBalancerJoinVip for vip id " + loadBalancerJoinVip6.getVirtualIp().getId());
            virtualIpv6Repository.removeJoinRecord(loadBalancerJoinVip6);
            reclaimIpv6VirtualIp(lb, loadBalancerJoinVip6.getVirtualIp());
        }

        lb.setLoadBalancerJoinVip6Set(null);
        LOG.debug("Reclaimed all IPv6 VIPs");
    }


    private void reclaimVirtualIp(LoadBalancer lb, VirtualIp virtualIp) {
        if (!isVipAllocatedToAnotherLoadBalancer(lb, virtualIp)) {
            LOG.debug("Deallocating an IPv4 address");
            virtualIpRepository.removeVirtualIp(virtualIp);
        }
    }

    private void reclaimIpv6VirtualIp(LoadBalancer lb, VirtualIpv6 ipv6) {
        if (!isIpv6VipAllocatedToAnotherLoadBalancer(lb, ipv6)) {
            LOG.debug("Deallocating an IPv6 address");
            virtualIpv6Repository.removeVirtualIpv6(ipv6);
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
}
