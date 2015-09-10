/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.majugurci.web.kontrole;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Mario
 * 
 * Servlet koji korisniku ispisuje poruke ako se dogodi neka pogreška u aplikaciji.
 */
public class ObradaPogreske extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        obradiPogresku(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void obradiPogresku(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        Throwable throwable = (Throwable) request
                .getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        String requestUri = (String) request
                .getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Nepoznat";
        }

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.write("<html><head><title>Iznimka - detalji</title></head><body>");
        if (statusCode != 500) {
            out.write("<h3>Podaci o pogrešci</h3>");
            out.write("<strong>Status kod</strong>: " + statusCode + "<br>");
            out.write("<strong>Zahtijevani URL</strong>: " + requestUri);
            
            out.write("<br><br>");
            out.write("<a href=\"" + request.getContextPath() + "/Kontroler\">Početna</a>");
        } else {
            out.write("<h3>Podaci o pogrešci</h3>");
            out.write("<li>Pogreška: " + throwable.getMessage() + "</li>");
            out.write("</ul>");

            out.write("<br><br>");
            out.write("<a href=\"" + request.getContextPath() + "/Kontroler\">Početna</a><br/>");
            out.write("<a href=\"" + request.getContextPath() + "/PrijavaKorisnika\">Prijava</a>");
        }

        out.write("</body></html>");
    }
}
