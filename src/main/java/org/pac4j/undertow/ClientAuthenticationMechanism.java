package org.pac4j.undertow;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;

import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;

public class ClientAuthenticationMechanism implements AuthenticationMechanism {

    private final String clientName;

    private final boolean isAjax;

    public ClientAuthenticationMechanism(String clientName, boolean isAjax) {
        this.clientName = clientName;
        this.isAjax = isAjax;
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        if (StorageHelper.getProfile(exchange) != null) {
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        } else {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        // TODO save request url here?
        WebContext webContext = new UndertowWebContext(exchange);
        final String requestedUrlToSave = webContext.getFullRequestURL();
        Session session = Config.getSessionManager().getSession(exchange, Config.getSessioncookieconfig());
        if (session == null) {
            session = Config.getSessionManager().createSession(exchange, Config.getSessioncookieconfig());
        }
        StorageHelper.saveRequestedUrl(exchange, clientName, requestedUrlToSave);
        // get client
        final BaseClient client = (BaseClient) Config.getClients().findClient(clientName);
        //logger.debug("client : {}", client);
        try {
            client.redirect(webContext, true, isAjax);
            return new ChallengeResult(true);
            //            switch (action.getType()) {
            //            case REDIRECT:
            //                HttpResponseHelper.redirect(req, action.getLocation());
            //                break;
            //            case SUCCESS:
            //                HttpResponseHelper.ok(req, action.getContent());
            //                break;
            //            default:
            //                throw new TechnicalException("Invalid redirect action type");
            //            }
        } catch (final RequiresHttpAction e) {
            return new ChallengeResult(false);
            // requires some specific HTTP action
            //            final int code = e.getCode();
            //            logger.debug("requires HTTP action : {}", code);
            //            if (code == HttpConstants.UNAUTHORIZED) {
            //                req.response().setStatusCode(401);
            //                req.response().sendFile(Config.getErrorPage401());
            //            } else if (code == HttpConstants.FORBIDDEN) {
            //                req.response().setStatusCode(403);
            //                req.response().sendFile(Config.getErrorPage403());
            //            }
            //            final String message = "Unsupported HTTP action : " + code;
            //            logger.error(message);
            //            throw new TechnicalException(message);
        }
    }
}
