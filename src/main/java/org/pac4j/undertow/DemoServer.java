package org.pac4j.undertow;

import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        System.out.println("You can login with the following credentials:");
        System.out.println("User: userOne Password: passwordOne");
        System.out.println("User: userTwo Password: passwordTwo");

        final Map<String, char[]> users = new HashMap<String, char[]>(2);
        users.put("userOne", "passwordOne".toCharArray());
        users.put("userTwo", "passwordTwo".toCharArray());

        final IdentityManager identityManager = new MapIdentityManager(users);

        Config.setClients(buildClients());

        PathHandler path = new PathHandler();
        path.addExactPath("/", addSecurity(new HttpHandler() {
            @Override
            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                final SecurityContext context = exchange.getSecurityContext();
                exchange.getResponseSender().send(
                        "Hello   " + context.getAuthenticatedAccount().getPrincipal().getName(),
                        IoCallback.END_EXCHANGE);
            }
        }, identityManager));
        path.addExactPath("/callback", new CallbackHandler());

        Undertow server = Undertow.builder().addListener(8080, "localhost").setHandler(path).build();
        server.start();
    }

    private static HttpHandler addSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        //        List<AuthenticationMechanism> mechanisms = Collections
        //                .<AuthenticationMechanism> singletonList(new BasicAuthenticationMechanism("My Realm"));
        List<AuthenticationMechanism> mechanisms = Collections
                .<AuthenticationMechanism> singletonList(new ClientAuthenticationMechanism("CasClient", false));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new CachedAuthenticatedSessionHandler(handler, Config.getSessionManager());
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
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
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");

        final Clients clients = new Clients("http://localhost:8080/callback", saml2Client, facebookClient,
                twitterClient, formClient, basicAuthClient, casClient);

        return clients;
    }
}
