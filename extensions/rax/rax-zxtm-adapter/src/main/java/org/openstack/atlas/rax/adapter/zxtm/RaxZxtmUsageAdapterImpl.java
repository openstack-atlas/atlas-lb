package org.openstack.atlas.rax.adapter.zxtm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.config.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.common.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.zxtm.ZxtmUsageAdapterImpl;
import org.openstack.atlas.adapter.zxtm.helper.ZxtmNameHelper;
import org.openstack.atlas.adapter.zxtm.service.ZxtmServiceStubs;
import org.openstack.atlas.common.config.Configuration;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service
public class RaxZxtmUsageAdapterImpl extends ZxtmUsageAdapterImpl implements RaxZxtmUsageAdapter {
    public static Log LOG = LogFactory.getLog(RaxZxtmUsageAdapterImpl.class.getName());

    @Autowired
    public RaxZxtmUsageAdapterImpl(Configuration configuration) {
        super(configuration);
        //Read settings from our adapter config file.
    }

    private LoadBalancerEndpointConfiguration getConfigbyHost(Host host) {
        try {
            Cluster cluster = host.getCluster();
            Host endpointHost = hostRepository.getEndPointHost(cluster.getId());
            List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
            return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
        } catch(

                DecryptException except)
        {
            LOG.error(String.format("Decryption exception: ", except.getMessage()));
            return null;
        }
    }

    @Override
    public Map<Integer, Integer> getCurrentConnectionCount(List<LoadBalancer> lbs) throws AdapterException {
        try {
            Host host = hostService.getDefaultActiveHost();

            if (host == null)
                throw new AdapterException("Cannot retrieve default active host from persistence layer");


            LoadBalancerEndpointConfiguration config = getConfigbyHost(host);

            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            Map<Integer, Integer> currentConnectionMap = new HashMap<Integer, Integer>();
            List<String> validVsNames = getValidVsNames(config, toVirtualServerNames(lbs));

            int[] currentConnections = serviceStubs.getSystemStatsBinding().getVirtualserverCurrentConn(validVsNames.toArray(new String[validVsNames.size()]));

            for (int i = 0; i < validVsNames.size(); i++) {
                currentConnectionMap.put(ZxtmNameHelper.stripLbIdFromName(validVsNames.get(i)), currentConnections[i]);
            }

            return currentConnectionMap;
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }
}
