package cl.bcs.reports.rest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/** Servlet lines 247-255: the body lines joined WITHOUT separators; an empty body becomes "{ }". */
final class BodyReader {

    private BodyReader() {
    }

    static String read(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }
        String bodyParms = sb.toString();
        if (bodyParms.length() == 0) {
            bodyParms = "{ }";
        }
        return bodyParms;
    }
}
