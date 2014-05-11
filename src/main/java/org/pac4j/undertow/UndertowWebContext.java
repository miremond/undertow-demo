package org.pac4j.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.util.HttpString;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pac4j.core.context.WebContext;

public class UndertowWebContext implements WebContext {

    private final HttpServerExchange exchange;

    public UndertowWebContext(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getRequestParameter(String name) {
        Deque<String> param = exchange.getQueryParameters().get(name);
        return (param != null) ? param.peek() : null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        Map<String, Deque<String>> params = exchange.getQueryParameters();
        Map<String, String[]> map = new HashMap<String, String[]>();
        for (Entry<String, Deque<String>> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return map;
    }

    @Override
    public String getRequestHeader(String name) {
        return exchange.getRequestHeaders().get(name, 0);
    }

    @Override
    public void setSessionAttribute(String name, Object value) {
        Session session = Config.getSessionManager().getSession(exchange, Config.getSessioncookieconfig());
        if (session != null) {
            if (value == null) {
                session.removeAttribute(name);
            } else {
                session.setAttribute(name, value);
            }
        }
    }

    @Override
    public Object getSessionAttribute(String name) {
        Session session = Config.getSessionManager().getSession(exchange, Config.getSessioncookieconfig());
        return (session != null) ? session.getAttribute(name) : null;
    }

    @Override
    public String getRequestMethod() {
        return exchange.getRequestMethod().toString();
    }

    @Override
    public void writeResponseContent(String content) {
        exchange.getResponseSender().send(content);
    }

    @Override
    public void setResponseStatus(int code) {
        exchange.setResponseCode(code);
    }

    @Override
    public void setResponseHeader(String name, String value) {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
    }

    @Override
    public String getServerName() {
        return exchange.getHostName();
    }

    @Override
    public int getServerPort() {
        return exchange.getHostPort();
    }

    @Override
    public String getScheme() {
        return exchange.getProtocol().toString();
    }

    @Override
    public String getFullRequestURL() {
        String full = exchange.getRequestURL();
        if (exchange.getQueryString() != null) {
            full = full + "?" + exchange.getQueryString();
        }
        return full;
    }

}
