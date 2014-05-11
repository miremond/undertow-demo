package org.pac4j.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import org.pac4j.core.context.HttpConstants;

public class HttpResponseHelper {

    public static void ok(HttpServerExchange exchange, String content) {
        exchange.setResponseCode(HttpConstants.OK);
        exchange.getResponseSender().send(content);
    }

    public static void redirect(HttpServerExchange exchange, String location) {
        exchange.setResponseCode(HttpConstants.TEMP_REDIRECT);
        if (location != null) {
            exchange.getResponseHeaders().put(HttpString.tryFromString(HttpConstants.LOCATION_HEADER), location);
        }
        exchange.endExchange();
    }

    public static void redirect(HttpServerExchange exchange) {
        redirect(exchange, null);
    }

    public static void unauthorized(HttpServerExchange exchange, String page) {
        exchange.setResponseCode(HttpConstants.UNAUTHORIZED);
        exchange.getResponseSender().send(page);
    }

}
