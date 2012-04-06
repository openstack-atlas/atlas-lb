package org.openstack.atlas.adapter.common.entity;



import javax.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)

@Table(name = "adapter_load_balancer_host")
public class LoadBalancerHost implements Serializable {
    private final static long serialVersionUID = 542572317L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 542572317L;

        @Column(name = "load_balancer_id")
        private Integer loadBalancerId;

        @Column(name = "host_id")
        private Integer hostId;


        public Id() {
        }

        public Id(Integer loadBalancerId, Integer hostId) {
            this.loadBalancerId = loadBalancerId;
            this.hostId = hostId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (loadBalancerId != null ? !loadBalancerId.equals(id.loadBalancerId) : id.loadBalancerId != null)
                return false;
            if (hostId != null ? !hostId.equals(id.hostId) : id.hostId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = loadBalancerId != null ? loadBalancerId.hashCode() : 0;
            result = 31 * result + (hostId != null ? hostId.hashCode() : 0);
            return result;
        }
    }

    @EmbeddedId
    private Id id = new Id();



    @ManyToOne
    @JoinColumn(name = "host_id", insertable = false, updatable = false)
    private Host host;

    public LoadBalancerHost() {
    }

    public LoadBalancerHost(Integer lbId, Host host) {
        this.host = host;
        this.id.loadBalancerId = lbId;
        this.id.hostId = host.getId();
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Integer getLoadBalancerId() {
        return this.id.loadBalancerId;
    }

    public void setLoadBalancerId(Integer lbId) {
        this.id.loadBalancerId = lbId;
    }


    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
        this.id.hostId = host.getId();
    }

}
