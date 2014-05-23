package org.pac4j.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

public class DemoHandlers {

    public static HttpHandler indexHandler = new HttpHandler() {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            WebContext context = new UndertowWebContext(exchange);
            Clients client = Config.getClients();
            FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
            TwitterClient twClient = (TwitterClient) client.findClient("TwitterClient");
            FormClient formClient = (FormClient) client.findClient("FormClient");
            BasicAuthClient baClient = (BasicAuthClient) client.findClient("BasicAuthClient");
            CasClient casClient = (CasClient) client.findClient("CasClient");
            Saml2Client saml2Client = (Saml2Client) client.findClient("Saml2Client");

            StringBuilder sb = new StringBuilder();
            sb.append("<h1>index</h1>");
            sb.append("<a href=\"facebook/index.html\">Protected url by Facebook : facebook/index.html</a><br />");
            sb.append("<a href=\"twitter/index.html\">Protected url by Twitter : twitter/index.html</a><br />");
            sb.append("<a href=\"form/index.html\">Protected url by form authentication : form/index.html</a><br />");
            sb.append("<a href=\"basicauth/index.html\">Protected url by basic auth : basicauth/index.html</a><br />");
            sb.append("<a href=\"cas/index.html\">Protected url by CAS : cas/index.html</a><br />");
            sb.append("<a href=\"saml2/index.html\">Protected url by SAML2 : saml2/index.html</a><br />");
            sb.append("<br />");
            sb.append("<a href=\"logout\">logout</a>");
            sb.append("<br /><br />");
            sb.append("profile : ");
            sb.append(StorageHelper.getProfile(exchange));
            sb.append("<br /><br />");
            sb.append("<hr />");
            try {
                sb.append("<a href=\"").append(fbClient.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with Facebook</a><br />");
                sb.append("<a href=\"").append(twClient.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with Twitter</a><br />");
                sb.append("<a href=\"").append(formClient.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with form</a><br />");
                sb.append("<a href=\"").append(baClient.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with basic auth</a><br />");
                sb.append("<a href=\"").append(casClient.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with CAS</a><br />");
                sb.append("<a href=\"").append(saml2Client.getRedirectAction(context, false, false).getLocation())
                        .append("\">Authenticate with SAML</a><br />");
            } catch (RequiresHttpAction e) {

            }

            exchange.getResponseSender().send(sb.toString());
        }
    };

    public static HttpHandler authenticatedHandler = new HttpHandler() {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("<h1>protected area</h1>");
            sb.append("<a href=\"..\">Back</a><br />");
            sb.append("<br /><br />");
            sb.append("profile : ");
            sb.append(StorageHelper.getProfile(exchange));
            sb.append("<br />");

            exchange.getResponseSender().send(sb.toString());
        }
    };

    public static HttpHandler formHandler = new HttpHandler() {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            FormClient formClient = (FormClient) Config.getClients().findClient("FormClient");
            StringBuilder sb = new StringBuilder();
            sb.append("<form action=\"").append(formClient.getCallbackUrl()).append("\" method=\"POST\">");
            sb.append("<input type=\"text\" name=\"username\" value=\"\" />");
            sb.append("<p />");
            sb.append("<input type=\"password\" name=\"password\" value=\"\" />");
            sb.append("<p />");
            sb.append("<input type=\"submit\" name=\"submit\" value=\"Submit\" />");
            sb.append("</form>");

            exchange.getResponseSender().send(sb.toString());
        }
    };

}
