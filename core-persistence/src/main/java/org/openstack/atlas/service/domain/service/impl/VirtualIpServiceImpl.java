package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.crypto.HashUtil;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.VirtualIpRepository;

import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VirtualIpServiceImpl implements VirtualIpService {
    private final Log LOG = LogFactory.getLog(VirtualIpServiceImpl.class);

    @Autowired
    protected VirtualIpRepository virtualIpRepository;


    @Override
    @Transactional(value="core_transactionManager")
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
                    newJoinVipSetConfig.addAll(getSharedVips(loadBalancerJoinVip.getVirtualIp(), vipsOnAccount, loadBalancer.getPort()));
                }
            }
            loadBalancer.setLoadBalancerJoinVipSet(newJoinVipSetConfig);
        }



        assignExtraVipsToLoadBalancer(loadBalancer);

        // By default, we always allocate at least an IPv6 address if none is specified by the user or added by extensions
        if (loadBalancer.getLoadBalancerJoinVipSet().isEmpty())
        {
            LOG.debug("Assigning the default IPV6 VIP to the loadbalancer");
            assignDefaultIPv6ToLoadBalancer(loadBalancer);
        }


        return loadBalancer;
    }

    @Override
    @Transactional(value="core_transactionManager")
    public void updateLoadBalancerVips(LoadBalancer loadBalancer) throws PersistenceServiceException {

        Set<LoadBalancerJoinVip> loadBalancerJoinVipSetConfig = loadBalancer.getLoadBalancerJoinVipSet();

        if (!loadBalancerJoinVipSetConfig.isEmpty()) {
            for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancerJoinVipSetConfig) {

                VirtualIp vip = loadBalancerJoinVip.getVirtualIp();

                virtualIpRepository.update(vip);

            }

        }
    }


    protected LoadBalancer assignDefaultIPv6ToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException
    {

        Set<LoadBalancerJoinVip> lbJoinVipConfig = loadBalancer.getLoadBalancerJoinVipSet();

        if (lbJoinVipConfig == null) {
            lbJoinVipConfig = new HashSet<LoadBalancerJoinVip>();
        }

        VirtualIp vip = allocateIpv6VirtualIp(loadBalancer);

        LoadBalancerJoinVip joinVip = new LoadBalancerJoinVip(loadBalancer.getPort(), loadBalancer, vip);

        lbJoinVipConfig.add(joinVip);

        loadBalancer.setLoadBalancerJoinVipSet(lbJoinVipConfig);

        return loadBalancer;
    }




    protected LoadBalancer assignExtraVipsToLoadBalancer(LoadBalancer loadBalancer) throws PersistenceServiceException
    {
        // Extensions can override this method and add extra VIPs to loadBalancer.
        return loadBalancer;
    }


    @Transactional(value="core_transactionManager")
    public void addAccountRecord(Integer accountId) throws NoSuchAlgorithmException {

        Set<Integer> accountsInAccount = new HashSet<Integer>(virtualIpRepository.getAccountIdsAlreadyShaHashed());

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

    private Set<LoadBalancerJoinVip> getSharedVips(VirtualIp vipConfig, List<VirtualIp> vipsOnAccount, Integer lbPort) throws AccountMismatchException, UniqueLbPortViolationException {
        Set<LoadBalancerJoinVip> sharedVips = new HashSet<LoadBalancerJoinVip>();
        boolean belongsToProperAccount = false;

        // Verify this is a valid virtual ip to share
        for (VirtualIp vipOnAccount : vipsOnAccount) {
            if (vipOnAccount.getId().equals(vipConfig.getId())) {
                if (this.isVipPortCombinationInUse(vipOnAccount, lbPort)) {
                    throw new UniqueLbPortViolationException("Another load balancer is currently using the requested port with the shared virtual ip.");
                }
                belongsToProperAccount = true;
                LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
                loadBalancerJoinVip.setVirtualIp(vipOnAccount);
                loadBalancerJoinVip.setPort(lbPort);
                sharedVips.add(loadBalancerJoinVip);
            }
        }

        if (!belongsToProperAccount) {
            throw new AccountMismatchException("Invalid requesting account for the shared virtual ip.");
        }
        return sharedVips;
    }
   
    @Transactional(value="core_transactionManager")
    public void updateIpv4VirtualIp(LoadBalancer loadBalancer, VirtualIp vip) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpRepository.getLockedAccountRecord(loadBalancer.getAccountId());
        vip.setAccountId(loadBalancer.getAccountId());

    }

    @Transactional(value="core_transactionManager")
    public void setVirtualIpAccount(LoadBalancer loadBalancer, VirtualIp vip) throws EntityNotFoundException {
        // Acquire lock on account row due to concurrency issue
        virtualIpRepository.getLockedAccountRecord(loadBalancer.getAccountId());
        vip.setAccountId(loadBalancer.getAccountId());
    }
        
    @Transactional(value="core_transactionManager")
    public VirtualIp allocateIpv6VirtualIp(LoadBalancer loadBalancer) throws EntityNotFoundException {


        VirtualIp vip = new VirtualIp();
        vip.setIpVersion(IpVersion.IPV6);
        vip.setVipType(VirtualIpType.PUBLIC);

        setVirtualIpAccount(loadBalancer, vip);
        virtualIpRepository.create(vip);
        return vip;
    }


    public boolean isVipPortCombinationInUse(VirtualIp virtualIp, Integer loadBalancerPort) {
        return virtualIpRepository.getPorts(virtualIp.getId()).containsKey(loadBalancerPort);
    }


    @Transactional(value="core_transactionManager")
    public void removeAllVipsFromLoadBalancer(LoadBalancer lb) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
            reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
        }

        lb.setLoadBalancerJoinVipSet(null);
    }


    private void reclaimVirtualIp(LoadBalancer lb, VirtualIp virtualIp) {
        if (!isVipAllocatedToAnotherLoadBalancer(lb, virtualIp)) {
            LOG.debug("Deallocating an address");
            virtualIpRepository.removeVirtualIp(virtualIp);
        }
    }



    @Transactional(value="core_transactionManager")
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


}
