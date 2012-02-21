package org.openstack.atlas.ctxs.adapter;

import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;

import org.openstack.atlas.adapter.exception.AdapterException;

import org.openstack.atlas.ctxs.service.domain.entity.CtxsLoadBalancer;
import org.openstack.atlas.service.domain.entity.Node;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.HealthMonitor;
import org.openstack.atlas.service.domain.entity.SessionPersistence;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

public interface CtxsLoadBalancerAdapter extends LoadBalancerAdapter {

    List<Certificate> createCertificates(LoadBalancerEndpointConfiguration config, List<Certificate> loadCert) throws AdapterException;
    Certificate getCertificate(LoadBalancerEndpointConfiguration config, Certificate certificate) throws AdapterException;
    void deleteCertificate(LoadBalancerEndpointConfiguration config, Certificate certificate) throws AdapterException;
}
