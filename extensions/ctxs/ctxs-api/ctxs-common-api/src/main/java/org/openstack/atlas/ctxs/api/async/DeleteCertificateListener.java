package org.openstack.atlas.ctxs.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import javax.jms.Message;
import java.util.HashMap;
import java.util.List;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.service.domain.repository.CertificateRepository;
import org.openstack.atlas.ctxs.service.domain.service.CertificateService;
import org.springframework.stereotype.Component;
import org.openstack.atlas.ctxs.api.integration.CtxsReverseProxyLoadBalancerService;
import org.openstack.atlas.ctxs.service.domain.pojo.CtxsMessageDataContainer;

@Component
public class DeleteCertificateListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(DeleteCertificateListener.class);

    @Autowired
    private CertificateRepository certificateRepository;

    @Override
    public void doOnMessage(final Message message) throws Exception {

        LOG.debug(message);
        CtxsMessageDataContainer dataContainer = (CtxsMessageDataContainer) getDataContainerFromMessage(message);

        HashMap<String, Object> queueData = dataContainer.getHashData();

        Certificate dbcert = (Certificate) queueData.get("Certificate");
        try
        {
            LOG.info(String.format("Deleting certificate %d.", dbcert.getId()));
            ((CtxsReverseProxyLoadBalancerService)reverseProxyLoadBalancerService).deleteCertificate(dbcert);

        } catch (Exception e) {
            dbcert.setStatus("DELETE_ERROR");
            dbcert = certificateRepository.update(dbcert);
            String alertDescription = String.format("An error occurred while deleting certificate %d via adapter.", dbcert.getId());
            LOG.error(alertDescription, e);
            return;
        }

        // Deleting the certificate from repository. Is it good to keep it in the repository?
        certificateRepository.delete(dbcert.getAccountId(), dbcert.getId());
        LOG.info(String.format("Successfully deleted certificate %d.", dbcert.getId()));
    }
}
