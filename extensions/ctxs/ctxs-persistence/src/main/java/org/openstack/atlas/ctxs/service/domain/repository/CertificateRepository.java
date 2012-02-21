package org.openstack.atlas.ctxs.service.domain.repository;

import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.service.domain.exception.*;
import java.util.List;
import java.util.Set;

public interface CertificateRepository {

        Certificate getById(Integer id) throws EntityNotFoundException;

        List<Certificate> getByAccountId(Integer accountId);

        Certificate getByIdAndAccountId(Integer id, Integer accountId, Boolean... fetchLinkCertificates) throws EntityNotFoundException;

        Certificate create(Certificate certificate);

        Certificate update(Certificate certificate);

        void delete(Integer accountId, Integer id) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException;

        void changeStatus(Integer accountId, Integer id, String newStatus) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException;

        boolean isUsed(Integer id) throws EntityNotFoundException;
}
