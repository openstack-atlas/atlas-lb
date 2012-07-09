package org.openstack.atlas.adapter.common.entity;

import java.io.Serializable;

public enum HostStatus implements Serializable {
    ACTIVE_TARGET,
    ACTIVE,
    FAIL_OVER,
    OFFLINE,
    BURN_IN;
    
    private final static long serialVersionUID = 532512316L;
}