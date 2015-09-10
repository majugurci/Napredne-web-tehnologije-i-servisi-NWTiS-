/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.majugurci.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.majugurci.rest.klijenti.GMKlijent;
import org.foi.nwtis.majugurci.rest.klijenti.OWMKlijent;
import org.foi.nwtis.majugurci.socketServer.ServerObradaDretva;
import org.foi.nwtis.majugurci.web.podaci.AdreseKorisnikaOmotac;
import org.foi.nwtis.majugurci.web.podaci.Lokacija;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaci;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaciListOmotac;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaciOmotac;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognoza;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognozaListOmotac;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognozaSati;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognozaSatiListOmotac;
import org.foi.nwtis.majugurci.web.podaci.MeteoStanice;
import org.foi.nwtis.majugurci.web.podaci.MeteoStaniceListOmotac;
import org.foi.nwtis.majugurci.web.podaci.RangListaOmotac;

/**
 *
 * @author Mario
 * 
 * SOAP implementacija koja pruža razne usluge vezane uz vremensku prognozu.
 */
@WebService(serviceName = "MeteoPodaciWS")
public class MeteoPodaciWS {

    private BP_Konfiguracija konfigBP = null;
    private Konfiguracija konfigRazno = null;
    private Konfiguracija konfigCjenik = null;

    //podaci za spajanje na bazu podataka
    private String server;
    private String baza;
    private String korisnik;
    private String lozinkaBP;
    private String driver;

    @Resource
    private WebServiceContext context;

    /**
     * Metoda koja samo jednom preuzima konfiguracijske datoteke i sprema ih u
     * globalne varijable
     *
     * Prouciti zasto ne mogu to napraviti u konstruktoru ili metodi s
     *
     * @postConstruct anotacijom
     */
    private void preuzmiKonfiguracije() {
        if (konfigBP == null || konfigRazno == null) {
            ServletContext servletContext
                    = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);

            konfigBP = (BP_Konfiguracija) servletContext.getAttribute("konfigBP");
            konfigRazno = (Konfiguracija) servletContext.getAttribute("konfigRazno");

            server = konfigBP.getServer_database();
            baza = server + konfigBP.getUser_database();
            korisnik = konfigBP.getUser_username();
            lozinkaBP = konfigBP.getUser_password();
            driver = konfigBP.getDriver_database(server);

            //preuzmi cjenik
            String putanjaCjenika = (String) servletContext.getAttribute("konfigCjenikPutanja");
            try {
                konfigCjenik = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanjaCjenika);
            } catch (Exception ex) {
                //ne postoji datoteka cjenika, svi zatjevi su besplatni
                System.out.println("Cjenik ne postoji");
            }

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Web service operation Daje trenutno važeće meteo podatke za prosljeđenu
     * adresu
     *
     * @param adresa naziv adrese
     * @param korisnik korisnik koji trazi zahtjev
     * @param lozinka lozinka korisnika
     * @return objekt MeteoPodaciOmotac
     */
    @WebMethod(operationName = "dajTrenutneMeteoPodatkeZaAdresu")
    public MeteoPodaciOmotac dajTrenutneMeteoPodatkeZaAdresu(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoPodaciOmotac mpo = new MeteoPodaciOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mpo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mpo;
        } else {
            if (adresa == null) {
                mpo.setPoruka("Morate unijeti adresu");
                return mpo;
            }
            if (adresa.trim().equals("")) {
                mpo.setPoruka("Morate unijeti adresu");
                return mpo;
            }

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "trenutniMeteoPodaci";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            // ako je admin uvijek se izvrsava if, nikad else
            if (imaSredstva) {
                GMKlijent gmk = new GMKlijent();
                Lokacija l = gmk.getGeoLocation(adresa);

                if (l == null) {
                    mpo.setPoruka("Unijeli ste nepostojeću adresu");
                    return mpo;
                }

                OWMKlijent owmk = new OWMKlijent(konfigRazno.dajPostavku("openWeatherMap.key"));

                MeteoPodaci mp = owmk.getRealTimeWeather(l.getLatitude(), l.getLongitude());

                if (mp == null) {
                    mpo.setPoruka("Web servis je trenutno nedostupan");
                    return mpo;
                } else {
                    //sve je proslo dobro, korisniku skini sredstva
                    if (autentikacija == 2 && konfigCjenik != null) {
                        umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                        upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                    }
                    mpo.setMeteoPodaci(mp);
                    return mpo;
                }
            } else {
                mpo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mpo;
            }
        }
    }

    /**
     * Web service operation Daje popis svih adresa za koje se preuzimaju meteo
     * podaci
     *
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return objekt AdreseKorisnikaOmotac
     */
    @WebMethod(operationName = "dajSveAdrese")
    public AdreseKorisnikaOmotac dajSveAdrese(@WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        AdreseKorisnikaOmotac ako = new AdreseKorisnikaOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            ako.setPoruka("Unijeli ste netočne korisničke podatke");
            return ako;
        } else {

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "sveAdrese";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {
                List<String> adrese = new ArrayList<>();

                String sqlUpit = "SELECT adresa "
                        + "FROM majugurci_adrese";

                try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                        PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                    try (ResultSet rs = ps.executeQuery()) {

                        //sve je proslo dobro, korisniku skini sredstva
                        if (autentikacija == 2 && konfigCjenik != null) {
                            umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                            upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                        }

                        boolean imaRezova = false;

                        while (rs.next()) {
                            imaRezova = true;
                            adrese.add(rs.getString("adresa"));
                        }

                        if (imaRezova) {
                            ako.setAdrese(adrese);
                            return ako;
                        } else {
                            ako.setPoruka("Nema adresa za koje se preuzimaju meteo podaci");
                            return ako;
                        }
                    }

                } catch (SQLException ex) {
                    //Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    ako.setPoruka("Dogodila se pogreška pri prustupu bazi podataka");
                    return ako;
                }

            } else {
                ako.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return ako;
            }

        }
    }

    /**
     * Web service operation Daje listu adresa koje je korisnik dodao
     *
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return objekt AdreseKorisnikaOmotac
     */
    @WebMethod(operationName = "dajAdreseKorisnika")
    public AdreseKorisnikaOmotac dajAdreseKorisnika(@WebParam(name = "korisnik") final String korisnik,
            @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        AdreseKorisnikaOmotac ako = new AdreseKorisnikaOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            ako.setPoruka("Unijeli ste netočne korisničke podatke");
            return ako;
        } else {

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "adreseKorisnika";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {
                List<String> adrese = new ArrayList<>();

                String sqlUpit = "SELECT a.adresa, k.korisnicko_ime "
                        + "FROM majugurci_adrese a JOIN majugurci_korisnici k "
                        + "ON a.korisnik = k.id_korisnik WHERE korisnicko_ime = ?";

                try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                        PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                    ps.setString(1, korisnik);

                    try (ResultSet rs = ps.executeQuery()) {

                        //sve je proslo dobro, korisniku skini sredstva
                        if (autentikacija == 2 && konfigCjenik != null) {
                            umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                            upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                        }

                        boolean imaRezova = false;

                        while (rs.next()) {
                            imaRezova = true;
                            adrese.add(rs.getString("adresa"));
                        }

                        if (imaRezova) {
                            ako.setAdrese(adrese);
                            return ako;
                        } else {
                            ako.setPoruka("Niste dodali niti jednu adresu");
                            return ako;
                        }
                    }

                } catch (SQLException ex) {
                    //Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    ako.setPoruka("Dogodila se pogreška pri prustupu bazi podataka");
                    return ako;
                }

            } else {
                ako.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return ako;
            }

        }

    }

    /**
     * Web service operation Daje n adresa za koje je prikupljeno najvise
     * podataka i broj podataka
     *
     * @param brojAdresa
     * @param korisnik korisnik koji trazi zahtjev
     * @param lozinka lozinka korisnika
     * @return objekt RangListaOmotac
     */
    @WebMethod(operationName = "dajRangListu")
    public RangListaOmotac dajRangListuAdresa(@WebParam(name = "brojAdresa") final int brojAdresa,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        RangListaOmotac rlo = new RangListaOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            rlo.setPoruka("Unijeli ste netočne korisničke podatke");
            return rlo;
        } else {

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "rangLista";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {

                List<String[]> adrese = new ArrayList<>();

                String sqlUpit = "SELECT a.adresa, COUNT(*) as broj_ponavljanja "
                        + "FROM majugurci_meteo m JOIN majugurci_adrese a "
                        + "ON a.id_adresa = m.adresa GROUP BY adresa ORDER BY broj_ponavljanja DESC LIMIT ?;";

                try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                        PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                    ps.setInt(1, brojAdresa);

                    try (ResultSet rs = ps.executeQuery()) {

                        //sve je proslo dobro, korisniku skini sredstva
                        if (autentikacija == 2 && konfigCjenik != null) {
                            umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                            upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                        }

                        boolean imaZapisa = false;

                        while (rs.next()) {
                            imaZapisa = true;
                            String[] a = new String[2];
                            a[0] = rs.getString("adresa");
                            a[1] = rs.getString("broj_ponavljanja");
                            adrese.add(a);
                        }

                        if (imaZapisa) {
                            rlo.setAdrese(adrese);
                            return rlo;
                        } else {
                            rlo.setPoruka("Ne postoje meteo zapisi o adresama");
                            return rlo;
                        }
                    }

                } catch (SQLException ex) {
                    //Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    rlo.setPoruka("Dogodila se pogreška pri prustupu bazi podataka");
                    return rlo;
                }

            } else {
                rlo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return rlo;
            }

        }
    }

    /**
     * Web service operation Daje posljednjih n meteo podataka za izabranu
     * adresu
     *
     * @param adresa naziv adrese
     * @param brojZapisa N
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return objekt MeteoPodaciListaOmotac
     */
    public MeteoPodaciListOmotac dajPosljednjihNMeteoPodataka(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "brojZapisa") final int brojZapisa,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoPodaciListOmotac mplo = new MeteoPodaciListOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mplo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mplo;
        } else {

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "posljednjihNMeteoPodataka";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {

                List<MeteoPodaci> meteo = new ArrayList<>();

                String sqlUpit = "SELECT m.* from majugurci_meteo m JOIN majugurci_adrese a "
                        + "ON a.id_adresa = m.adresa WHERE a.adresa = ? "
                        + "ORDER BY id_meteo_podaci DESC LIMIT ?;";

                try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                        PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                    ps.setString(1, adresa);
                    ps.setInt(2, brojZapisa);

                    try (ResultSet rs = ps.executeQuery()) {

                        //sve je proslo dobro, korisniku skini sredstva
                        if (autentikacija == 2 && konfigCjenik != null) {
                            umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                            upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                        }

                        boolean imaRezova = false;

                        while (rs.next()) {
                            imaRezova = true;
                            MeteoPodaci mp = new MeteoPodaci();

                            Timestamp datumSql = rs.getTimestamp("sunrise");
                            java.util.Date datumJava = new java.util.Date(datumSql.getTime());
                            mp.setSunRise(datumJava);

                            datumSql = rs.getTimestamp("sunset");
                            datumJava = new java.util.Date(datumSql.getTime());
                            mp.setSunSet(datumJava);

                            mp.setTemperatureValue(rs.getFloat("temperature_value"));
                            mp.setTemperatureMin(rs.getFloat("temperature_min"));
                            mp.setTemperatureMax(rs.getFloat("temperature_max"));
                            mp.setTemperatureUnit(rs.getString("temperature_unit"));

                            mp.setHumidityValue(rs.getFloat("humidity_value"));
                            mp.setHumidityUnit(rs.getString("humidity_unit"));

                            mp.setPressureValue(rs.getFloat("pressure_value"));
                            mp.setPressureUnit(rs.getString("pressure_unit"));

                            mp.setWindSpeedValue(rs.getFloat("wind_speed_value"));
                            mp.setWindSpeedName(rs.getString("wind_speed_name"));
                            mp.setWindDirectionValue(rs.getFloat("wind_direction_value"));
                            mp.setWindDirectionCode(rs.getString("wind_direction_code"));
                            mp.setWindDirectionName(rs.getString("wind_direction_name"));

                            mp.setCloudsValue(rs.getInt("clouds_value"));
                            mp.setCloudsName(rs.getString("clouds_name"));

                            mp.setVisibility(rs.getString("visibility"));

                            mp.setPrecipitationValue(rs.getFloat("precipitation_value"));
                            mp.setPrecipitationMode(rs.getString("precipitation_mode"));
                            mp.setPrecipitationUnit(rs.getString("precipitation_unit"));

                            mp.setWeatherNumber(rs.getInt("weather_number"));
                            mp.setWeatherValue(rs.getString("weather_value"));
                            mp.setWeatherIcon(rs.getString("weather_icon"));

                            datumSql = rs.getTimestamp("last_update");
                            datumJava = new java.util.Date(datumSql.getTime());
                            mp.setLastUpdate(datumJava);

                            meteo.add(mp);
                        }

                        if (imaRezova) {
                            mplo.setMeteoPodaci(meteo);
                            return mplo;
                        } else {
                            mplo.setPoruka("Ne postoje meteorološki zapisi za izabranu adresu");
                            return mplo;
                        }
                    }

                } catch (SQLException ex) {
                    //Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    mplo.setPoruka("Dogodila se pogreška pri prustupu bazi podataka");
                    return mplo;
                }

            } else {
                mplo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mplo;
            }

        }

    }

    /**
     * Web service operation Daje meteo podatke u vremenskom intervalu od-do
     * datuma
     *
     * @param adresa naziv adrese
     * @param datumOd početni datum
     * @param datumDo krajnji datum
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return Lista meteo podataka
     */
    @WebMethod(operationName = "dajMeteoPodatkeUDatumskomIntervalu")
    public MeteoPodaciListOmotac dajMeteoPodatkeUDatumskomIntervalu(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "datumOd") String datumOd, @WebParam(name = "datumDo") String datumDo,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoPodaciListOmotac mplo = new MeteoPodaciListOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mplo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mplo;
        } else {

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "meteoPodaciUDatumskomIntervalu";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {

                List<MeteoPodaci> meteo = new ArrayList<>();

                String sqlUpit = "SELECT m.* from majugurci_meteo m JOIN majugurci_adrese a "
                        + "ON a.id_adresa = m.adresa WHERE a.adresa = ? "
                        + "AND (vrijeme_spremanja BETWEEN ? AND ? '23:59:59:999') ORDER BY id_meteo_podaci DESC;";

                try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                        PreparedStatement ps = con.prepareStatement(sqlUpit);) {

                    System.out.println("datumOdServis: " + datumOd);
                    System.out.println("datumDoServis: " + datumDo);

                    datumOd += " 00:00:00:000";
                    datumDo += " 23:59:59:999";

                    System.out.println("datumOdServis: " + datumOd);
                    System.out.println("datumDoServis: " + datumDo);

                    LocalDateTime odDatuma;
                    LocalDateTime doDatuma;

                    List<LocalDateTime> datumi = pretvoriStringUDatum(datumOd, datumDo);

                    if (datumi == null) {
                        mplo.setPoruka("Unijeli ste krivi format datuma");
                        return mplo;
                    } else {
                        odDatuma = datumi.get(0);
                        doDatuma = datumi.get(1);
                    }

                    ps.setString(1, adresa);
                    ps.setTimestamp(2, java.sql.Timestamp.valueOf(odDatuma));
                    ps.setTimestamp(3, java.sql.Timestamp.valueOf(doDatuma));

                    try (ResultSet rs = ps.executeQuery()) {

                        //sve je proslo dobro, korisniku skini sredstva
                        if (autentikacija == 2 && konfigCjenik != null) {
                            umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                            upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                        }

                        boolean imaRezova = false;

                        while (rs.next()) {
                            imaRezova = true;
                            MeteoPodaci mp = new MeteoPodaci();

                            Timestamp datumSql = rs.getTimestamp("sunrise");
                            java.util.Date datumJava = new java.util.Date(datumSql.getTime());
                            mp.setSunRise(datumJava);

                            datumSql = rs.getTimestamp("sunset");
                            datumJava = new java.util.Date(datumSql.getTime());
                            mp.setSunSet(datumJava);

                            mp.setTemperatureValue(rs.getFloat("temperature_value"));
                            mp.setTemperatureMin(rs.getFloat("temperature_min"));
                            mp.setTemperatureMax(rs.getFloat("temperature_max"));
                            mp.setTemperatureUnit(rs.getString("temperature_unit"));

                            mp.setHumidityValue(rs.getFloat("humidity_value"));
                            mp.setHumidityUnit(rs.getString("humidity_unit"));

                            mp.setPressureValue(rs.getFloat("pressure_value"));
                            mp.setPressureUnit(rs.getString("pressure_unit"));

                            mp.setWindSpeedValue(rs.getFloat("wind_speed_value"));
                            mp.setWindSpeedName(rs.getString("wind_speed_name"));
                            mp.setWindDirectionValue(rs.getFloat("wind_direction_value"));
                            mp.setWindDirectionCode(rs.getString("wind_direction_code"));
                            mp.setWindDirectionName(rs.getString("wind_direction_name"));

                            mp.setCloudsValue(rs.getInt("clouds_value"));
                            mp.setCloudsName(rs.getString("clouds_name"));

                            mp.setVisibility(rs.getString("visibility"));

                            mp.setPrecipitationValue(rs.getFloat("precipitation_value"));
                            mp.setPrecipitationMode(rs.getString("precipitation_mode"));
                            mp.setPrecipitationUnit(rs.getString("precipitation_unit"));

                            mp.setWeatherNumber(rs.getInt("weather_number"));
                            mp.setWeatherValue(rs.getString("weather_value"));
                            mp.setWeatherIcon(rs.getString("weather_icon"));

                            datumSql = rs.getTimestamp("last_update");
                            datumJava = new java.util.Date(datumSql.getTime());
                            mp.setLastUpdate(datumJava);

                            datumSql = rs.getTimestamp("vrijeme_spremanja");
                            datumJava = new java.util.Date(datumSql.getTime());
                            mp.setVrijemeSpremanja(datumJava);

                            meteo.add(mp);
                        }

                        if (imaRezova) {
                            mplo.setMeteoPodaci(meteo);
                            return mplo;
                        } else {
                            mplo.setPoruka("Ne postoje meteorološki zapisi za izabranu adresu");
                            return mplo;
                        }
                    }

                } catch (SQLException ex) {
                    //Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
                    mplo.setPoruka("Dogodila se pogreška pri prustupu bazi podataka");
                    return mplo;
                }

            } else {
                mplo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mplo;
            }

        }

    }

    /**
     * Pomoćna metoda koja pretvara String u LocalDateTime Podržava više formata
     * datuma
     *
     * @param datumOd početni datum
     * @param datumDo krajnji datum
     * @return Lista, prvi element je formatirani početni datum, drugi element
     * je formatirani krajni datum
     */
    private List<LocalDateTime> pretvoriStringUDatum(String datumOd, String datumDo) {
        LocalDateTime odDatuma = null;
        LocalDateTime doDatuma = null;

        List<LocalDateTime> datumi = new ArrayList<>();

        boolean datumDobar;

        try {
            DateTimeFormatter dff = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss:SSS");
            odDatuma = LocalDateTime.parse(datumOd, dff);
            doDatuma = LocalDateTime.parse(datumDo, dff);
            datumDobar = true;
        } catch (Exception e) {
            datumDobar = false;
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("dd.M.yyyy HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("d.MM.yyyy. HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("dd.M.yyyy. HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("d.M.yyyy. HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }
        if (!datumDobar) {
            try {
                DateTimeFormatter dff = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss:SSS");
                odDatuma = LocalDateTime.parse(datumOd, dff);
                doDatuma = LocalDateTime.parse(datumDo, dff);
                datumDobar = true;
            } catch (Exception e) {
                datumDobar = false;
            }
        }

        if (!datumDobar) {
            return null;
        } else {
            datumi.add(odDatuma);
            datumi.add(doDatuma);
            return datumi;
        }
    }

    /**
     * Web service operation Daje n najbližih meteo stanica za adresu
     *
     * @param adresa naziv adrese
     * @param brojStanica N
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return Objekt MeteStaniceListOmotac
     */
    @WebMethod(operationName = "dajVremenskeStaniceUBliziniAdrese")
    public MeteoStaniceListOmotac dajVremenskeStaniceUBliziniAdrese(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "brojStanica") final int brojStanica,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoStaniceListOmotac mslo = new MeteoStaniceListOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mslo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mslo;
        } else {
            if (adresa == null) {
                mslo.setPoruka("Morate unijeti adresu");
                return mslo;
            }
            if (adresa.trim().equals("")) {
                mslo.setPoruka("Morate unijeti adresu");
                return mslo;
            }

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "meteoStanice";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            if (imaSredstva) {

                List<MeteoStanice> meteoStanice = new ArrayList<>();

                GMKlijent gmk = new GMKlijent();
                Lokacija l = gmk.getGeoLocation(adresa);

                if (l == null) {
                    mslo.setPoruka("Unijeli ste neispravnu adresu");
                    return mslo;
                }

                OWMKlijent owmk = new OWMKlijent(konfigRazno.dajPostavku("openWeatherMap.key"));

                MeteoStanice[] ms = owmk.getWeatherStations(adresa, l.getLatitude(), l.getLongitude(), brojStanica);

                if (ms == null) {
                    mslo.setPoruka("Web servis je trenutno nedostupan");
                    return mslo;
                }

                //sve je proslo dobro, korisniku skini sredstva
                if (autentikacija == 2 && konfigCjenik != null) {
                    umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                    upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                }

                for (MeteoStanice m : ms) {
                    meteoStanice.add(m);
                }

                mslo.setMeteoStanice(meteoStanice);
                return mslo;
            } else {
                mslo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mslo;
            }

        }

    }

    /**
     * Web service operation Daje vremensku prognozu za zadani broj sati
     *
     * @param adresa naziv adrese
     * @param brojSati N
     * @param korisnik korisnicko ime
     * @param lozinka lozinka korisnika
     * @return objekt MeteoPrognozaSatiListOmotac
     */
    @WebMethod(operationName = "dajPrognozuZaBrojSati")
    public MeteoPrognozaSatiListOmotac dajPrognozuZaBrojSati(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "brojSati") final int brojSati,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoPrognozaSatiListOmotac mpslo = new MeteoPrognozaSatiListOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mpslo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mpslo;
        } else {
            if (adresa == null) {
                mpslo.setPoruka("Morate unijeti adresu");
                return mpslo;
            }
            if (adresa.trim().equals("")) {
                mpslo.setPoruka("Morate unijeti adresu");
                return mpslo;
            }

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "meteoPrognozaSati";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));

                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            // ako je admin uvijek se izvrsava if, nikad else
            if (imaSredstva) {

                List<MeteoPrognozaSati> meteoPrognozaSati = new ArrayList<>();

                GMKlijent gmk = new GMKlijent();
                Lokacija l = gmk.getGeoLocation(adresa);

                if (l == null) {
                    mpslo.setPoruka("Unijeli ste nepostojeću lozinku");
                    return mpslo;
                }

                OWMKlijent owmk = new OWMKlijent(konfigRazno.dajPostavku("openWeatherMap.key"));

                MeteoPrognozaSati[] mps = owmk.getWeatherPerHours(adresa, l.getLatitude(), l.getLongitude(), brojSati);

                if (mps == null) {
                    mpslo.setPoruka("Web servis je trenutno nedostupan");
                    return mpslo;
                }

                //sve je proslo dobro, korisniku skini sredstva
                if (autentikacija == 2 && konfigCjenik != null) {
                    umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                    upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                }

                for (MeteoPrognozaSati m : mps) {
                    meteoPrognozaSati.add(m);
                }

                mpslo.setMeteoPrognozaSati(meteoPrognozaSati);
                return mpslo;

            } else {
                mpslo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mpslo;
            }
        }

    }

    /**
     * Web service operation Daje vremensku prognozu za zadani broj dana
     *
     * @param adresa naziv adrese
     * @param brojDana N
     * @param korisnik korisnicko ime
     * @param lozinka lozinka
     * @return Objekt MeteoPrognozaListOmotac
     */
    @WebMethod(operationName = "dajPrognozuZaBrojDana")
    public MeteoPrognozaListOmotac dajPrognozuZaBrojDana(@WebParam(name = "adresa") final String adresa,
            @WebParam(name = "brojDana") final int brojDana,
            @WebParam(name = "korisnik") final String korisnik, @WebParam(name = "lozinka") final String lozinka) {
        preuzmiKonfiguracije();

        MeteoPrognozaListOmotac mplo = new MeteoPrognozaListOmotac();

        int autentikacija = autenticirajKorisnika(korisnik, lozinka);

        if (autentikacija == 0) {
            //korisnik nije autenticiran
            mplo.setPoruka("Unijeli ste netočne korisničke podatke");
            return mplo;
        } else {
            if (adresa == null) {
                mplo.setPoruka("Morate unijeti adresu");
                return mplo;
            }
            if (adresa.trim().equals("")) {
                mplo.setPoruka("Morate unijeti adresu");
                return mplo;
            }

            boolean imaSredstva = true;

            float cijenaZahtjeva = 0;

            String zahtjev = "meteoPrognoza";

            if (autentikacija == 2) {   //običan korisnik
                if (konfigCjenik != null) {
                    try {
                        cijenaZahtjeva = Float.parseFloat(konfigCjenik.dajPostavku(zahtjev));
                        imaSredstva = korisnikImaSredstva(korisnik, cijenaZahtjeva);
                    } catch (Exception e) {
                        //ako nema definirane cijene unutar dnevnika, zahtjev je besplatan
                        imaSredstva = true;
                    }

                } else {
                    //nema cjenika, zahtjev je besplatan
                    imaSredstva = true;
                }
            }

            // ako je admin uvijek se izvrsava if, nikad else
            if (imaSredstva) {

                List<MeteoPrognoza> meteoPrognoza = new ArrayList<>();

                GMKlijent gmk = new GMKlijent();
                Lokacija l = gmk.getGeoLocation(adresa);

                if (l == null) {
                    mplo.setPoruka("Unijeli ste nepostojeću lozinku");
                    return mplo;
                }

                OWMKlijent owmk = new OWMKlijent(konfigRazno.dajPostavku("openWeatherMap.key"));

                MeteoPrognoza[] mp = owmk.getWeatherForecast(adresa, l.getLatitude(), l.getLongitude(), brojDana);

                if (mp == null) {
                    mplo.setPoruka("Web servis je trenutno nedostupan");
                    return mplo;
                }

                //sve je proslo dobro, korisniku skini sredstva
                if (autentikacija == 2 && konfigCjenik != null) {
                    umanjiKorisnickaSredstva(korisnik, cijenaZahtjeva);
                    upisiTransakcijuUEvidenciju(korisnik, cijenaZahtjeva, zahtjev);
                }

                for (MeteoPrognoza m : mp) {
                    meteoPrognoza.add(m);
                }

                mplo.setMeteoPrognoza(meteoPrognoza);
                return mplo;

            } else {
                mplo.setPoruka("Nemate dovoljno sredstava za izvršavanje zahtjeva");
                return mplo;
            }
        }
    }

    /**
     * Pomoćna metoda za autentikaciju korisnika
     *
     * @param korisnik
     * @param lozinka
     * @return 0 - neuspjesna autentikacija, 1 - admin, 2 - korisnik
     */
    private int autenticirajKorisnika(String korisnik, String lozinka) {
        String sqlUpit = "SELECT grupa FROM majugurci_korisnici "
                + "WHERE korisnicko_ime= ? AND lozinka= ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);
            ps.setString(2, lozinka);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("grupa");
                } else {
                    return 0;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Daje podatak ima li korisnik dovoljno sredstava za izvršavanje zahtjeva
     * ili ne
     *
     * @param korisnik korisnicko ime
     * @param cijenaUsluge cijena usluge iz cjenika
     * @return true ako ima, false inace
     */
    private boolean korisnikImaSredstva(String korisnik, float cijenaUsluge) {
        String sqlUpit = "SELECT korisnicko_ime, sredstva FROM majugurci_korisnici "
                + "WHERE korisnicko_ime= ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    float korisnickaSredstva = rs.getFloat("sredstva");
                    if ((korisnickaSredstva - cijenaUsluge) >= 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    //ovaj dio se ne bi trbao izvrsavati
                    return false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Metoda koja smanjuje korisnicki iznos u BP za cijenu usluge
     *
     * @param korisnik korisnicko_ime
     * @param cijenaUsluge iznos za koji se smanjuju kor. sredstva
     */
    private void umanjiKorisnickaSredstva(String korisnik, float cijenaUsluge) {
        String sqlUpit = "UPDATE majugurci_korisnici SET sredstva = sredstva - ? WHERE korisnicko_ime = ?";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setFloat(1, cijenaUsluge);
            ps.setString(2, korisnik);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za upisivanje evidencije transakcija
     *
     * @param korisnik korisnicko ime
     * @param cijenaZahtjeva cijena zahtjva
     * @param zahtjev opis zahtjeva
     */
    private void upisiTransakcijuUEvidenciju(String korisnik, float cijenaZahtjeva, String zahtjev) {
        cijenaZahtjeva = -cijenaZahtjeva;

        String sqlUpit = "INSERT INTO majugurci_transakcije "
                + "(korisnik, iznos_promjena, novo_stanje, datum, usluga) "
                + "VALUES ("
                + "(SELECT id_korisnik FROM majugurci_korisnici WHERE korisnicko_ime = ?), "
                + "?, "
                + "(SELECT sredstva FROM majugurci_korisnici WHERE korisnicko_ime = ?), "
                + "?, ?);";

        try (Connection con = DriverManager.getConnection(this.baza, this.korisnik, this.lozinkaBP);
                PreparedStatement ps = con.prepareStatement(sqlUpit);) {

            ps.setString(1, korisnik);
            ps.setFloat(2, cijenaZahtjeva);
            ps.setString(3, korisnik);

            java.util.Date utilDate = new Date();
            java.sql.Timestamp date = new java.sql.Timestamp(utilDate.getTime());
            ps.setTimestamp(4, date);
            ps.setString(5, zahtjev);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ServerObradaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
