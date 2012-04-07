package org.openstack.atlas.adapter.common.entity;


import org.openstack.atlas.common.ip.IPv6;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.service.domain.entity.VirtualIpType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)

@Table(name = "adapter_virtual_ipv4")
public class VirtualIpv4 implements Serializable {
    private final static long serialVersionUID = 549512317L;


    @Id
    @Column(name = "address", unique = true, length = 39, nullable = false)
    private String address;

    @Column(name = "vip_id", nullable = true)
    private Integer vip_id;

    @ManyToOne
    @JoinColumn(name = "cluster", nullable = true)
    private Cluster cluster;


    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private VirtualIpType vipType;


    @Column(name = "last_deallocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastDeallocation;

    @Column(name = "last_allocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastAllocation;

    @Column(name = "is_allocated", nullable = false)
    private Boolean isAllocated = false;

    @Column(name = "ref_count", nullable = false)
    private Integer refCount = 0;


    public VirtualIpv4()
    {}

    public VirtualIpv4(String address, VirtualIpType vipType, Cluster cluster) {
        this.address = address;
        this.vipType = vipType;
        this.cluster= cluster;
    }

    public Integer getVipId() {
        return vip_id;
    }

    public void setVipId(Integer vip_id) {
        this.vip_id = vip_id;
    }


    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public VirtualIpType getVipType() {
        return vipType;
    }

    public void setVipType(VirtualIpType vipType) {
        this.vipType = vipType;
    }

    public Calendar getLastDeallocation() {
        return lastDeallocation;
    }

    public void setLastDeallocation(Calendar lastDeallocation) {
        this.lastDeallocation = lastDeallocation;
    }

    public Calendar getLastAllocation() {
        return lastAllocation;
    }

    public void setLastAllocation(Calendar lastAllocation) {
        this.lastAllocation = lastAllocation;
    }

    public Boolean isAllocated() {
        return isAllocated;
    }

    public void setAllocated(Boolean allocated) {
        isAllocated = allocated;
    }

    public Integer getRefCount() {
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }
}
