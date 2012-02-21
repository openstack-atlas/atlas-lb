package org.openstack.atlas.ctxs.service.domain.helper;

import org.openstack.atlas.ctxs.service.domain.entity.CertificateRef;

import java.util.Set;

public class ExtensionConverter {

        public static org.openstack.atlas.api.v1.extensions.ctxs.CertificatesRef convertCertificatesRef(Set<CertificateRef> dbCertificateSet,  org.openstack.atlas.api.v1.extensions.ctxs.CertificatesRef dataModelCertificatesRef) {
        for (CertificateRef dbCertificateRef : dbCertificateSet) {
           org.openstack.atlas.api.v1.extensions.ctxs.CertificateRef certificateRef = new org.openstack.atlas.api.v1.extensions.ctxs.CertificateRef();
            certificateRef.setIdRef(dbCertificateRef.getIdRef());
            dataModelCertificatesRef.getCertificates().add(certificateRef);
        }
        return dataModelCertificatesRef;
    }
}
