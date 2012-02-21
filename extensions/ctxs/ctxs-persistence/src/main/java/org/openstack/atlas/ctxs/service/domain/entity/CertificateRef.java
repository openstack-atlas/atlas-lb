package org.openstack.atlas.ctxs.service.domain.entity;

import org.openstack.atlas.service.domain.entity.*;

import javax.persistence.*;
import java.io.Serializable;


@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="vendor",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("CTXS")
@Table(name = "load_balancer_certificate")
public class CertificateRef implements Serializable {
    private final static long serialVersionUID = 512512324L;

    @Embeddable
    public static class Id implements Serializable {
        private final static long serialVersionUID = 532512316L;

        @Column(name = "load_balancer_id")
        private Integer loadBalancerId;

        @Column(name = "idRef")
        private Integer idRef;

        public Id() {
        }

        public Id(Integer loadBalancerId, Integer idRef) {
            this.loadBalancerId = loadBalancerId;
            this.idRef = idRef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (loadBalancerId != null ? !loadBalancerId.equals(id.loadBalancerId) : id.loadBalancerId != null)
                return false;
            if (idRef != null ? !idRef.equals(id.idRef) : id.idRef != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = loadBalancerId != null ? loadBalancerId.hashCode() : 0;
            result = 31 * result + (idRef != null ? idRef.hashCode() : 0);
            return result;
        }
    }

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne
    @JoinColumn(name = "load_balancer_id", insertable = false, updatable = false)
    private CtxsLoadBalancer loadBalancer;

    public CertificateRef()
    {
    }

    public CertificateRef(CtxsLoadBalancer lb, Integer certificateid)
    {
        this.loadBalancer = lb;
        this.id.idRef = certificateid;
        this.id.loadBalancerId = loadBalancer.getId();
        lb.getCertificates().add(this);
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public CtxsLoadBalancer getLoadbalancer() {
        return loadBalancer;
    }

    public void setLoadbalancer(CtxsLoadBalancer loadbalancer) {
        this.loadBalancer = loadbalancer;
        this.id.loadBalancerId = loadbalancer.getId();
    }


    public Integer getIdRef() {
        return this.id.idRef;
    }

    public void setIdRef(Integer idRef) {
        this.id.idRef = idRef;
    }


    @Override
    public String toString() {
        return "CertificateRef {" +
                "    idRef=" + id.idRef.toString() +
                ",    loadbalancer_id=" + id.loadBalancerId.toString() +
                '}';
    }
}
