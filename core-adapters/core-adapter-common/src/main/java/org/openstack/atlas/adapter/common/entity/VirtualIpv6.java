package org.openstack.atlas.adapter.common.entity;

import org.openstack.atlas.common.ip.IPv6;
import org.openstack.atlas.common.ip.IPv6Cidr;
import org.openstack.atlas.common.ip.exception.IPStringConversionException1;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)

@Table(name = "adapter_virtual_ipv6")
public class VirtualIpv6 implements Serializable {
    private final static long serialVersionUID = 532712316L;


    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Id
    @Column(name = "vip_id", nullable = false)
    private Integer virtualIpId;
    
    @Column(name = "vip_octets", nullable = false)
    private Integer vipOctets;

    @Column(name = "ref_count", nullable = false)
    private Integer refCount = 0;

    public Integer getVirtualIpId() {
        return virtualIpId;
    }

    public void setVirtualIpId(Integer virtualIpv6Id) {
        this.virtualIpId = virtualIpv6Id;
    }    
        
    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }    
    
    public Integer getVipOctets() {
        return vipOctets;
    }

    public void setVipOctets(Integer vipOctets) {
        this.vipOctets = vipOctets;
    }

    public Integer getRefCount() {
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }


    public String getDerivedIpString(Cluster c) throws IPStringConversionException1 {
        String out;
        String clusterCidrString = c.getClusterIpv6Cidr();
        if (clusterCidrString == null) {
            String msg = String.format("Cluster[%d] has null value for ClusterIpv6Cider", c.getId());
            throw new IPStringConversionException1(msg);
        }
        IPv6Cidr v6Cidr = new IPv6Cidr(clusterCidrString);
        IPv6 v6 = new IPv6("::");
        v6.setClusterPartition(v6Cidr);
        v6.setAccountPartition(this.getAccountId());
        v6.setVipOctets(this.getVipOctets());
        out = v6.expand();
        return out;
    }
        
}
