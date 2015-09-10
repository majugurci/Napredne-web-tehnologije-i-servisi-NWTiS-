<%-- 
    Document   : index
    Created on : May 29, 2015, 7:19:54 PM
    Author     : Mario
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.List"%>
<%@page import="org.foi.nwtis.majugurci.web.kontrole.Korisnik"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Početna</title>
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
                        <c:if test="${sessionScope.korisnik.korisnickoIme != null}">
                            <p class="dobrodoslica">Dobrodošli: ${sessionScope.korisnik.korisnickoIme} </p><br/><br/>
                            <li class="selected-item"><a href="${pageContext.servletContext.contextPath}/Kontroler">Početna</a></li>
                            <li><a href="${pageContext.servletContext.contextPath}/OdjavaKorisnika">Odjava korisnika</a></li>
                            <li><a href="${pageContext.servletContext.contextPath}/PregledFinancija">Financijski podaci</a></li>
                            <li><a href="${pageContext.servletContext.contextPath}/PregledZahtjeva">Pregled zahtjeva</a></li>
                            </c:if>

                        <c:if test="${sessionScope.korisnik.korisnickoIme == null}">
                            <li class="selected-item"><a href="${pageContext.servletContext.contextPath}/Kontroler">Početna</a></li>
                            <li><a href="${pageContext.servletContext.contextPath}/PrijavaKorisnika">Prijava korisnika</a></li><br/>
                        </c:if>   
                    </ul>
                </nav>
            </aside>
            <section id="content" class="column-right">

                <article>
                    <h1>Početna</h1>

                    <p> Za nastavak odaberite jednu od akcija iz izbornika </p>
                </article>


                <footer class="clear">
                    <p>&copy; 2015 Mario Jugurčić. <a href="http://zypopwebtemplates.com/">Free CSS Templates</a> by ZyPOP</p>
                </footer>

            </section>

            <div class="clear"></div>

        </section>


    </body>
</html>
