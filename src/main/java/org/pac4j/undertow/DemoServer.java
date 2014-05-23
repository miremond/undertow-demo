package org.pac4j.undertow;

import io.undertow.Undertow;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.session.SessionAttachmentHandler;

import java.util.Collections;
import java.util.List;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.http.credentials.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

public class DemoServer {

    public static void main(final String[] args) {

        Config.setClients(buildClients());

        PathHandler path = new PathHandler();
        path.addExactPath("/", DemoHandlers.indexHandler);
        path.addExactPath("/facebook/index.html", addSecurity(DemoHandlers.authenticatedHandler, "FacebookClient"));
        path.addExactPath("/twitter/index.html", addSecurity(DemoHandlers.authenticatedHandler, "TwitterClient"));
        path.addExactPath("/form/index.html", addSecurity(DemoHandlers.authenticatedHandler, "FormClient"));
        path.addExactPath("/basicauth/index.html", addSecurity(DemoHandlers.authenticatedHandler, "BasicAuthClient"));
        path.addExactPath("/cas/index.html", addSecurity(DemoHandlers.authenticatedHandler, "CasClient"));
        path.addExactPath("/saml2/index.html", addSecurity(DemoHandlers.authenticatedHandler, "Saml2Client"));
        path.addExactPath("/callback", addFormParsing(new CallbackHandler()));

        path.addExactPath("/theForm.html", DemoHandlers.formHandler);
        path.addExactPath("/post", addFormParsing(new HttpHandler() {

            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);
                String response = "no data\r\n";
                if (data != null) {
                    response = "form data " + data.toString() + "\r\n";
                }
                exchange.getResponseSender().send(response);
            }
        }));

        Undertow server = Undertow.builder().addListener(8080, "localhost").setHandler(addSession(path)).build();
        server.start();
    }

    private static HttpHandler addFormParsing(final HttpHandler toWrap) {
        HttpHandler handler = toWrap;
        FormParserFactory factory = FormParserFactory.builder().addParser(new FormEncodedDataDefinition()).build();
        EagerFormParsingHandler formHandler = new EagerFormParsingHandler(factory);
        formHandler.setNext(handler);
        handler = formHandler;
        return handler;
    }

    private static HttpHandler addSession(final HttpHandler toWrap) {
        return new SessionAttachmentHandler(toWrap, Config.getSessionManager(), Config.getSessioncookieconfig());
    }

    private static HttpHandler addSecurity(final HttpHandler toWrap, final String clientName) {
        HttpHandler handler = toWrap;
        // protect resource
        handler = new AuthenticationCallHandler(handler);
        // set authentication required
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = Collections
                .<AuthenticationMechanism> singletonList(new ClientAuthenticationMechanism(clientName, false));
        // use pac4j as authentication mechanism
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        // put security context in exchange
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, null, handler);
        return handler;
    }

    private static Clients buildClients() {
        final Saml2Client saml2Client = new Saml2Client();
        saml2Client.setKeystorePath("resource:samlKeystore.jks");
        saml2Client.setKeystorePassword("pac4j-demo-passwd");
        saml2Client.setPrivateKeyPassword("pac4j-demo-passwd");
        saml2Client.setIdpMetadataPath("resource:testshib-providers.xml");

        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA",
                "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        // HTTP
        final FormClient formClient = new FormClient("http://localhost:8080/theForm.html",
                new SimpleTestUsernamePasswordAuthenticator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        // CAS
        final CasClient casClient = new CasClient();
        // casClient.setGateway(true);
        casClient.setCasLoginUrl("https://freeuse1.casinthecloud.com/leleujgithub/login");

        final Clients clients = new Clients("http://localhost:8080/callback", saml2Client, facebookClient,
                twitterClient, formClient, basicAuthClient, casClient);

        return clients;
    }
}
