package org.pac4j.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackHandler implements HttpHandler {

    protected static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    @SuppressWarnings("rawtypes")
    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        // clients group from config
        final Clients clientsGroup = Config.getClients();

        // web context
        final WebContext context = new UndertowWebContext(exchange);

        // get the client from its type
        final BaseClient client = (BaseClient) clientsGroup.findClient(context);
        logger.debug("client : {}", client);

        // get credentials
        Credentials credentials = null;
        try {
            credentials = client.getCredentials(context);
            logger.debug("credentials : {}", credentials);
        } catch (final RequiresHttpAction e) {
            // requires some specific HTTP action
            final int code = e.getCode();
            logger.debug("requires HTTP action : {}", code);
            exchange.endExchange();
            return;
        }

        // get user profile
        @SuppressWarnings("unchecked")
        final CommonProfile profile = client.getUserProfile(credentials, context);
        logger.debug("profile : {}", profile);

        // get or create sessionId
        //        final String sessionId = StorageHelper.getOrCreateSessionId(req);

        // save user profile only if it's not null
        if (profile != null) {
            StorageHelper.saveProfile(exchange, new ProfileWrapper(profile));
        }

        // get requested url
        final String requestedUrl = StorageHelper.getRequestedUrl(exchange);
        StorageHelper.saveRequestedUrl(exchange, "/");
        final String redirectUrl = defaultUrl(requestedUrl, Config.getDefaultSuccessUrl());

        // retrieve saved request and redirect
        HttpResponseHelper.redirect(exchange, redirectUrl);
    }

    /**
     * This method returns the default url from a specified url compared with a default url.
     * 
     * @param url
     * @param defaultUrl
     * @return the default url
     */
    public static String defaultUrl(final String url, final String defaultUrl) {
        String redirectUrl = defaultUrl;
        if (StringUtils.isNotBlank(url)) {
            redirectUrl = url;
        }
        logger.debug("defaultUrl : {}", redirectUrl);
        return redirectUrl;
    }

}
