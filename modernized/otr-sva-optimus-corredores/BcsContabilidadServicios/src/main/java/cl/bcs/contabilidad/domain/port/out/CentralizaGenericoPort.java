package cl.bcs.contabilidad.domain.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence port of {@link cl.bcs.contabilidad.domain.service.CentralizaGenerico} (CONT-C-50..69): the reads and
 * batch inserts of legacy CentralizaGenerico.java:37-51 (CentralizaGenerico_*.sql), the DB clock (FechaDB) and the
 * sequences (Utils.getSecuencia).
 *
 * <p>All calls of one {@code logica} run must share one connection and transaction: the two {@code *_Paso} working
 * tables written first are joined by the vigente-movement and dato-valor queries that follow. Insert methods accept an
 * empty list (nothing is written).
 */
public interface CentralizaGenericoPort {

    /** CentralizaGenerico_SelectEventoTipoDato.sql. */
    List<EventoTipoDato> queryEventosTipoDato();

    /** CentralizaGenerico_InsertMovimientoPaso.sql (GBL.CentralizaGenerico_ProcesoMovimientos_Paso). */
    void insertarMovimientosPaso(List<MovimientoPaso> movimientos);

    /** CentralizaGenerico_InsertDatoValorPaso.sql (GBL.CentralizaGenerico_ProcesoDatoValor_Paso). */
    void insertarValoresDatoPaso(List<ValorDatoPaso> valores);

    /** CentralizaGenerico_SelectTipoCentralizacion.sql. */
    List<TipoCentralizacion> queryTiposCentralizacion(List<Long> idsTipoCentralizacion);

    /** CentralizaGenerico_SelectCentralizacion.sql: latest Estado V centralization per type for the date. */
    List<Centralizacion> queryCentralizacionesVigentes(List<Long> idsTipoCentralizacion, String fechaContable);

    /** CentralizaGenerico_SelectMovimiento.sql: vigente movements matching the movimientos-paso rows. */
    List<MovimientoVigente> queryMovimientosVigentes(List<Long> idsTipoCentralizacion);

    /** CentralizaGenerico_SelectEventoTipo.sql. */
    List<EventoTipo> queryEventosTipo(List<String> idsTipoEvento);

    /** CentralizaGenerico_SelectEventoTipoDatoValor.sql: predefined values matching the dato-valor-paso rows. */
    List<EventoTipoDatoValor> queryEventosTipoDatoValor();

    /** FechaProceso, yyyy-MM-dd (legacy {@code Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, idEmpresa), "yyyy-MM-dd")}). */
    String obtenerFechaProceso(Long idEmpresa);

    /** HoraProceso, HH:mm:ss UTC (legacy {@code FechaDB.ObtenerHoraUTC(con)}). */
    String obtenerHoraProceso();

    /** {@code cantidad} values of the named sequence (legacy Utils.getSecuencia); none when cantidad is 0. */
    List<Long> obtenerSecuencia(String nombreSecuencia, long cantidad);

    /** CentralizaGenerico_InsertCentralizacion.sql. */
    void insertarCentralizaciones(List<InsertCentralizacion> centralizaciones);

    /** CentralizaGenerico_InsertMovimiento.sql. */
    void insertarMovimientos(List<InsertMovimiento> movimientos);

    /** CentralizaGenerico_InsertMovimientoDato.sql. */
    void insertarMovimientosDato(List<InsertMovimientoDato> datos);

    /** CentralizaGenerico_InsertEventoTipo.sql. */
    void insertarEventosTipo(List<EventoTipo> eventosTipo);

    /** CentralizaGenerico_InsertEvento.sql. */
    void insertarEventos(List<InsertEvento> eventos);

    /** CentralizaGenerico_InsertLog.sql (IdLog from its own sequence inside the statement). */
    void insertarLogs(List<InsertLog> logs);

    // ------------------------------------------------------------------ rows (legacy CentralizaGenerico inner classes)

    record EventoTipoDato(String idTipoDatoEvento, String tipoDato, String datosPreDefinidos) {}

    record EventoTipoDatoValor(String idTipoDatoEvento, String valorVarchar, BigDecimal valorDecimal, String estado) {}

    record TipoCentralizacion(Long idTipoCentralizacion, String nombre, String tipoEnvio) {}

    record Centralizacion(Long idTipoCentralizacion, Long idCentralizacion) {}

    record MovimientoVigente(Long idEmpresa, String fecha, String idTipoMovimiento, Long numero,
                             String documentoIdentidad, Long cuenta, Long idTipoCentralizacion) {}

    record EventoTipo(String idTipoEvento, String nombre, String estado) {}

    record MovimientoPaso(Long idEmpresa, String fecha, String idTipoMovimiento, Long numero,
                          String documentoIdentidad, Long idPortafolio) {}

    record ValorDatoPaso(String tipoDato, String valorVarChar, BigDecimal valorDecimal) {}

    record InsertCentralizacion(Long idCentralizacion, Long idTipoCentralizacion, Long idEmpresa, String fechaProceso,
                                String horaProceso, String fechaContable, String glosa, String codigoUsuario,
                                String tipoGeneracion, String estado) {}

    record InsertMovimiento(Long idMovimiento, String idTipoMovimiento, Long idEmpresa, Long numero, Long linea,
                            String fecha, String documentoIdentidad, Long cuenta, Long ejercicio, String periodo,
                            Long idCentralizacion, String idMoneda, BigDecimal monto, String estado) {}

    record InsertMovimientoDato(Long idMovimiento, String idTipoDatoEvento, String valorVarchar, BigDecimal valorDecimal,
                                String estado, String tipoEvento) {}

    record InsertEvento(Long idEmpresa, Long idEvento, String idTipoEvento, Long idCentralizacion, Long idMovimiento,
                        String fechaContable, BigDecimal monto, String codigoMoneda, String codigoUsuario,
                        String fechaProceso, String horaProceso, String estado) {}

    record InsertLog(Long idTipoLog, Long idEmpresa, Long ejercicio, Long idTipoProceso, String fecha, String hora,
                     String codigoUsuario, String glosa, Long idCentralizacion, Long idMovimiento, Long idEvento,
                     Long idDefinicionAsiento, Long idPreComprobante, Long idPreComprobanteAsiento,
                     String idCuentaContable, Long idCondicion, Long idTipoContabilidad, String idTipoEvento,
                     String idTipoDatoEvento, Long idTipoCentralizacion, String idTipoMovimiento,
                     Long numeroMovimiento, String fechaMovimiento, String rutMovimiento, Long cuentaMovimiento,
                     String indicadorError, String estado) {}
}
