package org.openstack.atlas.adapter.common.entity;


import org.openstack.atlas.common.ip.IPv6;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "virtual_ip__cluster")
public class VirtualIpCluster implements Serializable {
    private final static long serialVersionUID = 542512317L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 542512317L;

        @Column(name = "virtual_ip_id")
        private Integer virtualIpId;


        @Column(name = "cluster_id")
        private Integer clusterId;

        public Id() {
        }

        public Id(Integer virtualIpId, Integer clusterId) {
            this.virtualIpId = virtualIpId;
            this.clusterId = clusterId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (virtualIpId != null ? !virtualIpId.equals(id.virtualIpId) : id.virtualIpId != null)
                return false;
            if (clusterId != null ? !clusterId.equals(id.clusterId) : id.clusterId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = virtualIpId != null ? virtualIpId.hashCode() : 0;
            result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
            return result;
        }
    }

    @EmbeddedId
    private Id id = new Id();



    @ManyToOne
    @JoinColumn(name = "cluster", nullable = true) // TODO: Should not be nullable. Need to get cluster internally
    private Cluster cluster;


    @Column(name = "last_deallocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastDeallocation;

    @Column(name = "last_allocation")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastAllocation;

    @Column(name = "is_allocated", nullable = false)
    private Boolean isAllocated = false;

    public VirtualIpCluster() {
    }

    public VirtualIpCluster(Integer vipId, Cluster cluster) {
        this.cluster = cluster;
        this.id.virtualIpId = vipId;
        this.id.clusterId = cluster.getId();
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Integer getVirtualIpId() {
        return this.id.virtualIpId;
    }

    public void setVirtualIpId(Integer vipId) {
        this.id.virtualIpId = vipId;
    }


    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
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
}
