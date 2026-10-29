package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSimultaneasVigentes implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ConsultarSimultaneasVigentes.class);
	
	private final String SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ACTUAL	= loadResourceToString("cl/bcs/custodia/sql/ConsultarSimultaneasVigentes_SelectSimultaneasVigentesFechaActual.sql");
	private final String SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ANTERIOR	= loadResourceToString("cl/bcs/custodia/sql/ConsultarSimultaneasVigentes_SelectSimultaneasVigentesFechaAnterior.sql");
	
	private final String Actual = "ACT";

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarSimultaneasVigentes jin = gson.fromJson(in.toString(),EntradaConsultarSimultaneasVigentes.class);
		SalidaConsultarSimultaneasVigentes out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class EntradaConsultarSimultaneasVigentes extends BcsPojo{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_FechaConsulta;
		public String	p_TipoFecha;
		//Filtros
		public Long		p_IdCliente;
		public Long		p_IdPortafolio;
		public String	p_IdTipoOperacion;
		public Long		p_IdInstrumentoSubyacente;
		public String	p_PaloNormal;
		public String	p_PaloPlazo;
		public EntradaConsultarSimultaneasVigentes() {
			this.p_IdCliente				= 0L;
			this.p_IdPortafolio				= 0L;
			this.p_IdTipoOperacion			= "";
			this.p_IdInstrumentoSubyacente	= 0L;
			this.p_PaloNormal				= "";
			this.p_PaloPlazo				= "";
		}
	}
	
	public static class SalidaConsultarSimultaneasVigentes extends BcsPojo{
		public ArrayList<MensajeSalida>						P_OUT_MENSAJES;
		public Long 										P_OUT_INFORMACIONCONTADOR;
		public Long 										P_OUT_ADVERTENCIASCONTADOR;
		public Long 										P_OUT_ERRORESCONTADOR;
		public ArrayList<SalidaSimultaneasVigentes>			P_OUT_TABLA;
		public SalidaConsultarSimultaneasVigentes() {
			this.P_OUT_ERRORESCONTADOR =  0L;
			this.P_OUT_TABLA 			= new ArrayList<SalidaSimultaneasVigentes>();
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SalidaSimultaneasVigentes extends BcsPojo{
		public Long			FolioCartera;			
		public String		AccionOperacion;
		public String		AccionOperacionPlazo;
		public Date			FechaInversion;
		public Date			FechaVencimientoOperacion;
		public BigDecimal	MontoCompra;
		public BigDecimal	MontoAPlazo;
		public Date			EGRFechaOperacion;
		public BigDecimal	INGCantidad;
		public BigDecimal	INGPrecioCompra;
		public BigDecimal	INGTasaCompra;
		public BigDecimal	INGMontoATasaCompraActual;
		public BigDecimal	INGMontoAPlazo;
		public Date			INGFechaLiquidacion;
		public Date			EGRFechaLiquidacion;
		public String		suby_Nemotecnico;
		public Long			FolioComprobante;
		public String		suby_NombreTipoInstrumento;
		public String		Cliente;
		public String		Proposito;
		public String		CodigoPortafolio;
		public Date			FechaLiquidacion;
		public String		IdEstado;
		public String		PaloNormal;
		public String		PaloPlazo;
		public String		NombreCorredora;
		public Long			IdCorredoraContraparte;
		public String		IdBolsa;
		public String		NombreEjecutivo;
		public Date			FechaLiquidacionMovtoContado;
		public BigDecimal	PrecioInicial;
		public BigDecimal	PrecioAPlazo;
		public BigDecimal	suby_INGPrecioMercadoActual;
		public BigDecimal	PrecioActualCalculado;
		public String		NombreLargo;
		public String		IdCondicionLiquidacion;
		public String		IdTipoOperacion;
		public String		IdTipoComprobante;
		public String		IdTipoInstrumentoOperacion;
		public String		IdTipoProducto;
		public BigDecimal	INGMontoInteresCompraAnterior;
		public BigDecimal	INGMontoInteresCompraAnteriorMonedaInstrumento;
		public BigDecimal	INGMontoInteresCompraAnteriorMonedaAdicional;
		public BigDecimal	INGMontoInteresCorridoCompraActual;
		public BigDecimal	INGMontoInteresCorridoCompraActualMonedaInstrumento;
		public BigDecimal	INGMontoInteresCorridoCompraActualMonedaAdicional;
		public BigDecimal	INGMontoInteresCompraProximo;
		public BigDecimal	INGMontoInteresCompraProximoMonedaInstrumento;
		public BigDecimal	INGMontoInteresCompraProximoMonedaAdicional;
		public BigDecimal	INGMontoInteresMercadoAnterior;
		public BigDecimal	INGMontoInteresMercadoAnteriorMonedaInstrumento;
		public BigDecimal	INGMontoInteresMercadoAnteriorMonedaAdicional;
		public BigDecimal	INGMontoInteresCorridoMercadoActual;
		public BigDecimal	INGMontoInteresCorridoMercadoActualMonedaInstrumento;
		public BigDecimal	INGMontoInteresCorridoMercadoActualMonedaAdicional;
		public BigDecimal	INGMontoInteresMercadoProximo;
		public BigDecimal	INGMontoInteresMercadoProximoMonedaInstrumento;
		public BigDecimal	INGMontoInteresMercadoProximoMonedaAdicional;
		public String		DocumentoIdentidad;
		public BigDecimal	EGRCantidad;
		public BigDecimal	EGRCostoRetiroenFactorAjuste;
		public Date			EGRFechaComprobante;
		public String		suby_IdMonedaInstrumento;
		public String 		NumeroDias;
		public String 		FlagDeterioro;
		public BigDecimal 	PorcentajeDeterioro;
		public BigDecimal 	Deterioro;
		public Integer 		Fase;
		public String 		NombreLargoCliente;
	}
	
	public static class FiltrosConsulta extends BcsPojo{
		Long FiltroCliente;
		Long FiltroPortafolio;
		Long FiltroTipoOperacion;
		Long FiltroInstrumentoSubyacente;
		Long FiltroPalo;
		public FiltrosConsulta() {
			this.FiltroCliente					= 1L;
			this.FiltroPortafolio				= 1L;
			this.FiltroTipoOperacion			= 1L;
			this.FiltroInstrumentoSubyacente	= 1L;
			this.FiltroPalo						= 1L;

		}
	}
	
	public SalidaConsultarSimultaneasVigentes logica (Connection con, EntradaConsultarSimultaneasVigentes in) throws Exception {
		logger.info("INICIO::ConsultarSimultaneasVigentes");
		
		SalidaConsultarSimultaneasVigentes out = new SalidaConsultarSimultaneasVigentes();
		
		SQL sql = new SQL();
		
		//Filtros
		FiltrosConsulta filtros = new FiltrosConsulta();
		
		if(in.p_IdCliente != null && in.p_IdCliente.compareTo(0L) == 1) {
			filtros.FiltroCliente = 0L;
		}
		if(in.p_IdPortafolio != null && in.p_IdPortafolio.compareTo(0L) == 1) {
			filtros.FiltroPortafolio = 0L;
		}
		if(in.p_IdTipoOperacion != null && in.p_IdTipoOperacion.trim().length() > 0) {
			filtros.FiltroTipoOperacion = 0L;
		}
		if(in.p_IdInstrumentoSubyacente != null && in.p_IdInstrumentoSubyacente.compareTo(0L) == 1) {
			filtros.FiltroInstrumentoSubyacente = 0L;
		}
		if(in.p_PaloNormal != null && in.p_PaloNormal.trim().length() > 0) {
			filtros.FiltroPalo = 0L;
		}
		if(in.p_PaloPlazo != null && in.p_PaloPlazo.trim().length() > 0) {
			filtros.FiltroPalo = 0L;
		}
		
		List<SalidaSimultaneasVigentes> simultaneasVigentes = new ArrayList<>();
		
		if(Actual.equalsIgnoreCase(in.p_TipoFecha)) {
			simultaneasVigentes = consultarSimultaneasVigentesFechaActual(con, in, sql, filtros);
		}else {
			simultaneasVigentes = consultarSimultaneasVigentesFechaAnterior(con, in, sql, filtros);
		}
		out.P_OUT_TABLA.addAll(simultaneasVigentes);
		
		logger.info("FIN::ConsultarSimultaneasVigentes");
		return out;
		
	}
	private List<SalidaSimultaneasVigentes> consultarSimultaneasVigentesFechaActual(Connection con, EntradaConsultarSimultaneasVigentes in, SQL sql, FiltrosConsulta filtros) throws SQLException{
		logger.debug("INICIO::ConsultarSimultaneasVigentes.consultarSimultaneasVigentesFechaActual");
		long inicio = System.currentTimeMillis();
		List<SalidaSimultaneasVigentes> simultaneasVigentes = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(15);
		
		param.put("FechaVencimientoOperacion", in.p_FechaConsulta);
		param.put("INGFechaOperacion", in.p_FechaConsulta);
		param.put("IdEstado", "VIG");
		param.put("EGRCantidad", BigDecimal.ZERO);
		param.put("IdCliente", in.p_IdCliente);
		param.put("IdPortafolio", in.p_IdPortafolio);
		param.put("IdTipoOperacion", in.p_IdTipoOperacion);
		param.put("IdInstrumentoSubyacente", in.p_IdInstrumentoSubyacente);
		param.put("PaloNormal", in.p_PaloNormal);
		param.put("PaloPlazo", in.p_PaloPlazo);
		
		param.put("FiltroCliente", filtros.FiltroCliente);
		param.put("FiltroPortafolio", filtros.FiltroPortafolio);
		param.put("FiltroTipoOperacion", filtros.FiltroTipoOperacion);
		param.put("FiltroInstrumentoSubyacente", filtros.FiltroInstrumentoSubyacente);
		param.put("FiltroPalo", filtros.FiltroPalo);

		simultaneasVigentes = sql.query(con, SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ACTUAL , param , SalidaSimultaneasVigentes.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSimultaneasVigentes.consultarSimultaneasVigentesFechaActual tiempo: " + (fin - inicio));
		return simultaneasVigentes;
	}
	
	private List<SalidaSimultaneasVigentes> consultarSimultaneasVigentesFechaAnterior(Connection con, EntradaConsultarSimultaneasVigentes in, SQL sql, FiltrosConsulta filtros) throws SQLException{
		logger.debug("INICIO::ConsultarSimultaneasVigentes.consultarSimultaneasVigentesFechaAnterior");
		long inicio = System.currentTimeMillis();
		List<SalidaSimultaneasVigentes> simultaneasVigentes = new ArrayList<>();
		HashMap<String, Object> param = new HashMap<>(14);
		
		param.put("FechaVencimientoOperacion", in.p_FechaConsulta);
		param.put("EGRFechaOperacion", in.p_FechaConsulta);
		param.put("INGFechaOperacion", in.p_FechaConsulta);
		param.put("IdCliente", in.p_IdCliente);
		param.put("IdPortafolio", in.p_IdPortafolio);
		param.put("IdTipoOperacion", in.p_IdTipoOperacion);
		param.put("IdInstrumentoSubyacente", in.p_IdInstrumentoSubyacente);
		param.put("PaloNormal", in.p_PaloNormal);
		param.put("PaloPlazo", in.p_PaloPlazo);
		
		param.put("FiltroCliente", filtros.FiltroCliente);
		param.put("FiltroPortafolio", filtros.FiltroPortafolio);
		param.put("FiltroTipoOperacion", filtros.FiltroTipoOperacion);
		param.put("FiltroInstrumentoSubyacente", filtros.FiltroInstrumentoSubyacente);
		param.put("FiltroPalo", filtros.FiltroPalo);
		
		simultaneasVigentes = sql.query(con, SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ANTERIOR , param , SalidaSimultaneasVigentes.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarSimultaneasVigentes.consultarSimultaneasVigentesFechaAnterior tiempo: " + (fin - inicio));
		return simultaneasVigentes;
	}
	
}
