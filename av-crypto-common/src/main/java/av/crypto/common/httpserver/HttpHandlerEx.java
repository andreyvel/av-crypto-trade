package av.crypto.common.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public abstract class HttpHandlerEx implements HttpHandler {
    public abstract String getResponseBody(HttpExchange req);
    protected enum MediaType {NONE, APPLICATION_JSON, TEXT_HTML, TEXT_PLAIN};
    protected MediaType mediaType = MediaType.NONE;

    public HttpHandlerEx() {
    }

    public String getContentType() {
        String contentType = null;
        if (mediaType == MediaType.NONE) {
            contentType = null;
        } else if (mediaType == MediaType.APPLICATION_JSON) {
            contentType = "application/json; charset=UTF-8";
        } else if (mediaType == MediaType.TEXT_PLAIN) {
            contentType = "text/plain; charset=UTF-8";
        } else if (mediaType == MediaType.TEXT_HTML) {
            contentType = "text/html; charset=UTF-8";
        } else {
            throw new IllegalArgumentException("Unknown mediaType=" + mediaType);
        }
        return contentType;
    }

    @Override
    public void handle(HttpExchange req) throws IOException {
        String body = null;
        try {
            body = getResponseBody(req);
            String contentType = getContentType();

            if (contentType != null) {
                Headers headers = req.getResponseHeaders();
                headers.set("Content-Type", contentType);
            }
        } catch(Exception e) {
            body = e.getMessage();
        }

        if (body == null) {
            req.sendResponseHeaders(500, -1);
            return;
        }

        byte[] bytes = body.getBytes();
        req.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = req.getResponseBody()) {
            os.write(bytes);
        }
    }

    public HashMap<String, String> getParams(HttpExchange req) {
        HashMap<String, String> params = new HashMap<>();
        String query = req.getRequestURI().getQuery();

        if (query != null) {
            String[] data = query.split("&");
            for (String pair : data) {
                String[] data2 = pair.split("=");

                if (data2.length == 1) {
                    params.put(data2[0], null);
                } if (data2.length == 2) {
                    params.put(data2[0], data2[1]);
                } else {
                    int pos = pair.indexOf("=");
                    params.put(data2[0], pair.substring(pos + 1));
                }
            }
        }
        return params;
    }
}
