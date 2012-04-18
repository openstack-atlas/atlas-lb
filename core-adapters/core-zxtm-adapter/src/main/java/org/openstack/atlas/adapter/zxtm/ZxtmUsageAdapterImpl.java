package org.openstack.atlas.adapter.zxtm;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.common.config.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.common.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.adapter.common.entity.Cluster;
import org.openstack.atlas.adapter.common.entity.Host;
import org.openstack.atlas.adapter.common.entity.LoadBalancerHost;
import org.openstack.atlas.adapter.common.repository.HostRepository;
import org.openstack.atlas.adapter.common.service.HostService;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.adapter.exception.BadRequestException;
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
import java.util.*;

@Service
public class ZxtmUsageAdapterImpl implements UsageAdapter {
    private static Log LOG = LogFactory.getLog(ZxtmUsageAdapterImpl.class.getName());

    @Autowired
    protected HostService hostService;


    @Autowired
    protected HostRepository hostRepository;

    protected String logFileLocation;

    protected String adapterConfigFileLocation;


    @Autowired
    public ZxtmUsageAdapterImpl(Configuration configuration) {

        logFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.access_log_file_location);
        adapterConfigFileLocation = configuration.getString(PublicApiServiceConfigurationKeys.adapter_config_file_location);

        //Read settings from our adapter config file.
    }

    private LoadBalancerEndpointConfiguration getConfig(Integer loadBalancerId)  throws AdapterException
    {
        LoadBalancerEndpointConfiguration config = getConfigbyLoadBalancerId(loadBalancerId);
        if (config == null)
            throw new AdapterException("Adapter error: Cannot fetch information about LB devices");

        return config;
    }

    private LoadBalancerEndpointConfiguration getConfigbyLoadBalancerId(Integer lbId) {

        if (hostService == null) {
            LOG.debug("hostService is null !");
        }

        LoadBalancerHost lbHost = hostService.getLoadBalancerHost(lbId);
        Host host = lbHost.getHost();
        return getConfigbyHost(host);
    }


    private LoadBalancerEndpointConfiguration getConfigbyHost(Host host) {
        try {
            Cluster cluster = host.getCluster();
            Host endpointHost = hostRepository.getEndPointHost(cluster.getId());
            List<String> failoverHosts = hostRepository.getFailoverHostNames(cluster.getId());
            return new LoadBalancerEndpointConfiguration(endpointHost, cluster.getUsername(), CryptoUtil.decrypt(cluster.getPassword()), host, failoverHosts, logFileLocation);
        } catch(DecryptException except)
        {
            LOG.error(String.format("Decryption exception: ", except.getMessage()));
            return null;
        }
    }

    @Override
    public Map<Integer, Long> getTransferBytesIn(List<LoadBalancer> lbs) throws AdapterException {

        if (lbs.size() == 0) {
            return new HashMap<Integer, Long>();
        }

        LoadBalancer firstlb = lbs.get(0);
        LoadBalancerEndpointConfiguration config = getConfig(firstlb.getId());

        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            Map<Integer, Long> bytesInMap = new HashMap<Integer, Long>();
            List<String> validVsNames = getValidVsNames(config, toVirtualServerNames(lbs));

            long[] bytesIn = serviceStubs.getSystemStatsBinding().getVirtualserverBytesIn(validVsNames.toArray(new String[validVsNames.size()]));

            for (int i = 0; i < validVsNames.size(); i++) {
                bytesInMap.put(ZxtmNameHelper.stripLbIdFromName(validVsNames.get(i)), bytesIn[i]);
            }

            return bytesInMap;
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    @Override
    public Map<Integer, Long> getTransferBytesOut(List<LoadBalancer> lbs) throws AdapterException {

        if (lbs.size() == 0) {
            return new HashMap<Integer, Long>();
        }

        LoadBalancer firstlb = lbs.get(0);
        LoadBalancerEndpointConfiguration config = getConfig(firstlb.getId());

        try {
            ZxtmServiceStubs serviceStubs = getServiceStubs(config);
            Map<Integer, Long> bytesOutMap = new HashMap<Integer, Long>();
            List<String> validVsNames = getValidVsNames(config, toVirtualServerNames(lbs));

            long[] bytesOut = serviceStubs.getSystemStatsBinding().getVirtualserverBytesOut(validVsNames.toArray(new String[validVsNames.size()]));

            for (int i = 0; i < validVsNames.size(); i++) {
                bytesOutMap.put(ZxtmNameHelper.stripLbIdFromName(validVsNames.get(i)), bytesOut[i]);
            }

            return bytesOutMap;
        } catch (RemoteException e) {
            throw new AdapterException(e);
        }
    }

    /*
    * *********************
    * * PROTECTED METHODS *
    * *********************
    */

    protected ZxtmServiceStubs getServiceStubs(LoadBalancerEndpointConfiguration config) throws AxisFault {
        return ZxtmServiceStubs.getServiceStubs(config.getEndpointUrl(), config.getUsername(), config.getPassword());
    }

    protected List<String> toVirtualServerNames(List<LoadBalancer> lbs) throws BadRequestException {
        List<String> virtualServerNames = new ArrayList<String>();

        for (LoadBalancer lb : lbs) {
            virtualServerNames.add(ZxtmNameHelper.generateNameWithAccountIdAndLoadBalancerId(lb));
        }

        return virtualServerNames;
    }

    protected List<String> getValidVsNames(LoadBalancerEndpointConfiguration config, List<String> virtualServerNames) throws RemoteException, BadRequestException {
        Set<String> allLoadBalancerNames = new HashSet<String>(getStatsSystemLoadBalancerNames(config));
        Set<String> loadBalancerNamesForHost = new HashSet<String>(virtualServerNames);
        loadBalancerNamesForHost.retainAll(allLoadBalancerNames); // Get the intersection
        return new ArrayList<String>(loadBalancerNamesForHost);
    }

    protected List<String> getStatsSystemLoadBalancerNames(LoadBalancerEndpointConfiguration config) throws RemoteException {
        ZxtmServiceStubs serviceStubs = getServiceStubs(config);
        List<String> loadBalancerNames = new ArrayList<String>();
        loadBalancerNames.addAll(Arrays.asList(serviceStubs.getSystemStatsBinding().getVirtualservers()));
        return loadBalancerNames;
    }
}
