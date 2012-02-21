package org.openstack.atlas.ctxs.service.domain.entity;

import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.ctxs.service.domain.entity.CertificateRef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@DiscriminatorValue("CTXS")
public class CtxsLoadBalancer extends LoadBalancer  implements Serializable {
    private final static long serialVersionUID = 532552314L;

    @OneToMany(mappedBy = "loadBalancer", fetch = FetchType.EAGER)
    private Set<CertificateRef> certificates = new HashSet<CertificateRef>();

    @Column(name = "sslMode", length = 25)
    private String sslMode;

    public String getSslMode() {
        return sslMode;
    }

    public void setSslMode(String sslMode) {
        this.sslMode = sslMode;
    }

    public Set<CertificateRef> getCertificates() {
        return certificates;
    }

    public void setCertificates(Set<CertificateRef> certificates) {
        this.certificates = certificates;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CtxsLoadBalancer)) return false;

        CtxsLoadBalancer that = (CtxsLoadBalancer) o;

        if (certificates != null ? !certificates.equals(that.certificates) : that.certificates != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = certificates != null ? certificates.hashCode() : 0;
        result = 31 * result;
        return result;
    }
}
