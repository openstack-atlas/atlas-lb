package org.openstack.atlas.service.domain.entity;

import org.openstack.atlas.common.ip.IPv6;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "virtual_ipv6")
public class VirtualIpv6 extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToMany(mappedBy = "virtualIp")
    private Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set = new HashSet<LoadBalancerJoinVip6>();

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "address", length = 64, unique = true, nullable = true)
    private String address;


    public Set<LoadBalancerJoinVip6> getLoadBalancerJoinVip6Set() {
        return loadBalancerJoinVip6Set;
    }

    public void setLoadBalancerJoinVip6Set(Set<LoadBalancerJoinVip6> loadBalancerJoinVip6Set) {
        this.loadBalancerJoinVip6Set = loadBalancerJoinVip6Set;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDerivedIpString() throws IPStringConversionException1 {
        String out;
        String clusterCidrString = "fd24:f480:ce44:91bc::/64";
        if (clusterCidrString == null) {
            String msg = String.format("Cluster has null value for ClusterIpv6Cider");
            throw new IPStringConversionException1(msg);
        }
        IPv6Cidr v6Cidr = new IPv6Cidr(clusterCidrString);
        IPv6 v6 = new IPv6("::");
        v6.setClusterPartition(v6Cidr);
        v6.setAccountPartition(this.getAccountId());
        //v6.setVipOctets(this.getVipOctets());
        out = v6.expand();
        return out;
    }
}
