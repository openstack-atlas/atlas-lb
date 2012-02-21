package org.openstack.atlas.ctxs.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeInArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.openstack.atlas.api.v1.extensions.ctxs.CertificateRef;

@Component
@Scope("request")
public class CertificateRefValidatorBuilder extends ValidatorBuilder<CertificateRef> {

    public CertificateRefValidatorBuilder() {
        super(CertificateRef.class);

        result(validationTarget().getIdRef()).must().exist().withMessage("Certificate idRef field must be specified to designate the certificate to use.");
    }
}
