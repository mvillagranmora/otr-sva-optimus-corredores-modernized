package cl.bcs.reports.rest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** M8: the Content-Disposition name comes from the DB / request and must not break out of the quoted string. */
class ReplyFileNameTest {

    private static String header(String name) throws Exception {
        MockHttpServletResponse r = new MockHttpServletResponse();
        Reply.attachment(name, new byte[]{1}).writeTo(r);
        return r.getHeader("Content-Disposition");
    }

    @Test
    void ordinaryNamesAreByteIdentical() throws Exception {
        assertEquals("attachment; filename=\"Comprobante Cuenta_Inversión-1_abc.PDF\"",
                header("Comprobante Cuenta_Inversión-1_abc.PDF"));
        assertEquals("attachment; filename=\"_.rpt\"", header("_.rpt"));
    }

    @Test
    void quoteBackslashAndControlCharactersAreNeutralized() throws Exception {
        String h = header("evil\".pdf\r\nX-Injected: 1\\x");
        assertEquals("attachment; filename=\"evil_.pdfX-Injected: 1_x\"", h);
        assertEquals("attachment; filename=\"a_b\"", header("a\"b"));
    }
}
