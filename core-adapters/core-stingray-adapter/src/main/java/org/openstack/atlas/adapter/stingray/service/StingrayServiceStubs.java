package org.openstack.atlas.adapter.stingray.service;

import com.riverbed.stingray.service.client.*;
import org.apache.axis.AxisFault;

import java.net.URL;

public class StingrayServiceStubs {
    private PoolBindingStub stingrayPoolService;
    private SystemBackupsBindingStub stingraySystemBackupsService;
    private SystemMachineInfoBindingStub stingraySystemMachineInfoService;
    private TrafficIPGroupsBindingStub stingrayTrafficIpGroupService;
    private VirtualServerBindingStub stingrayVirtualServerService;
    private CatalogMonitorBindingStub stingrayMonitorCatalogService;
    private CatalogRateBindingStub stingrayRateCatalogService;
    private CatalogPersistenceBindingStub stingrayPersistenceService;
    private CatalogProtectionBindingStub stingrayProtectionService;
    private CatalogRuleBindingStub stingrayRuleCatalogService;
    private SystemStatsBindingStub stingraySystemStatsService;
    private ConfExtraBindingStub stingrayConfExtraService;

    public StingrayServiceStubs(PoolBindingStub stingrayPoolService,
                            SystemBackupsBindingStub stingraySystemBackupsService, SystemMachineInfoBindingStub stingraySystemMachineInfoService,
                            TrafficIPGroupsBindingStub stingrayTrafficIpGroupService, VirtualServerBindingStub stingrayVirtualServerService,
                            CatalogMonitorBindingStub stingrayMonitorCatalogService, CatalogPersistenceBindingStub stingrayPersistenceService,
                            CatalogProtectionBindingStub stingrayProtectionService, CatalogRuleBindingStub stingrayRuleCatalogService,
                            SystemStatsBindingStub stingraySystemStatsService, CatalogRateBindingStub stingrayRateCatalogService,
                            ConfExtraBindingStub stingrayConfExtraService) {
        this.stingrayPoolService = stingrayPoolService;
        this.stingraySystemBackupsService = stingraySystemBackupsService;
        this.stingraySystemMachineInfoService = stingraySystemMachineInfoService;
        this.stingrayTrafficIpGroupService = stingrayTrafficIpGroupService;
        this.stingrayVirtualServerService = stingrayVirtualServerService;
        this.stingrayMonitorCatalogService = stingrayMonitorCatalogService;
        this.stingrayPersistenceService = stingrayPersistenceService;
        this.stingrayProtectionService = stingrayProtectionService;
        this.stingraySystemStatsService = stingraySystemStatsService;
        this.stingrayRuleCatalogService = stingrayRuleCatalogService;
        this.stingrayRateCatalogService = stingrayRateCatalogService;
        this.stingrayConfExtraService = stingrayConfExtraService;

    }

    public static StingrayServiceStubs getServiceStubs(URL endpoint, String username, String password) throws AxisFault {
        PoolBindingStub stingrayPoolService = new PoolBindingStub(endpoint, null);
        stingrayPoolService.setUsername(username);
        stingrayPoolService.setPassword(password);

        SystemBackupsBindingStub stingraySystemBackupsService = new SystemBackupsBindingStub(endpoint, null);
        stingraySystemBackupsService.setUsername(username);
        stingraySystemBackupsService.setPassword(password);

        SystemMachineInfoBindingStub stingraySystemMachineInfoService = new SystemMachineInfoBindingStub(endpoint, null);
        stingraySystemMachineInfoService.setUsername(username);
        stingraySystemMachineInfoService.setPassword(password);

        TrafficIPGroupsBindingStub stingrayTrafficIpGroupService = new TrafficIPGroupsBindingStub(endpoint, null);
        stingrayTrafficIpGroupService.setUsername(username);
        stingrayTrafficIpGroupService.setPassword(password);

        VirtualServerBindingStub stingrayVirtualServerService = new VirtualServerBindingStub(endpoint, null);
        stingrayVirtualServerService.setUsername(username);
        stingrayVirtualServerService.setPassword(password);

        CatalogMonitorBindingStub stingrayMonitorCatalogService = new CatalogMonitorBindingStub(endpoint, null);
        stingrayMonitorCatalogService.setUsername(username);
        stingrayMonitorCatalogService.setPassword(password);

        CatalogPersistenceBindingStub stingrayMonitorPersistenceService = new CatalogPersistenceBindingStub(endpoint, null);
        stingrayMonitorPersistenceService.setUsername(username);
        stingrayMonitorPersistenceService.setPassword(password);

        CatalogProtectionBindingStub stingrayMonitorProtectionService = new CatalogProtectionBindingStub(endpoint, null);
        stingrayMonitorProtectionService.setUsername(username);
        stingrayMonitorProtectionService.setPassword(password);

        CatalogRateBindingStub stingrayRateCatalogService = new CatalogRateBindingStub(endpoint, null);
        stingrayRateCatalogService.setUsername(username);
        stingrayRateCatalogService.setPassword(password);

        CatalogRuleBindingStub stingrayRuleCatalogService = new CatalogRuleBindingStub(endpoint, null);
        stingrayRuleCatalogService.setUsername(username);
        stingrayRuleCatalogService.setPassword(password);

        SystemStatsBindingStub stingraySystemStatsService = new SystemStatsBindingStub(endpoint, null);
        stingraySystemStatsService.setUsername(username);
        stingraySystemStatsService.setPassword(password);

        ConfExtraBindingStub stingrayConfExtraService = new ConfExtraBindingStub(endpoint,null);
        stingrayConfExtraService.setUsername(username);
        stingrayConfExtraService.setPassword(password);

        return new StingrayServiceStubs(stingrayPoolService,
                stingraySystemBackupsService, stingraySystemMachineInfoService,
                stingrayTrafficIpGroupService, stingrayVirtualServerService,
                stingrayMonitorCatalogService, stingrayMonitorPersistenceService,
                stingrayMonitorProtectionService, stingrayRuleCatalogService,
                stingraySystemStatsService, stingrayRateCatalogService,
                stingrayConfExtraService);
    }

    public PoolBindingStub getPoolBinding() {
        return stingrayPoolService;
    }

    public SystemBackupsBindingStub getSystemBackupsBinding() {
        return stingraySystemBackupsService;
    }

    public SystemMachineInfoBindingStub getSystemMachineInfoBinding() {
        return stingraySystemMachineInfoService;
    }

    public TrafficIPGroupsBindingStub getTrafficIpGroupBinding() {
        return stingrayTrafficIpGroupService;
    }

    public VirtualServerBindingStub getVirtualServerBinding() {
        return stingrayVirtualServerService;
    }

    public CatalogMonitorBindingStub getMonitorBinding() {
        return stingrayMonitorCatalogService;
    }

    public CatalogPersistenceBindingStub getPersistenceBinding() {
        return stingrayPersistenceService;
    }

    public CatalogProtectionBindingStub getProtectionBinding() {
        return stingrayProtectionService;
    }

    public SystemStatsBindingStub getSystemStatsBinding() {
        return stingraySystemStatsService;
    }

    public CatalogRuleBindingStub getStingrayRuleCatalogService() {
        return stingrayRuleCatalogService;
    }

    public CatalogRateBindingStub getStingrayRateCatalogService() {
        return stingrayRateCatalogService;
    }

    public ConfExtraBindingStub getStingrayConfExtraService() {
        return stingrayConfExtraService;
    }
}
