/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.zrna;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.majugurci.konfiguracije.Konfiguracija;
import org.foi.nwtis.majugurci.web.podaci.Korisnik;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Mario
 * 
 * Zrno koje daje adminu mogućnost da preko korisničkog sučelja preuzima i 
 * šalje cjenik na server putem socket servera.
 */
@ManagedBean
@RequestScoped
public class UpravljanjeCjenikom {

    private UploadedFile datUpload;

    ResourceBundle bundle;

    /**
     * Creates a new instance of UpravljanjeCjenikom
     */
    public UpravljanjeCjenikom() {

    }

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        bundle = context.getApplication().getResourceBundle(context, "m");
    }

    /**
     * Obrada upload opcije cjenika.
     * Šalje se zahtjev socket serveru te se cjenik sprema u privremenu datoteku.
     * Tu datoteku se daje adminu na preuzimanje a potom se briše iz sustava.
     */
    public void uploadCjenika() {
        String datotekaPrivremena = "tempFileCjenik-" + System.currentTimeMillis() + ".xml";
        if (datUpload.getFileName().equals("")) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:porukaUpload", new FacesMessage(bundle.getString("cjenik_greskaOdaberiteCjenik")));
        } else {
            try {
                InputStream is = datUpload.getInputstream();

                File dat = new File(datotekaPrivremena);
                if (!dat.exists()) {
                    dat.createNewFile();
                }

                OutputStream os = new FileOutputStream(dat);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }

                String odgovorServera = adminUpload(datotekaPrivremena);

                System.out.println("odg servera: " + odgovorServera);

                if (odgovorServera.equals("OK 10;")) {
                    //upload je prosao ok
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage("forma:porukaUploadOK", new FacesMessage(bundle.getString("cjenik_porukaNoviCjenikPostavljen")));
                } else {
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage("forma:porukaUpload", new FacesMessage(odgovorServera));
                }

                File f = new File(datotekaPrivremena);
                f.delete();
            } catch (IOException ex) {
                Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
            }

            //adminUpload(datUpload.getFileName());
        }
    }

    /**
     * Čitanje cjenika sa diska te slanje komande socket serveru da vrati cjenik.
     * @param datoteka
     * @return 
     */
    private String adminUpload(String datoteka) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        Korisnik k = (Korisnik) session.getAttribute("korisnik");
        String adminIme = k.getKorisnickoIme();
        String adminLozinka = k.getLozinka();

        String porukaServera = "";

        File dat = new File(datoteka);
        try {
            byte[] mybytearray = new byte[(int) dat.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dat));
            bis.read(mybytearray, 0, mybytearray.length);
            String procitanaDatoteka = Arrays.toString(mybytearray);
            int velicinaDatoteke = mybytearray.length;
            String komanda = "USER " + adminIme + "; PASSWD " + adminLozinka + "; UPLOAD "
                    + velicinaDatoteke + ";\r\n" + procitanaDatoteka;
            System.out.println("Komanda: " + komanda);
            porukaServera = posaljiZahtjev(komanda);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
        }

        return porukaServera;
    }

    /**
     * Preuzimanje cjenika sa servera.
     * Cjenik se pomoću pomoćne metode preuzima i rade se provjere nad njime.
     */
    public void preuzmiCjenik() {

        String datotekaPrivremena = "tempFileCjenik-" + System.currentTimeMillis() + ".xml";

        String odgovorServera = preuzmiCjenikSaSocketServera(datotekaPrivremena);

        if (odgovorServera.equals("OK")) {
            //datoteka uspjesno preuzeta
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            File file = new File(datotekaPrivremena);
            String fileName = file.getName();
            String contentType = ec.getMimeType(fileName);
            int contentLength = (int) file.length();

            ec.responseReset();
            ec.setResponseContentType(contentType);
            ec.setResponseContentLength(contentLength);
            ec.setResponseHeader("Content-Disposition", "attachment; filename=cjenik.xml");

            try {
                OutputStream output = ec.getResponseOutputStream();
                Files.copy(file.toPath(), output);
                fc.responseComplete();
            } catch (IOException ex) {
                Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
            }

            file.delete();
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("forma:porukaDownload", new FacesMessage(odgovorServera));
        }

    }

    /**
     * Metoda koja poziva socket server te preuzima cjenik
     *
     * @param korIme ime korisnika
     * @return
     */
    private String preuzmiCjenikSaSocketServera(String datotekaPrivremena) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        Korisnik k = (Korisnik) session.getAttribute("korisnik");
        String adminIme = k.getKorisnickoIme();
        String adminLozinka = k.getLozinka();

        String datoteka = datotekaPrivremena;

        String komanda = "USER " + adminIme + "; PASSWD " + adminLozinka + "; DOWNLOAD;";
        String porukaServera = posaljiZahtjev(komanda);

        String odgovor;

        System.out.println("Poruka servera: " + porukaServera);

        if (porukaServera.startsWith("Greška") || porukaServera.startsWith("Error")) {
            odgovor = porukaServera;
        } else {

            String regexOdgovora = "^OK 10; DATA (\\d+);\r\n(.+)";
            Matcher m = provjeraParametara(porukaServera, regexOdgovora);
            if (m == null) {
                odgovor = bundle.getString("cjenik_greskaServerLosiPodaci");
            } else {
                int brojZnakova = Integer.parseInt(m.group(1));
                String datotekaUStringu = m.group(2);

                String[] byteValues = datotekaUStringu.substring(1, datotekaUStringu.length() - 1).split(",");
                byte[] bytes = new byte[byteValues.length];

                for (int i = 0, len = bytes.length; i < len; i++) {
                    bytes[i] = Byte.parseByte(byteValues[i].trim());
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(datoteka);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(bytes, 0, brojZnakova);
                    bos.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (bytes.length != brojZnakova) {
                    File f = new File(datoteka);
                    f.delete();
                    odgovor = bundle.getString("cjenik_greskaVelicinaDatotekeRaazlika");
                } else {
                    odgovor = "OK";
                }

            }
        }
        return odgovor;
    }

    /**
     * Otvara vezu prema serveru te mu šalje zahtjev
     *
     * @param komanda zahtjev koji se šalje serveru
     * @return odgovor od servera (String)
     */
    private String posaljiZahtjev(String komanda) {
        ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        Konfiguracija konfigRazno = (Konfiguracija) sc.getAttribute("konfigRazno");
        String server = konfigRazno.dajPostavku("server");
        int port = Integer.parseInt(konfigRazno.dajPostavku("port"));

        String porukaServera = null;

        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        StringBuilder sb = null;

        try {
            socket = new Socket(server, port);
        } catch (IOException ex) {
            //Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
            return bundle.getString("cjenik_greskaSpajanje");
        }

        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();

            os.write(komanda.getBytes("ISO-8859-2"));
            //os.write(komanda.getBytes());
            os.flush();

            socket.shutdownOutput();

            sb = new StringBuilder();

            // Klijent ceka do 5 sekundi na odgovor servera
            for (int i = 0; i < 100; i++) {
                if (is.available() > 0) {
                    while (is.available() > 0) {
                        int znak = is.read();
                        if (znak == -1) {
                            break;
                        }
                        sb.append((char) znak);
                    }
                    break;
                } else {
                    sleep(50);
                }

            }

            while (is.available() > 0) {
                int znak = is.read();
                if (znak == -1) {
                    break;
                }
                sb.append((char) znak);
            }

            porukaServera = sb.toString();

        } catch (IOException ex) {
            Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(UpravljanjeCjenikom.class.getName()).log(Level.SEVERE, null, ex);
        }

        return porukaServera;

    }

    /**
     * Provjera pruženog string-a u odnosu pruženu sintaksu
     *
     * @param p string
     * @param sintaksa regex izraz
     * @return objekt tipa Matcher ili null ako ne odgovara
     */
    public Matcher provjeraParametara(String p, String sintaksa) {
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            return m;
        } else {
            return null;
        }
    }

    public UploadedFile getDatUpload() {
        return datUpload;
    }

    public void setDatUpload(UploadedFile datUpload) {
        this.datUpload = datUpload;
    }
}
