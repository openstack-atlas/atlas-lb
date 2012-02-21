package org.openstack.atlas.ctxs.service.domain.service;

import java.util.List;
import java.util.Set;

import org.openstack.atlas.ctxs.service.domain.entity.Certificate;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;

import java.util.List;
import java.util.Set;

public interface CertificateService {

    Certificate createCertificate(Certificate certificate, org.openstack.atlas.api.v1.extensions.ctxs.Certificate apiCert) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;
    String delete(Integer accountId, Integer id) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException;
    Certificate getCertificate(Integer accountId, Integer id) throws EntityNotFoundException, UnprocessableEntityException;
}
