/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.rest.serveri;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;

/**
 * REST Web Service
 *
 * @author Mario
 * 
 * REST poziv koji vraća sve aktivne korisnike u sustavu.
 */
@Path("/korisnickeAdreseREST")
public class KorisnickeAdreseRESTsResourceContainer {

    @Context
    private UriInfo context;
    
    @Context
    private ServletContext sContext;

    /**
     * Creates a new instance of KorisnickeAdreseRESTsResourceContainer
     */
    public KorisnickeAdreseRESTsResourceContainer() {
    }

    /**
     * Retrieves representation of an instance of org.foi.nwtis.majugurci.rest.serveri.KorisnickeAdreseRESTsResourceContainer
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
        List<Korisnik> ak = (List) sContext.getAttribute("AktivniKorisnici");
        
        boolean imaKorisnika = !ak.isEmpty();
        
        JSONObject rezultat = new JSONObject();
        
        try { 
            JSONObject omotac = new JSONObject();
            if (imaKorisnika) {
                omotac.put("status", "OK");
                JSONArray poljeKorisnika = new JSONArray();
                int i = 0;
                for (Korisnik k : ak) {
                    JSONObject jo = new JSONObject();
                    jo.put("id", k.getIdKorisnika());
                    jo.put("korisnickoIme", k.getKorisnickoIme());
                    jo.put("lozinka", k.getLozinka());
                    jo.put("email", k.getEmail());
                    jo.put("grupaId", k.getGrupa());
                    jo.put("grupaOpis", k.getGrupaOpis());
                    jo.put("vrijemeRegistracije", k.getVrijemeRegistracije());
                    poljeKorisnika.put(i, jo);
                    i++;
                }
                omotac.put("korisnici", poljeKorisnika);
            } else {
                omotac.put("status", "Nema aktivnih korisnika");
            }
            rezultat.put("Aktivni korisnici", omotac);
        } catch (JSONException ex) {
            Logger.getLogger(KorisnickeAdreseRESTsResourceContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return rezultat.toString();
    }

    /**
     * POST method for creating an instance of KorisnickeAdreseRESTResource
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postJson(String content) {
        //TODO
        return Response.created(context.getAbsolutePath()).build();
    }

    /**
     * Sub-resource locator method for {id}
     */
    @Path("{id}")
    public KorisnickeAdreseRESTResource getKorisnickeAdreseRESTResource(@PathParam("id") String id) {
        List<Korisnik> ak = (List) sContext.getAttribute("AktivniKorisnici");
        return KorisnickeAdreseRESTResource.getInstance(id, ak);
    }
}
