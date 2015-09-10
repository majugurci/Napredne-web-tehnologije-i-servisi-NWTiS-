/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ejb.eb;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "MAJUGURCI_GRUPE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MajugurciGrupe.findAll", query = "SELECT m FROM MajugurciGrupe m"),
    @NamedQuery(name = "MajugurciGrupe.findByIdGrupa", query = "SELECT m FROM MajugurciGrupe m WHERE m.idGrupa = :idGrupa"),
    @NamedQuery(name = "MajugurciGrupe.findByOpis", query = "SELECT m FROM MajugurciGrupe m WHERE m.opis = :opis")})
public class MajugurciGrupe implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID_GRUPA")
    private Integer idGrupa;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    @Column(name = "OPIS")
    private String opis;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "grupa")
    private List<MajugurciKorisnici> majugurciKorisniciList;

    public MajugurciGrupe() {
    }

    public MajugurciGrupe(Integer idGrupa) {
        this.idGrupa = idGrupa;
    }

    public MajugurciGrupe(Integer idGrupa, String opis) {
        this.idGrupa = idGrupa;
        this.opis = opis;
    }

    public Integer getIdGrupa() {
        return idGrupa;
    }

    public void setIdGrupa(Integer idGrupa) {
        this.idGrupa = idGrupa;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    @XmlTransient
    public List<MajugurciKorisnici> getMajugurciKorisniciList() {
        return majugurciKorisniciList;
    }

    public void setMajugurciKorisniciList(List<MajugurciKorisnici> majugurciKorisniciList) {
        this.majugurciKorisniciList = majugurciKorisniciList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGrupa != null ? idGrupa.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MajugurciGrupe)) {
            return false;
        }
        MajugurciGrupe other = (MajugurciGrupe) object;
        if ((this.idGrupa == null && other.idGrupa != null) || (this.idGrupa != null && !this.idGrupa.equals(other.idGrupa))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.foi.nwtis.majugurci.ejb.eb.MajugurciGrupe[ idGrupa=" + idGrupa + " ]";
    }
    
}
