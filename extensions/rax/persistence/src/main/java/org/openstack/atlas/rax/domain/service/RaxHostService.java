package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.rax.domain.entity.RaxHost;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.springframework.stereotype.Service;

@Service
public interface RaxHostService {
    void create(RaxHost host) throws EntityNotFoundException, UnprocessableEntityException;
}
