package org.openstack.atlas.rax.domain.repository;

import org.openstack.atlas.rax.domain.entity.RaxHost;

import java.util.List;

public interface RaxHostRepository {
    List<RaxHost> getAllHosts();

    void save(RaxHost raxHost);
}
