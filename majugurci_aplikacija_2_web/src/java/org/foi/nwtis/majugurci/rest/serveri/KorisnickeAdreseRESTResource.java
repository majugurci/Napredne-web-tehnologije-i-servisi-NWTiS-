/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.rest.serveri;

import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;
import org.foi.nwtis.majugurci.ws.klijenti.MeteoPodaciWSKlijent;
import org.foi.nwtis.majugurci.ws.serveri.AdreseKorisnikaOmotac;

/**
 * REST Web Service
 *
 * @author Mario
 * 
 * REST poziv koji za prosljeđeni id aktivnog korisnika daje sve njegove adrese
 */
public class KorisnickeAdreseRESTResource {

    private String id;
    private List<Korisnik> ak;

    /**
     * Creates a new instance of KorisnickeAdreseRESTResource
     */
    private KorisnickeAdreseRESTResource(String id, List<Korisnik> ak) {
        this.id = id;
        this.ak = ak;
    }

    /**
     * Get instance of the KorisnickeAdreseRESTResource
     */
    public static KorisnickeAdreseRESTResource getInstance(String id, List<Korisnik> ak) {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of KorisnickeAdreseRESTResource class.
        return new KorisnickeAdreseRESTResource(id, ak);
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.majugurci.rest.serveri.KorisnickeAdreseRESTResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        boolean imaKorisnika = false;

        JSONObject rezultat = new JSONObject();
        JSONObject omotac = new JSONObject();

        try {

            int idKor = -1;
            try {
                idKor = Integer.parseInt(id);
                
                Korisnik korisnik = null;

                for (Korisnik k : ak) {
                    if (k.getIdKorisnika() == idKor) {
                        imaKorisnika = true;
                        korisnik = k;
                        break;
                    }
                }

                if (imaKorisnika) {
                    //vrati njegove adrese
                    String korisnickoIme = korisnik.getKorisnickoIme();
                    String lozinka = korisnik.getLozinka();
                    AdreseKorisnikaOmotac ako = MeteoPodaciWSKlijent.dajAdreseKorisnika(korisnickoIme, lozinka);
                    if (ako.getPoruka() != null) {
                        omotac.put("status", ako.getPoruka());
                    } else {
                        omotac.put("status", "OK");
                        List<String> sveAdreseKorisnika = ako.getAdrese();

                        JSONArray poljeAdresa = new JSONArray();
                        int i = 0;
                        for (String a : sveAdreseKorisnika) {
                            JSONObject jo = new JSONObject();
                            jo.put("adresa", a);
                            poljeAdresa.put(jo);
                            i++;
                        }
                        omotac.put("adrese", poljeAdresa);
                    }
                } else {
                    omotac.put("status", "Ne postoji aktivni korisnik s id: " + idKor);
                }

            } catch (NumberFormatException e) {
                omotac.put("status", "Morate unijti brojcanu vrijednost id korisnika");
            }

            rezultat.put("Adrese", omotac);

        } catch (JSONException e) {

        }

        return rezultat.toString();
    }

    /**
     * PUT method for updating or creating an instance of
     * KorisnickeAdreseRESTResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

    /**
     * DELETE method for resource KorisnickeAdreseRESTResource
     */
    @DELETE
    public void delete() {
    }
}
