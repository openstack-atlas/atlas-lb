package org.openstack.atlas.ctxs.api.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.v1.extensions.ctxs.Certificates;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.ctxs.service.domain.entity.LinkCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.openstack.atlas.api.async.BaseListener;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.service.domain.repository.CertificateRepository;
import org.openstack.atlas.ctxs.service.domain.service.CertificateService;
import org.springframework.stereotype.Component;
import org.openstack.atlas.ctxs.service.domain.pojo.CtxsMessageDataContainer;
import org.openstack.atlas.ctxs.api.integration.CtxsReverseProxyLoadBalancerService;

@Component
public class CreateCertificateListener  extends BaseListener {

    private final Log LOG = LogFactory.getLog(CreateCertificateListener.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateRepository certificateRepository;

    @Override
    public void doOnMessage(final Message message) throws Exception {

        LOG.debug(message);
        CtxsMessageDataContainer dataContainer = (CtxsMessageDataContainer) getDataContainerFromMessage(message);

        HashMap<String, Object> queueData = dataContainer.getHashData();

        List<Certificate> dbCertsKeys = ( List<Certificate> ) queueData.get("Certificates");
        List<Certificate> dbcerts = new ArrayList<Certificate>();
        for(Certificate dbCertKeys :  dbCertsKeys)
        {
            Certificate dbcert = certificateService.getCertificate(dbCertKeys.getId(), dbCertKeys.getAccountId());
            dbcerts.add(dbcert);
        }

        boolean berror = false;  // success condition
        List<Certificate> cloudCertificates = null;
        try
        {
            LOG.info(String.format("Creating '%d' certificates.", dbcerts.size()));
            cloudCertificates = ((CtxsReverseProxyLoadBalancerService) reverseProxyLoadBalancerService).createCertificates(dbcerts);
            LOG.info(String.format("Successfully created '%d' certificates.", dbcerts.size()));

        } catch (Exception e) {
            berror = true;
            String alertDescription = String.format("An error occurred while creating certificates via adapter.");
            LOG.error(alertDescription, e);
        }

        for (int i =0; i < dbcerts.size(); i++)
        {
            boolean bGetError=false;
            Certificate dbcert = dbcerts.get(i);
            if(!berror)
            {
                try {
                    Certificate temp_dbcert = cloudCertificates.get(i);
                    // temp_dbcert collected created from GET call of helios does not contain certain values writing them
                    temp_dbcert.setAccountId(dbcert.getAccountId());
                    temp_dbcert.setKcontent(CryptoUtil.encrypt(dbcert.getKcontent()));
                    temp_dbcert.setCcontent(dbcert.getCcontent());
                    temp_dbcert.setLcertificates(dbcert.getLcertificates());
                    dbcert = temp_dbcert;
                }
                catch(Exception ex) {
                    bGetError = true;
                }
            }
            dbcert.setStatus((berror || bGetError)? "ERROR": "ACTIVE");
            dbcert = certificateRepository.update(dbcert);
        }
    }
}
