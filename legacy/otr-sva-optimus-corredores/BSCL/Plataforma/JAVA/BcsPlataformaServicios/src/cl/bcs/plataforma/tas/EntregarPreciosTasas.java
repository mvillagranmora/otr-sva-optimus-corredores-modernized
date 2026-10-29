package cl.bcs.plataforma.tas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class EntregarPreciosTasas implements JavaCall{
	
	// Log
	static final Logger logger = LogManager.getLogger(EntregarPreciosTasas.class.getName());
	
	// Queries
	private final String SQL_SELECT_DATOS_VALIDACION = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectDatosValidacion.sql");
	private final String SQL_SELECT_CRITERIOS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectCriterios.sql");
	
	private final String SQL_INSERT_INSTRUMENTO_FECHA = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_InsertInstrumentoFecha.sql");
	private final String SQL_INSERT_INSTRUMENTOS_PROCESO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_InsertInstrumentosProceso.sql");
	private final String SQL_INSERT_INSTRUMENTO_FECHA_ENTIDAD = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_InsertInstrumentoFechaEntidad.sql");
	private final String SQL_INSERT_TASAS_PUBLICADAS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_InsertTasasPublicadas.sql");
	
	private final String SQL_DELETE_INSTRUMENTO_FECHA = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_DeleteInstrumentoFecha.sql");
	private final String SQL_DELETE_INSTRUMENTOS_PROCESO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_DeleteInstrumentosProceso.sql");
	private final String SQL_DELETE_TASAS_PUBLICADAS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_DeleteTasasPublicadas.sql");
	
	private final String SQL_SELECT_PRECIOS_MERCADO_BCS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosMercadoBcs.sql");
	private final String SQL_SELECT_PRECIOS_MERCADO_AGF = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosMercadoAGF.sql");
	private final String SQL_SELECT_PRECIOS_TASAS_INSTRUMENTO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosTasasInstrumento.sql");
	private final String SQL_SELECT_TASAIN_PARAMS_NEGOCIACION = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectTasainParamsNegociacion.sql");
	private final String SQL_SELECT_TASAIN_OBTENER_INFO_INS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectTasainObtenerInsRaiz.sql");
	private final String SQL_SELECT_PRECIOS_MEDIO_BCS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosMedioBcs.sql");
	
	private final String SQL_SELECT_ENTIDAD_VECTOR_PRECIOS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectEntidadVectorPrecios.sql");
	private final String SQL_SELECT_VECTOR_PRECIOS_GARANTIAS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectVectorPreciosGarantias.sql");
	private final String SQL_SELECT_PRECIOS_TASAS_PUBLICADAS = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosTasasPublicadas.sql");
	private final String SQL_SELECT_PRECIOS_TASAS_PUBLICADAS_PLAZO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosTasasPublicadasPlazo.sql");
	
	// MSH
	private final String SQL_INSERT_INSTRUMENTO_PLAZO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_InsertInstrumentoPlazo.sql");
	private final String SQL_SELECT_PRECIOS_TASAS_INSTRUMENTO_PLAZO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_SelectPreciosTasasInstrumentoPlazo.sql");
	private final String SQL_DELETE_INSTRUMENTO_PLAZO = loadResourceToString("cl/bcs/plataforma/tas/sql/EntregarPreciosTasas_DeleteInstrumentoPlazo.sql");	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class EntregarPreciosTasas_Entrada 
	{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPais;
		public List<EntregarPreciosTasas_Entrada_DatosInstrumentos> p_DatosInstrumentos;
		
		public EntregarPreciosTasas_Entrada() 
		{			
			p_DatosInstrumentos = new ArrayList<EntregarPreciosTasas_Entrada_DatosInstrumentos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	 
	public static class EntregarPreciosTasas_Entrada_DatosInstrumentos 
	{
		public Long 		 IdRegistro;
		public String 		 TipoCartera;   		// --> PROPIA: CARTERA PROPIA  /  TERCEROS: CUENTA TERCEROS / "0": TODOS 
		public String 		 IdUso; 				// --> CARTERA / GARANTIA / SUBYACENTE / ORDEN / PRECIOMEDIO / SUSOSA
		public String 		 IdTipoCriterioTasa;  	// --> COMERCIAL /  NORMATIVO  / "0": TODOS
		public String 		 IdClasificacionTipoInstrumento; //--> RV, RF, IF
		public String 		 IdTipoInstrumento; 	// -->  ACN, ACX, BBC,.. / "0": TODOS
		public Long 		 IdInstrumento;
		public String 		 Nemotecnico;
		public String 		 IdMoneda;
		public java.sql.Date FechaConsulta;
		public Long 		 PlazoResidual;
		public String		 NemoSVS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	
	public static class EntregarPreciosTasas_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<EntregarPreciosTasas_Salida_PreciosTasas> 	P_OUT_PRECIOSTASAS;
		
		
		public EntregarPreciosTasas_Salida() 
		{				
			P_OUT_MENSAJES		= new ArrayList<MensajeSalida>();
			P_OUT_PRECIOSTASAS	= new ArrayList<EntregarPreciosTasas_Salida_PreciosTasas>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntregarPreciosTasas_Salida_PreciosTasas {
		public Long 				IdRegistro;
		public String 				IdClasificacionTipoInstrumento;
		public String 				IdTipoInstrumento;
		public Long 				IdInstrumento;
		public String 				Nemotecnico;
		public String 				IdTipoTasa;
		public BigDecimal 			Tasa;
		public String 				IdTipoPrecio;
		public BigDecimal 			Precio;
		public String 				CodigoFuente;
		public String 				MensajeFuente;
		public java.sql.Timestamp 	FechaFuente;
		public String 				MensajeError;
		public String 				IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	// MSH para Instrumento-Plazo RF/IF
	public static class InstrumentoPlazo {
		public Long Correlativo;
		public String IdTipoInstrumento;
		public String IdTipoReajuste;
		public Long   Plazo;

		public String toString() {
			return Utils.printJson(this, false);
		}

		@Override
		public boolean equals(Object o) {
			InstrumentoPlazo f = (InstrumentoPlazo)o;
			// MSH
			if ( this.IdTipoInstrumento != null && f.IdTipoInstrumento != null &&
				 this.IdTipoReajuste != null && f.IdTipoReajuste != null &&
				 this.Plazo != null && f.Plazo != null ) {
				return this.IdTipoInstrumento.equals(f.IdTipoInstrumento) &&
					   this.IdTipoReajuste.equals(f.IdTipoReajuste) &&
					   this.Plazo.equals(f.Plazo);
			} else {
				return false;
			}
		}
	}
	
	public static class InstrumentoFecha {
		public String Fecha;
		public Long Correlativo;
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			InstrumentoFecha f = (InstrumentoFecha)o;
			return this.Fecha.equals(f.Fecha) && this.IdInstrumento.equals(f.IdInstrumento);
		}
	}
	
	public static class InstrumentoFechaEntidad {
		public String Fecha;
		public String FechaDesde;
		public Long Correlativo;
		public Long IdInstrumento;
		public String Entidad;
		public String TipoRegistro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		@Override
		public boolean equals(Object o) {
			InstrumentoFechaEntidad f = (InstrumentoFechaEntidad)o;
			return this.Fecha.equals(f.Fecha) && this.IdInstrumento.equals(f.IdInstrumento) &&
					this.TipoRegistro.equals(f.TipoRegistro) && this.Entidad.equals(f.Entidad);
		}
	}
	
	public static class Fuente {
		public String Codigo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoTasasPublicadas {
		public Long Correlativo;
		public String IdTipoTasa;
		public String IdTipoPrecio;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Date FechaDesde;
		public Date FechaHasta;
		public String CodigoFuente;
		public Long Plazo;
		public Date FechaActual;
		public Date FechaPlazo;
		public String IdMoneda;
		
		@Override
		public boolean equals(Object o) {
			InstrumentoTasasPublicadas f = (InstrumentoTasasPublicadas)o;
			return this.IdTipoTasa.equals(f.IdTipoTasa) && this.IdTipoPrecio.equals(f.IdTipoPrecio) && 
					this.IdInstrumento.equals(f.IdInstrumento) && this.Nemotecnico.equals(f.Nemotecnico) &&
					this.FechaDesde.compareTo(f.FechaDesde) == 0 && this.FechaHasta.compareTo(f.FechaHasta) == 0 &&
					this.CodigoFuente.equals(f.CodigoFuente) && (this.IdMoneda != null && f.IdMoneda != null && this.IdMoneda.equals(f.IdMoneda));
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public InstrumentoTasasPublicadas () {
			this.Plazo=0L;
			this.IdMoneda = "";
		}
	}
	
	// Precios para Tasas Publicadas
	public static class PreciosTasasPublicadas
	{
		public Long 				IdInstrumento;
		public String 				Nemotecnico; 
		public String				IdTipoInstrumento;
		public String				IdClasificacionTipoInstrumento;
		public String				CodigoFuente;
		public Long 				IdPrecioTasa;		
		public String 				IdTipoTasa;		
		public BigDecimal			Tasa;
		public BigDecimal			TasaEstimada;		
		public String 				IdTipoPrecio; 
		public BigDecimal 			Precio;
		public java.sql.Timestamp	FechaSolicitud;
		public Long 				Plazo;
		public java.sql.Date		FechaDesde;
		public java.sql.Date		FechaHasta;
		public String				IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "TIPOINSTRUMENTO"
	public static class PreciosTasasTipoInstrumento
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public BigDecimal			Tasa;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// MSH "TIPOINSTRUMENTO x PLAZO
	public static class PreciosTasasInstrumentoPlazo
	{
		public String 				IdTipoInstrumento;
		public String 				IdTipoReajuste;
		public Long					Plazo;		
		public BigDecimal 			Precio;
		public BigDecimal			Tasa;
		public java.sql.Timestamp	FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "VPG"
	public static class VectorPreciosGarantias
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public java.sql.Date		Fecha;
		public java.sql.Timestamp   FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioEntidadVector
	{
		public Long 				IdInstrumento;
		public BigDecimal 			Precio;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		public String 				TipoRegistro;
		public String				Entidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOSAGF"
	public static class PreciosMercadoAGF 
	{
		public Long 				IdInstrumento;
		public BigDecimal 			ValorCuota;
		public java.sql.Date		FechaEncontrada;
		public java.sql.Date		Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOSBCS"
	public static class PreciosMercadoBcs 
	{
		public Long 				IdInstrumento;
		public Date					Fecha;
		public Date					FechaEncontrada;
		public BigDecimal 			PrecioCierre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Precios para CodigoFuente = "PRECIOMEDIO"
	public static class PreciosMedioBcs 
	{	
		public Long 				IdInstrumento;
		public BigDecimal 			PrecioMedio;
		public java.sql.Date		Fecha;
		public java.sql.Date		FechaEncontrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Precios para CodigoFuente = "INSTRUMENTO"
	public static class PreciosTasasInstrumento 
	{	
		public Long 				IdInstrumento;		
		public BigDecimal 			Precio;	
		public BigDecimal 			Tasa;	
	}	

	public static class Criterio 
	{	
		public Long 		 IdCriterioSeleccionTasas;		
		public Long 		 Prioridad;
		public String 		 TipoCartera;
		public String 		 IdUso;
		public String 		 IdTipoCriterioTasa;
		public String 		 IdClasificacionTipoInstrumento;
		public String 		 IdTipoInstrumento;
		public Long 		 DiasAntiguedad;		
		public String 		 CodigoFuente;
		public String 		 IdTipoTasa;
		public String 		 IdTipoPrecio;
		public String 		 Interpolar;   // S/N
		public String 		 Nemotecnico;   // S/N
		public Long 		 IdEmpresa;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	public static class Instrumento_Proceso 
	{
		public Long 		 IdRegistro;		
		public String 		 TipoCartera;   // "PROPIA": CARTERA PROPIA  /  "TERCEROS": CUENTA TERCEROS  / "0": TODOS 
		public String 		 IdUso;
		public String 		 IdTipoCriterioTasa;
		public String 		 IdClasificacionTipoInstrumento;
		public String 		 IdTipoInstrumento;
		public Long 		 IdInstrumento;
		public String 		 Nemotecnico;
		public String 		 NemoSVS;
		public String 		 IdMoneda;
		public java.sql.Date FechaConsulta;
		public Long 		 PlazoResidual;
		public Long 		 IdInstrumentoRaiz;
		public List<Criterio> criterios = new ArrayList<>();
		
		//si esta el criterio, significa que lo visitamos. Si el valor esta null significa que no lo encontramos
		public HashMap<Criterio,EntregarPreciosTasas_Salida_PreciosTasas> precioPorCriterio = new HashMap<>();
		
		//Para validaciones
		public long 		 errores 		  = 0;
		public long 		 advertencias 	  = 0;
		public long 		 autorizaciones   = 0;
		public long 		 ok 			  = 0;
		
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	public static class Validacion {
		public String Tipo;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class InfoInstrumento 
	{	
		public Long 				IdInstrumento;
		public Long 				IdInstrumentoRaiz;
		public java.sql.Date 		FechaConsulta;
		public BigDecimal 			TasaIntradayBase360;		
		public BigDecimal 			AperturaBCS;	
		public BigDecimal 			PrecioSemilla;
		public BigDecimal 			PrecioT1;
		public String				IdCondicionLiquidacion;
		public String				Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntregarPreciosTasas_Entrada in = gson.fromJson(jin.toString() ,EntregarPreciosTasas_Entrada.class);
		EntregarPreciosTasas_Salida out = logica(conn,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public void limpiarInstrumentos(List<Instrumento_Proceso> instrumentos, List<Instrumento_Proceso> instrumentosConErrores) {
		for (Instrumento_Proceso ins : instrumentos) {
			if (ins.errores > 0) {
				instrumentosConErrores.add(ins);
			}
		}
		
		for (Instrumento_Proceso ins : instrumentosConErrores) {
			if (instrumentos.contains(ins))
				instrumentos.remove(ins);
		}
	}
	
	public boolean faltanInstrumentos(List<Instrumento_Proceso> instrumentosProceso, List<String> fuentesRequeridas) {
		//revisar si faltan por visitar alguna fuente
		boolean faltanInstrumentos = false;
		List<Instrumento_Proceso> instrumentosNOOK = new ArrayList<>();
		for (Instrumento_Proceso inst : instrumentosProceso) {
			//boolean instrumentoOK = false;
			int visitadosOK = 0;
			//int visitadosNOOK = 0;
			int porVisitar = 0;
			
			for (Criterio criterio : inst.criterios) {
				if (inst.precioPorCriterio.containsKey(criterio) && inst.precioPorCriterio.get(criterio) == null) {
					//visite y no habia precio
					//visitadosNOOK++;
				}
				else if (inst.precioPorCriterio.containsKey(criterio) && inst.precioPorCriterio.get(criterio) != null) {
					//visite y hay precio
					visitadosOK++;
				}
				else if (!inst.precioPorCriterio.containsKey(criterio)) {
					//no lo he visitado
					porVisitar++;
				}
			}
			
			//si no he encontrado precio y me faltan por visitar
			if (porVisitar > 0 && visitadosOK == 0) {
				faltanInstrumentos = true;
				instrumentosNOOK.add(inst);
			}
		}
		
		//actualizamos fuentes que aun son necesarias de visitas
		fuentesRequeridas.clear(); 
		List<String> asdf = calcularFuentesRequeridas(instrumentosNOOK);
		fuentesRequeridas.addAll(asdf);
		
		return faltanInstrumentos;
	}
	
	public List<String> calcularFuentesRequeridas(List<Instrumento_Proceso> instrumentosProceso) {
		List<String> fuentesRequeridas = new ArrayList<>();
		
		for (Instrumento_Proceso inst : instrumentosProceso) {
			for (Criterio criterio : inst.criterios) {
				if (!inst.precioPorCriterio.containsKey(criterio)) {
					if (!fuentesRequeridas.contains(criterio.CodigoFuente)) {
						fuentesRequeridas.add(criterio.CodigoFuente);
					}
				}	
			}
		}
		return fuentesRequeridas;
	}
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	@Trace
	public EntregarPreciosTasas_Salida logica(Connection con, EntregarPreciosTasas_Entrada entrada) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio EntregarPreciosTasas");
		
		EntregarPreciosTasas_Salida salida = new EntregarPreciosTasas_Salida();
		SQL sql = new SQL();
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		
		if (entrada.p_DatosInstrumentos.size() == 0)
		{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"VALORES_ENTRADA_INSTRUMENTOS_SIN_REGISTROS",null,con,entrada.p_IdEmpresa));				
			return salida;
		}
					
		//---------------------------------------------------------------------------------------------
		// OBTIENE DATOS ANEXOS A ENTRADA PARA VALIDACIONES
		HashMap<String,Object> paramsSelDatInsPro = new HashMap<>();
		paramsSelDatInsPro.put("IdEmpresa", entrada.p_IdEmpresa);
		//logger.debug("Inicio SQL_SELECT_DATOS_VALIDACION");
		//startTime = System.nanoTime();
		List<Validacion> _infoValidaciones = sql.query(con, SQL_SELECT_DATOS_VALIDACION, paramsSelDatInsPro, Validacion.class);
		//logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_DATOS_VALIDACION");
		
		List<String> usosReales = new ArrayList<>();
		List<String> tiposCarteraReales = new ArrayList<>();
		List<String> tiposInstrumentoReales = new ArrayList<>();
		List<String> tiposCriterioReales = new ArrayList<>();
		
		for (Validacion v : _infoValidaciones) {
			if (v.Tipo.equalsIgnoreCase("Uso")) {
				usosReales.add(v.Valor);
			}
			else if (v.Tipo.equalsIgnoreCase("TipoCartera")) {
				tiposCarteraReales.add(v.Valor);
			}
			else if (v.Tipo.equalsIgnoreCase("TipoInstrumento")) {
				tiposInstrumentoReales.add(v.Valor);
			}
			else if (v.Tipo.equalsIgnoreCase("TipoCriterio")) {
				tiposCriterioReales.add(v.Valor);
			}
		}
		
		//---------------------------------------------------------------------------------------------
		//VALIDACIONES DE DATOS DE ENTRADA
		List<Instrumento_Proceso> instrumentosProceso = new ArrayList<>();
		List<Instrumento_Proceso> instrumentosProcesoError = new ArrayList<>();
		
		List<String> idUsos = new ArrayList<>();
		List<String> idsClasificacionTipoInstrumento = new ArrayList<>();

		for (EntregarPreciosTasas_Entrada_DatosInstrumentos ins: entrada.p_DatosInstrumentos)	
		{
			cl.bcs.plataforma.tas.EntregarPreciosTasas.Instrumento_Proceso instrumentoProceso = new Instrumento_Proceso();
			//Utils.copyPojo(ins, instrumentoProceso);
			instrumentoProceso.IdRegistro = ins.IdRegistro;		
			instrumentoProceso.TipoCartera = ins.TipoCartera;	
			instrumentoProceso.IdUso = ins.IdUso;	
			instrumentoProceso.IdTipoCriterioTasa = ins.IdTipoCriterioTasa;	
			instrumentoProceso.IdClasificacionTipoInstrumento = ins.IdClasificacionTipoInstrumento;
			instrumentoProceso.IdTipoInstrumento = ins.IdTipoInstrumento;
			instrumentoProceso.IdInstrumento = ins.IdInstrumento;
			instrumentoProceso.Nemotecnico = ins.Nemotecnico;
			instrumentoProceso.NemoSVS = ins.NemoSVS;
			instrumentoProceso.IdMoneda = ins.IdMoneda;
			instrumentoProceso.FechaConsulta = ins.FechaConsulta;
			instrumentoProceso.PlazoResidual = ins.PlazoResidual;
			
			ProcesaRequisito(con, entrada.p_IdEmpresa, !tiposCarteraReales.contains(instrumentoProceso.TipoCartera) && !instrumentoProceso.TipoCartera.equalsIgnoreCase("0"), "NO EXISTE TIPO CARTERA", MensajeSalida.MENSAJE_ERROR, instrumentoProceso, salida.P_OUT_MENSAJES);
			ProcesaRequisito(con, entrada.p_IdEmpresa, !usosReales.contains(instrumentoProceso.IdUso) , "NO EXISTE USO", MensajeSalida.MENSAJE_ERROR, instrumentoProceso, salida.P_OUT_MENSAJES);
			ProcesaRequisito(con, entrada.p_IdEmpresa, !tiposCriterioReales.contains(instrumentoProceso.IdTipoCriterioTasa) && !instrumentoProceso.IdTipoCriterioTasa.equalsIgnoreCase("0"), "NO EXISTE TIPO CRITERIO TASA", MensajeSalida.MENSAJE_ERROR, instrumentoProceso, salida.P_OUT_MENSAJES);
			ProcesaRequisito(con, entrada.p_IdEmpresa, !tiposInstrumentoReales.contains(instrumentoProceso.IdTipoInstrumento) && !instrumentoProceso.IdTipoInstrumento.equalsIgnoreCase("0"), "NO EXISTE TIPO INSTRUMENTO", MensajeSalida.MENSAJE_ERROR, instrumentoProceso, salida.P_OUT_MENSAJES);
			
			if (instrumentoProceso.errores == 0) {
				if (!idUsos.contains(instrumentoProceso.IdUso)) {
					idUsos.add(instrumentoProceso.IdUso);
				}
				if (!idsClasificacionTipoInstrumento.contains(instrumentoProceso.IdClasificacionTipoInstrumento)) {
					idsClasificacionTipoInstrumento.add(instrumentoProceso.IdClasificacionTipoInstrumento);
				}
			}
			
			instrumentosProceso.add(instrumentoProceso);
		}
	
		limpiarInstrumentos(instrumentosProceso, instrumentosProcesoError);
		if (instrumentosProceso.size() == 0) {
			return salida;
		}
			
		//OBTENCION DE CRITERIOS
		HashMap<String,Object> params = new HashMap<>();
		params.put("idUsos", idUsos);
		params.put("idsClasificacionTipoInstrumento", idsClasificacionTipoInstrumento);
		//logger.debug("Inicio SQL_SELECT_CRITERIOS");
		//startTime = System.nanoTime();
		List<Criterio> criterios = sql.query(con, SQL_SELECT_CRITERIOS, params , Criterio.class);
		//logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CRITERIOS");
		
		List<String> fuentesOrdenadas = new ArrayList<>();
		
		//asignar criterios a cada entrada
		criterios.stream().forEach(criterio -> {
			instrumentosProceso.stream()
								.filter(inst -> (inst.TipoCartera.equalsIgnoreCase("0") || criterio.TipoCartera.equalsIgnoreCase("0") || inst.TipoCartera.equalsIgnoreCase(criterio.TipoCartera)) && inst.IdUso.equalsIgnoreCase(criterio.IdUso)	&& (inst.IdTipoCriterioTasa.equalsIgnoreCase("0") || criterio.IdTipoCriterioTasa.equalsIgnoreCase("0") || inst.IdTipoCriterioTasa.equalsIgnoreCase(criterio.IdTipoCriterioTasa)) && inst.IdClasificacionTipoInstrumento.equalsIgnoreCase(criterio.IdClasificacionTipoInstrumento))
								.forEach(inst -> {									
									inst.criterios.add(criterio);								
									if (!fuentesOrdenadas.contains(criterio.CodigoFuente)) {
										fuentesOrdenadas.add(criterio.CodigoFuente);
									}
								});
		});
		
		List<String> fuentesTasasPublicadas = new ArrayList<>();
		List<String> fuentesEntidadVectorPrecio = new ArrayList<>();
		
		List<String> tasasDisponibles = Arrays.asList("PRECIOSBCS","PRECIOMEDIO", "PRECIOSAGF","VPG","VPG100","VPG98","VPG95","TIPOINSTRUMENTO","INSTRUMENTO");
		List<String> fuentesDisponibles = Arrays.asList("VECTORPRECIOSCORREDORA", "VECTORPRECIOSREPORTEBVL", "VECTORPRECIOSCIERREBVL", "VECTORPRECIOSAPERTURABVL", "VECTORPRECIOSPROMEDIOBVL", "VECTORPRECIOSMERCADORIESGO", "BLOOMBERG","NAV", "RISK AMERICA", "REUTERS");

		for (String fuente : fuentesOrdenadas) {
			if (tasasDisponibles.contains(fuente)){
				
			}else if (fuentesDisponibles.contains(fuente)) {
				fuentesEntidadVectorPrecio.add(fuente);
			}else {
				fuentesTasasPublicadas.add(fuente);
			}
		}
		
		//instrumentos sin criterios
		String codigo = "NO_EXISTEN_CRITERIOS_SELECCION_PARA_VALORES_ENTRADA";
		String descripcion = null;
		for (Instrumento_Proceso inst : instrumentosProceso) {
			if (inst.criterios.size() == 0) {
				inst.errores++;
				EntregarPreciosTasas_Salida_PreciosTasas resultado = crearPojoSalidaEntregarPrecioTasa(inst);
				//Utils.copyPojo(inst, resultado);
				resultado = new EntregarPreciosTasas_Salida_PreciosTasas();
				resultado.IdRegistro = inst.IdRegistro;
				resultado.IdTipoTasa = null;
				resultado.Tasa = null;				
				resultado.IdTipoPrecio = null;
				resultado.Precio = null;				
				resultado.CodigoFuente = "CRITERIOS NO CONFIGURADOS";
				resultado.MensajeFuente = null;
				resultado.MensajeError = "PRECIO/TASA NO ENCONTRADO";
				resultado.FechaFuente =  null;
				salida.P_OUT_PRECIOSTASAS.add(resultado);
				if (descripcion == null) {
					descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).msg();
				}
				salida.P_OUT_MENSAJES.add(new MensajeSalida(inst.IdRegistro,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			}
		}
			
		limpiarInstrumentos(instrumentosProceso, instrumentosProcesoError);
		if (instrumentosProceso.size() == 0) {
			return salida;
		}
		
		//visitas criterios en orden y actualizar entradas que correspondan por cada entrada y criterio ver si encontro precio
		boolean faltanInstrumentos = true;
		List<String> fuentesRequeridas = new ArrayList<>(fuentesOrdenadas); //para detenerse si encontramos precios para todas
		List<String> fuentesPorVisitar = new ArrayList<>(fuentesOrdenadas); //para controlar tasas publicadas y precios genericos
		Long Correlativo = 0L;
		for (String fuente: fuentesOrdenadas) {
			if (!faltanInstrumentos) {
				break;
			}
			
			//eliminar fuentes que ya no sean necesarias
			if (!fuentesRequeridas.contains(fuente)) {
				continue;
			}
			
			//eliminar fuentes que ya no sean necesarias
			if (!fuentesPorVisitar.contains(fuente)) {
				continue;
			}
			
			logger.debug("VISITANDO FUENTE: " + fuente);
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIOS DE LA BCS
			if (fuente.equalsIgnoreCase("PRECIOSBCS"))
			{
				List<InstrumentoFecha> instrumentosFecha = new ArrayList<>();
				final Long correlativo = ++Correlativo;
				HashMap<String,Object> contains = new HashMap<String, Object>();
				instrumentosProceso.stream().forEach(inst -> inst.criterios.stream().filter(criterio -> criterio.CodigoFuente.equalsIgnoreCase(fuente)).forEach(criterio -> {
					InstrumentoFecha arg0 = new InstrumentoFecha();
					arg0.Fecha = Fecha.toStringDate(inst.FechaConsulta);
					arg0.IdInstrumento = inst.IdInstrumento;
					arg0.Correlativo = correlativo;
					
					if (!contains.containsKey(arg0.Fecha + arg0.IdInstrumento)) {
						instrumentosFecha.add(arg0);
						contains.put(arg0.Fecha + arg0.IdInstrumento, true);
					}
				}));
				
				sql.batch(con, SQL_INSERT_INSTRUMENTO_FECHA, instrumentosFecha, InstrumentoFecha.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				HashMap<String,PreciosMercadoBcs> hashPrecios = new HashMap<>();
				List<PreciosMercadoBcs> preciosBCS = sql.query(con, SQL_SELECT_PRECIOS_MERCADO_BCS, params, PreciosMercadoBcs.class);
				for (int i = 0; i<preciosBCS.size();i++) {
					hashPrecios.put("" + preciosBCS.get(i).Fecha + preciosBCS.get(i).IdInstrumento,preciosBCS.get(i));
				}
				instrumentosProceso.stream()
									.filter(inst -> hashPrecios.containsKey("" + inst.FechaConsulta + inst.IdInstrumento))
									.forEach(inst -> {
										PreciosMercadoBcs precio = hashPrecios.get("" + inst.FechaConsulta + inst.IdInstrumento);
										EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
										salida_PreciosTasas.IdTipoTasa = "";
										salida_PreciosTasas.Tasa = BigDecimal.ZERO;
										salida_PreciosTasas.IdTipoPrecio = "";
										salida_PreciosTasas.CodigoFuente = fuente;
										salida_PreciosTasas.MensajeFuente = "Fuente: PreciosMercadoBCS";
										salida_PreciosTasas.MensajeError = null;
										salida_PreciosTasas.FechaFuente = Fecha.toTimeStamp(precio.FechaEncontrada + " 00:00:00");
										salida_PreciosTasas.Precio = precio.PrecioCierre;
					
										guardarResultado(inst,fuente,salida_PreciosTasas);
									});
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIOS POR ENTIDAD
			else if (fuentesEntidadVectorPrecio.contains(fuente)) {
				List<String> fuentesEntidadVectorPrecioVisitar = new ArrayList<>();
				for (String fuentePorVisitar : fuentesOrdenadas) {
					if (fuentesEntidadVectorPrecio.contains(fuentePorVisitar) && !fuentesEntidadVectorPrecioVisitar.contains(fuentePorVisitar)) {
						fuentesEntidadVectorPrecioVisitar.add(fuentePorVisitar);
					}
				}
				
				logger.debug("VISITANDO FUENTES ADICIONALES: " + fuentesEntidadVectorPrecioVisitar.toString());		
				List<InstrumentoFechaEntidad> instrumentosFechaEntidad = new ArrayList<>();
				Correlativo++;
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (fuentesEntidadVectorPrecioVisitar.contains(criterio.CodigoFuente)) {
							InstrumentoFechaEntidad arg0 = new InstrumentoFechaEntidad();
							
							if (criterio.DiasAntiguedad.equals(0L)) {
								arg0.FechaDesde = Fecha.toStringDate(inst.FechaConsulta);
							}
							else {
								arg0.FechaDesde = Fecha.toStringDate(Fecha.substractDays(inst.FechaConsulta,criterio.DiasAntiguedad.intValue()));
							}
							
							arg0.Fecha = Fecha.toStringDate(inst.FechaConsulta);
							arg0.IdInstrumento = inst.IdInstrumento;
							arg0.Correlativo = Correlativo;
							
							if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSMERCADORIESGO")) {
								arg0.TipoRegistro = "PRECIOMERCADO";
								arg0.Entidad = "TASARIESGO";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSPROMEDIOBVL")) {
								arg0.TipoRegistro = "PRECIOPROMEDIO";
								arg0.Entidad = "BVL";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSAPERTURABVL")) {
								arg0.TipoRegistro = "PRECIOAPERTURA";
								arg0.Entidad = "BVL";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSCIERREBVL")) {
								arg0.TipoRegistro = "PRECIOCIERRE";
								arg0.Entidad = "BVL";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSREPORTEBVL")) {
								arg0.TipoRegistro = "PRECIOREPORTE";
								arg0.Entidad = "BVL";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSCORREDORA")) {
								arg0.TipoRegistro = "TASAANUAL";
								arg0.Entidad = "CORREDORA";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("BLOOMBERG")) {
								arg0.TipoRegistro = "BLOOMBERG PRECIO";
								arg0.Entidad = "BLOOMBERG";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("NAV")) {
								arg0.TipoRegistro = "NAV PRECIO";
								arg0.Entidad = "NAV";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("RISK AMERICA")) {
								arg0.TipoRegistro = "RISK AMERICA PRECIO";
								arg0.Entidad = "RISK AMERICA";
							}
							else if (criterio.CodigoFuente.equalsIgnoreCase("REUTERS")) {
								arg0.TipoRegistro = "REUTERS PRECIO";
								arg0.Entidad = "REUTERS";
							}
							else {
								arg0.TipoRegistro = "";
								arg0.Entidad = "";
							}
							if (!instrumentosFechaEntidad.contains(arg0)) {
								instrumentosFechaEntidad.add(arg0 );	
							}
						}
					}
				}
				
				sql.batch(con, SQL_INSERT_INSTRUMENTO_FECHA_ENTIDAD, instrumentosFechaEntidad, InstrumentoFechaEntidad.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				List<PrecioEntidadVector> preciosEntidadVector = sql.query(con, SQL_SELECT_ENTIDAD_VECTOR_PRECIOS, params, PrecioEntidadVector.class);
				
				HashMap<String,PrecioEntidadVector> hashPrecios = new HashMap<>();
				for (PrecioEntidadVector obj: preciosEntidadVector) {
					//este hash considera que los mas antiguos van primero,y los mas recientes al final, se queda con los precios mas recientes
					hashPrecios.put(obj.Fecha + "-" + obj.IdInstrumento + "-" + obj.TipoRegistro + "-" + obj.Entidad, obj);
				}
				
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (!fuentesEntidadVectorPrecioVisitar.contains(criterio.CodigoFuente)) {
							continue;
						}
						String TipoRegistro = "";
						String Entidad = "";
						if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSMERCADORIESGO")) {
							TipoRegistro = "PRECIOMERCADO";
							Entidad = "TASARIESGO";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSPROMEDIOBVL")) {
							TipoRegistro = "PRECIOPROMEDIO";
							Entidad = "BVL";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSAPERTURABVL")) {
							TipoRegistro = "PRECIOAPERTURA";
							Entidad = "BVL";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSCIERREBVL")) {
							TipoRegistro = "PRECIOCIERRE";
							Entidad = "BVL";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSREPORTEBVL")) {
							TipoRegistro = "PRECIOREPORTE";
							Entidad = "BVL";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("VECTORPRECIOSCORREDORA")) {
							TipoRegistro = "TASAANUAL";
							Entidad = "CORREDORA";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("BLOOMBERG")) {
							TipoRegistro = "BLOOMBERG PRECIO";
							Entidad = "BLOOMBERG";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("NAV")) {
							TipoRegistro = "NAV PRECIO";
							Entidad = "NAV";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("RISK AMERICA")) {
							TipoRegistro = "RISK AMERICA PRECIO";
							Entidad = "RISK AMERICA";
						}
						else if (criterio.CodigoFuente.equalsIgnoreCase("REUTERS")) {
							TipoRegistro = "REUTERS PRECIO";
							Entidad = "REUTERS";
						}
						
						
						//for (PrecioEntidadVector precio : preciosEntidadVector) {
						//	if (inst.FechaConsulta.equals(precio.Fecha) && inst.IdInstrumento.equals(precio.IdInstrumento)
						//			&& TipoRegistro.equals(precio.TipoRegistro) && Entidad.equals(precio.Entidad)) {
						
						PrecioEntidadVector precio = hashPrecios.get(inst.FechaConsulta + "-" + inst.IdInstrumento + "-" + TipoRegistro + "-" + Entidad);
						if (precio != null) {		
								EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
								//Utils.copyPojo(inst, salida_PreciosTasas);
								salida_PreciosTasas.IdTipoTasa = "";
								salida_PreciosTasas.Tasa = BigDecimal.ZERO;
								salida_PreciosTasas.IdTipoPrecio = "";
								salida_PreciosTasas.CodigoFuente = criterio.CodigoFuente;
								salida_PreciosTasas.MensajeFuente = "Fuente: " + criterio.CodigoFuente;
								salida_PreciosTasas.MensajeError = null;
								salida_PreciosTasas.FechaFuente =  Fecha.toTimeStamp(Fecha.toStringDate(precio.FechaEncontrada) + " 00:00:00");
								salida_PreciosTasas.Precio = precio.Precio;
								
								guardarResultado(inst,criterio.CodigoFuente,salida_PreciosTasas);
								//break;
						}
					
						//} // for precios
					}//for criterios
				}//for instrumentos
				
				for (String f : fuentesEntidadVectorPrecioVisitar) {
					fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(f));	
				}
				
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}//end if
			
			//-----------------------------------------------------------------------------
			//FUENTE: TASAS PUBLICADAS
			else if (fuentesTasasPublicadas.contains(fuente))
			{
				List<String> fuentesTasasPublicadasVisitar = new ArrayList<>();
				for (String fuentePorVisitar : fuentesOrdenadas) {
					if (fuentesTasasPublicadas.contains(fuentePorVisitar) && !fuentesTasasPublicadasVisitar.contains(fuentePorVisitar)) {
						fuentesTasasPublicadasVisitar.add(fuentePorVisitar);
					}
				}				
								
				//logger.debug("VISITANDO FUENTES ADICIONALES: " + fuentesTasasPublicadasVisitar.toString());
				
				//CALCULO DE DIAS DE ANTIGUEDAD PARA CRITERIOS
				InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (fuentesTasasPublicadasVisitar.contains(criterio.CodigoFuente)) {
							if (!criterio.DiasAntiguedad.equals(0L)) {
								FechasIn f1 = new FechasIn();			
								f1.fechaInicial = Fecha.toStringDate(inst.FechaConsulta);
								f1.numeroDias = criterio.DiasAntiguedad;
								inObtenerDiaAnterior.fechas.add(f1);
							}
						}
					}
				}
				
				OutObtenerDiaAnterior outObtenerDiaAnterior = new OutObtenerDiaAnterior();
				if (inObtenerDiaAnterior.fechas.size() > 0) {
					inObtenerDiaAnterior.idPais = entrada.p_IdPais;
					inObtenerDiaAnterior.diaHabil = "S";
					ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
					outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
				}
				
				List<InstrumentoTasasPublicadas> instrumentosTasasPublicadas = new ArrayList<>();
				Correlativo++;
				HashMap<String,Date> hashFechaDesde = new HashMap<>();
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (fuentesTasasPublicadasVisitar.contains(criterio.CodigoFuente)) {
							InstrumentoTasasPublicadas arg0 = new InstrumentoTasasPublicadas();
							if (criterio.DiasAntiguedad.equals(0L)) {
								arg0.FechaDesde = inst.FechaConsulta;
							}
							else {
								for (FechasOut fecha : outObtenerDiaAnterior.fechas) {
									if (Fecha.toDate(fecha.fechaInicial).compareTo(inst.FechaConsulta) == 0 && fecha.numeroDiasInicial.equals(criterio.DiasAntiguedad)) {
										//arg0.FechaDesde = Fecha.toDate(fecha.fechaCalculada);
										arg0.FechaDesde = Fecha.toDate(fecha.fechaCalculada);
										break;
									}
								}
								if (arg0.FechaDesde == null) {
									arg0.FechaDesde = inst.FechaConsulta;
								}
							}
														
							hashFechaDesde.put(inst.hashCode()+criterio.CodigoFuente, arg0.FechaDesde);
							arg0.IdInstrumento = inst.IdInstrumento;
							arg0.IdTipoPrecio = criterio.IdTipoPrecio;
							arg0.IdTipoTasa = criterio.IdTipoTasa;
							arg0.Nemotecnico = inst.Nemotecnico;
							arg0.Correlativo = Correlativo;
							arg0.FechaHasta = inst.FechaConsulta;
							arg0.CodigoFuente = criterio.CodigoFuente;
							arg0.Plazo = criterio.DiasAntiguedad;
							arg0.FechaActual = Fecha.toDate(FechaSistema);
							arg0.FechaPlazo = !arg0.FechaHasta.equals(Fecha.toDate("0001-01-01")) ? Fecha.substractDays(arg0.FechaHasta, arg0.Plazo.intValue()) : arg0.FechaHasta;
							arg0.FechaDesde = !arg0.FechaHasta.equals(Fecha.toDate("0001-01-01")) ? arg0.FechaDesde : arg0.FechaHasta;
							arg0.IdMoneda = inst.IdMoneda;

							if ( inst.IdClasificacionTipoInstrumento.equals("IF") ) {
								// TASAS PUBLICADAS PREGUNTAN POR NEMOSVS
								// Instrumento_Salida Nemo = (Instrumento_Salida) NemosSVS.get(inst.IdRegistro);
								arg0.Nemotecnico = inst.NemoSVS == null ? arg0.Nemotecnico : inst.NemoSVS;
							}
							
							if (!instrumentosTasasPublicadas.contains(arg0)) {
								instrumentosTasasPublicadas.add(arg0 );	
							}
						}
					}
				}
				
				//Busca tasas a la fecha actual
				sql.batch(con, SQL_INSERT_TASAS_PUBLICADAS, instrumentosTasasPublicadas, InstrumentoTasasPublicadas.class);
				
				params.clear();
				params.put("Correlativo",Correlativo);
				params.put("IdEmpresa",entrada.p_IdEmpresa);
				logger.debug("Inicio SQL_SELECT_PRECIOS_TASAS_PUBLICADAS");
				startTime = System.nanoTime();
				List<PreciosTasasPublicadas> preciosTasasPublicadas = sql.query(con, SQL_SELECT_PRECIOS_TASAS_PUBLICADAS, params, PreciosTasasPublicadas.class);
				logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PRECIOS_TASAS_PUBLICADAS");
				
				//FIX para que encuentre tasas en el plazo asignado en el criterio
				List<InstrumentoTasasPublicadas> ListaInsPublicados = new ArrayList<InstrumentoTasasPublicadas>();
				
				for(InstrumentoTasasPublicadas ins : instrumentosTasasPublicadas) {
					boolean insBuscado = false;
					
					for(PreciosTasasPublicadas pre : preciosTasasPublicadas) {
						if(ins.IdInstrumento.equals(pre.IdInstrumento)) {
							insBuscado=true;
							break;							
						}					
					}
					
					//if(insBuscado ||preciosTasasPublicadas.size()==0 ) {
					if(!insBuscado || preciosTasasPublicadas.size()==0 ) {	
						ListaInsPublicados.add(ins);
					}
				}				
	
				//delete  e insert tabla Fecha Desde hasta  
				SQL.exec(con, SQL_DELETE_TASAS_PUBLICADAS);
				
				//Insert Lista de tasas no encontrada en la fecha del dia
				sql.batch(con, SQL_INSERT_TASAS_PUBLICADAS, ListaInsPublicados, InstrumentoTasasPublicadas.class);
				
				//Lista de tasas y precios encontrados en el plazo 
				List<PreciosTasasPublicadas> ListaTasaPrecioEncontradoPlazo = sql.query(con, SQL_SELECT_PRECIOS_TASAS_PUBLICADAS_PLAZO, params, PreciosTasasPublicadas.class);
				
				//se agregan a las tasas encontradas en el dia consultado
				preciosTasasPublicadas.addAll(ListaTasaPrecioEncontradoPlazo);
				
				//Se obtienen los precio/tasa publicados mas recientes (maximo FechaSolicitud) por Nemotecnico y moneda
				List<PreciosTasasPublicadas> preciosTasasPublicadasRecientes = new ArrayList<PreciosTasasPublicadas>();
				List<PreciosTasasPublicadas> preciosTasasPublicadasRecientesNew = new ArrayList<PreciosTasasPublicadas>();
				
				Map<String, List<PreciosTasasPublicadas>> preciosTasasPublicadasRecientesMap = preciosTasasPublicadas.stream()
						.sorted((p1, p2) -> p2.FechaSolicitud.compareTo(p1.FechaSolicitud))
						.collect(Collectors.groupingBy(p -> p.Nemotecnico+"_"+p.IdMoneda+"_"+p.FechaSolicitud));
				for (Map.Entry<String, List<PreciosTasasPublicadas>> ptpMap : preciosTasasPublicadasRecientesMap.entrySet()) {
					java.sql.Timestamp fechaSolicitudMaxima = ((PreciosTasasPublicadas) ptpMap.getValue().get(0)).FechaSolicitud;
					for (PreciosTasasPublicadas ptp : ptpMap.getValue()) {
						if (ptp.FechaSolicitud.compareTo(fechaSolicitudMaxima) == 0) {
								preciosTasasPublicadasRecientes.add(ptp);	
						}
					}
				}
				
				//Busqueda precio/tasa
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios ) {
						Date fechaDesde = hashFechaDesde.get(inst.hashCode()+criterio.CodigoFuente);
						if (!fuentesTasasPublicadasVisitar.contains(criterio.CodigoFuente) || fechaDesde == null) {
							continue;
						}
						
						PreciosTasasPublicadas precioTasaPublicada = new PreciosTasasPublicadas();
						if ("N".equalsIgnoreCase(criterio.Interpolar)) {
							preciosTasasPublicadasRecientesNew.clear();
							
							preciosTasasPublicadasRecientesNew = preciosTasasPublicadasRecientes.stream()
									.filter(p ->
										p.CodigoFuente.equals(criterio.CodigoFuente) &&
										p.FechaDesde.compareTo(fechaDesde) == 0 &&										
										p.FechaHasta.compareTo(inst.FechaConsulta) == 0 && 
										//p.IdInstrumento.compareTo(inst.IdInstrumento) == 0 &&
										(p.IdTipoPrecio == null || p.IdTipoPrecio.equals(criterio.IdTipoPrecio)) &&
										(p.IdTipoTasa == null || p.IdTipoTasa.equals(criterio.IdTipoTasa)) &&
										(p.Nemotecnico.equals(inst.Nemotecnico) || (inst.NemoSVS != null && p.Nemotecnico.equals(inst.NemoSVS))) &&
										(p.IdMoneda == null || p.IdMoneda.equals(inst.IdMoneda))
									)
									.sorted((p2,p1) -> p1.FechaSolicitud.compareTo(p2.FechaSolicitud))
									.collect(Collectors.toList());
							
							for (PreciosTasasPublicadas ptpr : preciosTasasPublicadasRecientesNew) {
								//Para mercado RF o IF se obtiene precio/tasa sin considerar el Plazo
								if ("RF".equals(ptpr.IdClasificacionTipoInstrumento) || "IF".equals(ptpr.IdClasificacionTipoInstrumento)) {
									String fechaSolici, fechaCons;								
									DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
									fechaSolici = dateFormat.format(ptpr.FechaSolicitud);
									fechaCons = dateFormat.format(inst.FechaConsulta);
									
									precioTasaPublicada = ptpr;
									break;

								}
								//Se obtiene precio/tasa donde PlazoResidual sea menor al menor Plazo de los precios/tasas publicados
								else if (inst.PlazoResidual.compareTo(ptpr.Plazo) <= 0) {
									precioTasaPublicada = ptpr;
									break;
								}
							}
						}
						else if ("S".equalsIgnoreCase(criterio.Interpolar)) {
							//Se obtiene el maximo Plazo en donde PlazoResidual >= Plazo
							PreciosTasasPublicadas maxMenores = preciosTasasPublicadasRecientes.stream()
									.filter(p ->
										p.CodigoFuente.equals(criterio.CodigoFuente) &&
										p.FechaDesde.compareTo(fechaDesde) == 0 &&
										p.FechaHasta.compareTo(inst.FechaConsulta) == 0 &&
										(p.IdTipoPrecio == null || p.IdTipoPrecio.equals(criterio.IdTipoPrecio)) &&
										(p.IdTipoTasa == null || p.IdTipoTasa.equals(criterio.IdTipoTasa)) &&
										(p.Nemotecnico.equals(inst.Nemotecnico) || (inst.NemoSVS != null && p.Nemotecnico.equals(inst.NemoSVS))) &&
										(p.IdMoneda == null || p.IdMoneda.equals(inst.IdMoneda))
									)
									.sorted((p1, p2) -> {
										int fechaSolicitudComparison = p2.FechaSolicitud.toLocalDateTime().toLocalDate().compareTo(p1.FechaSolicitud.toLocalDateTime().toLocalDate());
								        if (fechaSolicitudComparison != 0) {
								            return fechaSolicitudComparison;
								        }
								        return p2.Plazo.compareTo(p1.Plazo);
									})
									.filter(p -> inst.PlazoResidual.compareTo(p.Plazo) >= 0)
									.findFirst().orElse(null);
							
							//Se obtiene el minimo Plazo en donde PlazoResidual <= Plazo
							PreciosTasasPublicadas minMayores = preciosTasasPublicadasRecientes.stream()
									.filter(p ->
										p.CodigoFuente.equals(criterio.CodigoFuente) &&
										p.FechaDesde.compareTo(fechaDesde) == 0 &&
										p.FechaHasta.compareTo(inst.FechaConsulta) == 0 &&
										(p.IdTipoPrecio == null || p.IdTipoPrecio.equals(criterio.IdTipoPrecio)) &&
										(p.IdTipoTasa == null || p.IdTipoTasa.equals(criterio.IdTipoTasa)) &&
										(p.Nemotecnico.equals(inst.Nemotecnico) || (inst.NemoSVS != null && p.Nemotecnico.equals(inst.NemoSVS))) &&
										(p.IdMoneda == null || p.IdMoneda.equals(inst.IdMoneda))
									)
									.sorted((p1, p2) -> {
										int fechaSolicitudComparison = p2.FechaSolicitud.toLocalDateTime().toLocalDate().compareTo(p1.FechaSolicitud.toLocalDateTime().toLocalDate());
								        if (fechaSolicitudComparison != 0) {
								            return fechaSolicitudComparison;
								        }
								        return p1.Plazo.compareTo(p2.Plazo);
									})
									.filter(p -> inst.PlazoResidual.compareTo(p.Plazo) <= 0)
									.findFirst().orElse(null);
							
							//Calculo Interpolacion
							if (maxMenores != null && minMayores != null && maxMenores.Plazo != null && minMayores.Plazo != null && maxMenores.Tasa != null && minMayores.Tasa != null) {
								precioTasaPublicada.IdTipoTasa = minMayores.IdTipoTasa;
								precioTasaPublicada.IdTipoPrecio = minMayores.IdTipoPrecio;
								precioTasaPublicada.Precio = minMayores.Precio;
								precioTasaPublicada.FechaSolicitud =  minMayores.FechaSolicitud;
								
							    //logger.debug("maxMenores :: IdRegistro = " + inst.IdRegistro + " , Moneda = " + maxMenores.IdMoneda + " , Tasa = " + maxMenores.Tasa + " , PlazoResidual = " + inst.PlazoResidual + " , Plazo = " + maxMenores.Plazo);
								//logger.debug("minMayores :: IdRegistro = " + inst.IdRegistro + " , Moneda = " + minMayores.IdMoneda + " , Tasa = " + minMayores.Tasa + " , PlazoResidual = " + inst.PlazoResidual + " , Plazo = " + minMayores.Plazo);
								//logger.debug("operacion = (((" + minMayores.Plazo.toString() + " - " + inst.PlazoResidual.toString() + ") * (" + minMayores.Tasa.toString() + " - " + maxMenores.Tasa.toString() + ") / (" + minMayores.Plazo.toString() + " - " + maxMenores.Plazo.toString() + ")) - " + minMayores.Tasa.toString() + ") * -1");
								
								if (minMayores.Plazo.longValue() - maxMenores.Plazo.longValue() != 0) {
									BigDecimal division = (minMayores.Tasa.subtract(maxMenores.Tasa)).divide(BigDecimal.valueOf(minMayores.Plazo.longValue() - maxMenores.Plazo.longValue()),16,RoundingMode.HALF_UP);
									precioTasaPublicada.Tasa = ((BigDecimal.valueOf(minMayores.Plazo.longValue() - inst.PlazoResidual.longValue())).multiply(division).subtract(minMayores.Tasa)).multiply(BigDecimal.valueOf(-1)).setScale(8, RoundingMode.DOWN);
								}
								else {
									precioTasaPublicada.Tasa = maxMenores.Tasa != null ? maxMenores.Tasa : minMayores.Tasa != null ? minMayores.Tasa : BigDecimal.ZERO;
								}
							}
						}
						
						if (precioTasaPublicada != null && ((precioTasaPublicada.IdTipoTasa != null && precioTasaPublicada.Tasa != null) || (precioTasaPublicada.IdTipoPrecio != null && precioTasaPublicada.Precio != null))) {
							EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
							//Utils.copyPojo(inst, salida_PreciosTasas);
							salida_PreciosTasas.IdTipoTasa = precioTasaPublicada.IdTipoTasa;
							salida_PreciosTasas.Tasa = precioTasaPublicada.Tasa;
							salida_PreciosTasas.IdTipoPrecio = precioTasaPublicada.IdTipoPrecio;
							salida_PreciosTasas.CodigoFuente = criterio.CodigoFuente;
							salida_PreciosTasas.MensajeFuente = "Fuente: "+ criterio.CodigoFuente;
							salida_PreciosTasas.MensajeError = null;
							salida_PreciosTasas.FechaFuente =  precioTasaPublicada.FechaSolicitud;
							salida_PreciosTasas.Precio = precioTasaPublicada.Precio;
							 
							guardarResultado(inst,criterio.CodigoFuente,salida_PreciosTasas);
						}
					}
				}
				
				for (String f : fuentesTasasPublicadasVisitar) {
					fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(f));	
				}
				
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIOS AGF
			else if (fuente.equalsIgnoreCase("PRECIOSAGF"))
			{
				List<InstrumentoFecha> instrumentosFecha = new ArrayList<>();
				Correlativo++;
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (criterio.CodigoFuente.equalsIgnoreCase(fuente)) {
							InstrumentoFecha arg0 = new InstrumentoFecha();
							arg0.Fecha = Fecha.toStringDate(inst.FechaConsulta);
							arg0.IdInstrumento = inst.IdInstrumento;
							arg0.Correlativo = Correlativo;
							if (!instrumentosFecha.contains(arg0)) {
								instrumentosFecha.add(arg0 );	
							}
						}
					}
				}
				
				sql.batch(con, SQL_INSERT_INSTRUMENTO_FECHA, instrumentosFecha, InstrumentoFecha.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				List<PreciosMercadoAGF> preciosAGF = sql.query(con, SQL_SELECT_PRECIOS_MERCADO_AGF, params, PreciosMercadoAGF.class);
				
				for (PreciosMercadoAGF precio : preciosAGF) {
					for (Instrumento_Proceso inst : instrumentosProceso) {
						if (inst.FechaConsulta.equals(precio.Fecha) && inst.IdInstrumento.equals(precio.IdInstrumento)) {
							EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
							//Utils.copyPojo(inst, salida_PreciosTasas);
							salida_PreciosTasas.IdTipoTasa = "";
							salida_PreciosTasas.Tasa = BigDecimal.ZERO;
							salida_PreciosTasas.IdTipoPrecio = "";
							salida_PreciosTasas.CodigoFuente = fuente;
							salida_PreciosTasas.MensajeFuente = "Fuente: PreciosMercadoAGF";
							salida_PreciosTasas.MensajeError = null;
							salida_PreciosTasas.FechaFuente =  Fecha.toTimeStamp(Fecha.toStringDate(precio.FechaEncontrada) + " 00:00:00");
							salida_PreciosTasas.Precio = precio.ValorCuota;
							
							guardarResultado(inst,fuente,salida_PreciosTasas);
						}
					}
				}
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: VECTOR DE PRECIO GARANTIAS BCS
			else if (Arrays.asList("VPG","VPG100","VPG98","VPG95").contains(fuente.toUpperCase()))
			{
				List<InstrumentoFecha> instrumentosFecha = new ArrayList<>();
				final Long correlativo = ++Correlativo;
				instrumentosProceso.stream()
								    .flatMap(inst -> inst.criterios.stream()
								        .filter(criterio -> criterio.CodigoFuente.equalsIgnoreCase(fuente))
								        .map(criterio -> {
								            InstrumentoFecha arg0 = new InstrumentoFecha();
								            arg0.Fecha = Fecha.toStringDate(inst.FechaConsulta);
								            arg0.IdInstrumento = inst.IdInstrumento;
								            arg0.Correlativo = correlativo;
								            return arg0;
								        }))
								    .distinct()
								    .forEach(instrumentosFecha::add);				
				
				sql.batch(con, SQL_INSERT_INSTRUMENTO_FECHA, instrumentosFecha, InstrumentoFecha.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				params.put("Fuente",fuente);
				List<VectorPreciosGarantias> preciosGarantias = sql.query(con, SQL_SELECT_VECTOR_PRECIOS_GARANTIAS, params, VectorPreciosGarantias.class);
				
				preciosGarantias.stream()
								.forEach(precio -> instrumentosProceso.stream()
																		.filter(inst -> inst.FechaConsulta.equals(precio.Fecha) && inst.IdInstrumento.equals(precio.IdInstrumento))
																		.forEach(inst -> {
																			EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
																			
																			salida_PreciosTasas.IdTipoTasa = "";
																			salida_PreciosTasas.Tasa = BigDecimal.ZERO;
																			salida_PreciosTasas.IdTipoPrecio = "";
																			salida_PreciosTasas.CodigoFuente = fuente;
																			salida_PreciosTasas.MensajeFuente = "Fuente: VectorPreciosGarantiasBcs";
																			salida_PreciosTasas.MensajeError = null;
																			salida_PreciosTasas.FechaFuente =  precio.FechaEncontrada;
																			salida_PreciosTasas.Precio = precio.Precio;
																			
																			guardarResultado(inst,fuente,salida_PreciosTasas);
																		})
										);
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIO/TASA GENERICA POR TIPO DE INSTRUMENTO
			else if (fuente.equalsIgnoreCase("TIPOINSTRUMENTO"))
			{
				// MSH cambio de rutina Fecha(s) > Plazo
				List<InstrumentoPlazo> instrumentosPlazo = new ArrayList<>();
				Correlativo++;
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (criterio.CodigoFuente.equalsIgnoreCase(fuente)) {
							InstrumentoPlazo itm0 = new InstrumentoPlazo();
							itm0.IdTipoInstrumento = inst.IdTipoInstrumento;
							itm0.IdTipoReajuste = inst.IdMoneda;
							itm0.Plazo = inst.PlazoResidual;
							itm0.Correlativo = Correlativo;
							if (!instrumentosPlazo.contains(itm0)) {
								instrumentosPlazo.add(itm0);	
							}
						}
					}
				}

				sql.batch(con, SQL_DELETE_INSTRUMENTO_PLAZO, new ArrayList<>(), Object.class);
				sql.batch(con, SQL_INSERT_INSTRUMENTO_PLAZO, instrumentosPlazo, InstrumentoPlazo.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				List<PreciosTasasInstrumentoPlazo> preciosTasasTipoInstrumentoPlazo = sql.query(con, SQL_SELECT_PRECIOS_TASAS_INSTRUMENTO_PLAZO,params, PreciosTasasInstrumentoPlazo.class);

				for (PreciosTasasInstrumentoPlazo precio : preciosTasasTipoInstrumentoPlazo) {
					for (Instrumento_Proceso inst : instrumentosProceso) {
						if (inst.IdTipoInstrumento.equals(precio.IdTipoInstrumento) 
							&& inst.IdMoneda.equals(precio.IdTipoReajuste) && inst.PlazoResidual.equals(precio.Plazo)) {
							EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
							//Utils.copyPojo(inst, salida_PreciosTasas);
							salida_PreciosTasas.IdTipoTasa = "";
							salida_PreciosTasas.Tasa = precio.Tasa;
							salida_PreciosTasas.IdTipoPrecio = "";
							salida_PreciosTasas.CodigoFuente = fuente;
							salida_PreciosTasas.MensajeFuente = "Fuente: TipoInstrumento";
							salida_PreciosTasas.MensajeError = null;
							salida_PreciosTasas.FechaFuente =  Fecha.toTimeStamp(Fecha.toStringDate(precio.FechaEncontrada) + " 00:00:00");
							salida_PreciosTasas.Precio = precio.Precio;
							
							guardarResultado(inst,fuente,salida_PreciosTasas);
						}
					}
				}
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIO MEDIO BCS
			else if (fuente.equalsIgnoreCase("PRECIOMEDIO"))
			{
				List<InstrumentoFecha> instrumentosFecha = new ArrayList<>();
				Correlativo++;
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (criterio.CodigoFuente.equalsIgnoreCase(fuente)) {
							InstrumentoFecha arg0 = new InstrumentoFecha();
							arg0.Fecha = Fecha.toStringDate(inst.FechaConsulta);
							arg0.IdInstrumento = inst.IdInstrumento;
							arg0.Correlativo = Correlativo;
							if (!instrumentosFecha.contains(arg0)) {
								instrumentosFecha.add(arg0 );	
							}
						}
					}
				}
				
				sql.batch(con, SQL_INSERT_INSTRUMENTO_FECHA, instrumentosFecha, InstrumentoFecha.class);
				params.clear();
				params.put("Correlativo",Correlativo);
				List<PreciosMedioBcs> preciosMediosBCS = sql.query(con, SQL_SELECT_PRECIOS_MEDIO_BCS,params, PreciosMedioBcs.class);
				HashMap<String,PreciosMedioBcs> hashPrecioMedio = new HashMap<>();
				for (PreciosMedioBcs precioMedio : preciosMediosBCS) {
					hashPrecioMedio.put(precioMedio.IdInstrumento + "-" + precioMedio.Fecha, precioMedio);
				}
				
				//for (PreciosMedioBcs precio : preciosMediosBCS) {
				for (Instrumento_Proceso inst : instrumentosProceso) {
					PreciosMedioBcs precioMedio = hashPrecioMedio.get(inst.IdInstrumento + "-" + inst.FechaConsulta);
					if (precioMedio != null) {
					//if (inst.FechaConsulta.equals(precio.Fecha) && inst.IdInstrumento.equals(precio.IdInstrumento)) {
						EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
						//Utils.copyPojo(inst, salida_PreciosTasas);
						salida_PreciosTasas.IdTipoTasa = "";
						salida_PreciosTasas.Tasa = BigDecimal.ZERO;
						salida_PreciosTasas.IdTipoPrecio = "";
						salida_PreciosTasas.CodigoFuente = fuente;
						salida_PreciosTasas.MensajeFuente = "Fuente: PreciosMedioBCS";
						salida_PreciosTasas.MensajeError = null;
						salida_PreciosTasas.FechaFuente =  Fecha.toTimeStamp(Fecha.toStringDate(precioMedio.FechaEncontrada) + " 00:00:00");
						salida_PreciosTasas.Precio = precioMedio.PrecioMedio;
						
						guardarResultado(inst,fuente,salida_PreciosTasas);
					}
				}
				//}
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIO BASE EN TABLA DE INSTRUMENTOS
			else if (fuente.equalsIgnoreCase("INSTRUMENTO")) {
				List<Long> idsInstrumentos = new ArrayList<>();
				
				instrumentosProceso.stream()
			    				.filter(inst -> inst.criterios.stream().anyMatch(criterio -> criterio.CodigoFuente.equalsIgnoreCase(fuente)))
			    				.map(inst -> inst.IdInstrumento)
							    .distinct()
							    .forEach(idsInstrumentos::add);
				
				params.clear();
				params.put("idsInstrumento", idsInstrumentos);
				List<PreciosTasasInstrumento> preciosInstrumento = sql.query(con, SQL_SELECT_PRECIOS_TASAS_INSTRUMENTO, params, PreciosTasasInstrumento.class);
				
				preciosInstrumento.stream().forEach(precio -> {
					instrumentosProceso.stream().filter(inst -> precio.IdInstrumento.equals(inst.IdInstrumento)).forEach(inst-> {
						EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
						
						salida_PreciosTasas.IdTipoTasa = "";									
						salida_PreciosTasas.IdTipoPrecio = "";
						salida_PreciosTasas.CodigoFuente = fuente;
						salida_PreciosTasas.MensajeFuente = "Fuente: Precio Base Instrumento";
						salida_PreciosTasas.MensajeError = null;
						salida_PreciosTasas.FechaFuente =  Fecha.toTimeStamp("0001-01-01 00:00:00");
						salida_PreciosTasas.Precio = precio.Precio;
						salida_PreciosTasas.Tasa = precio.Tasa;
						
						guardarResultado(inst,fuente,salida_PreciosTasas);
					});
				});
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			//-----------------------------------------------------------------------------
			//FUENTE: PRECIO BASE POR DEFAULT PARA INSTRUMENTOS NEGOCIACION
			else if (fuente.equalsIgnoreCase("DEFAULTNEGOCIACION")) {
			    List<EntregarPreciosTasas_Entrada_DatosInstrumentos> listaInstrumentos = new ArrayList<>();
			    List<InfoInstrumento> paramsInstrumentos2 = new ArrayList<>();
			    
			    // Procesar los resultados de la query 
			    for (EntregarPreciosTasas.Instrumento_Proceso instr : instrumentosProceso) {
			        if (instr.IdInstrumentoRaiz == null || instr.IdInstrumentoRaiz == 0) {
			            continue; // Salta esta iteración y sigue con la siguiente
			        }
			        EntregarPreciosTasas_Entrada_DatosInstrumentos instrumento = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			        instrumento.IdRegistro = instr.IdRegistro;
			        instrumento.TipoCartera = instr.TipoCartera;
			        instrumento.IdUso = instr.IdUso;
			        instrumento.IdTipoCriterioTasa = instr.IdTipoCriterioTasa;
			        instrumento.IdClasificacionTipoInstrumento = instr.IdClasificacionTipoInstrumento;
			        instrumento.IdTipoInstrumento = instr.IdTipoInstrumento;
			        instrumento.Nemotecnico = instr.Nemotecnico;
			        instrumento.IdMoneda = instr.IdMoneda;
			        instrumento.FechaConsulta = instr.FechaConsulta;
			        instrumento.PlazoResidual = instr.PlazoResidual;
			        instrumento.NemoSVS = instr.NemoSVS;
			        instrumento.IdInstrumento = instr.IdInstrumentoRaiz;
			        listaInstrumentos.add(instrumento);
			        
			        InfoInstrumento infoInstrumentoEntrada = new InfoInstrumento();
				    infoInstrumentoEntrada.IdInstrumento = instr.IdInstrumento;
				    infoInstrumentoEntrada.FechaConsulta = instr.FechaConsulta;
				    paramsInstrumentos2.add(infoInstrumentoEntrada);
			    }

			    if (listaInstrumentos.isEmpty()) {
			        return null; //No hay instrumentos negociables que procesar
			    }

			    EntregarPreciosTasas_Entrada Objin = new EntregarPreciosTasas_Entrada();
			    // Asignar valores generales
			    Objin.p_IdEmpresa = entrada.p_IdEmpresa;
			    Objin.p_IdUsuario = entrada.p_IdUsuario;
			    Objin.p_IdModulo = entrada.p_IdModulo;
			    Objin.p_IdPais = entrada.p_IdPais;
			    // Asignar los instrumentos procesados
			    Objin.p_DatosInstrumentos = listaInstrumentos;

			    // Llamar a la lógica de negocio
			    EntregarPreciosTasas demo = new EntregarPreciosTasas();
			    EntregarPreciosTasas_Salida ObjOut = demo.logica(con, Objin);

				sql.batch(con, SQL_INSERT_INSTRUMENTOS_PROCESO, paramsInstrumentos2, InfoInstrumento.class);

				List<InfoInstrumento> infoInstrumentos2 = sql.query(con, SQL_SELECT_TASAIN_PARAMS_NEGOCIACION, new HashMap<String, Object>(), InfoInstrumento.class);
				
				SQL.exec(con, SQL_DELETE_INSTRUMENTOS_PROCESO); //Limpiamos la tabla para la siguiente iteraci�n.
				
			    // Procesar cada precio obtenido
				infoInstrumentos2.forEach(precio -> {
			        instrumentosProceso.stream()
			            .filter(inst -> precio.IdInstrumento.equals(inst.IdInstrumento))
			            .forEach(inst -> {
			                EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas = crearPojoSalidaEntregarPrecioTasa(inst);
			                
			                BigDecimal precioCierre = (ObjOut.P_OUT_PRECIOSTASAS.isEmpty()) ? BigDecimal.ONE : ObjOut.P_OUT_PRECIOSTASAS.stream().filter(p -> p.IdInstrumento == inst.IdInstrumentoRaiz).map(p -> p.Precio).findFirst().orElse(BigDecimal.ONE);
			                
			                // Guardar resultados
			                salida_PreciosTasas.IdTipoTasa = "";
			                salida_PreciosTasas.IdTipoPrecio = "";
			                salida_PreciosTasas.CodigoFuente = fuente;
			                salida_PreciosTasas.MensajeFuente = "Cálculo realizado por DEFAULTNEGOCIACION";
			                salida_PreciosTasas.MensajeError = null;
			                salida_PreciosTasas.FechaFuente = Fecha.toTimeStamp("0001-01-01 00:00:00");
			                salida_PreciosTasas.Precio = precioCierre;
			                salida_PreciosTasas.Tasa = BigDecimal.ZERO;

			                guardarResultado(inst, fuente, salida_PreciosTasas);
			            });
			    });

			    fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
			    faltanInstrumentos = faltanInstrumentos(instrumentosProceso, fuentesRequeridas);
			    if (!faltanInstrumentos) {
			        continue;
			    }
			}


			//-----------------------------------------------------------------------------
			else {
				logger.debug("FUENTE " + fuente + " NO IMPLEMENTADA");
				//fuentes definidas en la tabla pero no esta la query
				//marcamos todos los criterios null
				for (Instrumento_Proceso inst : instrumentosProceso) {
					for (Criterio criterio : inst.criterios) {
						if (criterio.CodigoFuente.equalsIgnoreCase(fuente)) {
							inst.precioPorCriterio.put(criterio, null);
						}
					}
				}
				
				fuentesPorVisitar.remove(fuentesPorVisitar.indexOf(fuente));
				faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
				if (!faltanInstrumentos) {
					continue;
				}
			}
			
			faltanInstrumentos = faltanInstrumentos(instrumentosProceso,fuentesRequeridas);
			if (!faltanInstrumentos) {
				continue;
			}
		}
		
		//limpiamos
		//sql.batch(con, SQL_DELETE_INSTRUMENTO_FECHA, new ArrayList<>(), Object.class);
		SQL.exec(con, SQL_DELETE_INSTRUMENTO_FECHA);
		sql.batch(con, SQL_DELETE_TASAS_PUBLICADAS, new ArrayList<>(), Object.class);
		sql.batch(con, SQL_DELETE_INSTRUMENTO_PLAZO, new ArrayList<>(), Object.class);
		
		//armar la salida
		for (Instrumento_Proceso instrumento : instrumentosProceso) {
			EntregarPreciosTasas_Salida_PreciosTasas resultado = null;
			String fuenteTasaInstrumento = "";
			//hay que obtener el criterio por prioridad!!
			Long prioridad = Long.MAX_VALUE;//dejar la prioridad menor
			for (Criterio criterio : instrumento.precioPorCriterio.keySet()) {
			    if ((criterio.Prioridad < prioridad && (criterio.Nemotecnico.equals("") || instrumento.Nemotecnico.equals(criterio.Nemotecnico))) || (criterio.Prioridad == prioridad && instrumento.Nemotecnico.equals(criterio.Nemotecnico))) {
			        resultado = instrumento.precioPorCriterio.get(criterio);
			        prioridad = criterio.Prioridad;
			        fuenteTasaInstrumento = criterio.CodigoFuente;
			    }
			}
			
			if (resultado == null) {
				resultado = crearPojoSalidaEntregarPrecioTasa(instrumento);
				resultado.IdTipoTasa = null;
				resultado.Tasa = null;				
				resultado.IdTipoPrecio = null;
				resultado.Precio = null;				
				resultado.CodigoFuente = resultado.CodigoFuente = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ENTREGA_PRECIO/TASA").add("nemotecnico", "IF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) && instrumento.NemoSVS != null ? instrumento.NemoSVS : instrumento.Nemotecnico).add("codigofuente", fuenteTasaInstrumento).msg();
				resultado.MensajeFuente = null;
				resultado.MensajeError = resultado.CodigoFuente = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_ENTREGA_PRECIO/TASA").add("nemotecnico", "IF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) && instrumento.NemoSVS != null ? instrumento.NemoSVS : instrumento.Nemotecnico).add("codigofuente", fuenteTasaInstrumento).msg();
				resultado.FechaFuente =  null;
			}
			salida.P_OUT_PRECIOSTASAS.add(resultado);
		}
		
		logger.info("Termino del servicio EntregarPreciosTasas, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	public Date SumarRestar(Date fecha , int dias) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.DAY_OF_YEAR, dias);
		
		return (Date) calendar.getTime();
	}

	private EntregarPreciosTasas_Salida_PreciosTasas crearPojoSalidaEntregarPrecioTasa(Instrumento_Proceso inst) {
		EntregarPreciosTasas_Salida_PreciosTasas s = new EntregarPreciosTasas_Salida_PreciosTasas();
		s.IdRegistro = inst.IdRegistro;
		s.IdClasificacionTipoInstrumento = inst.IdClasificacionTipoInstrumento;
		s.IdTipoInstrumento = inst.IdTipoInstrumento;
		s.IdInstrumento = inst.IdInstrumento;
		s.Nemotecnico = inst.Nemotecnico;
		/*s.IdTipoTasa = inst.IdTipoTasa;
		s.Tasa = inst.Tasa;
		s.IdTipoPrecio = inst.IdTipoPrecio;
		s.Precio = inst.Precio;
		s.CodigoFuente = inst.CodigoFuente;
		s.MensajeFuente = inst.MensajeFuente;
		s.FechaFuente = inst.FechaFuente;
		s.MensajeError = inst.MensajeError;*/
		s.IdMoneda = inst.IdMoneda;
		return s;
	}
	

	private void guardarResultado(Instrumento_Proceso inst, String fuente,EntregarPreciosTasas_Salida_PreciosTasas salida_PreciosTasas) {
		//buscamos el criterio correspondiente
		Criterio criterioEncontrado = null;
		for (Criterio criterio : inst.criterios) {
			if (criterio.CodigoFuente.equalsIgnoreCase(fuente)) {
				criterioEncontrado = criterio;
				break;
			}
		}
		if (criterioEncontrado != null) {
			inst.precioPorCriterio.put(criterioEncontrado, salida_PreciosTasas);	
		}
	}

	//Validaciones
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, Instrumento_Proceso datos, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdRegistro", datos.IdRegistro)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(datos.IdRegistro,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				datos.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(datos.IdRegistro,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				datos.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(datos.IdRegistro,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				datos.advertencias++;
			}
			else 
			{
				datos.ok++;
			}
		}
		else 
		{
			datos.ok++;
		}		
	}
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	
	public static void main(String[] args) throws Exception 
	{	
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		Connection con = PlataformaUtils.getDESAConnection(); 
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MFW\",\"p_IdPais\":44,\"p_DatosInstrumentos\":[{\"IdRegistro\":961,\"TipoCartera\":\"0\",\"IdUso\":\"CARTERA\",\"IdTipoCriterioTasa\":\"0\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdTipoInstrumento\":\"FWD\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdMoneda\":\"USD\",\"FechaConsulta\":\"2018-10-19 00:00:00.000\",\"PlazoResidual\":9,\"NemoSVS\":null},{\"IdRegistro\":962,\"TipoCartera\":\"0\",\"IdUso\":\"CARTERA\",\"IdTipoCriterioTasa\":\"0\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdTipoInstrumento\":\"FWD\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdMoneda\":\"CLP\",\"FechaConsulta\":\"2018-10-19 00:00:00.000\",\"PlazoResidual\":6,\"NemoSVS\":null},{\"IdRegistro\":963,\"TipoCartera\":\"0\",\"IdUso\":\"CARTERA\",\"IdTipoCriterioTasa\":\"0\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdTipoInstrumento\":\"FWD\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdMoneda\":\"USD\",\"FechaConsulta\":\"2018-10-19 00:00:00.000\",\"PlazoResidual\":31,\"NemoSVS\":null},{\"IdRegistro\":964,\"TipoCartera\":\"0\",\"IdUso\":\"CARTERA\",\"IdTipoCriterioTasa\":\"0\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdTipoInstrumento\":\"FWD\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdMoneda\":\"CLP\",\"FechaConsulta\":\"2018-10-19 00:00:00.000\",\"PlazoResidual\":30,\"NemoSVS\":null}]}";
			EntregarPreciosTasas_Entrada Objin = gson.fromJson(json, EntregarPreciosTasas_Entrada.class);
			EntregarPreciosTasas demo = new EntregarPreciosTasas();
			
			//INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_IN_TasasPublicadas"
			//SELECT 0,'TASA',NULL, 261980,'BACEN-A2','2017-01-01','2017-04-02' FROM DUMMY

 			System.out.println("ENTRADA:" + Objin.toString());
 			try
 			{
 				EntregarPreciosTasas_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut.toString());
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.rollback();
			}
 			con.close();
	    }
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
