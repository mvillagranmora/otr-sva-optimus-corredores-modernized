package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
/** Write port: the log insert of the early exit (one JDBC batch). Joins the ambient transaction; never commits or rolls back. */
public interface CertificarAporteRescateEscritura {

    void insertarLog(List<LogFila> filas);
}
