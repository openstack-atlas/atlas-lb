package org.openstack.atlas.service.domain.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("CORE")
@Table(name = "virtual_ipv4")
public class VirtualIp extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @OneToMany(mappedBy = "virtualIp")
    private Set<LoadBalancerJoinVip> loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
    
    @Column(name = "account_id", nullable = false)
    private Integer accountId;
        
    @Column(name = "address", length = 39, unique = true, nullable = true)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private VirtualIpType vipType;


    public Set<LoadBalancerJoinVip> getLoadBalancerJoinVipSet() {
        if(loadBalancerJoinVipSet == null) loadBalancerJoinVipSet = new HashSet<LoadBalancerJoinVip>();
        return loadBalancerJoinVipSet;
    }

    public void setLoadBalancerJoinVipSet(Set<LoadBalancerJoinVip> loadBalancerJoinVipSet) {
        this.loadBalancerJoinVipSet = loadBalancerJoinVipSet;
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

    public VirtualIpType getVipType() {
        return vipType;
    }

    public void setVipType(VirtualIpType vipType) {
        this.vipType = vipType;
    }

    @Override
    public String toString() {
        return "VirtualIp{" +
                "loadBalancerJoinVipSet=" + loadBalancerJoinVipSet +
                ", ipAddress='" + address + '\'' +
                ", vipType=" + vipType +
                '}';
    }
}
