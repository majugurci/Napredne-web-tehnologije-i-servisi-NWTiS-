/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ws.klijenti;

import org.foi.nwtis.majugurci.ws.serveri.AdreseKorisnikaOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognozaListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoPrognozaSatiListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.MeteoStaniceListOmotac;
import org.foi.nwtis.majugurci.ws.serveri.RangListaOmotac;

/**
 *
 * @author Mario
 */
public class MeteoPodaciWSKlijent {

    public static AdreseKorisnikaOmotac dajAdreseKorisnika(java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajAdreseKorisnika(korisnik, lozinka);
    }

    public static MeteoPodaciListOmotac dajMeteoPodatkeUDatumskomIntervalu(java.lang.String adresa, java.lang.String datumOd, java.lang.String datumDo, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajMeteoPodatkeUDatumskomIntervalu(adresa, datumOd, datumDo, korisnik, lozinka);
    }

    public static MeteoPodaciListOmotac dajPosljednjihNMeteoPodataka(java.lang.String adresa, int brojZapisa, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajPosljednjihNMeteoPodataka(adresa, brojZapisa, korisnik, lozinka);
    }

    public static MeteoPrognozaListOmotac dajPrognozuZaBrojDana(java.lang.String adresa, int brojDana, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajPrognozuZaBrojDana(adresa, brojDana, korisnik, lozinka);
    }

    public static MeteoPrognozaSatiListOmotac dajPrognozuZaBrojSati(java.lang.String adresa, int brojSati, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajPrognozuZaBrojSati(adresa, brojSati, korisnik, lozinka);
    }

    public static RangListaOmotac dajRangListu(int brojAdresa, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajRangListu(brojAdresa, korisnik, lozinka);
    }

    public static AdreseKorisnikaOmotac dajSveAdrese(java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajSveAdrese(korisnik, lozinka);
    }

    public static MeteoPodaciOmotac dajTrenutneMeteoPodatkeZaAdresu(java.lang.String adresa, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajTrenutneMeteoPodatkeZaAdresu(adresa, korisnik, lozinka);
    }

    public static MeteoStaniceListOmotac dajVremenskeStaniceUBliziniAdrese(java.lang.String adresa, int brojStanica, java.lang.String korisnik, java.lang.String lozinka) {
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service service = new org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS_Service();
        org.foi.nwtis.majugurci.ws.serveri.MeteoPodaciWS port = service.getMeteoPodaciWSPort();
        return port.dajVremenskeStaniceUBliziniAdrese(adresa, brojStanica, korisnik, lozinka);
    }
    
    
}
