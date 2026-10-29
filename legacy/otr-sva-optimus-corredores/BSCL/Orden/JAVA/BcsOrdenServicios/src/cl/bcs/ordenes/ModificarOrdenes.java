package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden.Cobro_Entrada;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden.Orden_Salida;
import cl.bcs.ordenes.pojo.CobroOrden;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.EstadoModulo;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.com.pe.CrearDefinicionCobro.Rango;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ModificarOrdenes implements JavaCall {

	private final Log log = new Log("ORD.ModificarOrdenes");
	private final ConstantesOrdenes _C = new ConstantesOrdenes();
	private final String SQL_SELECT_ESTADO_MODULO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ModificarOrdenes_SelectOrdenes.sql");
	private final String SQL_SELECT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/ModificarOrdenes_SelectCobros.sql");
	private final String SQL_SELECT_COBROS_RANGO = loadResourceToString("cl/bcs/ordenes/sql/ModificarOrdenes_SelectCobrosRango.sql");
	private final String SQL_UPDATE_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ModificarOrdenes_UpdateOrdenes.sql");
	private final String SQL_INSERT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobros.sql");
	private final String SQL_INSERT_COBROS_PERU = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobrosPeru.sql");
	private final String SQL_INSERT_COBROS_RANGOS_PERU = loadResourceToString("cl/bcs/ordenes/sql/Globales_InsertCobrosRangosPeru.sql");
	private final String SQL_SELECT_ORDEN_ASOCIADA = loadResourceToString("cl/bcs/ordenes/sql/ModificarOrdenes_SelectOrdenAsociada.sql");
	
	public static class InformacionOrdenAsociada {
		public Long IdOrdenAsociada;
		public Long IdOrdenReportante;
		public Long IdOrdenReportado;
		public BigDecimal TasaPromedioReporte;
	}
	
	public static class CobroRango {
		public Long IdRangoCobro;
		public Long IdCobroOrden;
		public Long IdOrden;
		public Long IdGrupoCobro;
		public Long IdEmpresa;
		public String CodigoDefinicionCobro;
		public BigDecimal Hasta;
		public BigDecimal Valor;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarOrdenes_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Orden_Entrada> p_Ordenes;
		public List<CobroModificado_Entrada> p_CobrosModificados;
		
		public ModificarOrdenes_Entrada() {
			p_Ordenes = new ArrayList<Orden_Entrada>();
			p_CobrosModificados = new ArrayList<CobroModificado_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden_Entrada {
		public Long 		IdOrden;
		public String		ModificaCantidad;
		public BigDecimal 	Cantidad;
		public String 		ModificaCobros;
		public Long			IdAsociacion;
		public BigDecimal 	TasaPromedioReporte;
		public String 		ModificaTasaPromedioReporte;
		public String		ModificaMonto = "N";
		public BigDecimal 	Monto = BigDecimal.ZERO;
		public String		ModificaSistemaBursatil = "N";
		public BigDecimal	Precio = BigDecimal.ONE;
		public String		IdSistemaBursatil;
		
		public BigDecimal	MontoInteresContado = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo = BigDecimal.ZERO;
		
		public Long IdInstrumentoSubyacente;
	    public String IdEmisor;
	    public String IdMonedaLiquidacionOrden;
	    public String IdCondicionDeLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroModificado_Entrada {
		public Long			IdOrden;
		public String 		IdCobro;
		public BigDecimal	ValorCobroAplicado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarOrdenes_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ModificarOrdenes_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//JG: objeto de procesamiento (caso en que modificacion no implica recalculo de cobros)
	public static class ProcesamientoModificacionOrden {
		public Long IdOrden;
		public Boolean ModificaOrden = false;
		public Boolean ModificaCobro = false;
		public Orden Orden;
		public ProcesamientoModificacionOrden(){}
		public ProcesamientoModificacionOrden(Long IdOrden){
			this.IdOrden = IdOrden;
		}
	}
	
	public ModificarOrdenes_Salida logica(Connection con, ModificarOrdenes_Entrada in) throws Exception {
		SQL _sql = new SQL();
		ModificarOrdenes_Salida out = new ModificarOrdenes_Salida();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		if (in.p_Ordenes.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ORDENES",null,con,in.p_IdEmpresa));
			return out;
		}
		
		//validaciones basicas
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
		}
		
		//validacion modulo
		{
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdModulo", _C.ID_MODULO_ORDENES);
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<EstadoModulo> modulo = _sql.query(con, SQL_SELECT_ESTADO_MODULO, params, EstadoModulo.class);
			if (modulo.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_NO_EXISTE",null,con,in.p_IdEmpresa));
				return out;
			}
			if (modulo.get(0).IdEstadoModulo.equalsIgnoreCase(_C.ESTADO_MODULO_CERRADO)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",null,con,in.p_IdEmpresa));
				return out;
			}
		}
		
		//informacion ordenes y cobros
		HashMap<Long,Orden> infoOrdenes = new HashMap<>();
		List<CobroOrden> infoCobros = new ArrayList<>();
		List<DefinicionRangoCobro> infoCobrosRango = new ArrayList<>();
		List<Long> idsOrdenAsociada = new ArrayList<>();
		{
			HashMap<String,Object> params = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<Long>();
			for (Orden_Entrada ord : in.p_Ordenes) {
				idsOrden.add(ord.IdOrden);
			}
			params.put("idsOrden", idsOrden);
			List<Orden> _infoOrdenes = _sql.query(con, SQL_SELECT_ORDENES, params, Orden.class);
			for (Orden ord : _infoOrdenes) {
				infoOrdenes.put(ord.IdOrden, ord);
				
				//estos son reportantes
				if (ord.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_COMRRF)
						|| ord.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_COMRRV)) {
					idsOrdenAsociada.add(ord.IdOrden);
				}
				
			}
			
			infoCobros = _sql.query(con, SQL_SELECT_COBROS, params, CobroOrden.class);
			//Busco los rango guardados 
			if (infoCobros.size() > 0){
				infoCobrosRango = _sql.query(con, SQL_SELECT_COBROS_RANGO, params, DefinicionRangoCobro.class);
				for (CobroOrden cob:infoCobros){
					List<DefinicionRangoCobro> rangoCobro = new ArrayList<>();
					for (DefinicionRangoCobro RangoCob: infoCobrosRango){
						if (cob.IdOrden.equals(RangoCob.IdOrden) && cob.IdCobroOrden.equals(RangoCob.IdCobroOrden) && cob.IdGrupoCobro.equals(RangoCob.IdGrupoCobro)){
							rangoCobro.add(RangoCob);
						}							
					}
					cob.RangoCobros = rangoCobro;
				}
			}
			
		}
		
		//tasa promedio para reportantes
		HashMap<Long,BigDecimal> hashTasaPromedioReportante = new HashMap<>();
		{
			if (idsOrdenAsociada.size() > 0) {
				HashMap<String,Object> params = new HashMap<>();
				params.put("idsOrdenAsociada", idsOrdenAsociada);
				List<InformacionOrdenAsociada> _infoOrdenAsociada = _sql.query(con, SQL_SELECT_ORDEN_ASOCIADA, params, InformacionOrdenAsociada.class);
				for (InformacionOrdenAsociada asoc : _infoOrdenAsociada) {
					hashTasaPromedioReportante.put(asoc.IdOrdenAsociada,asoc.TasaPromedioReporte);
				}
			}
		}
		
		CalcularMontoYCobrosOrden serviceCalcularMontoYCobrosOrden = new CalcularMontoYCobrosOrden();
		CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada entradaCalcularMontoYCobrosOrden = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
		entradaCalcularMontoYCobrosOrden.p_IdEmpresa = in.p_IdEmpresa;
		entradaCalcularMontoYCobrosOrden.p_IdModulo = in.p_IdModulo;
		entradaCalcularMontoYCobrosOrden.p_IdUsuario = in.p_IdUsuario;
		
		HashMap<Long,Orden_Entrada> hashOrdenesEntrada = new HashMap<>();
				
		List<ProcesamientoModificacionOrden> procOrden = new ArrayList<ProcesamientoModificacionOrden>();
		
		for (Orden_Entrada ord : in.p_Ordenes) {
			ProcesamientoModificacionOrden itemProcOrden = new ProcesamientoModificacionOrden(ord.IdOrden);
			
			hashOrdenesEntrada.put(ord.IdOrden, ord);
			Orden info = null;
			
			if(ord.ModificaSistemaBursatil.equalsIgnoreCase(_C.SI)){
				info = infoOrdenes.get(ord.IdOrden);
				info.IdSistemaBursatil = ord.IdSistemaBursatil;
				itemProcOrden.ModificaOrden=true;
			}
			if (ord.ModificaCantidad.equalsIgnoreCase(_C.SI) 
					|| ord.ModificaCobros.equalsIgnoreCase(_C.SI) 
					|| ord.ModificaTasaPromedioReporte.equalsIgnoreCase(_C.SI)
					|| ord.ModificaMonto.equalsIgnoreCase(_C.SI)) {
				
				if(info==null) info = infoOrdenes.get(ord.IdOrden);
				if (info == null) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ORDEN_NO_EXISTE",null,con,in.p_IdEmpresa));
					continue;
				}
				
				if (!info.IdEstadoOrden.equalsIgnoreCase(_C.ESTADO_ORDEN_INGRESADA)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ESTADO_ORDEN_NO_PERMITE_MODIFICAR_CANTIDAD",null,con,in.p_IdEmpresa));
					continue;
				}
				
				if (ord.Cantidad.compareTo(info.Cantidad) == 1) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PERMITE_AUMENTAR_CANTIDAD",null,con,in.p_IdEmpresa));
					continue;
				}
				if (!info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_C.CLASIFICACION_RE) && ord.Monto.compareTo(info.MontoOrden) == 1) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PERMITE_AUMENTAR_MONTO",null,con,in.p_IdEmpresa));
					continue;
				}
				
				CalcularMontoYCobrosOrden.Orden_Entrada o1 = new CalcularMontoYCobrosOrden.Orden_Entrada();
				o1.BaseTasa = info.BaseTasa;
				if (ord.ModificaCantidad.equalsIgnoreCase(_C.SI)) {
					o1.CantidadOrden  = ord.Cantidad;
				}
				else {
					o1.CantidadOrden  = info.Cantidad;
				}
				
				if (ord.ModificaMonto.equalsIgnoreCase(_C.SI)) {
					o1.MontoOrden  = ord.Monto;
					o1.MontoTotal = ord.Monto;
					o1.EsPorMonto  = "S";
					o1.PrecioOrden = ord.Precio;
				}
				else {
					o1.MontoOrden  = info.MontoOrden;
					o1.EsPorMonto  = info.EsPorMonto;
					o1.PrecioOrden = info.PrecioOrden;
				}
				
				if (ord.ModificaTasaPromedioReporte.equalsIgnoreCase(_C.SI)) {
					o1.TasaReporte = ord.TasaPromedioReporte;
				}
				else {
					//SI ES REPORTANTE, LA TASA ESTA EN EL REPORTADO ENLAZADO POR LA TABLA OrdenAsociada
					if (info.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_COMRRV)
							|| info.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_COMRRF)) {
						o1.TasaReporte = hashTasaPromedioReportante.get(ord.IdAsociacion);
					}
					else if (info.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_VENRRV)
							|| info.IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_VENRRF)){
						o1.TasaReporte = info.TasaPromedioReporte;
					}				 
				}
				
				o1.EsFungible  = info.EsFungible;
				
				o1.FechaOperacion  = Fecha.toStringDate(info.FechaOperacion);
				o1.IdBolsa  = info.IdBolsa;
				o1.IdCorredorCorresponsal  = info.IdCorredorCorresponsal;
				o1.IdEmisor  = info.IdEmisor;
				o1.IdInstrumento = info.IdInstrumento;
				o1.IdInterno = info.IdOrden;
				o1.IdMonedaLiquidacionOrden = info.IdMonedaLiquidacionOrden;
				o1.IdPortafolio = info.IdPortafolioCliente;
				o1.IdTipoCanalIngreso = info.IdTipoCanalIngreso;
				o1.IdTipoOperacion = info.IdTipoOperacion;
				o1.IdTipoProducto = info.IdProducto;
				//o1.MontoOrden = info.MontoOrden;
				//o1.MontoTotal = BigDecimal.ZERO;
				o1.PlazoIF = info.PlazoIF;
				o1.PlazoReporte = info.PlazoReporte;
				o1.PlazoSimultanea = info.PlazoSimultanea;
				o1.PlazoVentaCorta = info.PlazoVentaCorta;
				//o1.PrecioOrden = info.PrecioOrden;
				o1.TipoInteres = info.TipoInteres;
				o1.TasaReportado = info.TasaReporte;
				o1.TasaRFIF = info.TasaRFIF;
				o1.TasaSimultanea = info.TasaSimultanea;
				o1.TasaVentaCorta = info.TasaVentaCorta;
				o1.TipoCambio = info.TipoCambio;
				o1.ValorRescateOrden = info.ValorRescate;
				
				o1.IdInstrumentoSubyacente = ord.IdInstrumentoSubyacente;
				o1.IdEmisor = ord.IdEmisor;
				o1.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
				
				info.IdInstrumentoSubyacente = ord.IdInstrumentoSubyacente;
				info.IdEmisor = ord.IdEmisor;
				info.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
				info.MontoInteresContado = ord.MontoInteresContado;
				info.MontoInteresPlazo = ord.MontoInteresPlazo;
				info.IdCondicionDeLiquidacion = ord.IdCondicionDeLiquidacion != null? ord.IdCondicionDeLiquidacion:"";
				
				entradaCalcularMontoYCobrosOrden.p_Ordenes.add(o1);
				itemProcOrden.ModificaOrden=true;
				itemProcOrden.ModificaCobro=true;
				
				itemProcOrden.Orden = info;
				
				for (CobroOrden cob : infoCobros) {
					if (cob.IdOrden.equals(ord.IdOrden)) {
						
						Cobro_Entrada cobEntrada = new Cobro_Entrada();
						cobEntrada.IdCobro = cob.IdCobro;
						cobEntrada.IdInterno = cob.IdOrden;
						
						if (ord.ModificaCobros.equalsIgnoreCase(_C.SI)) {
							
							for (CobroModificado_Entrada cobroModificadoEntrada : in.p_CobrosModificados) {
								if (cobroModificadoEntrada.IdOrden.equals(cob.IdOrden)
										&& cobroModificadoEntrada.IdCobro.equalsIgnoreCase(cob.IdCobro)) {
									cobEntrada.ValorCobroAplicado = cobroModificadoEntrada.ValorCobroAplicado;
								}
							}
							
						} else {
							cobEntrada.ValorCobroAplicado = cob.ValorCobroAplicado;
						}
						
						entradaCalcularMontoYCobrosOrden.p_CobrosModificados.add(cobEntrada);
						
					}
				}
			}
			
			procOrden.add(itemProcOrden);
		}
		
		List<Orden> ordenesPorModificar = new ArrayList<Orden>();
		List<CobroOrden> cobrosPorAgregar = new ArrayList<CobroOrden>();
		HashMap<Long,List<CobroOrden>> cobrosPorOrden = new HashMap<>();
		

		for(ProcesamientoModificacionOrden item:procOrden){
			if(item.ModificaOrden){
				Orden_Entrada ordEntrada = hashOrdenesEntrada.get(item.IdOrden);
				if(item.ModificaCobro==true){
					CalcularMontoYCobrosOrden_Salida salidaCalcularMontoYCobros = serviceCalcularMontoYCobrosOrden.logica(con, entradaCalcularMontoYCobrosOrden);
					List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::GrupoCobroOrden", new Long(salidaCalcularMontoYCobros.p_out_Ordenes.size()));
					
					
					Orden_Salida ordx = null;
					for (Orden_Salida itemsearh : salidaCalcularMontoYCobros.p_out_Ordenes) {
						if(item.IdOrden.equals(itemsearh.IdInterno)){
							ordx=itemsearh;
							break;
						}
					}
					if(ordx!=null){
						Orden ord = new Orden();
						ord.IdOrden = ordx.IdInterno;
						ord.IdSistemaBursatil = item.Orden.IdSistemaBursatil;
						//lo siguiente cambiarlo a item.Orden...
						ord.PrecioPlazo = ordx.PrecioPlazo;
						ord.Cantidad = ordx.CantidadOperacion;
						ord.MontoOrden = ordx.MontoOperacion;
						ord.MontoOrdenPlazo = ordx.MontoOperacionPlazo;
						ord.MontoLiquidacion = ordx.MontoOperacionLiquidacion;
						ord.MontoLiquidacionPlazo = ordx.MontoOperacionLiquidacionPlazo;
						ord.MontoLocal = ordx.MontoOperacionLocal;
						ord.MontoLocalPlazo = ordx.MontoOperacionLocalPlazo;
						
						ord.IdInstrumentoSubyacente = item.Orden.IdInstrumentoSubyacente;
						ord.IdEmisor = item.Orden.IdEmisor;
						ord.IdMonedaLiquidacionOrden = item.Orden.IdMonedaLiquidacionOrden;
						
						ord.MontoInteresContado = item.Orden.MontoInteresContado;
						ord.MontoInteresPlazo = item.Orden.MontoInteresPlazo;
						ord.IdCondicionDeLiquidacion = ordEntrada.IdCondicionDeLiquidacion;
						
						ord.IdGrupoCobro = secuencias.get(salidaCalcularMontoYCobros.p_out_Ordenes.indexOf(ordx));
						if (infoOrdenes.get(ordx.IdInterno).IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_VENRRF)
								|| infoOrdenes.get(ordx.IdInterno).IdTipoOperacion.equalsIgnoreCase(_C.TIPO_OPERACION_VENRRV)) {
							ord.TasaPromedioReporte = ordx.TasaReporte;
						}
						ordenesPorModificar.add(ord);
						
						cobrosPorOrden.put(ord.IdOrden, new ArrayList<CobroOrden>());
						for (CalcularMontoYCobrosOrden.Cobro_Salida cobx : salidaCalcularMontoYCobros.p_out_Cobros) {
							if (cobx.IdInterno.equals(ord.IdOrden)) {
								CobroOrden cob = new CobroOrden();
								Utils.copyPojo(cobx, cob);
								cob.IdAsociacion = ordEntrada.IdAsociacion;
								cob.EsComprobante = "N";
								cob.IdMoneda = cobx.IdMonedaCobro;
								cob.IdGrupoCobro = ord.IdGrupoCobro;
								cob.IdOrden = cobx.IdInterno;
								cobrosPorAgregar.add(cob);
							}
							
						}
					}else{
						log.write("Error, el item deberia tener datos");
					}
				}else{
					Orden ord = new Orden();
					ord.IdOrden = item.IdOrden;
					ord.IdSistemaBursatil = item.Orden.IdSistemaBursatil;
					
					ord.IdInstrumentoSubyacente = item.Orden.IdInstrumentoSubyacente;
					ord.IdEmisor = item.Orden.IdEmisor;
					ord.IdMonedaLiquidacionOrden = item.Orden.IdMonedaLiquidacionOrden;
					ord.MontoInteresContado = item.Orden.MontoInteresContado;
					ord.MontoInteresPlazo = item.Orden.MontoInteresPlazo;
					ord.IdCondicionDeLiquidacion = ordEntrada.IdCondicionDeLiquidacion;
					ordenesPorModificar.add(ord);
				}
			}else{
				log.write("orden id:"+item.IdOrden+" sin modificaciones");
			}
		}		
		if (ordenesPorModificar.size() > 0) {
			_sql.batch(con, SQL_UPDATE_ORDENES, ordenesPorModificar, Orden.class);
		}
		if (cobrosPorAgregar.size() > 0) {
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				List<Long> ids = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::CobrosOrden", (long) cobrosPorAgregar.size());
				for (int i=0; i < cobrosPorAgregar.size(); i++){
					cobrosPorAgregar.get(i).IdCobroOrden =  ids.get(i).longValue();
					System.out.println(cobrosPorAgregar.toString());
				}
				_sql.batch(con, SQL_INSERT_COBROS_PERU, cobrosPorAgregar , CobroOrden.class);
			}else {	
				_sql.batch(con, SQL_INSERT_COBROS, cobrosPorAgregar, CobroOrden.class);
			}
			List<CobroRango> rangoCobroFinal = new ArrayList<>();
			for (CobroOrden cobRanFin: cobrosPorAgregar){
				
				for (DefinicionRangoCobro ran : cobRanFin.RangoCobros){
					CobroRango rangoFin = new CobroRango();
					rangoFin.IdCobroOrden = cobRanFin.IdCobroOrden;
					rangoFin.IdOrden = cobRanFin.IdOrden;
					rangoFin.IdGrupoCobro = cobRanFin.IdGrupoCobro;
					rangoFin.IdEmpresa = ran.IdEmpresa;
					rangoFin.CodigoDefinicionCobro = ran.CodigoDefinicionCobro;
					rangoFin.Hasta = ran.Hasta;
					rangoFin.Valor = ran.Valor;
					System.out.println(rangoFin.toString());
					//System.out.println(rangoCobroFinal.indexOf(rangoFin));
					//if (!rangoCobroFinal.contains(rangoFin))
					rangoCobroFinal.add(rangoFin);
				}
			}
			if (rangoCobroFinal.size() > 0)
				_sql.batch(con, SQL_INSERT_COBROS_RANGOS_PERU, rangoCobroFinal, CobroRango.class);
		}
		
		String codigo = "ORDEN_MODIFICADA";
		for(Orden ord : ordenesPorModificar) {
			String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).add("IdOrden", ord.IdOrden).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdOrden,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
		}		
		log.write("TERMINO");
		return out;

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			
			
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_CobrosModificados\":[{\"IdOrden\":23704,\"IdCobro\":\"IGV\",\"ValorCobroAplicado\":18}],\"p_Ordenes\":[{\"IdInterno\":0,\"IdOrden\":23701,\"ModificaCantidad\":\"N\",\"Cantidad\":0,\"ModificaCobros\":\"S\",\"IdAsociacion\":0,\"ModificaTasaPromedioReporte\":\"S\",\"TasaPromedioReporte\":12,\"IdInstrumentoSubyacente\":177546,\"IdEmisor\":\"OPTIMUS\",\"IdMonedaLiquidacionOrden\":\"PEN\"},{\"IdInterno\":\"1321\",\"IdOrden\":23704,\"ModificaCantidad\":\"N\",\"Cantidad\":0,\"ModificaCobros\":\"S\",\"IdAsociacion\":\"132\",\"ModificaTasaPromedioReporte\":\"S\",\"TasaPromedioReporte\":12,\"IdInstrumentoSubyacente\":177546,\"IdEmisor\":\"OPTIMUS\",\"IdMonedaLiquidacionOrden\":\"PEN\"}]}";
				ModificarOrdenes.ModificarOrdenes_Entrada mod = gson.fromJson(json, ModificarOrdenes.ModificarOrdenes_Entrada.class);
				ModificarOrdenes service2 = new ModificarOrdenes();
				ModificarOrdenes_Salida aasdf = service2.logica(con, mod);
				System.out.println(aasdf);
				/*return;
				ModificarOrdenes.ModificarOrdenes_Entrada ent = new ModificarOrdenes.ModificarOrdenes_Entrada();
				ent.p_IdEmpresa = 1L;
				ent.p_IdModulo = "ORD";
				ent.p_IdUsuario = 4L;
				
				ModificarOrdenes.Orden_Entrada ord = new ModificarOrdenes.Orden_Entrada();
				ord.Cantidad = new BigDecimal(1000);
				ord.IdAsociacion = 10L;
				ord.IdOrden = 312196L;
				ord.ModificaCantidad = "S";
				ord.ModificaCobros = "S";
				ord.ModificaTasaPromedioReporte = "S";
				ord.TasaPromedioReporte = BigDecimal.ONE;
				ent.p_Ordenes.add(ord);
				
				ModificarOrdenes.CobroModificado_Entrada cob = new CobroModificado_Entrada();
				cob.IdOrden = 312196L;
				cob.IdCobro = "COVA";
				cob.ValorCobroAplicado = new BigDecimal("0.3");
				
				ent.p_CobrosModificados.add(cob);
				
				ModificarOrdenes service = new ModificarOrdenes();
				ModificarOrdenes_Salida aasdf = service.logica(con, ent);
				System.out.println(aasdf.toString());*/
				con.rollback();
			}
			
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 		}
		 	
		System.out.println("== END ==");
	}



	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ModificarOrdenes_Entrada in = gson.fromJson(jin.toString(), ModificarOrdenes_Entrada.class);
		ModificarOrdenes_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	

}