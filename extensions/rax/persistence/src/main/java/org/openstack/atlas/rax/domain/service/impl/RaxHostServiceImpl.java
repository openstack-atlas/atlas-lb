package org.openstack.atlas.rax.domain.service.impl;

import org.openstack.atlas.rax.domain.entity.RaxCluster;
import org.openstack.atlas.rax.domain.entity.RaxHost;
import org.openstack.atlas.rax.domain.repository.RaxClusterRepository;
import org.openstack.atlas.rax.domain.repository.RaxHostRepository;
import org.openstack.atlas.rax.domain.service.RaxHostService;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.HostStatus;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RaxHostServiceImpl implements RaxHostService {

    @Autowired
    private RaxHostRepository raxHostRepository;

    @Autowired
    private RaxClusterRepository raxClusterRepository;

    @Transactional(value="core_transactionManager")
    @Override
    public void create(RaxHost raxHost) throws EntityNotFoundException, UnprocessableEntityException {
        RaxCluster raxCluster = raxClusterRepository.getById(raxHost.getCluster().getId());
        raxHost.setCluster(raxCluster);

        List<RaxHost> raxHosts = raxHostRepository.getAllHosts();
        if(hostAlreadyExists(raxHosts, raxHost)) {
            throw new UnprocessableEntityException(ErrorMessages.HOST_ALREADY_EXISTS);
        }
        raxHost.setHostStatus(HostStatus.BURN_IN);
        raxHostRepository.save(raxHost);
    }

    private boolean hostAlreadyExists(List<RaxHost> raxHosts, RaxHost host) {
        for(RaxHost raxHost: raxHosts) {
            if(raxHost.getEndpoint().equals(host.getEndpoint())) {
                return true;
            }
        }
        return false;
    }
}
