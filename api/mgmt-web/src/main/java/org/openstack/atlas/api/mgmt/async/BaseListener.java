package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.api.async._BaseListener;
import org.openstack.atlas.service.domain.services.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseListener extends _BaseListener {
    protected Log LOG = LogFactory.getLog(this.getClass());
    protected CallbackService callbackService;
    protected BlackListService blackListService;
    protected ClusterService clusterService;
    protected SuspensionService suspensionService;

    public void setCallbackService(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    public void setSuspensionService(SuspensionService suspensionService) {
        this.suspensionService = suspensionService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public void setBlackListService(BlackListService blackListService) {
        this.blackListService = blackListService;
    }
}
