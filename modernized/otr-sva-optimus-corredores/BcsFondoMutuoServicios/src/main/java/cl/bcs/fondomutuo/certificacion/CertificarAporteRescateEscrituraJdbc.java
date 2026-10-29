package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/**
 * {@link CertificarAporteRescateEscritura} over the legacy INSERT of the log. The SQL file is the legacy one byte for byte; only in memory the single sequence
 * token is replaced by the parameter that carries the id taken from the sequence port.
 */
public class CertificarAporteRescateEscrituraJdbc implements CertificarAporteRescateEscritura {

    private static final String TOKEN_SECUENCIA = "\"BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate\".NEXTVAL";
    private static final String SQL_INSERT_LOG = SqlRecursos.modulo("AgregarLogCertificacionAporteRescate_InsertLogCertificacion.sql")
            .replace(TOKEN_SECUENCIA, ":IdLogCertificacionAporteRescate");

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateEscrituraJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public void insertarLog(List<LogFila> filas) {
        if (filas.isEmpty()) {
            return;
        }
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_LOG, filas));
    }
}
