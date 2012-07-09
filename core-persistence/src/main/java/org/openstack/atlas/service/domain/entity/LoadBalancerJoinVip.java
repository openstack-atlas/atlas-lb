package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "load_balancer_virtual_ip")
public class LoadBalancerJoinVip implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 532512316L;

        @Column(name = "load_balancer_id")
        private Integer loadBalancerId;

        @Column(name = "virtual_ip_id")
        private Integer virtualIpId;

        public Id() {
        }

        public Id(Integer loadBalancerId, Integer virtualIpId) {
            this.loadBalancerId = loadBalancerId;
            this.virtualIpId = virtualIpId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (loadBalancerId != null ? !loadBalancerId.equals(id.loadBalancerId) : id.loadBalancerId != null)
                return false;
            if (virtualIpId != null ? !virtualIpId.equals(id.virtualIpId) : id.virtualIpId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = loadBalancerId != null ? loadBalancerId.hashCode() : 0;
            result = 31 * result + (virtualIpId != null ? virtualIpId.hashCode() : 0);
            return result;
        }
    }

    @EmbeddedId
    private Id id = new Id();

    @Column(name = "port")
    private Integer port;

    @ManyToOne
    @JoinColumn(name = "load_balancer_id", insertable = false, updatable = false, nullable = true)
    private LoadBalancer loadBalancer;

    @ManyToOne
    @JoinColumn(name = "virtual_ip_id", insertable = false, updatable = false)
    private VirtualIp virtualIp;

    public LoadBalancerJoinVip() {
    }

    public LoadBalancerJoinVip(Integer port, LoadBalancer loadBalancer, VirtualIp virtualIp) {
        this.port = port;
        this.loadBalancer = loadBalancer;
        this.virtualIp = virtualIp;
        this.id.loadBalancerId = loadBalancer.getId();
        this.id.virtualIpId = virtualIp.getId();
        loadBalancer.getLoadBalancerJoinVipSet().add(this);
        virtualIp.getLoadBalancerJoinVipSet().add(this);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        this.id.loadBalancerId = loadBalancer.getId();
    }

    public VirtualIp getVirtualIp() {
        return virtualIp;
    }

    public void setVirtualIp(VirtualIp virtualIp) {
        this.virtualIp = virtualIp;
        this.id.virtualIpId = virtualIp.getId();
    }
}
