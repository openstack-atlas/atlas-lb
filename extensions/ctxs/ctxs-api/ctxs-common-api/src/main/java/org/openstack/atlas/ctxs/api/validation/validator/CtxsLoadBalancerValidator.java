package org.openstack.atlas.ctxs.api.validation.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.ctxs.service.domain.entity.CertificateRef;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.api.validation.validator.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifier.IsInstanceOf;
import org.openstack.atlas.ctxs.api.validation.validator.builder.CertificateRefValidatorBuilder;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class CtxsLoadBalancerValidator implements ResourceValidator<Object> {

    public static Log LOG = LogFactory.getLog(CtxsLoadBalancerValidator.class.getName());

    private final Validator<Object> validator;

    public CtxsLoadBalancerValidator() {
        validator = build(new ValidatorBuilder<Object>(Object.class) {
            {
                LOG.info("inside log + CtxsLoadBalancerValidator constructor");
                // POST EXPECTATIONS
                if_().adhereTo(new IsInstanceOf(CertificateRef.class)).then().must().delegateTo(new CertificateRefValidator(new CertificateRefValidatorBuilder()).getValidator(), POST).forContext(POST);
            }
        });
    }

    @Override
    public ValidatorResult validate(Object object, Object type) {
        ValidatorResult result = validator.validate(object, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Object> getValidator() {
        return validator;
    }
}
