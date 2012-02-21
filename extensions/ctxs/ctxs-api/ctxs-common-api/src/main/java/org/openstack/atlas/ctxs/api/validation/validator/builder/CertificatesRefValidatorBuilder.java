package org.openstack.atlas.ctxs.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.openstack.atlas.api.v1.extensions.ctxs.CertificatesRef;
import org.openstack.atlas.ctxs.api.validation.validator.builder.CertificateRefValidatorBuilder;
import org.openstack.atlas.ctxs.api.validation.validator.CertificateRefValidator;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@Component
@Scope("request")
public class CertificatesRefValidatorBuilder extends ValidatorBuilder<CertificatesRef> {
    protected final int MIN_CERTIFICATES = 1;
    protected final int MAX_CERTIFICATES = 10;

    @Autowired
    public CertificatesRefValidatorBuilder(CertificateRefValidatorBuilder certRefValidatorBuilder) {
        super(CertificatesRef.class);

        // POST EXPECTATIONS
        result(validationTarget().getCertificates()).must().haveSizeOfAtLeast(MIN_CERTIFICATES).forContext(POST).withMessage(String.format("Must provide at least %d certificate(s).", MIN_CERTIFICATES));
        result(validationTarget().getCertificates()).must().haveSizeOfAtMost(MAX_CERTIFICATES).forContext(POST).withMessage(String.format("Must not provide more than %d certificates.", MAX_CERTIFICATES));
        result(validationTarget().getCertificates()).if_().exist().then().must().delegateTo(new CertificateRefValidator(certRefValidatorBuilder).getValidator(), POST).forContext(POST);
    }
}
