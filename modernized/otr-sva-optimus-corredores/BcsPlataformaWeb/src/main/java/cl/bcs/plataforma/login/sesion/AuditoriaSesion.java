package cl.bcs.plataforma.login.sesion;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.plataforma.login.jdbc.SqlNombrado;

/**
 * Audit trail of a session (T-06): one row in {@code PLA.Log} and four in {@code PLA.DetalleLog} (entry date, action, channel, valid token), as the
 * legacy {@code CrearSesion.agregarLogSesion} + {@code AgregarLog.logica} wrote them. Ids come from the sequences {@code Secuencias::Log} and
 * {@code ::DetalleLog}; each row carries the SHA-256 authentication code of its own fields (the legacy formula, including its repeated
 * {@code ValorNuevo}). Runs on the connection of the login: the audit and the session are confirmed together.
 */
@Component
public class AuditoriaSesion {

    static final String SECUENCIA_LOG = "BSCL.Plataforma.BD.Secuencias::Log";
    static final String SECUENCIA_DETALLE = "BSCL.Plataforma.BD.Secuencias::DetalleLog";

    static final String INSERT_LOG = "insert into \"BSCL.Plataforma.BD::PLA.Log\" (\"IdLog\",\"IdTipoLog\",\"IdModulo\",\"IdUsuario\",\"IdEmpresa\",\"FechaLog\",\"PeriodoLog\","
            + "\"IdUsuarioAtendido\",\"IdCliente\",\"EstadoCliente\",\"NombreCliente\",\"IdTipoDocumentoIdentidad\",\"IdDocumentoIdentidad\",\"IdPortafolio\","
            + "\"EstadoPortafolio\",\"IdRegistro\",\"EstadoRegistro\",\"IdTipoRegistro\",\"IdTipoOperacion\",\"Folio\",\"Cantidad\",\"PrecioTasa\",\"Monto\","
            + "\"IdMonedaOrigen\",\"IdMonedaLiquidacion\",\"TipoCambio\",\"Nemo\",\"IdProceso\",\"EstadoProceso\",\"NombreProceso\",\"FechaRegistro\",\"IdPersona\","
            + "\"CodigoAutenticacion\") values(:IdLog,:IdTipoLog,:IdModulo,:IdUsuario,:IdEmpresa,:FechaLog,:PeriodoLog,:IdUsuarioAtendido,:IdCliente,:EstadoCliente,"
            + ":NombreCliente,:IdTipoDocumentoIdentidad,:IdDocumentoIdentidad,:IdPortafolio,:EstadoPortafolio,:IdRegistro,:EstadoRegistro,:IdTipoRegistro,"
            + ":IdTipoOperacion,:Folio,:Cantidad,:PrecioTasa,:Monto,:IdMonedaOrigen,:IdMonedaLiquidacion,:TipoCambio,:Nemo,:IdProceso,:EstadoProceso,"
            + ":NombreProceso,CURRENT_UTCTIMESTAMP,:IdPersona,:CodigoAutenticacion)";

    static final String INSERT_DETALLE = "insert into \"BSCL.Plataforma.BD::PLA.DetalleLog\"(\"IdDetalleLog\",\"IdLog\",\"SubRegistro\",\"CampoRegistro\","
            + "\"ValorAntiguo\",\"ValorNuevo\",\"CodigoAutenticacion\") values(:IdDetalleLog,:IdLog,:SubRegistro,:CampoRegistro,:ValorAntiguo,:ValorNuevo,:CodigoAutenticacion)";

    /** Row of {@code PLA.Log} (legacy {@code AgregarLog.Log_Proceso}). */
    public static class LogProceso {
        public Long IdLog;
        public String IdTipoLog;
        public String IdModulo;
        public Long IdUsuario;
        public Long IdEmpresa;
        public java.sql.Date FechaLog;
        public String PeriodoLog;
        public Long IdUsuarioAtendido;
        public Long IdCliente;
        public String EstadoCliente;
        public String NombreCliente;
        public String IdTipoDocumentoIdentidad;
        public String IdDocumentoIdentidad;
        public Long IdPortafolio;
        public String EstadoPortafolio;
        public String IdRegistro;
        public String EstadoRegistro;
        public String IdTipoRegistro;
        public String IdTipoOperacion;
        public Long Folio;
        public BigDecimal Cantidad;
        public BigDecimal PrecioTasa;
        public BigDecimal Monto;
        public String IdMonedaOrigen;
        public String IdMonedaLiquidacion;
        public String TipoCambio;
        public String Nemo;
        public Long IdProceso;
        public String EstadoProceso;
        public String NombreProceso;
        public Long IdPersona;
        public String CodigoAutenticacion;
    }

    /** Row of {@code PLA.DetalleLog}. */
    public static class DetalleLogProceso {
        public Long IdDetalleLog;
        public Long IdLog;
        public String CampoRegistro;
        public String SubRegistro;
        public String ValorAntiguo;
        public String ValorNuevo;
        public String CodigoAutenticacion;
    }

    public static final class SecuenciaId {
        public Long Id;
    }

    /** Records the session event {@code accion} of the user (legacy {@code CrearSesion.agregarLogSesion}, no additional field). */
    public void registrarSesion(Connection conn, Long idUsuario, String fechaSistema, String idCanal, Long idEmpresa, String accion) throws Exception {
        LogProceso l = new LogProceso();
        l.IdLog = siguiente(conn, SECUENCIA_LOG, 1).get(0);
        l.IdTipoLog = "INFORMATIVO";
        l.IdModulo = "PLA";
        l.IdUsuario = idUsuario;
        l.IdEmpresa = idEmpresa;
        l.FechaLog = java.sql.Date.valueOf(java.time.LocalDate.parse(fechaSistema));
        l.PeriodoLog = fechaSistema.substring(0, 7);
        l.IdUsuarioAtendido = idUsuario;
        l.IdCliente = 0L;
        l.EstadoCliente = "";
        l.NombreCliente = "";
        l.IdTipoDocumentoIdentidad = "";
        l.IdDocumentoIdentidad = "";
        l.IdPortafolio = 0L;
        l.EstadoPortafolio = "";
        l.IdRegistro = "";
        l.EstadoRegistro = "";
        l.IdTipoRegistro = "SESION";
        l.IdTipoOperacion = "";
        l.Folio = 0L;
        l.Cantidad = BigDecimal.ZERO;
        l.PrecioTasa = BigDecimal.ZERO;
        l.Monto = BigDecimal.ZERO;
        l.IdMonedaOrigen = "";
        l.IdMonedaLiquidacion = "";
        l.TipoCambio = "";
        l.Nemo = "";
        l.IdProceso = 0L;
        l.EstadoProceso = "";
        l.NombreProceso = "";
        l.IdPersona = 0L;
        l.CodigoAutenticacion = sha256Hex(l.IdLog + l.IdTipoLog + l.IdModulo + l.IdUsuario + l.IdEmpresa + l.FechaLog + fechaSistema.substring(0, 7)
                + l.IdUsuarioAtendido + l.IdCliente + l.EstadoCliente + l.NombreCliente + l.IdTipoDocumentoIdentidad + l.IdDocumentoIdentidad
                + l.IdPortafolio + l.EstadoPortafolio + l.IdRegistro + l.EstadoRegistro + l.IdTipoRegistro + l.IdTipoOperacion + l.Folio + l.Cantidad
                + l.PrecioTasa + l.Monto + l.IdProceso + l.EstadoProceso + l.NombreProceso + l.IdMonedaOrigen + l.IdMonedaLiquidacion + l.TipoCambio
                + l.Nemo + l.IdPersona).toUpperCase();

        String[][] detalles = {{"FechaIngreso", fechaSistema}, {"ACCION", accion}, {"CANAL", idCanal}, {"TokenValido", "S"}};
        List<Long> ids = siguiente(conn, SECUENCIA_DETALLE, detalles.length);
        List<DetalleLogProceso> filas = new ArrayList<>();
        int j = 0;
        for (String[] d : detalles) {
            DetalleLogProceso det = new DetalleLogProceso();
            det.IdLog = l.IdLog;
            det.IdDetalleLog = ids.get(j++);
            det.SubRegistro = "SESION";
            det.CampoRegistro = d[0];
            det.ValorAntiguo = "";
            det.ValorNuevo = d[1];
            // legacy formula: the two ids are ADDED (Long + Long) before the text is appended, and the new value takes part twice
            det.CodigoAutenticacion = sha256Hex((det.IdDetalleLog + det.IdLog) + det.SubRegistro + det.ValorNuevo + det.ValorNuevo + det.CampoRegistro).toUpperCase();
            filas.add(det);
        }
        SqlNombrado.lote(conn, INSERT_LOG, List.of(l), LogProceso.class);
        SqlNombrado.lote(conn, INSERT_DETALLE, filas, DetalleLogProceso.class);
    }

    /** {@code cantidad} next values of a sequence (legacy {@code Utils.getSecuencia}, one statement per value). */
    static List<Long> siguiente(Connection conn, String secuencia, int cantidad) throws SQLException {
        List<Long> ids = new ArrayList<>();
        Map<String, Object> sinParametros = new HashMap<>();
        for (int i = 0; i < cantidad; i++) {
            List<SecuenciaId> r = SqlNombrado.consultar(conn, "SELECT \"" + secuencia + "\".NEXTVAL AS \"Id\" FROM DUMMY", sinParametros, SecuenciaId.class);
            ids.add(r.get(0).Id);
        }
        return ids;
    }

    static String sha256Hex(String entrada) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(entrada.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
