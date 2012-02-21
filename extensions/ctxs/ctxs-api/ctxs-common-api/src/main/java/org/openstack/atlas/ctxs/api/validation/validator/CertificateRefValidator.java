package org.openstack.atlas.ctxs.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.api.validation.validator.ValidatorUtilities;

import org.openstack.atlas.ctxs.api.validation.validator.builder.CertificateRefValidatorBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import org.openstack.atlas.api.v1.extensions.ctxs.CertificateRef;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class CertificateRefValidator implements ResourceValidator<CertificateRef> {
    private Validator<CertificateRef> validator;
    private CertificateRefValidatorBuilder ruleBuilder;

    @Autowired
    public CertificateRefValidator(CertificateRefValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(CertificateRef certRef, Object type) {
        ValidatorResult result = validator.validate(certRef, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<CertificateRef> getValidator() {
        return validator;
    }
}
