<%-- 
    Document   : pregledFinancija
    Created on : May 29, 2015, 7:25:19 PM
    Author     : Mario
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="org.foi.nwtis.majugurci.web.kontrole.Transakcija"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link type="text/css" rel="stylesheet" href="<c:url value="/resources/css/displaytag.css" />"
      <!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Pregled financija</title>
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
                        <li class="selected-item"><a href="${pageContext.servletContext.contextPath}/PregledFinancija">Financijski podaci</a></li>
                        <li><a href="${pageContext.servletContext.contextPath}/PregledZahtjeva">Pregled zahtjeva</a></li>
                    </ul>
                </nav>
            </aside>
            <section id="content" class="column-right">
                <h1>Pregled financija</h1>

                <article>
                    <p> Ažuriranje stanja </p>

                    <fieldset>
                        <form action="${pageContext.servletContext.contextPath}/AzurirajStanje" method="POST">
                            <p><label for="korSredstva">Stanje računa</label>
                                <input id="korSredstva" name="korSredstva" value="${korSredstva}" readonly="true"/> <br/></p>
                            <p><input class="formbutton" name="Ažuriraj stanje" type="submit" value="Ažuriraj stanje"/></p>   
                        </form>
                    </fieldset>

                    <br/>
                    <!--poruka o neuspjehu neke akcije-->
                    <p style="display: inline; color: red; font-style: oblique;"> 
                        ${porukaGreska}  
                    </p>
                    <!--poruka o uspjehu neke akcije-->
                    <p style="display: inline; color: green; font-style: oblique;"> 
                        ${porukaUspjeh}    
                    </p>
                    <br/>
                </article>


                <article>
                    <p> Pregled ažuriranja </p>

                    <display:table id="data" requestURI="${pageContext.servletContext.contextPath}/PregledFinancija" 
                                   name="transakcije" pagesize="${paginacija}" sort="list">
                        <display:setProperty name="paging.banner.no_items_found" value="Nema zapisa"></display:setProperty>
                        <display:setProperty name="paging.banner.some_items_found" value=""></display:setProperty>

                        <display:column property="iznosPromjena" title="Iznos promjena" sortable="true"/>
                        <display:column property="novoStanje" title="Novo stanje" sortable="true"/>
                        <display:column property="datum" title="Datum" sortable="true"/>
                        <display:column property="usluga" title="Usluga" sortable="true"/>
                    </display:table>
                </article>


                <footer class="clear">
                    <p>&copy; 2015 Mario Jugurčić. <a href="http://zypopwebtemplates.com/">Free CSS Templates</a> by ZyPOP</p>
                </footer>

            </section>

            <div class="clear"></div>

        </section>


    </body>
</html>
