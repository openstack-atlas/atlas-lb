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
@DiscriminatorColumn(
        name = "vendor",
        discriminatorType = DiscriminatorType.STRING
)
@DiscriminatorValue("ADAPTER")
@Table(name = "adapter_vip_octets")
public class VirtualIpv6Octets extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532712316L;


    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "vip_id", nullable = false)
    private Integer virtualIpv6Id;
    
    @Column(name = "vip_octets", nullable = false)
    private Integer vipOctets;

    public Integer getVirtualIpv6Id() {
        return virtualIpv6Id;
    }

    public void setVirtualIpv6Id(Integer virtualIpv6Id) {
        this.virtualIpv6Id = virtualIpv6Id;
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
