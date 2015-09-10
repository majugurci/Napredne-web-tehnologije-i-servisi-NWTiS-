<%-- 
    Document   : azurirajStanje
    Created on : May 31, 2015, 4:02:17 PM
    Author     : Mario
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Ažuriranje stanja</title>
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

                <article>
                    <h1>Ažuriranje stanja</h1>
                    <fieldset>
                        <form action="${pageContext.servletContext.contextPath}/PregledFinancija" method="POST">
                            <p><label for="korSredstva">Trenutno stanje</label>
                                <input id="korSredstva" name="korSredstva" value="${korSredstva}" readonly="true"/><br/></p>	
                            <p><label for="korSredstvaNovo">Novo stanje</label>
                                <input id="korSredstvaNovo" name="korSredstvaNovo"/> <br/></p>
                            <p><input name="azuriraj" value="true" hidden="true"/> </p>
                            <p><input class="formbutton" name="Ažuriraj stanje" type="submit" value="Ažuriraj stanje"/></p>
                        </form>
                    </fieldset>
                </article>


                <footer class="clear">
                    <p>&copy; 2015 Mario Jugurčić. <a href="http://zypopwebtemplates.com/">Free CSS Templates</a> by ZyPOP</p>
                </footer>

            </section>

            <div class="clear"></div>

        </section>


    </body>
</html>
