package cl.bcs.canalagente.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.canalagente.adapter.out.jdbc.SqlNombrado;
import cl.bcs.canalagente.adapter.out.jdbc.SqlRecursos;
import cl.bcs.canalagente.kit.Compuertas;
import cl.bcs.canalagente.kit.GsonPlataforma;
import cl.bcs.canalagente.port.out.ConsultarCartolaDeMovimientoPort;

/**
 * Legacy {@code cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion}: the movement history of a portfolio, which is the cartola of the client of the portfolio
 * enriched with folio, portfolio code, DTE and identity document through a work table. The same SQL, in the same order, on the connection the caller owns: the module never
 * commits, rolls back or closes it. The defects of the legacy are kept on purpose (D-CAN-01..12, see TRANSFORMATION_NOTES.md); the only addition is gate G-CTA, because the
 * cartola is not ported into this module.
 */
public class ConsultarHistorialMovimientosCuentaInversionServicio {

    private static final Logger log = LoggerFactory.getLogger(ConsultarHistorialMovimientosCuentaInversionServicio.class);

    private static final String MENSAJE_ERROR = "ERROR";

    private static final String SQL_SELECT_IDCLIENTE = SqlRecursos.modulo("ConsultarHistorialMovimientosCuentaInversion_SelectIdClientePortafolio.sql");
    private static final String SQL_INSERT_CARTOLADEMOVIMIENTOCOMPROBANTE = SqlRecursos.modulo("ConsultarHistorialMovimientosCuentaInversion_InsertCartolaDeMovimientoComprobante.sql");
    private static final String SQL_SELECT_CARTOLADEMOVIMIENTOCOMPROBANTE = SqlRecursos.modulo("ConsultarHistorialMovimientosCuentaInversion_SelectCartolaDeMovimientoComprobante.sql");
    private static final String SQL_DELETE_CARTOLADEMOVIMIENTOCOMPROBANTE = SqlRecursos.modulo("ConsultarHistorialMovimientosCuentaInversion_DeleteCartolaDeMovimientoComprobante.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPortafolio;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_IdMoneda;
        public Long p_IdMatrizAgrupador;
        public String p_EsADC;

        public Entrada() {
            this.p_IdMatrizAgrupador = null;
            this.p_EsADC = null;
        }
    }

    public static class Salida {
        public BigDecimal P_SALDOEFECTIVOINICIAL;
        public BigDecimal P_SALDOEFECTIVOFINAL;
        public List<Movimiento> P_OUT_RESULTADO;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciasDetalle> P_OUT_ADVERTENCIASDETALLE;
        public Long P_OUT_ERRORESCONTADOR;
        public List<ErroresDetalle> P_OUT_ERRORESDETALLE;

        public Salida() {
            this.P_OUT_RESULTADO = new ArrayList<>();
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    public static class Movimiento {
        public Long IdCargoAbonoMovimiento;
        public String NombreCliente;
        public Long IdCliente;
        public Long IdPortafolio;
        public Date FechaMovimiento;
        public Date FechaLiquidacion;
        public String IdMoneda;
        public String IdConcepto;
        public String NombreConcepto;
        public BigDecimal Cargo;
        public BigDecimal Abono;
        public String FolioComprobante;
        public String IdEstado;
        public String NombreEstado;
        public String NombreTipoComprobante;
        public String NombreTipoProducto;
        public String NombreTipoOperacion;
        public String NombreTipoMovimiento;
        public BigDecimal SaldoSimulado;
        public String CodigoPortafolio;
        public Long IdComprobante;
        public Long IdRespuestaDTE;
        public String RespuestaPdfDTE;
        public String DocumentoIdentidad;
        public Long IdMatrizAgrupador;
        public String EsADC;
    }

    public static class AdvertenciasDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ErroresDetalle {
        public String Codigo;
        public String Descripcion;
    }

    public static class IdClientePortafolio {
        public Long IdCliente;
    }

    public static class MovimientoComprobante {
        public Long IdCargoAbonoMovimiento;
        public String FolioComprobante;
        public String CodigoPortafolio;
        public Long IdRespuestaDTE;
        public String RespuestaPdfDTE;
        public String DocumentoIdentidad;
    }

    private final Compuertas compuertas;
    private final ConsultarCartolaDeMovimientoPort cartola;

    public ConsultarHistorialMovimientosCuentaInversionServicio(Compuertas compuertas, ConsultarCartolaDeMovimientoPort cartola) {
        this.compuertas = compuertas;
        this.cartola = cartola;
    }

    public Salida logica(Connection con, Entrada entrada) throws Exception {
        compuertas.gCartola();
        log.info("INICIO::ConsultarHistorialMovimientosCuentaInversion");
        Salida salida = new Salida();
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("IdPortafolio", entrada.p_IdPortafolio);
            List<IdClientePortafolio> idClientePortafolio = SqlNombrado.consultar(con, SQL_SELECT_IDCLIENTE, params, IdClientePortafolio.class);
            if (idClientePortafolio.isEmpty()) {
                ErroresDetalle error = new ErroresDetalle();
                error.Codigo = MENSAJE_ERROR;
                error.Descripcion = "Cliente no encontrado.";
                salida.P_OUT_ERRORESCONTADOR++; // D-CAN-01: the counter is null, so this throws NullPointerException and the message never leaves
                salida.P_OUT_ERRORESDETALLE.add(error);
                return salida;
            }

            CartolaJson.Entrada entradaCartola = new CartolaJson.Entrada();
            entradaCartola.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaCartola.p_IdUsuario = entrada.p_IdUsuario;
            entradaCartola.p_IdModulo = entrada.p_IdModulo;
            entradaCartola.p_UsaReglasNegocio = "S";
            entradaCartola.p_IdCuentaInversion = null;
            entradaCartola.p_IdCliente = idClientePortafolio.get(0).IdCliente;
            entradaCartola.p_IdPortafolio = entrada.p_IdPortafolio;
            entradaCartola.p_IdMoneda = entrada.p_IdMoneda;
            entradaCartola.p_IdEstado = null;
            entradaCartola.p_FechaDesde = entrada.p_FechaDesde;
            entradaCartola.p_FechaHasta = entrada.p_FechaHasta;
            entradaCartola.p_SaldoPorFechaEfectiva = "N";
            entradaCartola.p_IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
            entradaCartola.p_EsADC = entrada.p_EsADC;

            JsonObject respuesta = cartola.consultar(con, GsonPlataforma.GSON.toJsonTree(entradaCartola).getAsJsonObject());
            CartolaJson.Salida salidaCartola = GsonPlataforma.GSON.fromJson(respuesta, CartolaJson.Salida.class);

            SqlNombrado.lote(con, SQL_INSERT_CARTOLADEMOVIMIENTOCOMPROBANTE, salidaCartola.P_OUT_CARTOLADEMOVIMIENTO, CartolaJson.Movimiento.class);

            List<MovimientoComprobante> comprobantes = SqlNombrado.consultar(con, SQL_SELECT_CARTOLADEMOVIMIENTOCOMPROBANTE, new HashMap<>(), MovimientoComprobante.class);
            HashMap<Long, MovimientoComprobante> porMovimiento = new HashMap<>();
            for (MovimientoComprobante c : comprobantes) {
                porMovimiento.put(c.IdCargoAbonoMovimiento, c); // D-CAN-06: the last row of the SELECT (it has no ORDER BY) wins
            }

            for (CartolaJson.Movimiento mov : salidaCartola.P_OUT_CARTOLADEMOVIMIENTO) {
                MovimientoComprobante comprobante = porMovimiento.get(mov.IdCargoAbonoMovimiento);
                comprobante = comprobante != null ? comprobante : new MovimientoComprobante(); // D-CAN-07: dead code, the LEFT JOIN row of the work table always exists
                Movimiento movimiento = new Movimiento();
                movimiento.IdCargoAbonoMovimiento = mov.IdCargoAbonoMovimiento;
                movimiento.NombreCliente = mov.NombreCliente;
                movimiento.IdCliente = mov.IdCliente;
                movimiento.IdPortafolio = mov.IdPortafolio;
                movimiento.FechaMovimiento = mov.FechaSistema;
                movimiento.FechaLiquidacion = mov.FechaLiquidacionOperacion;
                movimiento.IdMoneda = mov.IdMoneda;
                movimiento.IdConcepto = mov.IdConcepto;
                movimiento.NombreConcepto = mov.NombreConcepto;
                movimiento.Cargo = mov.Cargo;
                movimiento.Abono = mov.Abono;
                movimiento.FolioComprobante = comprobante.FolioComprobante;
                movimiento.IdEstado = mov.IdEstado;
                movimiento.NombreEstado = mov.NombreEstado;
                movimiento.NombreTipoComprobante = mov.NombreTipoComprobante;
                movimiento.NombreTipoProducto = mov.NombreTipoProducto;
                movimiento.NombreTipoOperacion = mov.NombreTipoOperacion;
                movimiento.NombreTipoMovimiento = mov.NombreTipoMovimiento;
                movimiento.SaldoSimulado = mov.SaldoSimulado;
                movimiento.CodigoPortafolio = comprobante.CodigoPortafolio;
                movimiento.IdComprobante = mov.IdComprobante;
                movimiento.IdRespuestaDTE = comprobante.IdRespuestaDTE;
                movimiento.RespuestaPdfDTE = comprobante.RespuestaPdfDTE;
                movimiento.DocumentoIdentidad = comprobante.DocumentoIdentidad;
                movimiento.IdMatrizAgrupador = mov.IdMatrizAgrupador;
                movimiento.EsADC = mov.EsADC;
                salida.P_OUT_RESULTADO.add(movimiento);
            }
            SqlNombrado.ejecutar(con, SQL_DELETE_CARTOLADEMOVIMIENTOCOMPROBANTE);
            salida.P_OUT_ERRORESCONTADOR = 0L; // D-CAN-02 / D-CAN-03 / D-CAN-04: advertencias and both balances stay null; the cartola's errors are dropped
        } catch (Exception e) {
            log.error("ERROR", e); // D-CAN-05: a failure half way leaves the work table as it is
            throw e;
        }
        log.info("FIN::ConsultarHistorialMovimientosCuentaInversion");
        return salida;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Entrada jin = GsonPlataforma.GSON.fromJson(in.toString(), Entrada.class);
        Salida out = logica(conn, jin);
        return JsonParser.parseString(GsonPlataforma.GSON.toJson(out)).getAsJsonObject();
    }
}
