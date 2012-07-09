package org.openstack.atlas.rax.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.jobs.usage.*;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openstack.atlas.jobs.batch.BatchExecutor.executeInBatches;

@Primary
@Component
public class RaxLoadBalancerUsagePollerWorker extends LoadBalancerUsagePollerWorker {
    private final Log LOG = LogFactory.getLog(RaxLoadBalancerUsagePollerWorker.class);


    public void execute() {
        try {

            List<LoadBalancer> loadBalancersForHost = loadBalancerRepository.getLoadBalancersWithStatus(CoreLoadBalancerStatus.ACTIVE);

            RaxUsageCollector usageCollector = new RaxUsageCollector(usageAdapter);
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageCollector);

            RaxUsageProcessor usageProcessor = new RaxUsageProcessor(usageRepository, usageCollector.getBytesInMap(), usageCollector.getBytesOutMap(), usageCollector.getCurrentConnectionMap());
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageProcessor);

            UsageInsert usageInsert = new UsageInsert(usageRepository);
            executeInBatches(usageProcessor.getRecordsToInsert(), BATCH_SIZE, usageInsert);

            UsageUpdate usageUpdate = new UsageUpdate(usageRepository);
            executeInBatches(usageProcessor.getRecordsToUpdate(), BATCH_SIZE, usageUpdate);

        } catch (DecryptException de) {
            LOG.error("Error decrypting configuration", de);
        } catch (Exception e) {
            LOG.error("Exception caught", e);
            e.printStackTrace();
        }
    }
}
