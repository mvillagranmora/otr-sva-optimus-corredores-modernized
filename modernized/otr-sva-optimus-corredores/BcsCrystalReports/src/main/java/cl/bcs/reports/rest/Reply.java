package cl.bcs.reports.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;

/** What the servlet wrote to the response: status, optional content type / Content-Disposition, body bytes. */
final class Reply {

    final int status;
    final String contentType;          // null = the servlet did not set one
    final String contentDisposition;   // null = none
    final byte[] body;
    String reason;                     // diagnostic header X-Crystal-Reason (never part of the body)

    private Reply(int status, String contentType, String contentDisposition, byte[] body) {
        this.status = status;
        this.contentType = contentType;
        this.contentDisposition = contentDisposition;
        this.body = body;
    }

    static Reply text(int status, String text) {
        return new Reply(status, null, null, text.getBytes(StandardCharsets.UTF_8));
    }

    static Reply json(String json) {
        return new Reply(200, "application/json", null, json.getBytes(StandardCharsets.UTF_8));
    }

    static Reply attachment(String fileName, byte[] bytes) {
        return new Reply(200, null, "attachment; filename=\"" + safeFileName(fileName) + "\"", bytes);
    }

    /**
     * The name comes from the DB / request: control characters are removed (header injection) and the quote and the
     * backslash, which would break out of the quoted-string, become '_'. Any other name is returned unchanged.
     */
    static String safeFileName(String name) {
        if (name == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 0x20 || c == 0x7f) {
                continue;
            }
            sb.append(c == '"' || c == '\\' ? '_' : c);
        }
        return sb.toString();
    }

    void writeTo(HttpServletResponse response) throws IOException {
        response.setStatus(status);
        if (contentType != null) {
            response.setContentType(contentType);
        }
        if (contentDisposition != null) {
            response.addHeader("Content-Disposition", contentDisposition);
        }
        if (reason != null) {
            response.setHeader("X-Crystal-Reason", reason);
        }
        response.getOutputStream().write(body);
        response.getOutputStream().flush();
    }
}
