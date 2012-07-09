package org.openstack.atlas.ctxs.api.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;


import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.adapter.CtxsLoadBalancerAdapter;

@Primary
@Service
public class CtxsReverseProxyLoadBalancerServiceImpl extends ReverseProxyLoadBalancerServiceImpl implements CtxsReverseProxyLoadBalancerService {

    private final Log LOG = LogFactory.getLog(CtxsReverseProxyLoadBalancerServiceImpl.class);


    @Override
    public List<Certificate> createCertificates(List<Certificate> dbCerts) throws AdapterException, DecryptException, MalformedURLException, Exception {
        try {
           return ((CtxsLoadBalancerAdapter)loadBalancerAdapter).createCertificates(dbCerts);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }

    @Override
    public Certificate getCertificate(Certificate certificate) throws AdapterException, DecryptException, MalformedURLException, Exception {
        try {
            return ((CtxsLoadBalancerAdapter)loadBalancerAdapter).getCertificate(certificate);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }
    public void deleteCertificate(Certificate certificate) throws Exception
    {
        try {
            ((CtxsLoadBalancerAdapter)loadBalancerAdapter).deleteCertificate(certificate);
        } catch (ConnectionException exc) {
            throw exc;
        }
    }
}
