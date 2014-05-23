package org.pac4j.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

public class StorageHelper {

    private static Session getSession(HttpServerExchange exchange) {
        return exchange.getAttachment(SessionManager.ATTACHMENT_KEY).getSession(exchange,
                exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
    }

    private static void save(HttpServerExchange exchange, String name, Object value) {
        Session session = getSession(exchange);
        if (session != null) {
            session.setAttribute(name, value);
        }
    }

    private static Object get(HttpServerExchange exchange, String name) {
        Session session = getSession(exchange);
        if (session != null) {
            return session.getAttribute(name);
        } else {
            return null;
        }
    }

    public static void saveProfile(HttpServerExchange exchange, ProfileWrapper profileWrapper) {
        save(exchange, Constants.PROFILE, profileWrapper);
    }

    public static ProfileWrapper getProfile(HttpServerExchange exchange) {
        return (ProfileWrapper) get(exchange, Constants.PROFILE);
    }

    public static String getRequestedUrl(HttpServerExchange exchange) {
        return (String) get(exchange, Constants.REQUESTED_URL);
    }

    public static void saveRequestedUrl(HttpServerExchange exchange, String requestedUrlToSave) {
        save(exchange, Constants.REQUESTED_URL, requestedUrlToSave);
    }

    public static void createSession(HttpServerExchange exchange) {
        Session session = getSession(exchange);
        if (session == null) {
            exchange.getAttachment(SessionManager.ATTACHMENT_KEY).createSession(exchange,
                    exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
        }
    }

}
