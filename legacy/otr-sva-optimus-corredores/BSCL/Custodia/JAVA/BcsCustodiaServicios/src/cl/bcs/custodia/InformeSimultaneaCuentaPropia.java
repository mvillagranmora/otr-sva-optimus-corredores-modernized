package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;

import cl.bcs.custodia.utils.ValorizarInforme;
import cl.bcs.custodia.utils.ValorizarInforme.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeEntrada;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeSalida;
import cl.bcs.mesa.MES.ConsultarCarteraPactosValorizada.ResumenCartera;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InformeSimultaneaCuentaPropia implements JavaCall {
	static final Logger logger = LogManager.getLogger(InformeSimultaneaCuentaPropia.class);
	
	private final String SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ACTUAL = loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectSimultaneasVigentes.sql");
	private final String SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ANTERIOR	= loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectSimultaneasVigentesFechaAnterior.sql");
    private final String SQL_SELECT_CLIENTE_SOCIONEGOCIO = loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectClienteSocioNegocio.sql");
	private final String SQL_SELECT_CLIENTE_MESA = loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectClienteMesa.sql");
	private final String SQL_SELECT_CARTERA_VALORIZADA = loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectCarteraValorizada.sql");
	private final String SQL_SELECT_FECHA_ULTIMO_RESPALDO = loadResourceToString("cl/bcs/custodia/sql/InformeSimultaneasCuentaPropia_SelectFechaUltimoRespaldo.sql");
	
	public static class SalidaSimultaneas {
		public Long			IdValorizacion;
		public Long 		IdPortafolio;
		public Long 		IdInstrumento;
		public String 		FechaOperacion;
		public Long			IdCorredoraContraparte;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdBolsa;
		public String 		IdTipoInstrumento;
		public String 		IdTipoOperacion;
		public String 		Nemotecnico;
		public String		NemoSVS;
		public String 		IdMonedaInstrumento;
		public BigDecimal 	Cantidad;
		public BigDecimal	ValorRescate;
		public BigDecimal 	PrecioCompra;
		public BigDecimal 	TasaCompra;
		public BigDecimal 	WS_PrecioSubyacente;
		public BigDecimal 	WS_TasaSubyacente;
		public BigDecimal 	WS_PrecioGarantia;
		public BigDecimal 	WS_TasaGarantia;
		public String 		TieneCorteDeCupon;
		public Long 		IdCliente;
		public Long 		IdCartera;
		public Long 		FolioCartera;
		public Long 		IdOperacion;
		public BigDecimal 	PlazoEnDiasOperacion;
		public String 		IdCondicionLiquidacion;
		public String 		EsRegistroNuevo;
		public BigDecimal 	PQ_Plazo;
		public String 		FechaVencimientoOperacion;
		public String 		FechaLiquidacion;
		public BigDecimal 	INGMontoATasaCompraActual;
		public String 		OrigenProcesamiento;
		public String 		Diferenciador;
		public String		IdBoveda;
		public Long			IdAgente;
		public BigDecimal	MontoPactado;
		public String		IdConcepto;
		public String		TipoCartera;
		public String		FechaVencimientoInstrumento;
		public Integer		PlazoActual;
		public BigDecimal	MontoCompra;
		public BigDecimal	MontoCompraMonedaInstrumento;
		public BigDecimal	MontoCompraMonedaAdicional;
		public String		Emisor;
		public String		NombreCliente;
		public Long			FolioComprobante;
		public BigDecimal	PrecioAPlazo;
		public BigDecimal	MontoAPlazo;
		public BigDecimal	MontoAPlazoMonedaAdicional;
		public BigDecimal	MontoAPlazoMonedaInstrumento;
		public String		FolioTransaccionNormal;
		public String		FolioTransaccionPlazo;
		public String		IdTipoProducto;
		public String       FechaUltimoRespaldo;

		public SalidaSimultaneas() {
			this.IdPortafolio = 0L;
			this.IdInstrumento = 0L;
			this.FechaOperacion = "";
			this.IdCorredoraContraparte = 0L;
			this.IdClasificacionTipoInstrumento = "";
			this.IdTipoInstrumento = "";
			this.Nemotecnico = "";
			this.IdMonedaInstrumento = "";
			this.Cantidad = BigDecimal.ZERO;
			this.ValorRescate = null;
			this.PrecioCompra = BigDecimal.ZERO;
			this.TasaCompra = BigDecimal.ZERO;
			this.WS_PrecioSubyacente = null;
			this.WS_TasaSubyacente = null;
			this.WS_PrecioGarantia = null;
			this.WS_TasaGarantia = null;
			this.TieneCorteDeCupon = "";
			this.IdCliente = 0L;
			this.IdCartera = 0L;
			this.EsRegistroNuevo = "N";
			this.PQ_Plazo = BigDecimal.ZERO;
			this.FechaVencimientoOperacion = "";
			this.FechaLiquidacion = "";
			this.INGMontoATasaCompraActual = BigDecimal.ZERO;
			this.OrigenProcesamiento = "CARTERA.SIMULTANEA";
		}
	}
	
	private static final String TIPO_INFORME = "SIMULTANEA";

	public static class EntradaConsultarSimultaneasVigentes_IN extends BcsPojo{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_FechaConsulta;
		public String	p_FechaValorizacion;
		public String	p_TipoFecha;
		public Long     p_IdPais;
		public String	p_TipoValorizacion;
		public Long     p_IdSocioNegocio;
		public Boolean  p_EjecutaValorizacion;
		
		//Filtros
		public Long		p_IdCliente;
		public Long		p_IdPortafolio;
		public String	p_IdTipoOperacion;
		public Long		p_IdInstrumentoSubyacente;
		public String	p_PaloNormal;
		public String	p_PaloPlazo;
		public EntradaConsultarSimultaneasVigentes_IN() {
			this.p_IdCliente				= 0L;
			this.p_IdPortafolio				= 0L;
			this.p_IdTipoOperacion			= "";
			this.p_IdInstrumentoSubyacente	= 0L;
			this.p_PaloNormal				= "";
			this.p_PaloPlazo				= "";
			this.p_EjecutaValorizacion = 	false;
		}
	}

	public static class SalidaInformeSimultaneasVigentes_OUT {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResumenCartera> P_OUT_CARTERA_RESUMEN;
		public String P_OUT_LLAVE_REPORTE;
		
		public SalidaInformeSimultaneasVigentes_OUT() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_LLAVE_REPORTE = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CARTERA_RESUMEN = new ArrayList<ResumenCartera>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

    public static class Cliente {
		public Long IdCliente;
		
		public Cliente() {
			this.IdCliente = 0L;
		}
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarSimultaneasVigentes_IN jin = gson.fromJson(in.toString(), EntradaConsultarSimultaneasVigentes_IN.class);
		SalidaInformeSimultaneasVigentes_OUT out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public boolean ValidacionIds(Long id) {
		return id == null || id.equals(0L);
	}
	
	public boolean ValidarFechas(String fecha) {
		return fecha == null || fecha.isEmpty();
	}

	public SalidaInformeSimultaneasVigentes_OUT logica (Connection con, EntradaConsultarSimultaneasVigentes_IN in) throws Exception {
		SalidaInformeSimultaneasVigentes_OUT out = new SalidaInformeSimultaneasVigentes_OUT();
		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<>(15);
		
		//VALIDACIONES IDS EMPRESA, SOCIONEGOCIO, CLIENTE
		if(ValidacionIds(in.p_IdCliente) && ValidacionIds(in.p_IdSocioNegocio) && ValidacionIds(in.p_IdEmpresa)) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoIdsNulos = "IDS_NULOS";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoIdsNulos, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoIdsNulos).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}
		
		//VALIDACION TIPO VALORIZACION
		if (!in.p_TipoValorizacion.equals("COMP")) {
			out.P_OUT_ERRORESCONTADOR++;
			String codigoTipoValorizacionInvalida = "TIPO_VALORIZACION_INVALIDA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoTipoValorizacionInvalida, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoTipoValorizacionInvalida).msg());
			out.P_OUT_MENSAJES.add(error);
			return out;
		}
		
		Long IdCliente = 0L;
		InformacionEmpresa empresa = new ConsultarEmpresa().logica(con, in.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Cliente> cliente = new ArrayList<>();
		
		HashMap<String,Object> params = new HashMap<>();
		if (in.p_IdCliente.equals(0L) && in.p_IdSocioNegocio.equals(0L) && !in.p_IdEmpresa.equals(0L)) {
			params.put("IdEmpresa", in.p_IdEmpresa);
			cliente = sql.query(con, SQL_SELECT_CLIENTE_MESA, params, Cliente.class);
			IdCliente = cliente.stream().findFirst().orElse(new Cliente()).IdCliente;
		} else if (in.p_IdCliente.equals(0L) && !in.p_IdSocioNegocio.equals(0L)) {
			params.put("IdSocioNegocio", in.p_IdSocioNegocio);
			cliente = sql.query(con, SQL_SELECT_CLIENTE_SOCIONEGOCIO, params, Cliente.class);
			IdCliente = cliente.stream().findFirst().orElse(new Cliente()).IdCliente;
		} else if (!in.p_IdCliente.equals(0L)) {
			IdCliente = in.p_IdCliente;
		}

		/* INICIO VALIDACIONES FECHA */
		if (this.ValidarFechas(in.p_FechaConsulta) || this.ValidarFechas(in.p_FechaValorizacion)) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR , "FECHA_INVALIDA", Diccionario.getMensaje(con,in.p_IdEmpresa, "FECHA_INVALIDA").msg()));
			return out;
		} else {
			in.p_FechaConsulta = in.p_FechaConsulta.substring(0, 10);
		}
		
		if (Fecha.toDate(in.p_FechaValorizacion).before(Fecha.toDate(in.p_FechaConsulta))) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,"FECHA_VALORIZACION_INVALIDA", Diccionario.getMensaje(con,in.p_IdEmpresa, "FECHA_VALORIZACION_INVALIDA").msg()));
			return out;
		}
        /* FIN VALIDACIONES FECHA */

        LocalDate parsedFechaConsulta = LocalDate.parse(in.p_FechaConsulta);
		LocalDate parsedFechaValorizacion = LocalDate.parse(in.p_FechaValorizacion);
		
		Long existeValorizacion = 0L;
		if (!in.p_EjecutaValorizacion) {
			
			param.clear();
            String partialKey = TIPO_INFORME + in.p_TipoValorizacion + parsedFechaConsulta + parsedFechaValorizacion;
            param.put("p_Key", partialKey);

            existeValorizacion = sql.countQuery(con, SQL_SELECT_CARTERA_VALORIZADA, param);
            param.clear();

            String tipo = existeValorizacion == 0L ? "ERROR" : "INFORMACION";
            String mensaje = existeValorizacion.equals(0L) ? "No existe Valorizaci�n para Fecha Seleccionada" : "Proceso Valorizaci�n Finalizado";

            if ("ERROR".equals(tipo)) {
                out.P_OUT_ERRORESCONTADOR++;
            }

            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, tipo, "", mensaje));

            return out;
        }

        Boolean esCuentaPropia = empresa.IdCliente.equals(IdCliente);
        
        //VALIDACION CUENTA PROPIA
        if (!esCuentaPropia) {
            out.P_OUT_ERRORESCONTADOR++;
            String codigoCuentaPropia = "ERROR_CUENTA_PROPIA";
            MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoCuentaPropia, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoCuentaPropia).msg());
            out.P_OUT_MENSAJES.add(error);
            return out;
        }
        
        
		logger.info("INICIO::ConsultarSimultaneasVigentes");
		
		param.put("IdCliente", IdCliente);
		param.put("IdPortafolio", in.p_IdPortafolio);
		param.put("FechaConsulta", in.p_FechaConsulta);
		
		List<SalidaSimultaneas> simultaneasVigentes = new ArrayList<>();
		if (Fecha.toDate(in.p_FechaConsulta).before(Fecha.toDate(fechaSistema))) {	
			HashMap<String, Object> paramRespaldo = new HashMap<>(1);
			paramRespaldo.put("p_Fecha", in.p_FechaConsulta);
			String fechaRespaldo = sql.query(con, SQL_SELECT_FECHA_ULTIMO_RESPALDO, paramRespaldo, SalidaSimultaneas.class).stream().findFirst().get().FechaUltimoRespaldo;
			param.put("FechaConsulta", fechaRespaldo);
			simultaneasVigentes.addAll(sql.query(con, SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ANTERIOR, param, SalidaSimultaneas.class));
		} else {
			simultaneasVigentes.addAll(sql.query(con, SQL_SELECT_SIMULTANEAS_VIGENTES_FECHA_ACTUAL, param, SalidaSimultaneas.class));
		}

		if(simultaneasVigentes.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR++;
            String codigoSinSimultaneas = "SIN_SIMULTANEAS_PARA_VALORIZAR";
            MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoSinSimultaneas, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoSinSimultaneas).msg());
            out.P_OUT_MENSAJES.add(error);
            return out;
		}

		logger.info("FIN::ConsultarSimultaneasVigentes");		

		List<Long> IdsPortafolio = new ArrayList<Long>();
	
		if(in.p_IdPortafolio == null || in.p_IdPortafolio.equals(0L)) {
			IdsPortafolio = simultaneasVigentes.stream()
					.map(detalle -> detalle.IdPortafolio)
					.distinct()
					.collect(Collectors.toList());     
		} else {
			IdsPortafolio.add(in.p_IdPortafolio);
		}
		
		// COMENZAR VALORIZACION
		if (esCuentaPropia && !simultaneasVigentes.isEmpty()) {
			
			//SERVICIO DE LOGS
			AgregarLog agregarLogService = new AgregarLog();
			InAgregarLog agregarLogEntrada = new InAgregarLog();
			
			agregarLogEntrada.p_IdEmpresa = in.p_IdEmpresa;
			agregarLogEntrada.p_IdUsuario = in.p_IdUsuario;
			agregarLogEntrada.p_IdModulo = in.p_IdModulo;
			agregarLogEntrada.p_UsaReglasNegocio = "Si";
			agregarLogEntrada.p_IdEstadoAutorizacion = "S";
			agregarLogEntrada.p_IdTrabajo = 0L;
			
			String tipoLog = "INFORMATIVO";
			String subregistroLog = "";
			String valorAntiguo = "";
			String valorNuevo = "";			
			
			//VALORIZA
			ValorizarInformeEntrada valorizarInformeEntrada = new ValorizarInformeEntrada();
			valorizarInformeEntrada.p_IdEmpresa 					= in.p_IdEmpresa;
			valorizarInformeEntrada.p_IdUsuario 					= in.p_IdUsuario;
			valorizarInformeEntrada.p_IdModulo 						= in.p_IdModulo;
			valorizarInformeEntrada.p_IdPais 						= in.p_IdPais;
			valorizarInformeEntrada.p_IdMonedaAdicional 			= "USD";
			valorizarInformeEntrada.p_IdsPortafolio 				= IdsPortafolio;
			valorizarInformeEntrada.p_FechaInicial					= in.p_FechaConsulta;
			valorizarInformeEntrada.p_FechaFinal					= in.p_FechaValorizacion;
			valorizarInformeEntrada.p_OrigenProcesamiento 			= "CARTERA.SIMULTANEA"; 
			valorizarInformeEntrada.p_IdTipoTasaValorizacion		= in.p_TipoValorizacion;
			valorizarInformeEntrada.p_IdFechaHoraSistema			= FechaDB.ObtenerFechaHoraSistemaLocal(con,in.p_IdEmpresa);
			valorizarInformeEntrada.p_IdClienteSolicitante          = IdCliente;
			
			ValorizarInformeSalida valorizarInformeSalidaGeneral = new ValorizarInformeSalida();
			
			for (int i = 0; i < simultaneasVigentes.size() ; i++ ){
				SalidaSimultaneas detalle = simultaneasVigentes.get(i); //<- Crear DetalleCartera
				ValorizarInforme.ObjEntradaInstrumentos entradaInstrumento = new ObjEntradaInstrumentos();
				//BASE
				entradaInstrumento.TablaAfecta 						= "CARTERA";
				entradaInstrumento.IdValorizacion	 				= detalle.IdValorizacion;
				entradaInstrumento.IdCliente		 				= detalle.IdCliente;
				entradaInstrumento.IdPortafolio		 				= detalle.IdPortafolio;
				entradaInstrumento.IdCartera		 				= detalle.IdCartera;
				entradaInstrumento.FolioCartera		 				= detalle.FolioCartera;
				
				//OPERACION
				entradaInstrumento.IdTipoOperacion					= detalle.IdTipoOperacion;
				entradaInstrumento.FechaOperacion					= detalle.FechaOperacion;				
				entradaInstrumento.FechaVencimientoOperacion		= detalle.FechaVencimientoOperacion;
				entradaInstrumento.IdOperacion						= detalle.IdOperacion;
				entradaInstrumento.PlazoEnDiasOperacion				= detalle.PlazoEnDiasOperacion.longValue();
				entradaInstrumento.IdCondicionLiquidacion			= detalle.IdCondicionLiquidacion;
				entradaInstrumento.FechaLiquidacion					= detalle.FechaLiquidacion;
				entradaInstrumento.Diferenciador					= detalle.Diferenciador;
				
				//INSTRUMENTO
				entradaInstrumento.IdClasificacionTipoInstrumento	= detalle.IdClasificacionTipoInstrumento;
				entradaInstrumento.IdTipoInstrumento				= detalle.IdTipoInstrumento; 
				entradaInstrumento.IdInstrumento					= detalle.IdInstrumento;
				entradaInstrumento.Emisor							= detalle.Emisor;								//valorizacionCL		
				entradaInstrumento.Nemotecnico 						= detalle.Nemotecnico;
				entradaInstrumento.NemoSVS							= detalle.NemoSVS;
				entradaInstrumento.IdMonedaInstrumento				= detalle.IdMonedaInstrumento;
				entradaInstrumento.FechaVencimientoInstrumento		= detalle.FechaVencimientoInstrumento;
				
				//VALOR ACTUAL
				entradaInstrumento.Cantidad							= detalle.Cantidad; 
				entradaInstrumento.ValorRescate						= detalle.ValorRescate; 
				entradaInstrumento.PrecioCompra						= detalle.PrecioCompra; 
				entradaInstrumento.TasaCompra						= detalle.TasaCompra;
				entradaInstrumento.TasaCompraValorizacionFechaOperacion = detalle.TasaCompra;
				entradaInstrumento.MontoCompra						= detalle.MontoCompra;								
				entradaInstrumento.MontoCompraMonedaSistema			= BigDecimal.ZERO;					
				entradaInstrumento.MontoCompraMonedaInstrumento		= detalle.MontoCompraMonedaInstrumento;				
				entradaInstrumento.MontoCompraMonedaAdicional		= detalle.MontoCompraMonedaAdicional;
				entradaInstrumento.NombreCliente					= detalle.NombreCliente;
				entradaInstrumento.PrecioAPlazo						= detalle.PrecioAPlazo;
				entradaInstrumento.MontoAPlazo						= detalle.MontoAPlazo;
				entradaInstrumento.MontoAPlazoMonedaAdicional		= detalle.MontoAPlazoMonedaAdicional;
				entradaInstrumento.MontoAPlazoMonedaInstrumento		= detalle.MontoAPlazoMonedaInstrumento;
				
				//DATOS INFORME
				entradaInstrumento.IdTipoReajusteCabecera			= "";     	
				entradaInstrumento.TablaOrigen						= "CARTERA";		
				entradaInstrumento.IdUso							= null; 
				entradaInstrumento.TipoCartera						= detalle.TipoCartera; 
				entradaInstrumento.IdTipoCriterioTasa				= "NORMATIVO";

				entradaInstrumento.OrigenProcesamiento				= detalle.OrigenProcesamiento;
				entradaInstrumento.IdConcepto						= detalle.IdConcepto;
				entradaInstrumento.IdBoveda							= detalle.IdBoveda;
				entradaInstrumento.IdBolsa  						= detalle.IdBolsa;
				entradaInstrumento.IdAgente							= detalle.IdAgente;
				entradaInstrumento.MontoPactado						= detalle.MontoPactado;
				entradaInstrumento.IdCorredoraContraparte			= detalle.IdCorredoraContraparte;
				entradaInstrumento.FolioComprobante					= detalle.FolioComprobante;
				entradaInstrumento.FolioTransaccionNormal			= detalle.FolioTransaccionNormal;
				entradaInstrumento.FolioTransaccionPlazo			= detalle.FolioTransaccionPlazo;
				entradaInstrumento.IdTipoProducto					= detalle.IdTipoProducto;
				
				valorizarInformeEntrada.p_Instrumentos.add(entradaInstrumento);	
				
				//AL FINAL O CADA 5000
				if ((i!= 0 && i%5000 == 0) || i == simultaneasVigentes.size() - 1) {
					logger.info("Valorizando hasta " + i);
					ValorizarInforme ValorizarPapeles = new ValorizarInforme();
					ValorizarInformeSalida Salida = new ValorizarInformeSalida();
					try {
						Salida = ValorizarPapeles.logica(con, valorizarInformeEntrada);
					} catch(Exception err) {
						logger.error(err);
						err.printStackTrace();
						valorNuevo = err.toString();
						Salida.P_OUT_ERRORESCONTADOR++;
					} finally {
						valorizarInformeSalidaGeneral.P_OUT_ERRORESCONTADOR += Salida.P_OUT_ERRORESCONTADOR;
						valorizarInformeSalidaGeneral.P_OUT_ERRORVALORIZACIONES.addAll(Salida.P_OUT_ERRORVALORIZACIONES);
						valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS.addAll(Salida.P_OUT_INSTRUMENTOSVALORIZADOS);
						valorizarInformeSalidaGeneral.P_OUT_MENSAJES.addAll(Salida.P_OUT_MENSAJES);
						valorizarInformeSalidaGeneral.LlaveReporte = Salida.LlaveReporte;
						//LIMPIA LOS INSTRUMENTOS ANTERIORERMENTE VALORIZADOS PARA LA SIGUIENTE TANDA
						valorizarInformeEntrada.p_Instrumentos.clear();
					}
				}
			} 			
			
			out.P_OUT_LLAVE_REPORTE = valorizarInformeSalidaGeneral.LlaveReporte;			
				
			if (valorizarInformeSalidaGeneral.P_OUT_ERRORESCONTADOR > 0) {
				logger.debug("Valorizacion erronea");				
				out.P_OUT_ERRORESCONTADOR++;
				String codigoError = "VALORIZACION_SIMULTANEA_ERRONEA";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorValorizacion);
				tipoLog = "ERROR";
				subregistroLog = "ERROR";
			}
			
			Long ultimaValorizacion = valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS.size() != 0 ?
					valorizarInformeSalidaGeneral.P_OUT_INSTRUMENTOSVALORIZADOS
					.stream()
					.max(Comparator.comparingLong(inst -> inst.IdValorizacion))
					.get().IdValorizacion : (long) (Math.random())*100000;
			
			Log logValorizacion = new Log();			
			logValorizacion.IdCabeceraLog = ultimaValorizacion; 
			logValorizacion.IdTipoLog = tipoLog; 
			logValorizacion.IdModulo = in.p_IdModulo; 
			logValorizacion.IdUsuario = in.p_IdUsuario; 
			logValorizacion.IdEmpresa = in.p_IdEmpresa; 
			//logValorizacion.FechaSistemaLog = fechaSistema; 
			logValorizacion.IdCliente = IdCliente;
			logValorizacion.NombreCliente = empresa.NombreCompleto;
			logValorizacion.IdTipoRegistro = "INFORME_SIMULTANEAS_CUENTA_PROPIA";
			logValorizacion.NombreProceso = "Valorizacion Informe Simultanea"; 		
			
			agregarLogEntrada.p_Log.add(logValorizacion);	
			agregarLogEntrada.agregarDetalleLogBasico(ultimaValorizacion, 0L, "VALORIZACION", valorAntiguo, valorNuevo, subregistroLog);
			
			OutAgregarLog agregarLogSalida = agregarLogService.logica(con, agregarLogEntrada);
			
			if(agregarLogSalida.P_OUT_ERRORESCONTADOR > 0L) {
				//SI SE AGREGALOG TIENE ERRORES, SE ENVIA MENSAJE DE ADVERTENCIA 				
				logger.debug("No se pudo agregar log");
				String codigoError = "ERROR_AGREGAR_LOG";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, codigoError, Diccionario.getMensaje(con,in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorValorizacion);				
			}
		}
		// FINAL VALORIZACION
		return out;
		
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master01","C:\\Users\\gonzalo.pacheco.ext\\Documents\\Desarrollo\\optimus-corredores\\connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CUS\",\"p_Fecha\":\"2025-08-21\",\"p_FechaConsulta\":\"2025-08-21\",\"p_FechaValorizacion\":\"2025-08-22\",\"p_FechaValorizada\":\"2025-08-22\",\"p_TipoValorizacion\":\"MERC\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdSocioNegocio\":0,\"p_TipoReporte\":\"INF_SIM\",\"p_EjecutaValorizacion\":true,\"p_IdPais\":44,\"p_TipoFecha\":\"ACT\",\"p_IdTipoOperacion\":\"\",\"p_IdInstrumentoSubyacente\":0,\"p_PaloNormal\":\"\",\"p_PaloPlazo\":\"\"}";
			EntradaConsultarSimultaneasVigentes_IN ObjIn = gson.fromJson(Json,EntradaConsultarSimultaneasVigentes_IN.class);
			
			EntradaConsultarSimultaneasVigentes_IN entrada = new EntradaConsultarSimultaneasVigentes_IN();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 97L;
			entrada.p_IdModulo = "MRF";
			entrada.p_FechaConsulta = "2025-03-30T03:00:00.000Z"; // validar fechas siempre valorizada>fechaconsulta
			entrada.p_FechaValorizacion = "2025-03-31T03:00:00.000Z";
			entrada.p_IdPais = 44L;
			entrada.p_TipoFecha = "ACT";
			entrada.p_TipoValorizacion = "C";
			entrada.p_IdCliente = 4846L; // 4846 pillar idEmpresa
			entrada.p_IdPortafolio = 2226L; // 2226
			entrada.p_IdSocioNegocio = 1596L; // 1596
			entrada.p_IdTipoOperacion = "";
			entrada.p_IdInstrumentoSubyacente = 0L;
			entrada.p_PaloNormal = "";
			entrada.p_PaloPlazo = "";
			
			InformeSimultaneaCuentaPropia simultaneasCP = new InformeSimultaneaCuentaPropia();		
			
			try {
				SalidaInformeSimultaneasVigentes_OUT salida = simultaneasCP.logica(con, ObjIn);
				System.out.println(salida);
			} catch(Exception er) {
				er.printStackTrace();
			} finally {
				//con.rollback();
				//con.commit();
			}
			
		}

		System.out.println("== END ==");
	}
}
