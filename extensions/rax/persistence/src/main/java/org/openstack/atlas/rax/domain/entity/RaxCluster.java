package org.openstack.atlas.rax.domain.entity;

import org.openstack.atlas.service.domain.entity.Cluster;
import org.openstack.atlas.service.domain.entity.Host;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import java.io.Serializable;

@javax.persistence.Entity
@DiscriminatorValue(Discriminator.RAX)
public class RaxCluster extends Cluster implements Serializable {
    private final static long serialVersionUID = 532512316L;

    @Column
    private String foo;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }
}
