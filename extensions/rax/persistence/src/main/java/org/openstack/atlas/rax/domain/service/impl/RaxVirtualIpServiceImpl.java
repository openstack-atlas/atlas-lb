package org.openstack.atlas.rax.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.collections.ListUtil;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.rax.domain.repository.RaxVirtualIpRepository;
import org.openstack.atlas.rax.domain.service.RaxVirtualIpService;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.common.StringHelper;
import org.openstack.atlas.service.domain.common.StringUtilities;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.*;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.AccountLimitService;
import org.openstack.atlas.service.domain.service.impl.VirtualIpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Primary
public class RaxVirtualIpServiceImpl extends VirtualIpServiceImpl implements RaxVirtualIpService {
    private final Log LOG = LogFactory.getLog(RaxVirtualIpServiceImpl.class);

    @Autowired
    private AccountLimitService accountLimitService;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;

    @Override
    @Transactional(value="core_transactionManager", rollbackFor = {EntityNotFoundException.class, UnprocessableEntityException.class, ImmutableEntityException.class, BadRequestException.class, OutOfVipsException.class, UniqueLbPortViolationException.class, AccountMismatchException.class})
    public VirtualIp addIpv6VirtualIpToLoadBalancer(VirtualIp vipConfig, LoadBalancer lb) throws PersistenceServiceException {
        VirtualIp vipToAdd;
        LoadBalancer dlb = loadBalancerRepository.getByIdAndAccountId(lb.getId(), lb.getAccountId());

        Integer ipv6Limit = accountLimitService.getLimit(dlb.getAccountId(), AccountLimitType.IPV6_LIMIT);
        Set<LoadBalancerJoinVip> loadBalancerJoinVips = dlb.getLoadBalancerJoinVipSet();

        int num_ipv6 = 0;

        for (LoadBalancerJoinVip lbJoinVip : loadBalancerJoinVips)
        {
            if (lbJoinVip.getVirtualIp().getIpVersion().equals(IpVersion.IPV6)) {
                num_ipv6++;
            }
        }

        if (num_ipv6 >= ipv6Limit) {
            throw new BadRequestException(String.format("Your load balancer cannot have more than %d IPv6 virtual ips.", ipv6Limit));
        }

        if (!vipTypeMatchesTypeForLoadBalancer(VirtualIpType.PUBLIC, dlb)) {
            String message = StringHelper.mismatchingVipType(VirtualIpType.PUBLIC);
            LOG.debug(message);
            throw new BadRequestException(message);
        }

        vipToAdd = allocateIpv6VirtualIp(dlb);

        if (!loadBalancerRepository.testAndSetStatus(dlb.getAccountId(), dlb.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dlb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        LoadBalancerJoinVip jv = new LoadBalancerJoinVip(dlb.getPort(), dlb, vipToAdd);
        virtualIpRepository.persist(jv);
        return vipToAdd;
    }

    @Override
    @Transactional(value="core_transactionManager", rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void prepareForVirtualIpDeletion(LoadBalancer lb, Integer vipId) throws PersistenceServiceException {
        List<Integer> vipIdsToDelete = new ArrayList<Integer>();
        vipIdsToDelete.add(vipId);
        prepareForVirtualIpsDeletion(lb.getAccountId(), lb.getId(), vipIdsToDelete);
    }

    private boolean vipTypeMatchesTypeForLoadBalancer(VirtualIpType vipType, LoadBalancer dbLoadBalancer) {
        for (LoadBalancerJoinVip loadBalancerJoinVip : dbLoadBalancer.getLoadBalancerJoinVipSet()) {
            if (!loadBalancerJoinVip.getVirtualIp().getVipType().equals(vipType)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(value="core_transactionManager", rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void prepareForVirtualIpsDeletion(Integer accountId, Integer loadbalancerId, List<Integer> virtualIpIds) throws PersistenceServiceException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(loadbalancerId, accountId);

        if (!hasAtLeastMinRequiredVips(dbLoadBalancer, virtualIpIds)) {
            LOG.debug("Updating the lb status to active");
            throw new BadRequestException(String.format("Cannot delete virtual ips. Minimum number of virtual ips required is %d.", Constants.MIN_REQUIRED_VIPS));
        }

        List<Integer> badVipIds = doVipsBelongToLoadBalancer(dbLoadBalancer, virtualIpIds);
        if (!badVipIds.isEmpty()) {
            LOG.debug("Updating the lb status to active");
            throw new BadRequestException(String.format("Must provide valid virtual ips, %s could not be found.", StringUtilities.DelimitString(badVipIds, ",")));
        }

        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }
    }

    @Override
    @Transactional(value="core_transactionManager")
    public boolean hasAtLeastMinRequiredVips(LoadBalancer lb, List<Integer> virtualIpIds) {
        Long numVipsAssignedToLoadBalancer = ((RaxVirtualIpRepository) virtualIpRepository).getNumVipsForLoadBalancer(lb);
        LOG.debug(String.format("%d vip(s) currently assigned to load balancer '%d'", numVipsAssignedToLoadBalancer, lb.getId()));
        return (numVipsAssignedToLoadBalancer - virtualIpIds.size()) >= Constants.MIN_REQUIRED_VIPS;
    }

    @Override
    @Transactional(value="core_transactionManager")
    public boolean hasExactlyMinRequiredVips(LoadBalancer lb) {
        Long numVipsAssignedToLoadBalancer = ((RaxVirtualIpRepository) virtualIpRepository).getNumVipsForLoadBalancer(lb);
        LOG.debug(String.format("%d vip(s) currently assigned to load balancer '%d'", numVipsAssignedToLoadBalancer, lb.getId()));
        return numVipsAssignedToLoadBalancer == Constants.MIN_REQUIRED_VIPS;
    }

    private List<Integer> doVipsBelongToLoadBalancer(LoadBalancer dbLoadBalancer, List<Integer> virtualIpIdsToDelete) {
        return ListUtil.compare(virtualIpIdsToDelete, getVipIdsInDb(dbLoadBalancer));
    }

    private List<Integer> getVipIdsInDb(LoadBalancer loadBalancer) {
        List<Integer> vipIdsInDb = new ArrayList<Integer>();
        for (LoadBalancerJoinVip loadBalancerJoinVip : loadBalancer.getLoadBalancerJoinVipSet()) {
            vipIdsInDb.add(loadBalancerJoinVip.getVirtualIp().getId());
        }

        for (LoadBalancerJoinVip loadBalancerJoinVip6 : loadBalancer.getLoadBalancerJoinVipSet()) {
            vipIdsInDb.add(loadBalancerJoinVip6.getVirtualIp().getId());
        }
        return vipIdsInDb;
    }

    @Override
    @Transactional(value="core_transactionManager")
    public boolean doesVipBelongToLoadBalancer(LoadBalancer lb, Integer vipId) {
        List<VirtualIp> vipsForLb = virtualIpRepository.getVipsByLoadBalancerId(lb.getId());
        for (VirtualIp vipForLb : vipsForLb) {
            if (vipId.equals(vipForLb.getId())) {
                return true;
            }
        }

        List<VirtualIp> ipv6VipsForLb = virtualIpRepository.getVipsByLoadBalancerId(lb.getId());
        for (VirtualIp vipForLb : ipv6VipsForLb) {
            if (vipId.equals(vipForLb.getId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(value="core_transactionManager")
    public boolean doesVipBelongToAccount(VirtualIp virtualIp, Integer accountId) {
        List<Integer> accountsUsingVip = ((RaxVirtualIpRepository) virtualIpRepository).getAccountIds(virtualIp);
        if (accountsUsingVip.size() > Constants.MIN_ACCOUNTS_PER_VIP) {
            LOG.warn(String.format("Multiple accounts using virtual ip '%d'", virtualIp.getId()));
        }

        for (Integer accountUsingVip : accountsUsingVip) {
            if (accountId.equals(accountUsingVip)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional(value="core_transactionManager")
    public void removeVipsFromLoadBalancer(LoadBalancer lb, List<Integer> vipIdsToDelete) {
        for (Integer vipIdToDelete : vipIdsToDelete) {
            removeVipFromLoadBalancer(lb, vipIdToDelete);
        }
    }

    @Override
    @Transactional(value="core_transactionManager")
    public void removeVipFromLoadBalancer(LoadBalancer lb, Integer vipId) {

        for (LoadBalancerJoinVip loadBalancerJoinVip : lb.getLoadBalancerJoinVipSet()) {
            if (loadBalancerJoinVip.getVirtualIp().getId().equals(vipId)) {
                virtualIpRepository.removeJoinRecord(loadBalancerJoinVip);
                reclaimVirtualIp(lb, loadBalancerJoinVip.getVirtualIp());
                lb.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
                break;
            }
        }
    }



    @Override
    protected LoadBalancer assignExtraVipsToLoadBalancer(LoadBalancer raxLoadBalancer) throws PersistenceServiceException
    {
        boolean emptyVirtualIpv6Set = false;

        // If Virtual Ips have already been assigned by core, then we need do nothing
        if (raxLoadBalancer.getLoadBalancerJoinVipSet().isEmpty() && raxLoadBalancer.getLoadBalancerJoinVipSet().isEmpty())   {
            assignDefaultIPv6ToLoadBalancer(raxLoadBalancer);
            emptyVirtualIpv6Set = true;
        }

        if (!raxLoadBalancer.getLoadBalancerJoinVipSet().isEmpty())
            return raxLoadBalancer;



        if (emptyVirtualIpv6Set) {
            Set<LoadBalancerJoinVip> newVipConfig = raxLoadBalancer.getLoadBalancerJoinVipSet();

            if (newVipConfig == null)
                newVipConfig = new HashSet<LoadBalancerJoinVip>();


            VirtualIp vip = new VirtualIp();
            vip.setAddress(null);
            vip.setVipType(VirtualIpType.PUBLIC);

            updateIpv4VirtualIp(raxLoadBalancer, vip);
            vip = virtualIpRepository.create(vip);

            LoadBalancerJoinVip newJoinRecord = new LoadBalancerJoinVip();
            newJoinRecord.setVirtualIp(vip);
            newVipConfig.add(newJoinRecord);

            raxLoadBalancer.setLoadBalancerJoinVipSet(newVipConfig);

        }

        return raxLoadBalancer;
    }
}
