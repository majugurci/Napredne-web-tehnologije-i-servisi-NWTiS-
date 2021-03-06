/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ejb.sb;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciKorisnici;

/**
 *
 * @author Mario
 */
@Stateless
public class MajugurciKorisniciFacade extends AbstractFacade<MajugurciKorisnici> {

    @PersistenceContext(unitName = "majugurci_aplikacija_2_ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MajugurciKorisniciFacade() {
        super(MajugurciKorisnici.class);
    }

    /**
     * Daje korisnika prema korisničkom imenu
     * @param zaKorisnika korisničko ime
     * @return objekt MajugurciKorisnici
     */
    public List<MajugurciKorisnici> findByKorisnik(String zaKorisnika) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<MajugurciKorisnici> korisnik = cq.from(MajugurciKorisnici.class);
        Expression<String> premaKorisniku = korisnik.get("korisnickoIme");
        cq.where(cb.and(cb.equal(premaKorisniku, zaKorisnika)));
        return getEntityManager().createQuery(cq).getResultList();
    }
    
    /**
     * Metoda za autentikaciju korisnika
     * Pretražuje tablicu u BP za imenom korisnika te validira lozinku
     * Ako je sve dobro vraća korisnički objekt, inače vraća null
     * @param korisnickoIme
     * @param lozinka
     * @return objekt MajugurciKorisnici ako je korisnik validiran, inače null
     */
    public MajugurciKorisnici authenticateUser(String korisnickoIme, String lozinka) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        
        Root<MajugurciKorisnici> majugurciKorisnici = cq.from(MajugurciKorisnici.class);
        cq.select(majugurciKorisnici);
        
        Predicate korIme = cb.equal(majugurciKorisnici.get("korisnickoIme"), korisnickoIme);
        Predicate loz = cb.equal(majugurciKorisnici.get("lozinka"), lozinka);
        
        Predicate pAnd = cb.and(korIme, loz);
        
        cq.where(pAnd);
        
        Query q = em.createQuery(cq);
        List<MajugurciKorisnici> korisnici = q.getResultList();
        
        if (korisnici.size() == 1) {
            return korisnici.get(0);
        } else {
            return null;
        }
    }
}
