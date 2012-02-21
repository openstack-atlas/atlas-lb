package org.openstack.atlas.ctxs.service.domain.entity;

import org.openstack.atlas.service.domain.entity.*;

import javax.persistence.*;
import java.io.Serializable;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "linkcertificate")
public class LinkCertificate extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 512512324L;

    @Enumerated(EnumType.STRING)
    @Column(name = "encoding")
    private CertificateEncodingType encoding;

    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private CertificateFormat format;

    @Lob
    @Column(name = "certificateContent")
    private String certificatecontent;

    @ManyToOne
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    public CertificateEncodingType getEncoding() {
        return encoding;
    }

    public void setEncoding(CertificateEncodingType encoding) {
        this.encoding = encoding;
    }

    public CertificateFormat getFormat() {
        return format;
    }

    public void setFormat(CertificateFormat format) {
        this.format = format;
    }

    public String getCertificatecontent() {
        return certificatecontent;
    }

    public void setCertificatecontent(String certificateContent) {
        this.certificatecontent = certificateContent;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

}
