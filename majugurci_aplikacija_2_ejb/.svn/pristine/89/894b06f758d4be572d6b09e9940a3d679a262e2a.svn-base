/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ejb.sb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.majugurci.ejb.eb.MajugurciDnevnik;

/**
 *
 * @author Mario
 */
@Stateless
public class MajugurciDnevnikFacade extends AbstractFacade<MajugurciDnevnik> {

    @PersistenceContext(unitName = "majugurci_aplikacija_2_ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MajugurciDnevnikFacade() {
        super(MajugurciDnevnik.class);
    }

}
