package org.openstack.atlas.adapter.common.entity;

import org.openstack.atlas.service.domain.entity.VirtualIp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)

@Table(name = "adapter_cluster")
public class Cluster extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;


    @Column(name = "cluster_ipv6_cidr", length = 43, nullable = true)
    private String clusterIpv6Cidr;


    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClusterIpv6Cidr() {
        return clusterIpv6Cidr;
    }

    public void setClusterIpv6Cidr(String clusterIpv6Cidr) {
        this.clusterIpv6Cidr = clusterIpv6Cidr;
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", clusterIpv6Cidr='" + clusterIpv6Cidr + '\'' +
                '}';
    }
}
