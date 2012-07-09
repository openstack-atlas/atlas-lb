package org.openstack.atlas.ctxs.adapter;

import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;

import org.openstack.atlas.adapter.exception.AdapterException;

import java.util.List;


public interface CtxsLoadBalancerAdapter extends LoadBalancerAdapter {

    List<Certificate> createCertificates(List<Certificate> loadCert) throws AdapterException;
    Certificate getCertificate(Certificate certificate) throws AdapterException;
    void deleteCertificate(Certificate certificate) throws AdapterException;
}
