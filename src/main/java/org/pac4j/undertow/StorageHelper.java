package org.pac4j.undertow;

import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;

import org.pac4j.core.profile.CommonProfile;

public class StorageHelper {

    private static void save(HttpServerExchange exchange, String name, Object value) {
        Session session = Config.getSessionManager().getSession(exchange, Config.getSessioncookieconfig());
        if (session != null) {
            session.setAttribute(name, value);
        }
    }

    private static Object get(HttpServerExchange exchange, String name) {
        Session session = Config.getSessionManager().getSession(exchange, Config.getSessioncookieconfig());
        if (session != null) {
            return session.getAttribute(name);
        } else {
            return null;
        }
    }

    public static void saveProfile(HttpServerExchange exchange, CommonProfile profile) {
        save(exchange, Constants.PROFILE, profile);
    }

    public static CommonProfile getProfile(HttpServerExchange exchange) {
        return (CommonProfile) get(exchange, Constants.PROFILE);
    }

    public static String getRequestedUrl(HttpServerExchange exchange, String clientName) {
        return (String) get(exchange, clientName + Constants.SEPARATOR + Constants.REQUESTED_URL);
    }

    public static void saveRequestedUrl(HttpServerExchange exchange, String clientName, String requestedUrlToSave) {
        save(exchange, clientName + Constants.SEPARATOR + Constants.REQUESTED_URL, requestedUrlToSave);
    }

    public static void saveAccount(HttpServerExchange exchange, Account account) {
        save(exchange, Constants.ACCOUNT, account);
    }

    public static Account getAccount(HttpServerExchange exchange) {
        return (Account) get(exchange, Constants.ACCOUNT);
    }

}
