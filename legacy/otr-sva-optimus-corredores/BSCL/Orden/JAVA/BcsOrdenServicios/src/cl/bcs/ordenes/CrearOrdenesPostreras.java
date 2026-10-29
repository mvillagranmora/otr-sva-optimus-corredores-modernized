package cl.bcs.ordenes;

import cl.bcs.ordenes.CrearAsignaciones.Asignacion_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Entrada;
import cl.bcs.ordenes.CrearAsignaciones.CrearAsignaciones_Salida;
import cl.bcs.ordenes.CrearConfiguracionShortName.ClaseShortName;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.ObjCobrosModificados;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.BarraProgresoWrapper;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import datadog.trace.api.Trace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




public class CrearOrdenesPostreras implements JavaCall,JavaCallMetadata {
	private static final Logger logger = LogManager.getLogger(CrearOrdenesPostreras.class);
	
	private final String SQL_SELECT_TRANSACCIONES= loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectTransacciones.sql");
	private final String SQL_SELECT_DOCUMENTOS_IDENTIDAD_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectDocumentosIdentidadPortafolio.sql");
	private final String SQL_SELECT_LIBROS_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectLibroOrdenesGrupo.sql");
	private final String SQL_UPDATE_LIBRO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesPostreras_UpdateLibroOrdenes.sql");
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private static final String CARACTER_VACIO = "";
	private static final String FICTICIO = "FIC";
	private static final String RUT_CAVALI = "RUT";
	private static final String FUERADERUEDA = "FRD";
	
	private static final String LOCALIZACION_PERU = "PE";
	
	public static class CrearOrdenesPostreras_Entrada {
		public Long								p_IdEmpresa;
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public String							p_OrigenPost = "";
		public String							p_simularOperacion;
		public String							p_UsaReglasNegocio;
		public BarraProgreso					p_BarraProgreso;
		public List<CobroModificados_Entrada>	p_CobrosModificados;
		public List<Cobro_Fijo>  				p_CobrosFijos;
		public List<OrdenPostrera_Entrada>		p_Ordenes;
		public String 							p_AplicarComision;
		public String							p_EnviarFacturarAutomatico;
		public String							p_EsAsignacionMasivaPrecioPromedio;
		public String							p_UsaLogDetallado;
		
		public CrearOrdenesPostreras_Entrada() {
			p_CobrosModificados = new ArrayList<CobroModificados_Entrada>();
			p_Ordenes = new ArrayList<OrdenPostrera_Entrada>();
			p_CobrosFijos = new ArrayList<Cobro_Fijo>();
			p_AplicarComision = "S";
			p_EnviarFacturarAutomatico = "N";
			p_EsAsignacionMasivaPrecioPromedio = "N";
			p_UsaLogDetallado = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Fijo {
		public Long IdInterno; 
		public Long IdInternoCobro;
		public Long IdPortafolio; 
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro; 
		public String FormaCobro; 
		public BigDecimal ValorTopeMinimoOrden; 
		public String IdMoneda; 
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroModificados_Entrada {
		public Long			IdInterno;
		public String		IdCobro;
		public BigDecimal	ValorCobroAplicado;
	}

	public static class OrdenPostrera_Entrada {
		public Long			IdInterno;
		public String		FechaRecepcion;
		public String 		Glosa;
		public String 		IdBoveda;
		public String 		IdFormaCustodia;
		public String 		EsFacturaInmediata;
		public Long 		IdTransaccion;
		public Long 		IdPortafolio;
		public BigDecimal 	Cantidad;
		public BigDecimal 	ValorRescate;
		public Long			IdGrupo;
		public Long 		FolioCartera;
		public BigDecimal 	TasaVentaCorta;
		public Long 		PlazoVentaCorta;
		public BigDecimal 	TipoCambio;
		public String 		IdMonedaLiquidacionOrden;
		public String		PagoCobroCuentaInversion;
		public Long			IdPreferenciaPagoPortafolio;
		public Long 		IdCuentaCustodioPortafolio;
		public String		CodigoIFRS;
		public BigDecimal	PorcentajeAllIn;
		public String		FlujoOrigen;
		public java.sql.Timestamp	FechaTrade;
		public String		IngresoDesde = "";
		public String 		FolioTransaccion = "";
		public String		CodigoPortafolio = "";
		public String 		DocumentoIdentidad = "";
		public String 		NombreCompleto = "";
		public String 		Nemotecnico = "";
		public BigDecimal	Monto = BigDecimal.ZERO;
		public String 		IdTipoOperacion = "";

		public OrdenPostrera_Entrada() {
			FlujoOrigen = "INGRESOORDEN";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}			
	}

	public static class Resultado_Salida {
		public Long	IdInterno;
		public Long IdAsignacion;
		public Long IdOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdCobroAsignacion;
		public String CodigoCobroClasificacion;
		public String IdCobro;
		public String IdMonedaCobro;
		public BigDecimal ValorCobroPerfil;
		public String FormaCobro;
		public BigDecimal MontoMinimo;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
	}
	
	public static class CrearOrdenesPostreras_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<Resultado_Salida>	P_OUT_RESULTADO;
		public List<Cobro_Salida>		P_OUT_COBROS;
		
		public CrearOrdenesPostreras_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado_Salida>();
			P_OUT_COBROS = new ArrayList<Cobro_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DocumentoIdentidadPortafolio {
		public Long IdPortafolio;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
	}
	
	public static class LibrosOrdenesGrupo {
		public Long IdLibroOrdenes;
		public Long IdTransaccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearOrdenesPostreras_Entrada in = gson.fromJson(jin.toString(), CrearOrdenesPostreras_Entrada.class);
		CrearOrdenesPostreras_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		setMetadata(in,out);
		return dataout;
	}
	
	public void setMetadata(Object in,Object out) {
		CrearOrdenesPostreras_Salida s = (CrearOrdenesPostreras_Salida)out;
		metadata[0] = s.P_OUT_RESULTADO.size()+"";
	}
	
	public static class Transaccion_Proceso {
		public Long 				IdTransaccion;
		public String 				Folio;
		public java.sql.Date 		FechaEmision;
		public java.sql.Timestamp 	FechaOperacion;
		public java.sql.Timestamp	FechaIngresoOferta;
		public String 				IdClasificacionTipoInstrumento;
		public BigDecimal 			Precio;
		public String 				EsFungible;
		public Long 				Tamano;
		public BigDecimal 			Monto;
		public BigDecimal 			Cantidad;
		public BigDecimal 			ValorRescate;
		public String 				IdTipoReajuste;
		public String 				IdEmisor;
		public String 				IdBolsa;
		public String 				IdMoneda;
		public Long 				IdInstrumento;
		public Long 				IdInstrumentoSubyacente;
		public String 				IdTipoOperacion;
		public String 				IdCondicionDeLiquidacion;
		public BigDecimal 			Tasa;
		public Long 				Plazo;
		public String 				EsVentaCorta;
		public Long 				IdCorredorCorresponsal;
		public String 				AccionOrden;
		public String 				IdTipoInstrumento;
		public Long 				IdLibroOrdenes;
		public String 				IdTipoLibroOrden;
		public Long 				BaseTasa;
		public String 				TipoInteres;
		public String 				Nemotecnico;
		public String 				NemotecnicoSVS;
		public BigDecimal 			CorteMinimo;
		public java.sql.Date        FechaVencimiento; 
		public String 				IndicadorOrdenDirecta;
		public String		        NombreCompleto;
		public String 		        DocumentoIdentidad;
	}

	
	@Trace
	public CrearOrdenesPostreras_Salida logica(Connection con, CrearOrdenesPostreras_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		Gson gson = Utils.getGsonBuilder();
		JsonParser parser = new JsonParser();
		SQL _sql = new SQL();
		CrearOrdenesPostreras_Salida out = new CrearOrdenesPostreras_Salida();
		logger.info("Inicio servicio CrearOrdenesPostreras");
		if (in.p_BarraProgreso == null) {
			// No habilitado para servicio principal
		} else {
            in.p_BarraProgreso.actualizar(0L, "PROCESANDO", "Inicio creacion de ordenes");
        }
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		List<Long> idsTransaccion = new ArrayList<Long>();
		for (OrdenPostrera_Entrada ordenPostrera : in.p_Ordenes) {
			idsTransaccion.add(ordenPostrera.IdTransaccion);
		}
		
		params1.put("idsTransaccion", idsTransaccion);
		List<Transaccion_Proceso> _transacciones = _sql.query(con, SQL_SELECT_TRANSACCIONES, params1, Transaccion_Proceso.class);
		HashMap<Long,Transaccion_Proceso> transacciones = new HashMap<Long,Transaccion_Proceso>();
		for ( Transaccion_Proceso tran : _transacciones ) {
			transacciones.put(tran.IdTransaccion, tran);
		}
		//--------------------------
		
		
		if (in.p_Ordenes.get(0).IdGrupo != null) {
			HashMap<String,Object> param = new HashMap<String,Object>();
			param.put("IdGrupo", in.p_Ordenes.get(0).IdGrupo);
			List<LibrosOrdenesGrupo> LibrosGrupo = _sql.query(con, SQL_SELECT_LIBROS_ORDENES, param, LibrosOrdenesGrupo.class);
			if (!LibrosGrupo.isEmpty()) {
				List<LibrosOrdenesGrupo> LibroOrden = new ArrayList<>();
				for ( Transaccion_Proceso tran : _transacciones ) {
					if(!tran.IdLibroOrdenes.equals(LibrosGrupo.get(0).IdLibroOrdenes)) {
						LibrosOrdenesGrupo Libro = new  LibrosOrdenesGrupo();
						Libro.IdLibroOrdenes = LibrosGrupo.get(0).IdLibroOrdenes;
						Libro.IdTransaccion = tran.IdTransaccion;
						LibroOrden.add(Libro);
						tran.IdLibroOrdenes = LibrosGrupo.get(0).IdLibroOrdenes;
					}
				}
				_sql.batch(con,SQL_UPDATE_LIBRO_ORDEN,LibroOrden, LibrosOrdenesGrupo.class);
			}
		}
		
		
		//----------------------------
		CrearOrdenesEntrada crearOrdenesEntrada = new CrearOrdenesEntrada();
		crearOrdenesEntrada.p_IdEmpresa = in.p_IdEmpresa;
		crearOrdenesEntrada.p_IdModulo = in.p_IdModulo;
		crearOrdenesEntrada.p_IdUsuario = in.p_IdUsuario;
		crearOrdenesEntrada.p_simularOperacion = _c.NO;
		if(in.p_simularOperacion != null) {
			crearOrdenesEntrada.p_simularOperacion = in.p_simularOperacion;
		}
		crearOrdenesEntrada.p_UsaReglasNegocio = _c.SI;
	
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		for (OrdenPostrera_Entrada ordenPostrera : in.p_Ordenes) {
			ObjOrdenesEntrada orden = new ObjOrdenesEntrada();
			
			Transaccion_Proceso tran = transacciones.get(ordenPostrera.IdTransaccion);
			if (tran == null) {
				String codigo = "TRANSACCION_NO_EXISTE";
				String desc = Diccionario.getMensaje(con,in.p_IdEmpresa, codigo).msg();
				out.P_OUT_MENSAJES.add(new MensajeSalida(orden.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}
			//Verificacion de Cliente con Rut Interno para Peru
			if (LOCALIZACION_PERU.equals(localizacionSistema)) {
				params1 = new HashMap<String,Object>();
				params1.put("idPortafolio", ordenPostrera.IdPortafolio);
				String documentoIdentidadFicticio = CARACTER_VACIO;
				String documentoIdentidadRutCavali = CARACTER_VACIO;
				List<DocumentoIdentidadPortafolio> documentosIdentidad = _sql.query(con, SQL_SELECT_DOCUMENTOS_IDENTIDAD_PORTAFOLIO, params1, DocumentoIdentidadPortafolio.class);
				for (DocumentoIdentidadPortafolio documentoIdentidad : documentosIdentidad) {
					if (FICTICIO.equals(documentoIdentidad.IdTipoDocumentoIdentidad))
						documentoIdentidadFicticio = documentoIdentidad.DocumentoIdentidad;
					else if (RUT_CAVALI.equals(documentoIdentidad.IdTipoDocumentoIdentidad))
						documentoIdentidadRutCavali = documentoIdentidad.DocumentoIdentidad;
				}
				boolean esRutInterno = false;
				if (!CARACTER_VACIO.equals(documentoIdentidadFicticio) && !CARACTER_VACIO.equals(documentoIdentidadRutCavali) && documentoIdentidadFicticio.equals(documentoIdentidadRutCavali)) {
					esRutInterno = true;
				}
				if (esRutInterno && !FUERADERUEDA.equals(tran.IdBolsa)) {
					String codigo = "CLIENTE_OPERA_MERCADO_OTC";
					String desc = Diccionario.getMensaje(con, in.p_IdEmpresa, codigo).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(orden.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					out.P_OUT_ERRORESCONTADOR++;
					return out;
				}
			}
			
			//Utils.copyPojo(ordenPostrera, orden);
			orden = copyPojoObjOrdenesEntrada(ordenPostrera);
			
			orden.EsPorMonto = "N";
			orden.FechaVigencia = "0001-01-01";
			orden.FechaVencimiento = ("IF".equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)? tran.FechaVencimiento: Fecha.toDate("0001-01-01"));

			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con,in.p_IdEmpresa);	
			if (_c.ORIGEN_REFACTURACION.equalsIgnoreCase(in.p_OrigenPost) || (tran.FechaIngresoOferta.before(Fecha.toDate(fechaHoraSistema))))
			{	
				//Para refacturacion la FechaOperacion de la orden debe ser la fecha actual ya que la transaccion puede ser anterior a la fecha actual		
				orden.FechaOperacion = fechaHoraSistema;
				orden.FechaRecepcion = fechaHoraSistema;
				
			}
			else
			{
				//FechaOperacion de la orden postrera tendra 5 minutos menos que FechaOperacion de la Transaccion. FechaRecepcion 4 minutos menos. FechaAceptacion 3 -> este en CrearOrdenes.java
				orden.FechaOperacion = Fecha.toStringDatetime(Fecha.substractSeconds(tran.FechaIngresoOferta, 300));//300 segundos = 5 minutos
				orden.FechaRecepcion = Fecha.toStringDatetime(Fecha.substractSeconds(tran.FechaIngresoOferta, 300));
			}
			
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				orden.FechaEmision = Fecha.toStringDate(tran.FechaEmision);
				orden.IdMonedaOrden= tran.IdMoneda;
			} else {
				orden.FechaEmision = "0001-01-01";
			}
			orden.IdTipoVigencia = _c.TIPO_VIGENCIA_SOLO_HOY;
			
			//AJUSTE DE CANTIDAD Y VALOR RESCATE
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.SI)) {
				orden.Cantidad = BigDecimal.ONE;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && tran.EsFungible.equalsIgnoreCase(_c.NO) && 
					tran.CorteMinimo != null && tran.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
				orden.Cantidad = ordenPostrera.ValorRescate.divide(tran.CorteMinimo,0,RoundingMode.FLOOR);
			}
			else {
				orden.ValorRescate = BigDecimal.ZERO;
			}
			
			
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
				orden.MontoNeto = tran.Precio.multiply(ordenPostrera.Cantidad);
				if((tran.IdTipoOperacion.equalsIgnoreCase("COMACN") || tran.IdTipoOperacion.equalsIgnoreCase("VENACN")) && ordenPostrera.IdMonedaLiquidacionOrden.equalsIgnoreCase("USD")){ 
					orden.IdMonedaOrden= tran.IdMoneda;
				}
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				orden.MontoNeto = tran.Precio.multiply(ordenPostrera.Cantidad);
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
				orden.MontoNeto = tran.Precio.multiply(ordenPostrera.Cantidad).multiply(new BigDecimal(tran.Tamano)) ;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
				orden.MontoNeto = ordenPostrera.Cantidad.divide(tran.Cantidad,20,RoundingMode.HALF_UP).multiply(tran.Monto);
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				orden.MontoNeto = ordenPostrera.ValorRescate.divide(tran.ValorRescate,20,RoundingMode.HALF_UP).multiply(tran.Monto);
			}
			else {
				orden.MontoNeto = BigDecimal.ZERO;
			}
			orden.IdMecanismoCaptacion = _c.MECANISMO_CAPTACION_OTROS_MEDIOS;
			orden.IdPorcentajeVisibilidad = 1L;
			orden.PrecioLimite = tran.Precio;
			orden.IdTipoPrecio = _c.TIPO_PRECIO_LIMITE;
			orden.IdTipoLibroOrden = tran.IdTipoLibroOrden;
			if (!orden.IdTipoLibroOrden.equalsIgnoreCase("NORMAL"))
				orden.IdLibroOrdenes = tran.IdLibroOrdenes;
			orden.IdTipoCanalIngreso = _c.TIPO_CANAL_INGRESO_TRADER;
			orden.IdTipoReajuste = tran.IdTipoReajuste;
			orden.IdEmisor = tran.IdEmisor;
			orden.IdBolsa = tran.IdBolsa;
			orden.EspecificaBolsa = _c.SI;
			if (ordenPostrera.IdMonedaLiquidacionOrden != null) {
				orden.IdMonedaLiquidacionOrden = ordenPostrera.IdMonedaLiquidacionOrden;
			}
			else {
				orden.IdMonedaLiquidacionOrden = tran.IdMoneda;
			}
			
			if (ordenPostrera.TipoCambio != null) {
				orden.TipoCambio = ordenPostrera.TipoCambio;
			}
			else {
				orden.TipoCambio = BigDecimal.ZERO;
			}
			
			orden.IdInstrumento = tran.IdInstrumento;
			orden.IdInstrumentoSubyacente = tran.IdInstrumentoSubyacente;
			
			orden.IdCondicionDeLiquidacion = tran.IdCondicionDeLiquidacion;
			orden.IdTipoOperacion = tran.IdTipoOperacion;
			
			orden.IdCondicionDeLiquidacionPlazo = "T0";
			orden.NemotecnicoSVS = tran.NemotecnicoSVS;
			orden.EsVentaCorta = tran.EsVentaCorta;
			if (tran.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
				//se mantienen los valores de entrada
			}
			else if (tran.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
				orden.TasaSimultanea = tran.Tasa;
				orden.PlazoSimultanea = tran.Plazo;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				orden.PlazoReporte = tran.Plazo;
				orden.TasaReporte = tran.Tasa;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				orden.PlazoIF = tran.Plazo;
				orden.TasaRFIF = tran.Tasa;
			}
			else if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
				orden.PlazoIF = 0L;
				orden.TasaRFIF = tran.Tasa;
			}
			else {
				orden.PlazoIF = 0L;
				orden.TasaRFIF = BigDecimal.ZERO;
				orden.PlazoReporte = 0L;
				orden.TasaReporte = BigDecimal.ZERO;
				orden.TasaSimultanea = BigDecimal.ZERO;
				orden.PlazoSimultanea = 0L;
				orden.TasaVentaCorta = BigDecimal.ZERO;
				orden.PlazoVentaCorta = 0L;
			}
			orden.EsFungible = tran.EsFungible;
			orden.IdCorredorCorresponsal = tran.IdCorredorCorresponsal;
			orden.IdPersonaAutorizadoDarOrdenes = 0L;
			orden.IdTipoCanalDistribucion = _c.TIPO_CANAL_DISTRIBUCION_MANUAL;
			orden.IdPreferenciaPagoPortafolio = ordenPostrera.IdPreferenciaPagoPortafolio;
			orden.PagoCobroCuentaInversion = ordenPostrera.PagoCobroCuentaInversion;
			orden.TipoInteres = tran.TipoInteres;
			orden.BaseTasa = tran.BaseTasa;
			orden.PorcentajeAllIn = ordenPostrera.PorcentajeAllIn;
			if(ordenPostrera.Glosa.equalsIgnoreCase("")) {
				orden.Glosa = _c.GLOSA_ORDEN_POSTRERA;				
			}
			else{
				orden.Glosa = ordenPostrera.Glosa;
			}

			orden.IngresoDesde = ordenPostrera.IngresoDesde;
			
			if (_c.ORIGEN_REFACTURACION.equalsIgnoreCase(ordenPostrera.Glosa) && _c.CLASIFICACION_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento))
			{	
				orden.GlosaRefactura = _c.ORIGEN_REFACTURACION;
			}
			
			crearOrdenesEntrada.p_Ordenes.add(orden);
		}
		if(in.p_CobrosFijos.size()>0){
			for(Cobro_Fijo cob : in.p_CobrosFijos) {
				//Utils.copyPojo(cob, cobro);
				cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo cobro = copyPojoCrearOrdenesCobroFijo(cob); 
				crearOrdenesEntrada.p_CobrosFijos.add(cobro);
			}
			
		}
		
		
		for(CobroModificados_Entrada cob : in.p_CobrosModificados) {
			ObjCobrosModificados cobro = new ObjCobrosModificados();
			//Utils.copyPojo(cob, cobro);
			cobro.IdInterno = cob.IdInterno;
			cobro.IdCobro = cob.IdCobro;
			cobro.ValorCobroAplicado = cob.ValorCobroAplicado;
			crearOrdenesEntrada.p_CobrosModificados.add(cobro);
		}
		{
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(1L,"PROCESANDO","Inicio creacion de ordenes");
				BarraProgreso subBarraCrearOrdenes = in.p_BarraProgreso.subrango(1, 70);
				crearOrdenesEntrada.p_BarraProgreso = subBarraCrearOrdenes;
			}
		}
		CrearOrdenes crearOrdenesService = new CrearOrdenes();
		CrearOrdenesSalida crearOrdenesSalida = crearOrdenesService.logica(con,crearOrdenesEntrada);
		
		out.P_OUT_MENSAJES.addAll(crearOrdenesSalida.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(crearOrdenesSalida.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			if(in.p_BarraProgreso != null) {
				JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
				in.p_BarraProgreso.actualizar(100L,"ERROR",dataout);
			}
			return out;
		}
		
		HashMap<Long,ObjRESULTADO> ordenes = new HashMap<Long,ObjRESULTADO>();
		for (ObjRESULTADO res : crearOrdenesSalida.P_OUT_RESULTADO) {
			ordenes.put(res.IdInterno, res);
		}
		
		CrearAsignaciones_Entrada crearAsignacionesEntrada = new CrearAsignaciones_Entrada();
		crearAsignacionesEntrada.p_IdEmpresa = in.p_IdEmpresa;
		crearAsignacionesEntrada.p_IdModulo = in.p_IdModulo;
		crearAsignacionesEntrada.p_IdUsuario = in.p_IdUsuario;
		crearAsignacionesEntrada.p_OrigenAsig = in.p_OrigenPost;
		crearAsignacionesEntrada.p_AplicarComision = in.p_AplicarComision;
		crearAsignacionesEntrada.p_EsAsignacionMasivaPrecioPromedio = in.p_EsAsignacionMasivaPrecioPromedio;
		crearAsignacionesEntrada.p_UsaLogDetallado = in.p_UsaLogDetallado;
		
		for (OrdenPostrera_Entrada ordenPostrera : in.p_Ordenes) {
			Asignacion_Entrada asig = new Asignacion_Entrada();
			asig.Cantidad = ordenPostrera.Cantidad;
			asig.FolioCartera = ordenPostrera.FolioCartera;
			asig.IdInterno = ordenPostrera.IdInterno;
			asig.IdOrden = ordenes.get(ordenPostrera.IdInterno).IdOrden;
			asig.IdTransaccion = ordenPostrera.IdTransaccion;
			asig.ValorRescate = ordenPostrera.ValorRescate;
			asig.CodigoIFRS = ordenPostrera.CodigoIFRS;
			asig.DocumentoIdentidad = ordenPostrera.DocumentoIdentidad;
			asig.NombreCompleto = ordenPostrera.NombreCompleto;
			asig.Monto = ordenPostrera.Monto;
			asig.GlosaRefactura = ordenes.get(ordenPostrera.IdInterno).GlosaRefactura;
			asig.FechaOperacionOriginal = ordenes.get(ordenPostrera.IdInterno).FechaOperacionOriginal;

			if ("INGRESOINSTRUCCION".equals(ordenPostrera.FlujoOrigen)) {
				asig.FlujoOrigen = ordenPostrera.FlujoOrigen;
				asig.FechaTrade = ordenPostrera.FechaTrade;
			}

			crearAsignacionesEntrada.p_Asignaciones.add(asig);
			
		}
		
		CrearAsignaciones crearAsignacionesService = new CrearAsignaciones();
		if(crearOrdenesEntrada.p_simularOperacion.equals(_c.NO)) {
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(51L,"PROCESANDO","Inicio creacion de asignaciones");
					BarraProgreso subBarraCrearAsignaciones = in.p_BarraProgreso.subrango(71, 99);
					crearAsignacionesEntrada.p_BarraProgreso = subBarraCrearAsignaciones;
				}
			}
			CrearAsignaciones_Salida crearAsignacionesSalida = crearAsignacionesService.logica(con, crearAsignacionesEntrada);	
			EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
			entradaEnviarAsignacionesFacturar.p_IdEmpresa = in.p_IdEmpresa;
			entradaEnviarAsignacionesFacturar.p_IdUsuario = in.p_IdUsuario;
			entradaEnviarAsignacionesFacturar.p_IdModulo = in.p_IdModulo;
			
			for (cl.bcs.ordenes.CrearAsignaciones.Resultado_Salida resAsignacion : crearAsignacionesSalida.p_out_Resultado) {
				Resultado_Salida resPostrera = new Resultado_Salida();
				//Utils.copyPojo(resAsignacion, resPostrera);
				resPostrera.IdInterno = resAsignacion.IdInterno;
				resPostrera.IdAsignacion = resAsignacion.IdAsignacion;
				resPostrera.IdOrden = ordenes.get(resPostrera.IdInterno).IdOrden;
				out.P_OUT_RESULTADO.add(resPostrera);
				
				entradaEnviarAsignacionesFacturar.p_Asignaciones.add(new EnviarAsignacionesFacturar.Asignacion_Entrada(resAsignacion.IdAsignacion, "N"));
			}
		
			if("S".equals(in.p_EnviarFacturarAutomatico) && !entradaEnviarAsignacionesFacturar.p_Asignaciones.isEmpty()) {
				EnviarAsignacionesFacturar servicioEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
				EnviarAsignacionesFacturar_Salida salidaEnviarAsignacionesFacturar = servicioEnviarAsignacionesFacturar.logica(con, entradaEnviarAsignacionesFacturar);

	            Map<Long, cl.bcs.ordenes.CrearAsignaciones.Resultado_Salida> hashAsignaciones = crearAsignacionesSalida.p_out_Resultado.stream().collect(Collectors.toMap(h -> h.IdAsignacion, h -> h));
	            salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES.stream().forEach(msj -> {
	            	cl.bcs.ordenes.CrearAsignaciones.Resultado_Salida resultado = hashAsignaciones.get(msj.id);
	            	msj.id = resultado.IdInterno;
	            });
	            
				out.P_OUT_MENSAJES.addAll(salidaEnviarAsignacionesFacturar.P_OUT_MENSAJES);
			}
			
			for (cl.bcs.ordenes.CrearAsignaciones.Cobro_Salida cobroAsignacion : crearAsignacionesSalida.p_out_Cobros) {
				//Utils.copyPojo(cobroAsignacion, cobroPostrera);
				Cobro_Salida cobroPostrera = copyPojoCobroSalida(cobroAsignacion);
				out.P_OUT_COBROS.add(cobroPostrera);
			}
			
			out.P_OUT_MENSAJES.addAll(crearAsignacionesSalida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		}	
		logger.info("Termino del servicio CrearOrdenesPostreras, tiempo:" + (System.currentTimeMillis() - init));
		if(in.p_BarraProgreso != null) {
			in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Fin creacion de asignaciones");
		}
		return out;
	}
	
	public ObjOrdenesEntrada copyPojoObjOrdenesEntrada(OrdenPostrera_Entrada ordenPostrera) {
		ObjOrdenesEntrada orden = new ObjOrdenesEntrada();
		
		orden.IdInterno = ordenPostrera.IdInterno;
		orden.FechaRecepcion = ordenPostrera.FechaRecepcion;
		//orden.FechaVigencia = ordenPostrera.FechaVigencia;
		//orden.FechaEmision = ordenPostrera.FechaEmision;
		//orden.FechaOperacion = ordenPostrera.FechaOperacion;
		//orden.IdTipoVigencia = ordenPostrera.IdTipoVigencia;
		orden.Glosa = ordenPostrera.Glosa;
		orden.Cantidad = ordenPostrera.Cantidad;
		//orden.MontoNeto = ordenPostrera.MontoNeto;
		orden.ValorRescate = ordenPostrera.ValorRescate;
		orden.IdBoveda = ordenPostrera.IdBoveda;
		orden.IdFormaCustodia = ordenPostrera.IdFormaCustodia;
		//orden.IdPorcentajeVisibilidad = ordenPostrera.IdPorcentajeVisibilidad;
		//orden.PrecioLimite = ordenPostrera.PrecioLimite;
		orden.EsFacturaInmediata = ordenPostrera.EsFacturaInmediata;
		//orden.IdTipoPrecio = ordenPostrera.IdTipoPrecio;
		orden.IdPortafolio = ordenPostrera.IdPortafolio;
		//orden.IdTipoLibroOrden = ordenPostrera.IdTipoLibroOrden;
		//orden.IdLibroOrdenes = ordenPostrera.IdLibroOrdenes;
		//orden.IdTipoCanalIngreso = ordenPostrera.IdTipoCanalIngreso;
		//orden.IdTipoReajuste = ordenPostrera.IdTipoReajuste;
		//orden.IdEmisor = ordenPostrera.IdEmisor;
		//orden.IdBolsa = ordenPostrera.IdBolsa;
		//orden.EspecificaBolsa = ordenPostrera.EspecificaBolsa;
		orden.IdMonedaLiquidacionOrden = ordenPostrera.IdMonedaLiquidacionOrden;
		//orden.IdMonedaOrden = ordenPostrera.IdMonedaOrden;
		orden.TipoCambio = ordenPostrera.TipoCambio;
		//orden.IdInstrumento = ordenPostrera.IdInstrumento;
		//orden.IdInstrumentoSubyacente = ordenPostrera.IdInstrumentoSubyacente;
		//orden.IdCondicionDeLiquidacionPlazo = ordenPostrera.IdCondicionDeLiquidacionPlazo;
		orden.TasaVentaCorta = ordenPostrera.TasaVentaCorta;
		//orden.TasaSimultanea = ordenPostrera.TasaSimultanea;
		//orden.TasaRFIF = ordenPostrera.TasaRFIF;
		//orden.PlazoIF = ordenPostrera.PlazoIF;
		//orden.PlazoSimultanea = ordenPostrera.PlazoSimultanea;
		orden.PlazoVentaCorta = ordenPostrera.PlazoVentaCorta;
		//orden.EsVentaCorta = ordenPostrera.EsVentaCorta;
		//orden.EsOD = ordenPostrera.EsOD;
		//orden.EsFungible = ordenPostrera.EsFungible;
		//orden.IdCorredorCorresponsal = ordenPostrera.IdCorredorCorresponsal;
		//orden.IdPersonaAutorizadoDarOrdenes = ordenPostrera.IdPersonaAutorizadoDarOrdenes;
		//orden.IdTipoCanalDistribucion = ordenPostrera.IdTipoCanalDistribucion;
		//orden.TasaReporte = ordenPostrera.TasaReporte;
		//orden.PlazoReporte = ordenPostrera.PlazoReporte;
		//orden.BaseTasa = ordenPostrera.BaseTasa;
		//orden.TipoInteres = ordenPostrera.TipoInteres;
		//orden.IdCondicionDeLiquidacion = ordenPostrera.IdCondicionDeLiquidacion;
		//orden.IdTipoOperacion = ordenPostrera.IdTipoOperacion;
		orden.IdCuentaCustodioPortafolio = ordenPostrera.IdCuentaCustodioPortafolio;
		//orden.EsPorMonto = ordenPostrera.EsPorMonto;
		//orden.EsOrdenEspecial = ordenPostrera.EsOrdenEspecial;
		orden.PagoCobroCuentaInversion = ordenPostrera.PagoCobroCuentaInversion;
		orden.IdPreferenciaPagoPortafolio = ordenPostrera.IdPreferenciaPagoPortafolio;
		//orden.IdMecanismoCaptacion = ordenPostrera.IdMecanismoCaptacion;
		//orden.TasaPromedioReporte = ordenPostrera.TasaPromedioReporte;
		//orden.TasaReportanteReferencia = ordenPostrera.TasaReportanteReferencia;
		//orden.NemotecnicoSVS = ordenPostrera.NemotecnicoSVS;
		//orden.IdSistemaBursatil = ordenPostrera.IdSistemaBursatil;
		//orden.InstrumentoAcreditado = ordenPostrera.InstrumentoAcreditado;
		//orden.FormadorMercado = ordenPostrera.FormadorMercado;
		//orden.EsClienteFinal = ordenPostrera.EsClienteFinal;
		//orden.MontoInteresContado = ordenPostrera.MontoInteresContado;
		//orden.MontoInteresPlazo = ordenPostrera.MontoInteresPlazo;
		orden.PorcentajeAllIn = ordenPostrera.PorcentajeAllIn;
		//orden.IdOrdenReferencia = ordenPostrera.IdOrdenReferencia;
		
		return orden;
	}
	
	public cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo copyPojoCrearOrdenesCobroFijo(Cobro_Fijo cob) {
		cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo cobro = new cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo();
		cobro.IdInterno = cob.IdInterno;
		cobro.IdInternoCobro = cob.IdInternoCobro;
		cobro.IdPortafolio = cob.IdPortafolio;
		cobro.IdBolsa = cob.IdBolsa;
		cobro.IdTipoProducto = cob.IdTipoProducto;
		cobro.IdTipoOperacion = cob.IdTipoOperacion;
		cobro.IdTipoCanalIngreso = cob.IdTipoCanalIngreso;
		cobro.IdCobro = cob.IdCobro;
		cobro.ValorCobro = cob.ValorCobro;
		cobro.FormaCobro = cob.FormaCobro;
		cobro.ValorTopeMinimoOrden = cob.ValorTopeMinimoOrden;
		cobro.IdMoneda = cob.IdMoneda;
		cobro.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
		cobro.PermiteCambiarValor = cob.PermiteCambiarValor;
		return cobro;
	}
	
	private Cobro_Salida copyPojoCobroSalida(cl.bcs.ordenes.CrearAsignaciones.Cobro_Salida cobroAsignacion) {
		Cobro_Salida cobroPostrera = new Cobro_Salida();
		cobroPostrera.IdInterno = cobroAsignacion.IdInterno;
		cobroPostrera.IdAsignacion = cobroAsignacion.IdAsignacion;
		cobroPostrera.IdCobroAsignacion = cobroAsignacion.IdCobroAsignacion;
		cobroPostrera.CodigoCobroClasificacion = cobroAsignacion.CodigoCobroClasificacion;
		cobroPostrera.IdCobro = cobroAsignacion.IdCobro;
		cobroPostrera.IdMonedaCobro = cobroAsignacion.IdMonedaCobro;
		cobroPostrera.ValorCobroPerfil = cobroAsignacion.ValorCobroPerfil;
		cobroPostrera.FormaCobro = cobroAsignacion.FormaCobro;
		cobroPostrera.MontoMinimo = cobroAsignacion.MontoMinimo;
		cobroPostrera.ValorCobroAplicado = cobroAsignacion.ValorCobroAplicado;
		cobroPostrera.MontoCobroCalculado = cobroAsignacion.MontoCobroCalculado;
		cobroPostrera.MontoCobroCalculadoLocal = cobroAsignacion.MontoCobroCalculadoLocal;
		cobroPostrera.MontoCobroCalculadoLiquidacion = cobroAsignacion.MontoCobroCalculadoLiquidacion;
		return cobroPostrera;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 0,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_Ordenes\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"FechaRecepcion\": \"2024-11-04 19:13:49\",\r\n"
					+ "            \"Glosa\": \"\",\r\n"
					+ "            \"IdBoveda\": \"DCV\",\r\n"
					+ "            \"IdFormaCustodia\": \"CUS\",\r\n"
					+ "            \"EsFacturaInmediata\": \"N\",\r\n"
					+ "            \"IdTransaccion\": 344070,\r\n"
					+ "            \"FolioTransaccion\": \"123955\",\r\n"
					+ "            \"IdPortafolio\": 302341,\r\n"
					+ "            \"Cantidad\": 30,\r\n"
					+ "            \"ValorRescate\": 0,\r\n"
					+ "            \"FolioCartera\": 0,\r\n"
					+ "            \"TipoCambio\": 0,\r\n"
					+ "            \"IdMonedaLiquidacionOrden\": \"USD\",\r\n"
					+ "            \"TasaVentaCorta\": 0,\r\n"
					+ "            \"PlazoVentaCorta\": 0,\r\n"
					+ "            \"PagoCobroCuentaInversion\": \"S\",\r\n"
					+ "            \"IdCuentaCustodioPortafolio\": null,\r\n"
					+ "            \"CodigoIFRS\": null,\r\n"
					+ "            \"IngresoDesde\": \"COR\"\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"p_CobrosModificados\": []\r\n"
					+ "}";
			CrearOrdenesPostreras_Entrada Objin = gson.fromJson(json, CrearOrdenesPostreras_Entrada.class);
			
			CrearOrdenesPostreras demo=new CrearOrdenesPostreras();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
 			try {
 				CrearOrdenesPostreras_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			con.rollback();
	    	}
		 	
		System.out.println("== END ==");
	}

}