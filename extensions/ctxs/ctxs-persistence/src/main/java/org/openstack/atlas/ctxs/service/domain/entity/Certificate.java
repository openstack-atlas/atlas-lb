package org.openstack.atlas.ctxs.service.domain.entity;

import org.openstack.atlas.service.domain.entity.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="vendor",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("CTXS")
@Table(name = "certificate")
public class Certificate extends org.openstack.atlas.service.domain.entity.Entity implements Serializable {
    private final static long serialVersionUID = 512512324L;

    @Column(name = "account_id", nullable = false, length = 32)
    private Integer accountId;

    @Column(name = "name", length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "encoding")
    private CertificateEncodingType certificateEncodingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private CertificateFormat certificateFormat;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "subject")
    private String subject;

    @Column(name = "Issuer")
    private String Issuer;

    @Column(name = "notBefore")
    private String notBefore;

    @Column(name = "notAfter")
    private String notAfter;

    @Column(name = "version")
    private String version;

    @Column(name = "serialVersion")
    private String serialVersion;

    @Column(name = "signatureAlgorithm")
    private String signatureAlgorithm;

    @Column(name = "publicKeyAlgorithm")
    private String publicKeyAlgorithm;

    @Column(name = "publicKeySize")
    private String publicKeySize;

    @Column(name = "certificatestatus")
    private String certificatestatus;

    @Lob
    @Column(name = "certificateContent")
    private String ccontent;

    @Lob
    @Column(name = "keyContent")
    private String kcontent;

    @OrderBy("id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "certificate", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LinkCertificate> lcertificates = new HashSet<LinkCertificate>();


    public String getSubject() {
        return subject;
    }

    public String getCcontent() {
        return ccontent;
    }

    public void setCcontent(String certificatecontent) {
        this.ccontent = certificatecontent;
    }

    public String getKcontent() {
        return kcontent;
    }

    public void setKcontent(String keycontent) {
        this.kcontent = keycontent;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return Issuer;
    }

    public void setIssuer(String issuer) {
        Issuer = issuer;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSerialVersion() {
        return serialVersion;
    }

    public void setSerialVersion(String serialVersion) {
        this.serialVersion = serialVersion;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }

    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }

    public String getPublicKeySize() {
        return publicKeySize;
    }

    public void setPublicKeySize(String publicKeySize) {
        this.publicKeySize = publicKeySize;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CertificateEncodingType getCertificateEncodingType() {
        return certificateEncodingType;
    }

    public void setCertificateEncodingType(CertificateEncodingType encoding) {
        this.certificateEncodingType = encoding;
    }

    public CertificateFormat getCertificateFormat() {
        return certificateFormat;
    }

    public void setCertificateFormat(CertificateFormat format) {
        this.certificateFormat = format;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<LinkCertificate> getLcertificates() {
        return lcertificates;
    }

    public void setLcertificates(Set<LinkCertificate> lcertificates) {
        this.lcertificates = lcertificates;
    }

    public String getCertificatestatus() {
        return certificatestatus;
    }

    public void setCertificatestatus(String certificatestatus) {
        this.certificatestatus = certificatestatus;
    }

}
