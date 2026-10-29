package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.ordenes.ModificarOrdenes.ModificarOrdenes_Entrada;
import cl.bcs.ordenes.ModificarOrdenes.ModificarOrdenes_Salida;
import cl.bcs.ordenes.ModificarOrdenes.Orden_Entrada;
import cl.bcs.ordenes.pojo.Asociacion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearAsociaciones implements JavaCall { 
	
	private final Log log = new Log("ORD.CrearAsociaciones");
	private final ConstantesOrdenes _C = new ConstantesOrdenes();
	private final String SQL_SELECT_ESTADO_MODULO  = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_INSERT_ORDEN_ASOCIADA = loadResourceToString("cl/bcs/ordenes/sql/CrearAsociaciones_InsertOrdenAsociada.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearAsociaciones_SelectOrdenes.sql");

	
	public static class CrearAsociaciones_Entrada {
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		
		public String	p_simularOperacion;
		public String	p_UsaReglasNegocio;

		
		public List<Asociacion_Entrada> p_Asociaciones;
		
		public CrearAsociaciones_Entrada() {
			this.p_simularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
			this.p_Asociaciones = new ArrayList<Asociacion_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Asociacion_Entrada {
		public Long 		IdInterno;
		public Long 		IdOrdenN;//Orden reportado
		public Long 		IdOrdenM;//Orden reportante
		public Long 		IdOrdenMOrigen = 0L;//Orden reportante
		public BigDecimal	Precio;
		public String		ModalidadCalculo;
		public BigDecimal	Monto;
		public BigDecimal	Cantidad;
		public BigDecimal	Tasa;
				
		public Asociacion_Entrada() {
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class CrearAsociaciones_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<Resultado_Salida> p_out_Resultado;
				
		public CrearAsociaciones_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.p_out_Resultado = new ArrayList<Resultado_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class Resultado_Salida {
		public Long 		IdInterno;
		public Long 		IdOrdenAsociada;
		public BigDecimal	Monto;
		public BigDecimal	Cantidad;			
		public String Msg;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ordenObj {
		public Long 		IdOrden;
		
		public ordenObj(Long idOrden) {
			IdOrden = idOrden;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Orden {
		public Long IdOrden;
		public String FolioOrden;
		public Date FechaRecepcion;
		public Date FechaIngresoRegistro;
		public Date FechaIngresoRegistroCorta;
		public Date FechaSolicitudAnulacion;
		public Date FechaAnulacion;
		public Date FechaOferta;
		public Date FechaOperacionOrden;
		public Date FechaVigencia;
		public Date FechaVencimiento;
		public Date FechaEmision;
		public Date FechaLiquidacion;
		public BigDecimal MontoLiquidacion;
		public Date FechaAceptacion;
		public Date FechaAutorizacion;
		public String IdTipoVigenciaOrden;
		public String NombreTipoVigenciaOrden;
		public String GlosaOrden;
		public String IdEstadoOrden;
		public String EstadoOrden;
		public BigDecimal CantidadOrden;
		public BigDecimal MontoOrden;
		public String IdBoveda;
		public String IdFormaCustodia;
		public String NombreFormaCustodia;
		//public BigDecimal PorcentajeVisibilidad;
		public BigDecimal ValorRescate;
		public String IdTipoReajuste;
		public BigDecimal PrecioOrden;
		public BigDecimal PrecioMercado;
		public BigDecimal Tasa;
		public BigDecimal TasaSimultanea;
		public BigDecimal TasaVentaCorta;
		public BigDecimal TasaRFIF;
		public BigDecimal TasaReporte;
		public Long Plazo;
		public Long PlazoVentaCorta;
		public Long PlazoSimultanea;
		public Long PlazoIF;
		public String EsVentaCorta;
		public String IndicadorFacturacionInmediata; 
		public String IdTipoPrecio;
		public String IdTipoCanalDistribucion;
		public String EspecificaBolsa;
		public String Reservado;
		public String Bloqueado;
		public String TipoPrecio;
		public String IdTipoTasa;
		public String IdCondicionDeLiquidacion;
		public String IdTipoOperacion;
		public String IdTipoOperacionRelacionada;
		public String IdTipoProducto;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdGrupoCobro;
		public String IdTipoCanalIngreso;
		public Long IdLibroOrdenes;
		public String IdEmisor;
		public Long IdEmpresa;
		public String IdBolsa;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdEstadoAsignacion;
		public String IdMonedaInstrumento;
		public String IdMonedaOrden;
		public String IdMonedaLiquidacionOrden;
		public BigDecimal CantidadDisponibleOrden;
		public BigDecimal CantidadAsignadaOrden;
		public BigDecimal MontoDisponibleOrden;
		public BigDecimal MontoAsignadoOrden;
		public Long IdEmpleadoPortafolio;
		public Long IdEmpleadoDigitador;
		public String CodigoClaseOrden;
		public BigDecimal MontoDisponibleAsociar;
		public String EsConfirmada;
		public BigDecimal TasaReportante;
		public BigDecimal MontoOrdenPlazo;
		public BigDecimal PrecioPlazo;
		public BigDecimal MontoLiquidacionPlazo;
		public BigDecimal MontoLocalPlazo;
		public BigDecimal CantidadDisponibleAsociar;
		public BigDecimal MontoAsociado;
		public BigDecimal CantidadAsociada;
		public BigDecimal TasaPromedioReporte;
		public BigDecimal TasaReportanteReferencia;
		public String IdMedioDePago;
		public BigDecimal MontoInteresContado;
		public BigDecimal MontoInteresPlazo;
		public Long IdCuentaCustodioPortafolio;
		public Long BaseTasa;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ObjOrdenesAsocEntrada {
		public Long			IdInterno;
		public Long			IdOrden;
		public String		FechaRecepcion;
		public String		FechaVigencia;
		public String		FechaEmision;
		public String		FechaOperacion;
		public String		IdTipoVigencia;
		public String		Glosa;
		public BigDecimal	Cantidad;
		public BigDecimal	MontoNeto;
		public BigDecimal	ValorRescate;
		public String		IdBoveda;
		public String		IdFormaCustodia;
		public Long			IdPorcentajeVisibilidad;
		public BigDecimal	PrecioLimite;
		public String		EsFacturaInmediata;
		public String		IdTipoPrecio;
		public Long			IdPortafolio;
		public String		IdTipoLibroOrden;
		public Long			IdLibroOrdenes;
		public String		IdTipoCanalIngreso;
		public String		IdTipoReajuste;
		public String		IdEmisor;
		public String		IdBolsa;
		public String		EspecificaBolsa;
		public String		IdMonedaInstrumento;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal	TipoCambio;
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public String		IdCondicionDeLiquidacionPlazo;
		
		public BigDecimal	TasaVentaCorta = BigDecimal.ZERO;
		public BigDecimal	TasaSimultanea = BigDecimal.ZERO;
		public BigDecimal	TasaRFIF = BigDecimal.ZERO;
		
		public Long			PlazoIF = 0L;
		public Long			PlazoSimultanea = 0L;
		public Long			PlazoVentaCorta = 0L;
		
		public String		EsVentaCorta = "N";
		public String		EsOD = "N";
		public String		EsFungible = "N";
		public Long			IdCorredorCorresponsal;
		public Long			IdPersonaAutorizadoDarOrdenes;
		public String		IdTipoCanalDistribucion;
		public BigDecimal	TasaReporte = BigDecimal.ZERO;
		public Long			PlazoReporte = 0L;
		public Long			BaseTasa = 0L; 
		public String		TipoInteres; //C o S (Compuesto Simple) 
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoOperacion;
		public Long 		IdCuentaCustodioPortafolio;
		public String		EsPorMonto = "N";
		public String		EsOrdenEspecial = "N";
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public String		IdMecanismoCaptacion = "";
		public BigDecimal	TasaPromedioReporte = BigDecimal.ZERO;
		public BigDecimal	TasaReportanteReferencia = BigDecimal.ZERO;
		public String 		NemotecnicoSVS = "";
		
		public String 		IdSistemaBursatil;
		public String 		InstrumentoAcreditado = "N";
		
		public String		FormadorMercado = "N";
		public String		EsClienteFinal;
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}/*
	public ObjOrdenesAsocEntrada copyDataReportado(int pos, Orden ord, BigDecimal cantidad, String fechaSistema) throws Exception{
		ObjOrdenesAsocEntrada out = new ObjOrdenesAsocEntrada();
		Utils.copyPojo(ord, out);
		out.IdInterno = new Long(pos);
		out.FechaRecepcion 				= fechaSistema;
		out.FechaVigencia 				= fechaSistema;
		out.FechaEmision 				= fechaSistema;
		out.FechaOperacion 				= fechaSistema;
		out.IdTipoOperacion 			= ord.IdTipoOperacion;
		out.IdCondicionDeLiquidacion 	= ord.IdCondicionDeLiquidacion;
		out.IdTipoVigencia 				= ord.IdTipoVigenciaOrden;
		out.Glosa						= ord.GlosaOrden;
		out.Cantidad 					= cantidad;
		out.MontoNeto					= BigDecimal.ZERO;//hay que repasar desppues de ir a la paridad
		out.ValorRescate 				= BigDecimal.ZERO;
		out.IdBoveda					= ord.IdBoveda;
		out.IdFormaCustodia				= ord.IdFormaCustodia;
		out.IdPorcentajeVisibilidad 	= 1L;
		out.PrecioLimite 				= ord.PrecioOrden;
		out.EsFacturaInmediata			= "N";
		out.IdTipoPrecio				= "M";
		out.IdPortafolio				= ord.IdPortafolio;
		out.IdTipoLibroOrden			= "NORMAL";
		out.IdTipoCanalIngreso			= ord.IdTipoCanalIngreso;
		out.IdTipoReajuste				= ord.IdTipoReajuste;//"";
		out.IdEmisor					= ord.IdEmisor;
		out.IdBolsa						= ord.IdBolsa;
		out.EspecificaBolsa				= ord.EspecificaBolsa;
		out.IdMonedaLiquidacionOrden	= ord.IdMonedaLiquidacionOrden;
		out.TipoCambio					= BigDecimal.ZERO;//repaasar afuera con el tipo cambio
		out.IdInstrumento				= ord.IdInstrumento;
		out.IdInstrumentoSubyacente		= ord.IdInstrumentoSubyacente;
		out.IdCondicionDeLiquidacionPlazo	= "TP";
		out.TasaVentaCorta				= BigDecimal.ZERO;
		out.TasaSimultanea				= BigDecimal.ZERO;
		out.TasaRFIF					= BigDecimal.ZERO;
		out.PlazoIF						= 0L;
		out.PlazoSimultanea				= 0L;
		out.PlazoVentaCorta				= 0L;
		out.EsVentaCorta				= "N";
		out.EsOD						= "N";
		out.EsFungible					= "N";
		out.IdCorredorCorresponsal		= null;
		out.IdPersonaAutorizadoDarOrdenes	= null;
		out.IdTipoCanalDistribucion		= "MAN";
		out.TasaReporte					= ord.TasaReporte;
		out.TasaReportanteReferencia	= ord.TasaReportanteReferencia;
		out.TasaPromedioReporte			= ord.TasaPromedioReporte;
		out.PlazoReporte				= ord.Plazo;
		out.BaseTasa					= 0L;
		out.PagoCobroCuentaInversion	= "S";
		out.IdPreferenciaPagoPortafolio	= 0L;
		out.TipoInteres					= "C";
		out.IdMecanismoCaptacion		= "";
		out.MontoInteresContado			= BigDecimal.ZERO;
		out.MontoInteresPlazo			= BigDecimal.ZERO;
		out.IdCuentaCustodioPortafolio	= ord.IdCuentaCustodioPortafolio;
		
		return out;
	}*/
	public ObjOrdenesAsocEntrada copyDataReportante (int pos, Orden ordReportante, Orden ordReportado, BigDecimal monto, BigDecimal cantidad, BigDecimal Precio, BigDecimal Tasa, String fechaSistema, String fechaHoraSistema) throws Exception{
		ObjOrdenesAsocEntrada out = new ObjOrdenesAsocEntrada();
		
		Utils.copyPojo(ordReportante, out);
		out.IdInterno = new Long(pos);
		out.IdOrden						= ordReportante.IdOrden;
		out.FechaRecepcion 				= fechaHoraSistema;
		out.FechaVigencia 				= fechaSistema;
		out.FechaEmision 				= fechaSistema;
		out.FechaOperacion 				= fechaSistema;
		out.IdTipoOperacion 			= ordReportante.IdTipoOperacion;
		out.IdCondicionDeLiquidacion 	= ordReportado.IdCondicionDeLiquidacion;
		out.IdTipoVigencia 				= ordReportante.IdTipoVigenciaOrden;
		out.Glosa						= ordReportante.GlosaOrden;
		out.Cantidad 					= cantidad;//ordReportante.CantidadOrden;//
		out.MontoNeto					= monto;//hay que repasar desppues de ir a la paridad
		out.ValorRescate 				= BigDecimal.ZERO;
		out.IdBoveda					= ordReportado.IdBoveda;
		out.IdFormaCustodia				= ordReportante.IdFormaCustodia;
		out.IdPorcentajeVisibilidad 	= 1L;
		out.PrecioLimite 				= Precio;//ordReportante.PrecioOrden;
		out.EsFacturaInmediata			= "N";
		out.IdTipoPrecio				= "M";
		out.IdPortafolio				= ordReportante.IdPortafolio;
		out.IdTipoLibroOrden			= "NORMAL";
		out.IdTipoCanalIngreso			= ordReportante.IdTipoCanalIngreso;
		out.IdTipoReajuste				= ordReportante.IdTipoReajuste;//"";
		out.IdEmisor					= ordReportado.IdEmisor;//ordReportante.IdEmisor;
		out.IdBolsa						= ordReportante.IdBolsa;
		out.EspecificaBolsa				= ordReportante.EspecificaBolsa;
		out.IdMonedaInstrumento			= ordReportado.IdMonedaInstrumento;//ordReportante.IdMonedaInstrumento;
		out.IdMonedaLiquidacionOrden	= ordReportado.IdMonedaLiquidacionOrden;//ordReportante.IdMonedaLiquidacionOrden;
		out.TipoCambio					= BigDecimal.ZERO;//repaasar afuera con el tipo cambio
		out.IdInstrumento				= ordReportante.IdInstrumento;
		out.IdInstrumentoSubyacente		= ordReportado.IdInstrumentoSubyacente;//ordReportante.IdInstrumentoSubyacente;
		out.IdCondicionDeLiquidacionPlazo	= "TP";
		out.TasaVentaCorta				= BigDecimal.ZERO;
		out.TasaSimultanea				= BigDecimal.ZERO;
		out.TasaRFIF					= BigDecimal.ZERO;
		out.PlazoIF						= 0L;
		out.PlazoSimultanea				= 0L;
		out.PlazoVentaCorta				= 0L;
		out.EsVentaCorta				= "N";
		out.EsOD						= "N";
		out.EsFungible					= "N";
		out.IdCorredorCorresponsal		= null;
		out.IdPersonaAutorizadoDarOrdenes	= null;
		out.IdTipoCanalDistribucion		= "MAN";
		out.TasaReporte					= Tasa;//ordReportante.TasaReporte;
		out.TasaReportanteReferencia	= ordReportante.TasaReportanteReferencia;
		out.TasaPromedioReporte			= (Tasa.add(ordReportante.TasaPromedioReporte).divide(new BigDecimal(2)));
		out.PlazoReporte				= ordReportante.Plazo;
		out.BaseTasa					= ordReportante.BaseTasa;
		out.PagoCobroCuentaInversion	= "S";
		out.IdPreferenciaPagoPortafolio	= 0L;
		out.TipoInteres					= "C";
		out.IdMecanismoCaptacion		= ordReportante.CodigoClaseOrden;
		out.MontoInteresContado			= (monto.multiply(ordReportado.MontoInteresContado)).divide(ordReportado.MontoOrden,20,RoundingMode.HALF_UP);//BigDecimal.ZERO;
		out.MontoInteresPlazo			= (monto.multiply(ordReportado.MontoInteresPlazo)).divide(ordReportado.MontoOrdenPlazo,20,RoundingMode.HALF_UP);//BigDecimal.ZERO;
		out.IdCuentaCustodioPortafolio	= ordReportante.IdCuentaCustodioPortafolio;
		
		return out;
	}
	
	public Orden_Entrada copyDataReportanteUpd (int pos, Orden ordReportante, Orden ordReportado, BigDecimal monto, BigDecimal cantidad,boolean act) throws Exception{
		Orden_Entrada out = new Orden_Entrada();
		Utils.copyPojo(ordReportante, out);
		out.IdOrden				= ordReportante.IdOrden;
		out.ModificaCantidad 	= "N";
		out.Cantidad 			= cantidad;
		out.ModificaCobros 		= "N";
		out.IdAsociacion 		= 0L;
		out.TasaPromedioReporte = ordReportante.TasaPromedioReporte;
		out.ModificaTasaPromedioReporte = "S";
		out.ModificaSistemaBursatil 	= "N";
		out.IdSistemaBursatil	= "";
		out.ModificaMonto 		= "S";
		out.Monto 				= monto;
		out.Precio				= ordReportado.PrecioOrden;
		out.IdInstrumentoSubyacente  = ordReportante.IdInstrumentoSubyacente;
		out.IdEmisor				 = ordReportante.IdEmisor;
		out.IdMonedaLiquidacionOrden = ordReportante.IdMonedaLiquidacionOrden;
		out.MontoInteresContado			= (monto.multiply(ordReportado.MontoInteresContado)).divide(ordReportado.MontoOrden,20,RoundingMode.HALF_UP);
		out.MontoInteresPlazo			= (monto.multiply(ordReportado.MontoInteresPlazo)).divide(ordReportado.MontoOrdenPlazo,20,RoundingMode.HALF_UP);
		if (act){
			out.IdCondicionDeLiquidacion = ordReportado.IdCondicionDeLiquidacion;
		}
		/*out.IdInstrumentoSubyacente  = ordReportado.IdInstrumentoSubyacente;
		out.IdEmisor				 = ordReportado.IdEmisor;
		out.IdMonedaLiquidacionOrden = ordReportado.IdMonedaLiquidacionOrden;*/
				
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearAsociaciones_Entrada in = gson.fromJson(jin.toString(), CrearAsociaciones_Entrada.class);
		CrearAsociaciones_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	
	public CrearAsociaciones_Salida logica(Connection conn, CrearAsociaciones_Entrada in) throws Exception {
		SQL _sql = new SQL();
		CrearAsociaciones_Salida out = new CrearAsociaciones_Salida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");		
		
		//Procesa las asociaciones. Asumo que ya existen las ordenes de reportado y reportante.
		HashMap<String, Object> idOrd = new HashMap<>();
		List<Object> id = new ArrayList<>();
		for (Asociacion_Entrada asig: in.p_Asociaciones) {
			if (!id.contains(asig.IdOrdenM))
				id.add(asig.IdOrdenM);
			if (!id.contains(asig.IdOrdenN))
				id.add(asig.IdOrdenN);
		}
		idOrd.put("Ordenes", id);
		List<Orden> ordenesOut = _sql.query(conn, SQL_SELECT_ORDENES, idOrd, Orden.class);
		HashMap<Long, Object> ordenesOutHash = Utils.List2Hash(ordenesOut, "IdOrden");
		//validaciones basicas
		{				
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			if (in.p_Asociaciones.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ASOCIACIONES",null,conn,in.p_IdEmpresa));
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (Asociacion_Entrada asig: in.p_Asociaciones) {
				if (correlativos.contains(asig.IdInterno)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(asig.IdInterno);
				}
			}
			if (ordenesOut.size() != id.size()){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ORDENES_INVALIDAS",null,conn,in.p_IdEmpresa));
			}
			
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdModulo", _C.ID_MODULO_ORDENES);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<EstadoModulo> modulo = _sql.query(conn, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
			if (modulo.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",null,conn,in.p_IdEmpresa));
				return out;
			}
			if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_C.ESTADO_MODULO_CERRADO)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,conn,in.p_IdEmpresa));
				return out;
			}
		}
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 )
			return out;
		
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
		List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::OrdenAsociada", new Long(in.p_Asociaciones.size()));
						
		//TODO: obtiene informacion de la ordenes 
		//-- estado de ordenes
		//-- status confirmada
		//-- condiciones para hacer match
		//-- situacion actual (no asociada, parcialmente, total)
				
		//TODO: otiene asociaciones actuales de ordenN y ordenM
			
		List<ObjOrdenesAsocEntrada> ordenesCrear = new ArrayList<>();
		List<Orden_Entrada> ordenesActualizar = new ArrayList<>();
		//List<Orden> ordenesReportante = new ArrayList<>();
		for (Asociacion_Entrada asoc: in.p_Asociaciones) {
			Orden ordReportante = (Orden) ordenesOutHash.get(asoc.IdOrdenM);
			Orden ordReportado = (Orden) ordenesOutHash.get(asoc.IdOrdenN);
			if (ordReportante != null){
				BigDecimal remMonto = ordReportante.MontoDisponibleAsociar.subtract(asoc.Monto);
				BigDecimal remCantidad = ordReportante.CantidadDisponibleAsociar.subtract(asoc.Cantidad);
				BigDecimal cantidad = (asoc.Monto.multiply(new BigDecimal(100))).divide(ordReportante.MontoDisponibleAsociar,0,RoundingMode.HALF_DOWN);
				if(cantidad.compareTo(BigDecimal.ZERO)==0 ){//Evitar Division por 0 por el redondeo
					cantidad =  BigDecimal.ONE;
				}
				if (remMonto.compareTo(BigDecimal.ZERO) > 0){// || remCantidad.compareTo(BigDecimal.ZERO) > 0){
					//ordenesCrear.add(copyDataReportante(ordenesCrear.size(), ordReportante, remMonto, remCantidad, fechaSistema,fechaHoraSistema)); //creo orden nueva con el monto asoc para el reportante
					//ordenesActualizar.add(copyDataReportanteUpd(ordenesActualizar.size(), ordReportante, ordReportado, asoc.Monto, asoc.Cantidad, asoc.Precio, asoc.Tasa));//actualizo la orden existente del reportante con el remanente
					ordenesCrear.add(copyDataReportante(ordenesCrear.size(), ordReportante, ordReportado, asoc.Monto, cantidad, asoc.Precio, asoc.Tasa, fechaSistema,fechaHoraSistema)); //creo orden nueva con el monto asoc para el reportante
					ordenesActualizar.add(copyDataReportanteUpd(ordenesActualizar.size(), ordReportante, ordReportado, remMonto, remCantidad, false));//actualizo la orden existente del reportante con el remanente
				}
				if (remMonto.compareTo(BigDecimal.ZERO) == 0){
					ordenesActualizar.add(copyDataReportanteUpd(ordenesActualizar.size(), ordReportante, ordReportado, asoc.Monto, cantidad, true));//actualizo la orden existente del reportante con el remanente
				}
			}
			
		}/*
		for (Asociacion_Entrada asoc: in.p_Asociaciones) {
			Orden ordReportado = (Orden) ordenesOutHash.get(asoc.IdOrdenN);
			//Orden ordReportante = (Orden) ordenesOutHash.get(asig.IdOrdenM);
			if (ordReportado != null){
				BigDecimal remanente = ordReportado.MontoDisponibleAsociar.subtract(asoc.Monto);
				if (remanente.compareTo(BigDecimal.ZERO) > 0){ //debe crear orden por la diferencia
					ordenesCrear.add(copyDataReportado(ordenesCrear.size(),ordReportado, remanente, fechaSistema));
				}
				
			}
		}*/
		
		CalcularParidad paridad = new CalcularParidad();
		CalcularParidadEntrada parIn = new CalcularParidadEntrada();
		parIn.p_IdEmpresa = in.p_IdEmpresa;
		parIn.p_IdTrabajo = 12L;
		for(ObjOrdenesAsocEntrada ord: ordenesCrear){
			Orden ordReportante = (Orden) ordenesOutHash.get(ord.IdOrden);
			CalculoEntrada calIn = new CalculoEntrada();
			calIn.IdTrabajo = parIn.p_IdTrabajo;
			calIn.IdInterno	= ord.IdInterno;
			calIn.Fecha		= fechaSistema;
			calIn.IdMonedaOrigen = ord.IdMonedaInstrumento;
			calIn.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
			calIn.ValorCambioLocalModificado = BigDecimal.ZERO;
			calIn.MontoOrigen = ord.MontoNeto;
			calIn.IdUso = "ORDENES";
			parIn.p_CalculosEntrada.add(calIn);
		}
		CalcularParidadSalida paridadOut = paridad.logica(conn, parIn); //ver si necesito calcular la paridad.
		CrearOrdenesSalida ordenOut = new CrearOrdenesSalida();
		if (ordenesCrear.size() > 0){
			//*** creo nueva orden a partir de la orden del reportante con el monto y cantidad asociada (solo cuando el monto es menor al del reportado)
			CrearOrdenes orden = new CrearOrdenes();
			CrearOrdenesEntrada ordIn = new CrearOrdenesEntrada();
			
			ordIn.p_IdEmpresa 			= in.p_IdEmpresa;
			ordIn.p_IdUsuario 			= in.p_IdUsuario;
			ordIn.p_IdModulo 			= in.p_IdModulo;
			ordIn.p_simularOperacion 	= in.p_simularOperacion;
			ordIn.p_UsaReglasNegocio 	= in.p_UsaReglasNegocio;
			for (ObjOrdenesAsocEntrada ord: ordenesCrear){
				ObjOrdenesEntrada ordInObj = new ObjOrdenesEntrada();
				Utils.copyPojo(ord, ordInObj);
				ordIn.p_Ordenes.add(ordInObj); //asigno las ordenes nuevas a crear
			}
			ordIn.p_Garantias = new ArrayList<>(); // va sin garantias
			ordIn.p_CobrosModificados = new ArrayList<>();// va sin cobros
			ordenOut = orden.logica(conn, ordIn);
			out.P_OUT_MENSAJES.addAll(ordenOut.P_OUT_MENSAJES);
		}
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 )
			return out;
		HashMap<String , Object> ordenOutHash = Utils.List2Hash(ordenOut.P_OUT_RESULTADO, "IdInterno");
		
		if (ordenesActualizar.size() > 0){
			ModificarOrdenes modOrden = new ModificarOrdenes();
			ModificarOrdenes_Entrada ordIn = new ModificarOrdenes_Entrada();
			
			ordIn.p_IdEmpresa = in.p_IdEmpresa;
			ordIn.p_IdUsuario = in.p_IdUsuario;
			ordIn.p_IdModulo = in.p_IdModulo;
			ordIn.p_Ordenes.addAll(ordenesActualizar);
			ordIn.p_CobrosModificados = new ArrayList<>();
			ModificarOrdenes_Salida modOrdenOut = modOrden.logica(conn, ordIn);
			out.P_OUT_MENSAJES.addAll(modOrdenOut.P_OUT_MENSAJES);
		}
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 )
			return out;
		List<Asociacion> asociacionPojos = new ArrayList<Asociacion>();
		for (Asociacion_Entrada asig: in.p_Asociaciones) {
			for (ObjOrdenesAsocEntrada ord: ordenesCrear){
				if (asig.IdOrdenM.equals(ord.IdOrden)){
					ObjRESULTADO ordHash = (ObjRESULTADO) ordenOutHash.get(ord.IdInterno);
					asig.IdOrdenMOrigen = asig.IdOrdenM;
					asig.IdOrdenM = ordHash.IdOrden;
					
				}
			}			
			
			Resultado_Salida rsal= new Resultado_Salida();
			rsal.IdInterno=asig.IdInterno;
			rsal.Msg="";
			out.p_out_Resultado.add(rsal);
			
			//TODO: validar precio
			
			//TODO: validar estado de la orden ya no esta confirmada
			
			//TODO: validar estado de ambas  ordenN - ordenM	
			
			//TODO: validar modalidad de calculo M=Monto C=Cantidad
			
			//TODO: validar que la ordenM no este ya asociada		

			//TODO: validar que la orden no esté sobre-asociada 
			
			
			
			
			//Registro agregar
			Asociacion newReg=new Asociacion();
			Utils.copyPojo(asig, newReg);
			
			if (asig.ModalidadCalculo.equals("M")) {
				BigDecimal precio = asig.Precio.setScale(2, RoundingMode.HALF_UP);
				newReg.Cantidad=asig.Monto.divide(precio,0,RoundingMode.FLOOR) ;
				if (newReg.Cantidad.doubleValue()<0.0) newReg.Cantidad=BigDecimal.ZERO;
				newReg.Monto=newReg.Cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);;
				newReg.Precio = asig.Precio;
			}
			if (asig.ModalidadCalculo.equals("C")) {
				 newReg.Cantidad=asig.Cantidad;
				 newReg.Monto=newReg.Cantidad.multiply(asig.Precio);
				 newReg.Precio = asig.Precio;
			}
			if (asig.ModalidadCalculo.equals("R")) {
				 newReg.Monto=asig.Monto;
				 newReg.Precio = asig.Precio;
				 newReg.Cantidad= asig.Monto.multiply(new BigDecimal(100)).divide(asig.Precio,0,RoundingMode.HALF_UP);
			}
			newReg.IdOrdenAsociada=secuencias.remove(0);
			newReg.IdUsuario=in.p_IdUsuario;
			asociacionPojos.add(newReg);
			rsal.IdOrdenAsociada=newReg.IdOrdenAsociada;
			rsal.Monto=newReg.Monto;
			rsal.Cantidad=newReg.Cantidad;
			rsal.Msg="OK";			
		}
		
		
		//Ingreso Base datos
		if (in.p_simularOperacion.equalsIgnoreCase("N") && asociacionPojos.size()>0) {
			_sql.batch(conn, SQL_INSERT_ORDEN_ASOCIADA, asociacionPojos, Asociacion.class);
		}
		out.P_OUT_ERRORESCONTADOR=MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Asociaciones\":[{\"IdInterno\":41966,\"IdOrdenN\":41964,\"IdOrdenM\":41966,\"Precio\":6.664,\"ModalidadCalculo\":\"M\",\"Monto\":7998.66,\"Cantidad\":0,\"Tasa\":3.2}]}";
				
				
				CrearAsociaciones_Entrada Objin = gson.fromJson(json, CrearAsociaciones_Entrada.class);
				
				CrearAsociaciones demo=new CrearAsociaciones();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			CrearAsociaciones_Salida ObjOut=demo.logica(con, Objin);	 			
		    	System.out.println(ObjOut.toString());	
		    	con.rollback();
		    	//con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}	
}
