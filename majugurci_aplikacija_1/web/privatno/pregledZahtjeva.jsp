<%-- 
    Document   : pregledZahtjeva
    Created on : May 29, 2015, 7:25:29 PM
    Author     : Mario
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="org.foi.nwtis.majugurci.web.kontrole.Dnevnik"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link type="text/css" rel="stylesheet" href="<c:url value="/resources/css/displaytag.css" />"
      <!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Pregled zahtjeva</title>
        <link type="text/css" rel="stylesheet" href="<c:url value="/resources/css/styles.css" />"
              <!--[if lt IE 9]>
              <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
              <![endif]-->
              <!--
              anatine, a free CSS web template by ZyPOP (zypopwebtemplates.com/)

Download: http://zypopwebtemplates.com/

License: Creative Commons Attribution
//-->
              <meta name="viewport" content="width=device-width, minimum-scale=1.0, maximum-scale=1.0" />
    </head>

    <body>

        <%
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
        %>

        <section id="body" class="width">
            <aside id="sidebar" class="column-left">
                <header>
                    <h1><a href="#">App 1</a></h1>

                    <h2>by Mario Jugurčić</h2>
                </header>
                <nav id="mainnav">
                    <ul>
                        <p class="dobrodoslica">Dobrodošli: ${sessionScope.korisnik.korisnickoIme} </p><br/><br/>
                        <li><a href="${pageContext.servletContext.contextPath}/Kontroler">Početna</a></li>
                        <li><a href="${pageContext.servletContext.contextPath}/OdjavaKorisnika">Odjava korisnika</a></li>
                        <li><a href="${pageContext.servletContext.contextPath}/PregledFinancija">Financijski podaci</a></li>
                        <li class="selected-item"><a href="${pageContext.servletContext.contextPath}/PregledZahtjeva">Pregled zahtjeva</a></li>
                    </ul>
                </nav>
            </aside>
            <section id="content" class="column-right">
                <h1>Pregled zahtjeva</h1>

                <article>
                    <fieldset>
                        <legend>Filteri</legend>
                        <form action="${pageContext.servletContext.contextPath}/PregledZahtjeva" method="POST">
                            <p><label for="filterKomanda">Komanda</label>
                                <textarea type="textarea" id="filterKomanda" name="filterKomanda">${filterKomanda}</textarea><br/></p>
                            <p><label for="filterOdgovor">Odgovor</label>
                                <textarea type="textarea" id="filterOdgovor" name="filterOdgovor">${filterOdgovor}</textarea><br/></p>
                            <p><label for="filteripAdresa">IpAdresa</label>
                                <input type="text" id="filteripAdresa" name="filteripAdresa" value="${filteripAdresa}"/><br/></p>
                            <p><label for="filterVrijemePocetak">Vrijeme početak</label>
                                <input type="text" id="filterVrijemePocetak" name="filterVrijemePocetak" value="${filterVrijemePocetak}"/> (dd.MM.yyyy hh.mm.ss)<br/></p>
                            <p><label for="filterVrijemeKraj">Vrijeme kraj</label>
                                <input type="text" id="filterVrijemeKraj" name="filterVrijemeKraj" value="${filterVrijemeKraj}"/> (dd.MM.yyyy hh.mm.ss)<br/></p>          
                            <p><label for="filterTrajanje">Trajanje</label>
                                <input type="text" id="filterTrajanje" name="filterTrajanje" value="${filterTrajanje}"/><br/></p>
                            <p><input class="formbutton" type="submit" value="Primjeni filtere"/></p>  
                        </form>
                    </fieldset>

                    <display:table id="data" requestURI="${pageContext.servletContext.contextPath}/PregledZahtjeva" 
                                   name="dnevnik" pagesize="${paginacija}" sort="list">
                        <display:setProperty name="paging.banner.no_items_found" value="Nema zapisa"></display:setProperty>
                        <display:setProperty name="paging.banner.some_items_found" value=""></display:setProperty>

                        <display:column property="komanda" title="Komanda" sortable="true"/>
                        <display:column property="odgovor" title="Odgovor" sortable="true"/>
                        <display:column property="ipAdresa" title="IpAdresa" sortable="true"/>
                        <display:column property="datum" title="Datum" sortable="true"/>
                        <display:column property="trajanje" title="Trajanje" sortable="true"/>
                    </display:table>

                    <br/><br/>
                    <!--poruka o neuspjehu neke akcije-->
                    <p style="display: inline; color: red; font-style: oblique;"> 
                        ${porukaGreska}  
                    </p>
                    <!--poruka o uspjehu neke akcije-->
                    <p style="display: inline; color: green; font-style: oblique;"> 
                        ${porukaUspjeh}    
                    </p>
                    <br/><br/> 
                </article>

                <footer class="clear">
                    <p>&copy; 2015 Mario Jugurčić. <a href="http://zypopwebtemplates.com/">Free CSS Templates</a> by ZyPOP</p>
                </footer>

            </section>

            <div class="clear"></div>

        </section>


    </body>
</html>
