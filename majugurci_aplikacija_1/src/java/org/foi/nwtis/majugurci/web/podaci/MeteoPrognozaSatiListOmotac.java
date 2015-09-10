/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.podaci;

import java.util.List;

/**
 *
 * @author Mario
 */
public class MeteoPrognozaSatiListOmotac {
    
    private String poruka = null;
    
    private List<MeteoPrognozaSati> meteoPrognozaSati = null;

    public MeteoPrognozaSatiListOmotac() {
    }

    public List<MeteoPrognozaSati> getMeteoPrognozaSati() {
        return meteoPrognozaSati;
    }

    public void setMeteoPrognozaSati(List<MeteoPrognozaSati> meteoPrognozaSati) {
        this.meteoPrognozaSati = meteoPrognozaSati;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }
    
    
    
}
