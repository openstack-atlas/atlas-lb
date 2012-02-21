package org.openstack.atlas.ctxs.api.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerAdapter;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.ConnectionException;
import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.*;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerServiceImpl;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.adapter.CtxsLoadBalancerAdapter;

@Primary
@Service
public class CtxsReverseProxyLoadBalancerServiceImpl extends ReverseProxyLoadBalancerServiceImpl implements CtxsReverseProxyLoadBalancerService {

    private final Log LOG = LogFactory.getLog(CtxsReverseProxyLoadBalancerServiceImpl.class);

    @Autowired
    protected org.openstack.atlas.service.domain.service.HostService hostService;

    @Override
    public List<Certificate> createCertificates(List<Certificate> dbCerts) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = super.getConfigbyHost(hostService.getDefaultActiveHost());
        try {
           return ((CtxsLoadBalancerAdapter)loadBalancerAdapter).createCertificates(config, dbCerts);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }

    @Override
    public Certificate getCertificate(Certificate certificate) throws AdapterException, DecryptException, MalformedURLException, Exception {
        LoadBalancerEndpointConfiguration config = super.getConfigbyHost(hostService.getDefaultActiveHost());
        try {
            return ((CtxsLoadBalancerAdapter)loadBalancerAdapter).getCertificate(config, certificate);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }
    public void deleteCertificate(Certificate certificate) throws Exception
    {
        LoadBalancerEndpointConfiguration config = super.getConfigbyHost(hostService.getDefaultActiveHost());
        try {
            ((CtxsLoadBalancerAdapter)loadBalancerAdapter).deleteCertificate(config, certificate);
        } catch (ConnectionException exc) {
            checkAndSetIfEndPointBad(config, exc);
            throw exc;
        }
    }
}
