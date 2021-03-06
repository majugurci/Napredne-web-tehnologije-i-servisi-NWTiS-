/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.rest.klijenti;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.foi.nwtis.majugurci.web.podaci.Lokacija;
import org.foi.nwtis.majugurci.web.podaci.MeteoPodaci;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognoza;
import org.foi.nwtis.majugurci.web.podaci.MeteoPrognozaSati;
import org.foi.nwtis.majugurci.web.podaci.MeteoStanice;

/**
 *
 * @author Mario
 */
public class OWMKlijent {

    String apiKey;
    OWMRESTHelper helper;
    Client client;

    public OWMKlijent(String apiKey) {
        this.apiKey = apiKey;
        helper = new OWMRESTHelper(apiKey);
        client = ClientBuilder.newClient();
    }

    /**
     * Daje trenutno važeće meteo podatke za adresu određenu koordinatama
     * @param latitude geografska širina
     * @param longitude geografska dužina
     * @return meteo podaci
     */
    public MeteoPodaci getRealTimeWeather(String latitude, String longitude) {
        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Current_Path());
        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);

        
        try {
            String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
            
            JSONObject jo = new JSONObject(odgovor);
            MeteoPodaci mp = new MeteoPodaci();
            mp.setSunRise(new Date(jo.getJSONObject("sys").getLong("sunrise") * 1000));
            mp.setSunSet(new Date(jo.getJSONObject("sys").getLong("sunset") * 1000));

            mp.setTemperatureValue(Float.parseFloat(jo.getJSONObject("main").getString("temp")));
            mp.setTemperatureMin(Float.parseFloat(jo.getJSONObject("main").getString("temp_min")));
            mp.setTemperatureMax(Float.parseFloat(jo.getJSONObject("main").getString("temp_max")));
            mp.setTemperatureUnit("celsius");

            mp.setHumidityValue(Float.parseFloat(jo.getJSONObject("main").getString("humidity")));
            mp.setHumidityUnit("%");

            mp.setPressureValue(Float.parseFloat(jo.getJSONObject("main").getString("pressure")));
            mp.setPressureUnit("hPa");

            mp.setWindSpeedValue(Float.parseFloat(jo.getJSONObject("wind").getString("speed")));
            mp.setWindSpeedName("");

            mp.setWindDirectionValue(Float.parseFloat(jo.getJSONObject("wind").getString("deg")));
            mp.setWindDirectionCode("");
            mp.setWindDirectionName("");

            mp.setCloudsValue(jo.getJSONObject("clouds").getInt("all"));
            mp.setCloudsName(jo.getJSONArray("weather").getJSONObject(0).getString("description"));
            mp.setPrecipitationMode("");

            mp.setWeatherNumber(jo.getJSONArray("weather").getJSONObject(0).getInt("id"));
            mp.setWeatherValue(jo.getJSONArray("weather").getJSONObject(0).getString("description"));
            mp.setWeatherIcon(jo.getJSONArray("weather").getJSONObject(0).getString("icon"));

            mp.setLastUpdate(new Date(jo.getLong("dt") * 1000));

            return mp;

        } catch (Exception ex) {
            Logger.getLogger(OWMKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Daje vremensku prognozu za adresu određenu koordinatama za određeni broj dana
     * @param adresa naziv adrese
     * @param latitude geografska širina
     * @param longitude geografska dužina
     * @param noDays broj dana za prognozu
     * @return meteo prognoza za broj dana
     */
    public MeteoPrognoza[] getWeatherForecast(String adresa, String latitude, String longitude, int noDays) {
        MeteoPrognoza[] mpr = new MeteoPrognoza[noDays];

        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_ForecastDaily_Path());

        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);
        webResource = webResource.queryParam("cnt", noDays);
        webResource = webResource.queryParam("mode", "json");

        

        try {
            String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
            
            JSONObject jo = new JSONObject(odgovor);

            for (int i = 0; i < noDays; i++) {

                MeteoPodaci mp = new MeteoPodaci();

                mp.setSunRise(null);
                mp.setSunSet(null);

                mp.setTemperatureMin(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getJSONObject("temp").getString("min")));
                mp.setTemperatureMax(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getJSONObject("temp").getString("max")));

                //buduci da nema prosjecne temperature odlucio sam je sam izracunati kao prosjek najvece i najnize
                mp.setTemperatureValue((mp.getTemperatureMin() + mp.getTemperatureMax()) / 2);
                mp.setTemperatureUnit("celsius");

                mp.setHumidityValue(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getString("humidity")));
                mp.setHumidityUnit("%");

                mp.setPressureValue(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getString("pressure")));
                mp.setPressureUnit("hPa");

                mp.setWindSpeedValue(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getString("speed")));
                mp.setWindSpeedName("");

                mp.setWindDirectionValue(Float.parseFloat(jo.getJSONArray("list").getJSONObject(i).getString("deg")));
                mp.setWindDirectionCode("");
                mp.setWindDirectionName("");

                mp.setWeatherNumber(jo.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getInt("id"));
                mp.setWeatherValue(jo.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description"));
                mp.setWeatherIcon(jo.getJSONArray("list").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon"));

                mp.setLastUpdate(new Date(jo.getJSONArray("list").getJSONObject(i).getLong("dt") * 1000));

                MeteoPrognoza metPro = new MeteoPrognoza(adresa, i + 1, mp);

                mpr[i] = metPro;
            }

        } catch (Exception ex) {
            Logger.getLogger(OWMKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mpr;
    }

    /**
     * Daje popis vremenskih stanica u blizini adrese određene koordinatama
     * @param adresa naziv adrese
     * @param latitude geografska širina
     * @param longitude geografska dužina
     * @param brojStanica broj stanica
     * @return popis stanica u blizini adrese
     */
    public MeteoStanice[] getWeatherStations(String adresa, String latitude, String longitude, int brojStanica) {
        MeteoStanice[] meteoStanice = new MeteoStanice[brojStanica];

        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_StationsNear_Path());

        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);
        webResource = webResource.queryParam("cnt", brojStanica);
        webResource = webResource.queryParam("mode", "json");


        

        //stvarni broj vraćenih stanica, kako bi se izbjegla null mjesta u polju
        int brojacUnosa = 0;

        try {
            String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
            
            JSONArray joArray = new JSONArray(odgovor);

            for (int i = 0; i < brojStanica; i++) {

                MeteoStanice ms = new MeteoStanice();

                JSONObject jo1;

                try {
                    jo1 = joArray.getJSONObject(i);
                } catch (JSONException e) {
                    //znači da nema više stanica i da prekinemo ptelju
                    break;
                }

                brojacUnosa++;

                JSONObject joStanica = jo1.getJSONObject("station");

                ms.setName(joStanica.getString("name"));
                ms.setType(joStanica.getInt("type"));
                ms.setStatus(joStanica.getInt("status"));
                ms.setId(joStanica.getInt("id"));

                Lokacija l = new Lokacija();
                l.setLatitude(joStanica.getJSONObject("coord").getString("lat"));
                l.setLongitude(joStanica.getJSONObject("coord").getString("lon"));
                ms.setLocation(l);

                ms.setDistanceValue(Float.parseFloat(jo1.getString("distance")));
                ms.setDistanceUnit("km");

                meteoStanice[i] = ms;
            }

        } catch (Exception ex) {
            Logger.getLogger(OWMKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }

        MeteoStanice[] msStvarniBroj = new MeteoStanice[brojacUnosa];

        for (int i = 0; i < brojacUnosa; i++) {
            msStvarniBroj[i] = meteoStanice[i];
        }

        return msStvarniBroj;
    }

    /**
     * Daje prognozu po satima za adresu određenu koordinatama
     * @param adresa naziv adrese
     * @param latitude geograska širina
     * @param longitude geografska dužina
     * @param brojSati broj sati
     * @return vremenska prognoza po satima
     */
    public MeteoPrognozaSati[] getWeatherPerHours(String adresa, String latitude, String longitude, int brojSati) {

        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Forecast_Path());

        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);
        webResource = webResource.queryParam("mode", "json");

        

        Date sadasnjeVrijeme = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(sadasnjeVrijeme);
        cal.add(Calendar.HOUR_OF_DAY, brojSati);
        sadasnjeVrijeme = cal.getTime();

        try {
            String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
            
            JSONObject jo = new JSONObject(odgovor);

            int brojPrognoza = jo.getInt("cnt");

            if (brojPrognoza == 0) {
                return null;
            } else {
                MeteoPrognozaSati[] mpr = new MeteoPrognozaSati[brojPrognoza];

                JSONArray array = jo.getJSONArray("list");

                for (int i = 0; i < brojPrognoza; i++) {

                    MeteoPodaci mp = new MeteoPodaci();

                    JSONObject ob = array.getJSONObject(i);

                    Date datum = new Date(ob.getLong("dt") * 1000);

                    mp.setSunRise(null);
                    mp.setSunSet(null);

                    mp.setTemperatureMin(Float.parseFloat(ob.getJSONObject("main").getString("temp_min")));
                    mp.setTemperatureMax(Float.parseFloat(ob.getJSONObject("main").getString("temp_max")));

                    mp.setTemperatureValue(Float.parseFloat(ob.getJSONObject("main").getString("temp")));
                    mp.setTemperatureUnit("celsius");

                    mp.setHumidityValue(Float.parseFloat(ob.getJSONObject("main").getString("humidity")));
                    mp.setHumidityUnit("%");

                    mp.setPressureValue(Float.parseFloat(ob.getJSONObject("main").getString("pressure")));
                    mp.setPressureUnit("hPa");

                    mp.setWindSpeedValue(Float.parseFloat(ob.getJSONObject("wind").getString("speed")));
                    mp.setWindSpeedName("");

                    mp.setWindDirectionValue(Float.parseFloat(ob.getJSONObject("wind").getString("deg")));
                    mp.setWindDirectionCode("");
                    mp.setWindDirectionName("");

                    mp.setWeatherNumber(ob.getJSONArray("weather").getJSONObject(0).getInt("id"));
                    mp.setWeatherValue(ob.getJSONArray("weather").getJSONObject(0).getString("description"));
                    mp.setWeatherIcon(ob.getJSONArray("weather").getJSONObject(0).getString("icon"));

                    mp.setLastUpdate(null);

                    MeteoPrognozaSati mps = new MeteoPrognozaSati(adresa, datum, mp);
                    
                    mpr[i] = mps;

                    if (datum.after(sadasnjeVrijeme)) {
                        //ako je dosegnuo zadani korisnicki sat izadi iz petlje
                        break;
                    }

                }
                
                return mpr;
            }
        } catch (Exception ex) {
            Logger.getLogger(OWMKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
