package cl.bcs.reports.rest;

/**
 * Early exit of the servlet flow with a plain-text body ("404 - NOT FOUND"), the way the legacy CallReportServlet
 * sets the status, prints the text and returns. {@code reason} only goes to the diagnostic header X-Crystal-Reason
 * (the body stays identical to the servlet's).
 */
final class Stop extends RuntimeException {

    private static final long serialVersionUID = 1L;

    final int status;
    final String text;
    final String reason;

    private Stop(int status, String text, String reason) {
        super(reason, null, false, false);
        this.status = status;
        this.text = text;
        this.reason = reason;
    }

    static Stop notFound(String reason) {
        return new Stop(404, "404 - NOT FOUND", reason);
    }
}
