/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ejb.eb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "MAJUGURCI_DNEVNIK")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MajugurciDnevnik.findAll", query = "SELECT m FROM MajugurciDnevnik m"),
    @NamedQuery(name = "MajugurciDnevnik.findByIdDnevnik", query = "SELECT m FROM MajugurciDnevnik m WHERE m.idDnevnik = :idDnevnik"),
    @NamedQuery(name = "MajugurciDnevnik.findByUrl", query = "SELECT m FROM MajugurciDnevnik m WHERE m.url = :url"),
    @NamedQuery(name = "MajugurciDnevnik.findByIpadresa", query = "SELECT m FROM MajugurciDnevnik m WHERE m.ipadresa = :ipadresa"),
    @NamedQuery(name = "MajugurciDnevnik.findByVrijeme", query = "SELECT m FROM MajugurciDnevnik m WHERE m.vrijeme = :vrijeme"),
    @NamedQuery(name = "MajugurciDnevnik.findByTrajanje", query = "SELECT m FROM MajugurciDnevnik m WHERE m.trajanje = :trajanje"),
    @NamedQuery(name = "MajugurciDnevnik.findByStatus", query = "SELECT m FROM MajugurciDnevnik m WHERE m.status = :status"),
    @NamedQuery(name = "MajugurciDnevnik.findByOpis", query = "SELECT m FROM MajugurciDnevnik m WHERE m.opis = :opis")})
public class MajugurciDnevnik implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID_DNEVNIK")
    private Integer idDnevnik;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "URL")
    private String url;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "IPADRESA")
    private String ipadresa;
    @Basic(optional = false)
    @NotNull
    @Column(name = "VRIJEME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vrijeme;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TRAJANJE")
    private int trajanje;
    @Basic(optional = false)
    @NotNull
    @Column(name = "STATUS")
    private int status;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "OPIS")
    private String opis;
    @JoinColumn(name = "KORISNIK", referencedColumnName = "ID_KORISNIK")
    @ManyToOne(optional = false)
    private MajugurciKorisnici korisnik;

    public MajugurciDnevnik() {
    }

    public MajugurciDnevnik(Integer idDnevnik) {
        this.idDnevnik = idDnevnik;
    }

    public MajugurciDnevnik(Integer idDnevnik, String url, String ipadresa, Date vrijeme, int trajanje, int status, String opis) {
        this.idDnevnik = idDnevnik;
        this.url = url;
        this.ipadresa = ipadresa;
        this.vrijeme = vrijeme;
        this.trajanje = trajanje;
        this.status = status;
        this.opis = opis;
    }

    public Integer getIdDnevnik() {
        return idDnevnik;
    }

    public void setIdDnevnik(Integer idDnevnik) {
        this.idDnevnik = idDnevnik;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpadresa() {
        return ipadresa;
    }

    public void setIpadresa(String ipadresa) {
        this.ipadresa = ipadresa;
    }

    public Date getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(Date vrijeme) {
        this.vrijeme = vrijeme;
    }

    public int getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(int trajanje) {
        this.trajanje = trajanje;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public MajugurciKorisnici getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(MajugurciKorisnici korisnik) {
        this.korisnik = korisnik;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDnevnik != null ? idDnevnik.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MajugurciDnevnik)) {
            return false;
        }
        MajugurciDnevnik other = (MajugurciDnevnik) object;
        if ((this.idDnevnik == null && other.idDnevnik != null) || (this.idDnevnik != null && !this.idDnevnik.equals(other.idDnevnik))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.foi.nwtis.majugurci.ejb.eb.MajugurciDnevnik[ idDnevnik=" + idDnevnik + " ]";
    }
    
}
