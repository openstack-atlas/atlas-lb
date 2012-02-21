package org.openstack.atlas.ctxs.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.common.crypto.CryptoUtil;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.common.crypto.exception.EncryptException;
import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.ctxs.service.domain.entity.LinkCertificate;
import org.openstack.atlas.ctxs.service.domain.repository.CertificateRepository;
import org.openstack.atlas.ctxs.service.domain.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.openstack.atlas.service.domain.exception.*;
import org.springframework.stereotype.Service;


@Service
public class CertificateServiceImpl implements CertificateService{
    public static Log LOG = LogFactory.getLog(CertificateServiceImpl.class.getName());
    @Autowired
    protected CertificateRepository certificateRepository;

    @Override
    public Certificate createCertificate(Certificate certificate, org.openstack.atlas.api.v1.extensions.ctxs.Certificate apiCert) throws  EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        try {
            if(certificate.getKcontent() != null)
                certificate.setKcontent(CryptoUtil.encrypt(certificate.getKcontent()));
        } catch (EncryptException e) {
            e.printStackTrace();
            throw new BadRequestException("Could not encrypt the certificates key content");
        }
        for(LinkCertificate linkCertificate : certificate.getLcertificates())
        {
            linkCertificate.setCertificate(certificate);
        }
        certificate.setStatus("BUILD");
        Certificate dbcert= certificateRepository.create(certificate);
        return dbcert;
    }

    @Override
    public String delete(Integer accountId, Integer id) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {

        Certificate dbcert = null;
        try {
            dbcert = certificateRepository.getByIdAndAccountId(id, accountId);
        } catch (EntityNotFoundException e) {
            throw new BadRequestException(String.format("No Certificate with id '%d' found.", id));
        }

        if (dbcert.getStatus().equals("ERROR")) {
            certificateRepository.delete(accountId, id);
            return "COMPLETE_DELETE";
        }

        if (dbcert.getStatus().equals("ACTIVE")) {
            if(certificateRepository.isUsed(id))
                throw new BadRequestException(String.format("Certificate '%d' is in use, and cannot be deleted", id));
            certificateRepository.changeStatus(accountId, id, "PENDING_DELETE");
            return "PENDING_DELETE";
        }
        else // Reaches here in transition states like PENDING_DELETE or BUILD
        {
            LOG.warn(String.format("Certificate '%d' has a status of '%s' and is considered immutable.", id, dbcert.getStatus()));
            throw new BadRequestException(String.format("Certificate '%d' has a status of '%s' and is considered immutable.", id, dbcert.getStatus()));
        }
    }

    @Override
    public Certificate getCertificate(Integer id, Integer accountId) throws EntityNotFoundException, UnprocessableEntityException {
        Certificate dbcert = null;
        dbcert = certificateRepository.getByIdAndAccountId(id, accountId, true);
        try
        {
            dbcert.setKcontent(CryptoUtil.decrypt(dbcert.getKcontent()));
        } catch (DecryptException e) {
            e.printStackTrace();
            throw new UnprocessableEntityException(String.format("Unable to retrieve certificate with id '%d'.", id));
        }

        if(dbcert.getLcertificates() != null)
            dbcert.getLcertificates().size();

        return dbcert;
    }
}
