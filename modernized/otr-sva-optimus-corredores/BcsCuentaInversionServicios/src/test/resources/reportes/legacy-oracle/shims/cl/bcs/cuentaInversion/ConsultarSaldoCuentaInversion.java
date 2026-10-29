package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaMovimiento;
import cl.bcs.oracle.Colaboradores;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion (1214 lines; it computes balances with HANA stored
 * procedures and temporary tables, and belongs to the transactional family, pinned by the other half of this work package).
 * The request/response data classes are copied VERBATIM from the legacy file (ConsultarSaldoCuentaInversion.java:103-139 EntradaConsultarSaldoCuentaInversion; ConsultarSaldoCuentaInversion.java:213-232 SalidaConsultarSaldoCuentaInversion; ConsultarSaldoCuentaInversion.java:234-240 OtrosSaldos; ConsultarSaldoCuentaInversion.java:242-248 MontoEnTransitoAporte; ConsultarSaldoCuentaInversion.java:250-274 InfoCuentaInversion; ConsultarSaldoCuentaInversion.java:276-279 InformacionDetalle; ConsultarSaldoCuentaInversion.java:281-285 AdvertenciasDetalle; ConsultarSaldoCuentaInversion.java:287-290 ErroresDetalle; ConsultarSaldoCuentaInversion.java:293-310 CuentaInversion_Proceso; ConsultarSaldoCuentaInversion.java:163-166 InstrumentosMoneyMarket; ConsultarSaldoCuentaInversion.java:167-173 CantidadCustodia).
 * logica() answers with the canned response in case.json colaboradores.consultarSaldo (an object, or an array answered in call
 * order with the last one repeated; JSON of SalidaConsultarSaldoCuentaInversion, absent fields keep the legacy constructor
 * defaults) and records every request under llamadasColaboradores. Without a canned answer it returns an empty, error-free
 * output. The reporting classes are characterised AGAINST this contract, not against the balance calculation.
 */
public class ConsultarSaldoCuentaInversion implements JavaCall {

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        throw new UnsupportedOperationException("not part of the reportes oracle");
    }

    public SalidaConsultarSaldoCuentaInversion logica(Connection con, EntradaConsultarSaldoCuentaInversion entrada) throws Exception {
        Colaboradores.registrar("ConsultarSaldoCuentaInversion", entrada);
        JsonElement canned = Colaboradores.siguiente("consultarSaldo");
        SalidaConsultarSaldoCuentaInversion out;
        if (canned == null) {
            out = new SalidaConsultarSaldoCuentaInversion();
        } else {
            out = Utils.getGsonBuilder().fromJson(canned, SalidaConsultarSaldoCuentaInversion.class);
        }
        if (out.P_OUT_ERRORESCONTADOR == null) out.P_OUT_ERRORESCONTADOR = 0L;
        if (out.P_OUT_ADVERTENCIASCONTADOR == null) out.P_OUT_ADVERTENCIASCONTADOR = 0L;
        if (out.P_OUT_INFORMACIONCONTADOR == null) out.P_OUT_INFORMACIONCONTADOR = 0L;
        return out;
    }

	public static class EntradaConsultarSaldoCuentaInversion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InCuentaInversionConsultar> p_CuentaInversionAConsultar;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_SoloSaldoNegativo;
		public String p_SoloSaldoDisponible;
		public String p_SoloSaldoDisponibleNegativo;
		public String p_IncluyeCargosPendientes;
		public String p_IncluyeAbonosPendientes;
		public String p_IncluyeOtrosSaldos;
		public Long p_IdPortafolio;
		
		public String p_IncluyeProductoApv;
		public Long p_IdEjecutivo;
		public Long p_IdCliente;
		public Long p_IdProductoAgrupador;
		public String p_IdMoneda;

		public List<String> p_TipoSaldoConsultar; // TODO, CONTABLE, DISPONIBLE, RETENIDO , TRANSITO, PENDIENTE
		
		public EntradaConsultarSaldoCuentaInversion(){
			this.p_SoloSaldoNegativo = "N";
			this.p_SoloSaldoDisponible = "N";
			this.p_SoloSaldoDisponibleNegativo = "N";
			this.p_IncluyeCargosPendientes = "N";
			this.p_IncluyeAbonosPendientes = "N";
			this.p_IncluyeOtrosSaldos = "N";
			this.p_IncluyeProductoApv = "N";
			this.p_IdProductoAgrupador = 9999L;
			this.p_CuentaInversionAConsultar = new ArrayList<InCuentaInversionConsultar>();
			this.p_TipoSaldoConsultar = new ArrayList<>(Arrays.asList("TODO"));
		}
	}

	public static class SalidaConsultarSaldoCuentaInversion extends BcsPojo{
		public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL;
		public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA;
		public List<SaldoPorFechaMovimiento> P_OUT_SALDOPORFECHAMOVIMIENTO;
		public Long P_OUT_INFORMACIONCONTADOR;
		public List<InformacionDetalle> P_OUT_INFORMACIONDETALLE;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciasDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErroresDetalle> P_OUT_ERRORESDETALLE;
		
		public SalidaConsultarSaldoCuentaInversion(){
			this.P_OUT_SALDOINICIALFINAL = new ArrayList<SaldoInicialFinal>();
			this.P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<SaldoPorFechaEfectiva>();
			this.P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<SaldoPorFechaMovimiento>();
			this.P_OUT_INFORMACIONDETALLE = new ArrayList<InformacionDetalle>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciasDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErroresDetalle>();
		}
	}

	public static class OtrosSaldos extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaSaldo;
	}

	public static class MontoEnTransitoAporte extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaEmision;
	}

	public static class InfoCuentaInversion extends BcsPojo{
		public Long IdCuentaInversion;
	    public Long IdEjecutivo;
	    public String NombreEjecutivo;
	    public String CodigoSucursal;
	    public String NombreSucursal;
	    public Long IdPersona;
	    public String DocumentoIdentidad;
	    public String NombreCliente;
	    public String NombrePortafolio;
	    public String PropositoPortafolio;
	    public String CodigoPortafolio;
	    public String CodigoMoneda;
	    public String NombreMoneda;
	    public String CuentaInversionPrincipal;
	    public String IdMoneda;
	    public Long IdPortafolio;
	    public String Producto;
	    public Long IdMatrizAgrupador;
	    public String EsADC;
	    public String Relacion;
	    public String TipoCliente;
	    public String CodigoEjecutivo;
	    public Long IdProductoAgrupador;
	}

	public static class InformacionDetalle extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}

	public static class AdvertenciasDetalle extends BcsPojo{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErroresDetalle extends BcsPojo{
		public String Codigo;
		public String Descripcion;
	}

	public static class CuentaInversion_Proceso extends BcsPojo{
		public Long IdTrabajo;
		public Long IdEjecutivo;
		public Long IdPortafolio;
		public String IdMoneda;
		
		@Override
		public boolean equals(Object b) {
			CuentaInversion_Proceso a = (CuentaInversion_Proceso)b;
			return (this.IdTrabajo == null && a.IdTrabajo == null || this.IdTrabajo.equals(a.IdTrabajo)) && (this.IdMoneda == null || this.IdMoneda.equals(a.IdMoneda)) 
					&& (this.IdPortafolio == null || this.IdPortafolio.equals(a.IdPortafolio)) || (this.IdEjecutivo == null || this.IdEjecutivo.equals(a.IdEjecutivo));
		}
		
		@Override
		public int hashCode() {
			return ((this.IdEjecutivo==null?0:this.IdEjecutivo) + (this.IdMoneda == null?"":this.IdMoneda) + (this.IdPortafolio == null?0:this.IdPortafolio) + (this.IdTrabajo == null?0:this.IdTrabajo)).hashCode();
		}
	}

	public static class InstrumentosMoneyMarket extends BcsPojo{
		public String Nemotecnico;
		public Long IdInstrumento;
	}

	public static class CantidadCustodia extends BcsPojo{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal CantidadCustodia;
		public String Nemotecnico;
	}
}
