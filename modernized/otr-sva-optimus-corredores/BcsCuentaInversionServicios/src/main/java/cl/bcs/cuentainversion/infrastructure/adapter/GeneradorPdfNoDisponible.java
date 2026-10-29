package cl.bcs.cuentainversion.infrastructure.adapter;

import cl.bcs.cuentainversion.port.out.GeneradorPdf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Placeholder {@link GeneradorPdf} so the application starts: no PDF library is available yet (human decision: which one). Any attempt
 * to produce a PDF fails with {@link UnsupportedOperationException}; a real adapter bean replaces this one automatically.
 */
@Component
@ConditionalOnMissingBean(value = GeneradorPdf.class, ignored = GeneradorPdfNoDisponible.class)
public class GeneradorPdfNoDisponible implements GeneradorPdf {

    @Override
    public byte[] generar(DocumentoPdf documento) {
        throw new UnsupportedOperationException("PDF generation is not available: no GeneradorPdf adapter is configured");
    }
}
