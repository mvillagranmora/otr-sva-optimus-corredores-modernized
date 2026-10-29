package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;

/** Write port: the log insert of the early exit (one JDBC batch). Joins the ambient transaction; never commits or rolls back. */
public interface CertificarAporteRescateEscritura {

    void insertarLog(List<LogFila> filas);
}
