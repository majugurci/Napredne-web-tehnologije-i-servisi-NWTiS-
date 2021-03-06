<%-- 
    Document   : login
    Created on : May 29, 2015, 8:23:40 PM
    Author     : Mario
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Prijava</title>
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
                        <li><a href="${pageContext.servletContext.contextPath}/Kontroler">Početna</a></li>
                        <li class="selected-item"><a href="${pageContext.servletContext.contextPath}/PrijavaKorisnika">Prijava korisnika</a></li><br/>
                    </ul>
                </nav>
            </aside>
            <section id="content" class="column-right">

                <article>
                    <h1>Prijava korisnika</h1>
                    <fieldset>
                        <form method="POST" action="${pageContext.servletContext.contextPath}/ProvjeraKorisnika">
                            <p><label for="korinik">Korisničko ime:</label>
                                <input id="korisnik" name="korisnik"/><br /></p>	
                            <p><label for="lozinka">Lozinka:</label>
                                <input id="lozinka" name="lozinka" type="password"/><br /></p>
                            <p><input class="formbutton" name="akcija" value=" Prijavi se " type="submit"/></p>
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
