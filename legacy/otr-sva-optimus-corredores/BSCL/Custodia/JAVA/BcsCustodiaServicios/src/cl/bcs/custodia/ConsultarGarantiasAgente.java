package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarGarantiasAgente implements JavaCall {
	
	private final String SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/custodia/sql/ConsultarGarantiasAgente_InsertIdPortafolioTablaPaso.sql");
	private final String SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/custodia/sql/ConsultarGarantiasAgente_InsertFullIdPortafolioTablaPaso.sql");
	
	private final String SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/custodia/sql/ConsultarGarantiasAgente_DeleteIdPortafolioTablaPaso.sql");
	private final String SQL_SELECT_OPERACIONES = loadResourceToString("cl/bcs/custodia/sql/ConsultarGarantiasAgente_SelectOperaciones.sql");
	
	static final Logger logger = LogManager.getLogger(ConsultarGarantiasAgente.class.getName());
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarGarantiasAgente in = gson.fromJson(jin.toString(), EntradaConsultarGarantiasAgente.class);
		SalidaConsultarGarantiasAgente out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
		public Portafolio(Long idPortafolio, Long IdTrabajo) {
			this.IdPortafolio = idPortafolio;
			this.IdTrabajo = IdTrabajo;
		}
		public Portafolio() {}
	}
	
	public static class EntradaConsultarGarantiasAgente{
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		//cliente
		public Long		p_IdPortafolio;
		public Long 	p_IdCliente;
		public Long 	p_Producto;
		
		//filtros
		public String p_IdTipoInstrumentoGarantizado;
		public String p_IdInstrumentoGarantizado;
		public String p_IdContraparteGarantia;
		public String p_IdOrigenGarantia;
		public String p_IdConcepto;
		public String p_IdMonedaSistema;
		public String p_IdInstrumento;
		public String p_FolioCartera;
		public String p_FechaConsulta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultar{
		//lista resultado consulta
		public Long	IdGarantia;
		public Long	FolioGarantia;
		public Long	FolioCarteraOrigen;
		public Long	FolioCarteraGarantizada;
		public String	EsEspecifica;
		public Long	IdInstrumentoCarteraGarantizada;
		public String	NemoGarantizado;
		public String	IdTipoInstrumentoGarantizado;
		public Long	IdContraparteGarantia;
		public String	RazonSocialContraparte;
		public String	IdOrigenGarantia;
		public String	NombreOrigenGarantia;
		public Long	IdEspecieGarantia;
		public String	DescripcionEspecie;
		public Timestamp	FechaIngresoRegistro;
		public Timestamp	FechaModificacionRegistro;
		public Long	IdEmpresa;
		public Long	IdUsuario;
		public String	IdModulo;
		public Long	IdCliente;
		public Long	IdPortafolio;
		public String	NombreCliente;
		public String	DocumentoIdentidad;
		public String	CodigoPortafolio;
		public String	IdEstado;
		public String	NombreEstado;
		public String	IdTipoProductoOperacionOrigen;
		public String	IdTipoCanalIngreso;
		public String	IdMonedaSistema;
		public String	NombreMonedaSistema;
		public String	CodigoIFRS;
		public String	IdConcepto;
		public String	NombreConcepto;
		public String	IdTipoOperacionOperacionOrigen;
		public Date	FechaOperacion;
		public Date	FechaVencimientoOperacion;
		public Long	PlazoEnDiasOperacion;
		public String	FolioExternoReferencia;
		public Long	IdMesaOperacionOrigen;
		public String	IdBolsaOperacionOrigen;
		public Long	IdSucursal;
		public Long	IdAgente;
		public String	IdCondicionLiquidacion;
		public Date	FechaLiquidacion;
		public String	IdClasificacionTipoInstrumento;
		public String	ClasificacionTipoInstrumento;
		public String	IdTipoInstrumento;
		public String	NombreTipoInstrumento;
		public Long	IdInstrumento;
		public String	Nemotecnico;
		public String	IdMonedaEmision;
		public String	NombreInstrumento;
		public String	IdEmisor;
		public String	NemoSVS;
		public String	IdTipoReajuste;
		public Date	FechaEmisionInstrumento;
		public Date	FechaVencimientoInstrumento;
		public String	EsFungible;
		public String	IdBovedaOperacionOrigen;
		public String	IdMonedaInstrumento;
		public BigDecimal	MontoGarantiaMonedaInstrumento;
		public String	NombreMonedaInstrumento;
		public String	IdTipoComprobanteOperacionOrigen;
		public Long	FolioComprobanteOperacionOrigen;
		public Date	FechaComprobanteOperacionOrigen;
		public BigDecimal	Cantidad;
		public BigDecimal	PrecioGarantia;
		public BigDecimal	TasaGarantia;
		public BigDecimal	MontoGarantia;
		public BigDecimal	CorteMinimo;
		public BigDecimal	ValorRescate;
		public BigDecimal	PrecioCompraOperacionOrigen;
		public BigDecimal	TasaCompraOperacionOrigen;
		public BigDecimal	PrecioPromedioCompraOperacionOrigen;
		public BigDecimal	MontoPrecioPromedioCompraOperacionOrigen;
		public BigDecimal	MontoPrecioPromedioMonedaInstrumentoOperacionOrigen;
		public BigDecimal	ValorTipoCambioCierre;
		public Date	INGFechaOperacion;
		public Date	INGFechaLiquidacion;
		public BigDecimal	INGCantidad;
		public BigDecimal	CantidadGarantia;
		public BigDecimal	INGPrecioGarantia;
		public BigDecimal	INGTasaGarantia;
		public BigDecimal	INGValorRescate;
		public BigDecimal	INGMonto;
		public BigDecimal	INGMontoATasaCompraActual;
		public BigDecimal	INGMontoCompraActualMonedaInstrumento;
		public BigDecimal	INGMontoGarantiaActualMonedaInstrumento;
		public BigDecimal	INGMontoGarantiaProximoMonedaInstrumento;
		public BigDecimal	INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal	INGMontoMercadoProximoMonedaInstrumento;
		public BigDecimal	MontoValorizadoMonedaInstrumento;
		public BigDecimal	MontoValorizado;
		public BigDecimal	INGMontoATasaGarantiaActual;
		public BigDecimal	INGMontoATasaCompraAnterior;
		public BigDecimal	INGPrecioMercadoActual;
		public BigDecimal	INGTasaMercadoActual;
		public BigDecimal	INGMontoATasaMercadoActual;
		public BigDecimal	INGMontoATasaMercadoAnterior;
		public String	IdTipoInstrumentoAval;
		public String	NombreTipoInstrumentoAval;
		public String	IdClasificacionTipoInstrumentoAval;
		public String	NombreClasificacionTipoInstrumentoAval;
		public String	ValorClasificacionRiesgo;
		public String	IdEntidadClasificadoraRiesgo;
		public String	IdTipoClasificacionRiesgo;
		public BigDecimal	INGDurationCompraActual;
		public BigDecimal	INGPrecioMercadoProximo;
		public BigDecimal	INGTasaMercadoProximo;
		public BigDecimal	INGMontoATasaCompraProximo;
		public BigDecimal	INGMontoATasaMercadoProximo;
		public BigDecimal	INGDurationMercadoActual;
		public BigDecimal	INGMontoInteresCompraProximo;
		public BigDecimal	INGDurationCompraProximo;
		public BigDecimal	INGMontoReajusteCompraProximo;
		public String	FuenteTasaPrecioMercado;
		public Timestamp	FechaValorizacionGarantia;
		public BigDecimal	INGPrecioMercadoAnterior;
		public BigDecimal	EGRCantidad;
		public BigDecimal	EGRMonto;
		public BigDecimal	EGRMontoMonedaInstrumento;
		public BigDecimal	EGRMontoMonedaAdicional;
		public Date	EGRFechaLiquidacion;
		public Date	EGRFechaOperacion;
		public String	ISIN;
		public BigDecimal	MontoInteresCorrido;
		public BigDecimal	MontoReajusteCorrido;
		public BigDecimal	MontoInteresCorridoMercado;
		public BigDecimal	MontoReajusteCorridoMercado;
		public Date	INGFechaValorizacionAnterior;
		public Date	INGFechaValorizacionActual;
		public BigDecimal	ValorNominal;
		public String	EGRIdConcepto;
		public Long	IdCuentaCustodioPortafolio;
		public Long	IdEntidadDestinoGarantiaEmpresa;
		public Long	IdOperacionOrigenGarantia;
		public String	TipoCarteraNormativa;
		public String	ConCertificadoAutomatico;
		public String	IdRegimenFondoMutuo;
		public BigDecimal	INGMontoMercadoAnteriorMonedaInstrumento;
		public BigDecimal	INGMontoCompraAnteriorMonedaInstrumento;
		public Long	FolioComprobanteGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarGarantiasAgente{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<SalidaConsultar> P_OUT_OPERACIONES;
		
		public SalidaConsultarGarantiasAgente() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_OPERACIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
	}
	
	private void insertTablaPaso(Connection conn, EntradaConsultarGarantiasAgente entrada, Long i_idTrabajo) throws Exception {
		SQL _sql = new SQL();

		List<Long> listIdPortafolio = new ArrayList<Long>();
		logger.debug("INICIO PORTAFOLIO EJECUTIVO");
		try {
			if(entrada.p_IdPortafolio != 0) 
			{
				listIdPortafolio.add(entrada.p_IdPortafolio);
				logger.debug("INSERT TABLA DE PASO");
				//se utiliza una tabla de paso para guardar idsPortafolio
				List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = id;
					portafolio.IdTrabajo = i_idTrabajo;
					return portafolio;
				}).collect(Collectors.toList());
				
				_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
			}
			else 
			{
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					logger.debug("INSERT TABLA DE PASO");
					//se utiliza una tabla de paso para guardar idsPortafolio
					List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
						Portafolio portafolio = new Portafolio();
						portafolio.IdPortafolio = id;
						portafolio.IdTrabajo = i_idTrabajo;
						return portafolio;
					}).collect(Collectors.toList());
					
					_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
				}
				else {
					logger.debug("No restringe cartera");
					
					Portafolio portafolio = new Portafolio();
					portafolio.IdPortafolio = null;
					portafolio.IdTrabajo = i_idTrabajo;
					List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
					idsPortafolioTablaPaso.add(portafolio);
					
					_sql.batch(conn, SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
					
				}
				
			}
		}
		catch (Exception e) {
			logger.debug("ERROR INSERT TABLA DE PASO - " + e.getMessage());
			throw e;
		}
		logger.debug("FIN PORTAFOLIO EJECUTIVO");
		
	}
	
	
	@SuppressWarnings("deprecation")
	public SalidaConsultarGarantiasAgente logica(Connection conn, EntradaConsultarGarantiasAgente entrada) throws Exception{
		SalidaConsultarGarantiasAgente salida = new SalidaConsultarGarantiasAgente();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		logger.info("INICIO CONSULTA GARANTIAS");
		SQL _sql = new SQL();
		
		//ingresa portafolios a tabla de paso de cliente correspondientes al ejecutivo 
		insertTablaPaso(conn, entrada, idTrabajo); 
		
		try {
			//logica consulta
			int varIdTipoInstrumentoGarantizado = 0;
			int varIdInstrumentoGarantizado = 0;
			int varIdContraparteGarantia = 0;
			int varIdOrigenGarantia = 0;
			int varIdConcepto = 0;
			int varIdMonedaSistema = 0;
			int varIdInstrumento = 0;
			int varFolioCartera = 0;
			int varFechaConsulta = 0;
			
			if(("").equals(entrada.p_IdTipoInstrumentoGarantizado) || entrada.p_IdTipoInstrumentoGarantizado == null) {

				entrada.p_IdTipoInstrumentoGarantizado = "";
				varIdTipoInstrumentoGarantizado = 1;
			}
			if(("").equals(entrada.p_IdInstrumentoGarantizado) || entrada.p_IdInstrumentoGarantizado == null) {
				entrada.p_IdInstrumentoGarantizado = "0";
				varIdInstrumentoGarantizado = 1;
				
			}
			if(("").equals(entrada.p_IdContraparteGarantia) || entrada.p_IdContraparteGarantia == null) {
				entrada.p_IdContraparteGarantia = "0";
				varIdContraparteGarantia = 1;
				
			}
			if(("").equals(entrada.p_IdOrigenGarantia) || entrada.p_IdOrigenGarantia == null) {
				entrada.p_IdOrigenGarantia = "";
				varIdOrigenGarantia = 1;
				
			}
			if(("").equals(entrada.p_IdConcepto) || entrada.p_IdConcepto == null) {
				entrada.p_IdConcepto = "";
				varIdConcepto = 1;
				
			}
			if(("").equals(entrada.p_IdMonedaSistema) || entrada.p_IdMonedaSistema == null) {
				entrada.p_IdMonedaSistema = "";
				varIdMonedaSistema = 1;
				
			}
			if(("").equals(entrada.p_IdInstrumento) || entrada.p_IdInstrumento == null) {
				entrada.p_IdInstrumento = "0";
				varIdInstrumento = 1;
				
			}
			if(("").equals(entrada.p_FolioCartera) || entrada.p_FolioCartera == null) {
				entrada.p_FolioCartera = "0";
				varFolioCartera = 1;
				
			}
			if(("").equals(entrada.p_FechaConsulta) || entrada.p_FechaConsulta == null) {
				entrada.p_FechaConsulta = "";
				varFechaConsulta = 1;
				
			}
			
			HashMap<String,Object> params =  new HashMap<>();
			params.put("IdTrabajo", idTrabajo);
			params.put("IdTipoInstrumentoGarantizado", entrada.p_IdTipoInstrumentoGarantizado);
			params.put("var_IdTipoInstrumentoGarantizado", varIdTipoInstrumentoGarantizado);
			params.put("IdInstrumentoGarantizado", entrada.p_IdInstrumentoGarantizado);
			params.put("var_IdInstrumentoGarantizado", varIdInstrumentoGarantizado);
			params.put("IdContraparteGarantia", entrada.p_IdContraparteGarantia);
			params.put("var_IdContraparteGarantia", varIdContraparteGarantia);
			params.put("IdOrigenGarantia", entrada.p_IdOrigenGarantia);
			params.put("var_IdOrigenGarantia", varIdOrigenGarantia);
			params.put("IdConcepto", entrada.p_IdConcepto);
			params.put("var_IdConcepto", varIdConcepto);
			params.put("IdMonedaSistema", entrada.p_IdMonedaSistema);
			params.put("var_IdMonedaSistema", varIdMonedaSistema);
			params.put("IdInstrumento", entrada.p_IdInstrumento);
			params.put("var_IdInstrumento", varIdInstrumento);
			params.put("FolioCartera", entrada.p_FolioCartera);
			params.put("var_FolioCartera", varFolioCartera);
			params.put("FechaConsulta", entrada.p_FechaConsulta);
			params.put("var_FechaConsulta", varFechaConsulta);
					
			salida.P_OUT_OPERACIONES = _sql.query(conn, SQL_SELECT_OPERACIONES, params, SalidaConsultar.class);
			
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
		finally {
			Portafolio portafolio = new Portafolio();
			portafolio.IdPortafolio = null;
			portafolio.IdTrabajo = idTrabajo;
			List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
			idsPortafolioTablaPaso.add(portafolio);
			
			_sql.batch(conn, SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
			
		}
				
		logger.info("FIN CONSULTA GARANTIAS");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();			
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"MSP","p_IdEmpleadoConsulta":7889,"p_IdPortafolio":0,"p_IdCliente":0,"p_Producto":"0","p_Mesa":"MEME","p_FechaDesde":"2021-04-13T00:00:00","p_FechaHasta":"2021-04-13T23:59:59","p_IdTipoOperacion":"0"}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":7305,\"p_IdPortafolio\":0,\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-04-13T23:59:59\",\"p_IdTipoOperacion\":\"COMPRA\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_IdPortafolio\":0,\"p_IdCliente\":0,\"p_Producto\":\"0\",\"p_IdTipoInstrumentoGarantizado\":\"\",\"p_IdInstrumentoGarantizado\":\"\",\"p_IdContraparteGarantia\":\"\",\"p_IdOrigenGarantia\":\"\",\"p_IdMonedaSistema\":\"\",\"p_IdInstrumento\":\"\",\"p_FolioCartera\":\"\",\"p_FechaConsulta\":\"2021-04-14\"}";
			EntradaConsultarGarantiasAgente Objin = gson.fromJson(json, EntradaConsultarGarantiasAgente.class);
			ConsultarGarantiasAgente demo = new ConsultarGarantiasAgente();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				SalidaConsultarGarantiasAgente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA:" + ObjOut.P_OUT_OPERACIONES.size());
				
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	

}
