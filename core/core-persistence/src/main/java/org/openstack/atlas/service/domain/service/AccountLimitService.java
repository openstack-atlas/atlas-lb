package org.openstack.atlas.service.domain.service;

import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.LimitReachedException;

public interface AccountLimitService {
    void verifyLoadBalancerLimit(Integer accountId) throws EntityNotFoundException, LimitReachedException;
}
