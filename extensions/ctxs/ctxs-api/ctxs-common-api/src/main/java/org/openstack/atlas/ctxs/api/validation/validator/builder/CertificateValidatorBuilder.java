package org.openstack.atlas.ctxs.api.validation.validator.builder;

import org.openstack.atlas.api.v1.extensions.ctxs.LinkCertificateDef;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeEmptyOrNull;
import org.openstack.atlas.api.validation.verifier.MustBeInArray;

import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.openstack.atlas.api.v1.extensions.ctxs.Certificate;

import java.util.List;

@Component
@Scope("request")
public class CertificateValidatorBuilder extends ValidatorBuilder<Certificate> {

    public CertificateValidatorBuilder() {
        super(Certificate.class);

        result(validationTarget().getKeycontent()).must().exist().withMessage("Certificate keycontent field must be specified to designate the certificate to use.");
        result(validationTarget().getCertificatecontent()).must().exist().withMessage("Certificate certificatecontent field must be specified to designate the certificate to use.");
        result(validationTarget().getLinkcertificates()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<List<LinkCertificateDef>>() {
            @Override
            public VerifierResult verify(List<LinkCertificateDef> linkCertificates) {
                for(LinkCertificateDef linkCertificate: linkCertificates)
                {
                    if(linkCertificate.getCertificatecontent() == null || linkCertificate.getCertificatecontent().trim().length()==0)
                        return new VerifierResult(false);
                }
                return new VerifierResult(true);
            }
        }).withMessage("linkcertificate's cerfificatecontent field must be specified");
    }
}
