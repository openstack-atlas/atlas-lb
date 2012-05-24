package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openstack.atlas.jobs.batch.BatchExecutor.executeInBatches;

/*
 *  Thread per host machine
 */
@Component
public class LoadBalancerUsagePollerWorker {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePollerWorker.class);
    protected final int BATCH_SIZE = 100; // TODO: Externalize

    protected UsageAdapter usageAdapter;
    protected UsageRepository usageRepository;
    protected LoadBalancerRepository loadBalancerRepository;


    public LoadBalancerUsagePollerWorker() {
        super();
    }

    public LoadBalancerUsagePollerWorker(LoadBalancerRepository loadBalancerRepository, UsageAdapter usageAdapter, UsageRepository usageRepository) {

        this.loadBalancerRepository = loadBalancerRepository;
        this.usageAdapter = usageAdapter;
        this.usageRepository = usageRepository;
    }


    public void execute() {
        try {

            List<LoadBalancer> loadBalancersForHost = loadBalancerRepository.getLoadBalancersWithStatus(CoreLoadBalancerStatus.ACTIVE);

            UsageCollector usageCollector = new UsageCollector(usageAdapter);
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageCollector);

            UsageProcessor usageProcessor = new UsageProcessor(usageRepository, usageCollector.getBytesInMap(), usageCollector.getBytesOutMap());
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageProcessor);

            UsageInsert usageInsert = new UsageInsert(usageRepository);
            executeInBatches(usageProcessor.getRecordsToInsert(), BATCH_SIZE, usageInsert);

            UsageUpdate usageUpdate = new UsageUpdate(usageRepository);
            executeInBatches(usageProcessor.getRecordsToUpdate(), BATCH_SIZE, usageUpdate);

        } catch (DecryptException de) {
            LOG.error("Error decrypting configuration for", de);
        } catch (Exception e) {
            LOG.error("Exception caught", e);
            e.printStackTrace();
        }
    }

    public void setUsageAdapter(UsageAdapter usageAdapter) {
        this.usageAdapter = usageAdapter;
    }

    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

}
