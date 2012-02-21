package org.openstack.atlas.ctxs.service.domain.entity;

import java.io.Serializable;

public enum CertificateFormat implements Serializable{
    PEM, DER;
    private final static long serialVersionUID = 999999919L;
}
