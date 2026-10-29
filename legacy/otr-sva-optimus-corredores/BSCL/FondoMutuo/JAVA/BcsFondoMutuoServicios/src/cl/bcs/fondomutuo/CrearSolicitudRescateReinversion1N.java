package cl.bcs.fondomutuo;


/**
 * Proposito : Recibe las solicitudes de reinversion 1 a N y antes de enviar a crearlas, las reordena en nuevos grupos de acuerdo a:
 *  	- SWITCH		: Reinversiones entre fondos extranjeros de un mismo emisor (Ej: USD > USD  mismo emisor)
 *  	- INTERNACIONAL : Reinversiones entre fondos extranjeros de distintos emisores (Ej: USD > USD distinto emisor)
 *  	- NACIONAL      : Reinversion de un fondo extranjo a uno nacional (Ej: USD > CLP)
 *  	- SON GRUPO     : Reinversion de un fondo nacional a cualquier otro (Ej:  CLP > *)
 * Fecha     : 15/06/2020
 */

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.ValorCuotaTipoParidad;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.EntradaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesRescate;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.SalidaCrearSolicitudRescateReinversion;



public class CrearSolicitudRescateReinversion1N implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudRescateReinversion1N.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql");
	
	// ----------------------------------------------------------------------------
	// CLASES DE ENTRADA
    // ----------------------------------------------------------------------------
	public static class EntradaCrearSolicitudRescateReinversion1N{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_SimularOperacion = "N";
		public String 	p_UsaReglasNegocio;
		public String 	p_IdTipoCanalIngreso = "OPT";
		public List<InSolicitudesRescate> p_SolicitudesRescate;
		public List<InSolicitudesAporte> p_SolicitudesAporte;
		public EntradaCrearSolicitudRescateReinversion1N() {
			p_SolicitudesAporte = new ArrayList<InSolicitudesAporte>();
			p_SolicitudesRescate = new ArrayList<InSolicitudesRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	

	
	// ----------------------------------------------------------------------------
	// CLASES DE SALIDA
    // ----------------------------------------------------------------------------
	public static class SalidaCrearSolicitudRescateReinversion1N{
		public Long 					 P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida>		 P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudRescateReinversion1N(){
			this.P_OUT_ERRORESCONTADOR	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO 		= new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------------------------------------
	// CLASES UTILITARIAS
    // ----------------------------------------------------------------------------
	public static class Grupo{
		public Long   		IdAgrupador;  			// Id agrupacion original	
		public Long   		FolioContrato;  		// FolioContrato original		
		public String 		TipoReinversion; 		// SWITCH   -  INTERNACIONAL -  NACIONAL  - SIN GRUPO		
		public List<InSolicitudesRescate> SolicitudesRescate;
		public List<InSolicitudesAporte>  SolicitudesAporte;
		public List<AuxNumCuotas>  AuxNumeroCuotas;
		public Grupo(){			
			this.SolicitudesRescate = new ArrayList<InSolicitudesRescate>();
			this.SolicitudesAporte  = new ArrayList<InSolicitudesAporte>();
			this.AuxNumeroCuotas  = new ArrayList<AuxNumCuotas>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	    public static class AuxNumCuotas{
		public Long IdInterno;
		public BigDecimal NumeroCuotas; 
    	}
	// -----------------------------------------------------------------------------
	// CALL
    // -----------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearSolicitudRescateReinversion1N jin = gson.fromJson(in.toString(), EntradaCrearSolicitudRescateReinversion1N.class);
		SalidaCrearSolicitudRescateReinversion1N out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// -----------------------------------------------------------------------------
	// LOGICA
    // -----------------------------------------------------------------------------
	public SalidaCrearSolicitudRescateReinversion1N logica(Connection con, EntradaCrearSolicitudRescateReinversion1N entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudRescateReinversion1N");
		logger.info("ENTRADA > " + entrada.toString());
		SalidaCrearSolicitudRescateReinversion1N out = new SalidaCrearSolicitudRescateReinversion1N();
				
		SQL sql=new SQL();
		
		
		// ---------------------------------------------------------------------------------------
		// VALIDACIONES
		// ---------------------------------------------------------------------------------------
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}		
		
		if(entrada.p_SolicitudesRescate.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitudesRescate mov: entrada.p_SolicitudesRescate) {
			if(mov.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdInterno);
			}
		}
	


		
		
		// ---------------------------------------------------------------------------------------
		// SE OBTIENE INFORMACION DE SERIE FONDO
		// ---------------------------------------------------------------------------------------
		List<String> series = new ArrayList<String>();
		
		for(InSolicitudesRescate rescates : entrada.p_SolicitudesRescate){	
			if(!series.contains(rescates.IdSerieFondo)) series.add(rescates.IdSerieFondo);		
		}
		
		//Se obtiene cantidad de decimales utilizados por el NumeroCuotas de las series Rescate
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		long inicioSelectvcSeriesRescate = System.currentTimeMillis();
		logger.debug("INICIO::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
		List<ValorCuotaTipoParidad> vcSeriesRescate = sql.query(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class);
		logger.debug("FIN::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
		long finSelectvcSeriesRescate = System.currentTimeMillis();
		logger.debug("Tiempo SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO: " + (finSelectvcSeriesRescate - inicioSelectvcSeriesRescate));
		Map<String,ValorCuotaTipoParidad> hVCSeriesRescate = vcSeriesRescate.parallelStream().collect(Collectors.toMap(serie->serie.IdSerieFondo,serie->serie));;
		
		for(InSolicitudesAporte aportes : entrada.p_SolicitudesAporte){
			if(!series.contains(aportes.IdSerieFondo)) series.add(aportes.IdSerieFondo);			
		}
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		
		
		
		// ---------------------------------------------------------------------------------------
		// CALCULAR NUEVOS GRUPOS DE ACUERDO A : SWITH / INTERNACIONALES / NACIONALES  / SIN GRUPO
		// ---------------------------------------------------------------------------------------
		
		HashMap<String,Grupo> hGrupos = new HashMap<String,Grupo>();
		String tipoReinversion = "";
		String keyTipoGrupo = "";
		

		
		for(InSolicitudesRescate rescate : entrada.p_SolicitudesRescate)
		{
			for(InSolicitudesAporte aporte : entrada.p_SolicitudesAporte)
			{
				SerieFondoSolicitudes serieRescate = (SerieFondoSolicitudes) hseries.get(rescate.IdSerieFondo);
				if (aporte.IdAgrupador.compareTo(rescate.IdAgrupador) == 0 && aporte.IdInterno.compareTo(rescate.IdInterno) == 0)
				{							
					SerieFondoSolicitudes serieAporte = (SerieFondoSolicitudes) hseries.get(aporte.IdSerieFondo);
					keyTipoGrupo = "";
					
					
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S")  && serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
					{
						tipoReinversion = "SWITCH";  //SWICH:  USD > USD (Mismo Emisor)
					}					
					
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S")  && !serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
					{
						tipoReinversion = "INTERNACIONAL";  //INTERNACIONAL:  USD > USD (Distinto Emisor)
					}					
					
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("N"))
					{
						tipoReinversion = "NACIONAL";    //NACIONAL:  USD > CLP
					}					
					
					if("S".equals(serieRescate.FondoExtranjero) && ("S".equals(serieAporte.FondoNacionalDolar) && "N".equals(serieAporte.FondoExtranjero))) {
						tipoReinversion = "NACIONAL DOLAR";
					}
					
					if (serieRescate.FondoExtranjero.equalsIgnoreCase("N"))
					{						
						tipoReinversion = "SIN GRUPO";  //SIN GRUPO: Para este caso no se agrupa(cuando rescate no es extranjero): CLP > *
					}
					
								
					keyTipoGrupo = tipoReinversion + aporte.IdAgrupador.toString();
					if(!hGrupos.containsKey(keyTipoGrupo)) {
						Grupo grupo = new Grupo();
						grupo.IdAgrupador = rescate.IdAgrupador;
		
						grupo.FolioContrato = rescate.FolioContrato;   // Si es que es enviado desde el negocio (de lo contrario se genera aca)					
						
						grupo.TipoReinversion = tipoReinversion;
						grupo.SolicitudesRescate.add(rescate);
						grupo.SolicitudesAporte.add(aporte);
						hGrupos.put(keyTipoGrupo, grupo);
					}
					else
					{
						Grupo grupo = (Grupo)hGrupos.get(keyTipoGrupo);
						grupo.SolicitudesRescate.add(rescate);
						grupo.SolicitudesAporte.add(aporte);						
					}
					
					
				}
			}
		}
		
		
		//Para revisar conformacion de nuevos grupos		
		//revisarGrupos(hGrupos, log);
		
		
		// ---------------------------------------------------------------------------------------
		// GENERAR NUEVOS FOLIOS Y SECUENCAS PARA NUEVOS GRUPOS
		// ---------------------------------------------------------------------------------------
		
		// Para los grupos originales se generar un mismo FolioContrato que es el que aprupara las solicitudes de reinversion
		List<Long> gruposSinFolioContrato =new ArrayList<Long>();
		
		for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
		    Grupo value = entry.getValue();
		    
		    if (!gruposSinFolioContrato.contains(value.IdAgrupador) && value.FolioContrato.compareTo(0L) == 0)
		    	gruposSinFolioContrato.add(value.IdAgrupador);		    
		    
		}

		
		List<Long> secuenciasFolioContrato = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato" , new Long(gruposSinFolioContrato.size()));

		
		//Asignacion de nuevos folio contrato y nuevo IdAgrupador
		Long nuevoIdAgrupador   = 1L;
		Long nuevoFolioContrato = 0L;
		
		for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
		    Grupo value = entry.getValue();

		    nuevoFolioContrato = 0L;		    
		  
		    if (gruposSinFolioContrato.contains(value.IdAgrupador))
		    {	// Si no es enviado desde el negocio, Se genera FolioContrato de acuerdo a los antiguos grupos
		    	nuevoFolioContrato = secuenciasFolioContrato.get(gruposSinFolioContrato.indexOf(value.IdAgrupador));  
		    }
		    else
		    {	// Si es enviado desde el negocio, se mantiene el mismo folio de contrato
		    	nuevoFolioContrato = value.FolioContrato;
		    }
		    
		    for(InSolicitudesRescate rescate : value.SolicitudesRescate)
		    {
		    	rescate.FolioContrato = nuevoFolioContrato;	// Se asigna FolioContrato de acuerdo a la antigua agrupacion
		    	rescate.IdAgrupador   = nuevoIdAgrupador;   // Se asigna nuevo IdAgrupador de acuerdo a los nuevos grupos
		    }
		    
		    for(InSolicitudesAporte aporte : value.SolicitudesAporte)
		    {
		    	aporte.FolioContrato = nuevoFolioContrato;	// Se asigna FolioContrato de acuerdo a la antigua agrupacion
		    	aporte.IdAgrupador   = nuevoIdAgrupador;    // Se asigna nuevo IdAgrupador de acuerdo a los nuevos grupos
		    }
		    nuevoIdAgrupador++;
		}
		
		// ---------------------------------------------------------------------------------------
		// RECALCULA PORCENTAJES Y MONTOS DE ACUERDO A NUEVOS GRUPOS
		// ---------------------------------------------------------------------------------------
		BigDecimal totalPorcentajeAporte = BigDecimal.ZERO;
		BigDecimal totalCuotasRescateRecalculadas = BigDecimal.ZERO;
		BigDecimal totalCuotasRescateSinFormateo = BigDecimal.ZERO;
		BigDecimal numeroCuotasMayor = BigDecimal.ZERO;
		Set<String> hashSolicitudesRescates = new HashSet<>();
		List<InSolicitudesRescate> totalSolicitudesRescates = new ArrayList<InSolicitudesRescate>();
		
		for (InSolicitudesRescate r : entrada.p_SolicitudesRescate) {
            String hashSolicitud = 	r.IdPortafolio + "_" + r.IdSerieFondo + "_" +  r.CalculoPorMontoCuota + "_" + r.Monto + "_" 
            						+ r.NumeroCuotas + "_" + r.IdRegimenTributario;
            
            if (hashSolicitudesRescates.add(hashSolicitud)) {
            	InSolicitudesRescate solRescate = new InSolicitudesRescate();
            	solRescate.IdPortafolio = r.IdPortafolio;
            	solRescate.IdSerieFondo = r.IdSerieFondo;
            	solRescate.CalculoPorMontoCuota = r.CalculoPorMontoCuota;
            	solRescate.Monto = r.Monto;
            	solRescate.NumeroCuotas = r.NumeroCuotas;
            	solRescate.IdRegimenTributario = r.IdRegimenTributario;
            	totalSolicitudesRescates.add(solRescate);
            }
        }
		
		for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
			Grupo value = entry.getValue();
			totalPorcentajeAporte = BigDecimal.ZERO;
		    
		    for(InSolicitudesAporte aporte : value.SolicitudesAporte){		    	
		    	totalPorcentajeAporte = totalPorcentajeAporte.add(aporte.Porcentaje);
		    }
		    
		    //Se recalculan los porcentajes y montos de los rescates de acuerdo a las nuevas agrupaciones
		    for(InSolicitudesRescate rescate : value.SolicitudesRescate){
		    	AuxNumCuotas NumeroCuotasAux = new AuxNumCuotas();
		    	int decimalesCuota = hVCSeriesRescate.get(rescate.IdSerieFondo).DecimalesCuota.intValue();
		    	rescate.Monto = rescate.Monto.multiply(totalPorcentajeAporte, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);
		    	rescate.Porcentaje = rescate.Porcentaje.multiply(totalPorcentajeAporte, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);
		    	//Primero se almacena el numero de cuotas sin formateo
		    	rescate.NumeroCuotas = rescate.NumeroCuotas.multiply(totalPorcentajeAporte, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);
		    	totalCuotasRescateSinFormateo = totalCuotasRescateSinFormateo.add(rescate.NumeroCuotas);
		    	//Luego se almacena el numero de cuotas sin formateo
		        NumeroCuotasAux.NumeroCuotas = rescate.NumeroCuotas.setScale(decimalesCuota +1,RoundingMode.HALF_DOWN);
		        NumeroCuotasAux.IdInterno= rescate.IdInterno;
		    	rescate.NumeroCuotas = rescate.NumeroCuotas.setScale(decimalesCuota ,RoundingMode.HALF_DOWN);
		    	totalCuotasRescateRecalculadas = totalCuotasRescateRecalculadas.add(rescate.NumeroCuotas.setScale(decimalesCuota, RoundingMode.HALF_DOWN));
		    	
		    	if(numeroCuotasMayor.compareTo(rescate.NumeroCuotas) < 0) {
					numeroCuotasMayor = rescate.NumeroCuotas;
		    	}
		    	value.AuxNumeroCuotas.add(NumeroCuotasAux);
		    }
		    
		    //Se recalculan los porcentajes para los aportes, de acuerdo a las nuevas agrupaciones
		    for(InSolicitudesAporte aporte : value.SolicitudesAporte){	 
		    	aporte.PorcentajeReal = aporte.Porcentaje;
		    	aporte.Porcentaje = aporte.Porcentaje.multiply(new BigDecimal(100), MathContext.DECIMAL128).divide(totalPorcentajeAporte, MathContext.DECIMAL128);
		    }
		}

		//Si el numero de cuotas recalculadas en base a los porcentajes no coincide con el numero de cuotas total del rescate se vuelven a calcular
		if(totalCuotasRescateRecalculadas.compareTo(totalCuotasRescateSinFormateo) > 0) {
			for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
				Grupo value = entry.getValue();
				totalPorcentajeAporte = BigDecimal.ZERO;
				
				for(InSolicitudesAporte aporte : value.SolicitudesAporte){		    	
			    	totalPorcentajeAporte = totalPorcentajeAporte.add(aporte.PorcentajeReal);
			    }
				
				for(InSolicitudesRescate rescate : value.SolicitudesRescate){
					int decimalesCuota = hVCSeriesRescate.get(rescate.IdSerieFondo).DecimalesCuota.intValue();
					if(numeroCuotasMayor.compareTo(rescate.NumeroCuotas) == 0) {
						rescate.NumeroCuotas = rescate.NumeroCuotas.subtract((totalCuotasRescateRecalculadas.subtract(totalCuotasRescateSinFormateo, MathContext.DECIMAL128)), MathContext.DECIMAL128).setScale(decimalesCuota,RoundingMode.HALF_DOWN);
					}
				}
			}
		}
		
		if(totalCuotasRescateRecalculadas.compareTo(totalCuotasRescateSinFormateo) < 0) {
			for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
				Grupo value = entry.getValue();
				value.SolicitudesRescate.parallelStream().forEach(rescate->{
					value.AuxNumeroCuotas.parallelStream().forEach(NumCuotaAux->{
						if (rescate.IdInterno.equals(NumCuotaAux.IdInterno) && rescate.NumeroCuotas.compareTo(BigDecimal.ZERO) == 0) {
							rescate.NumeroCuotas=  NumCuotaAux.NumeroCuotas;
							}
						});
					});
				}
			}
		
		//Cuadrar numero de cuotas
		for (InSolicitudesRescate r : totalSolicitudesRescates) {
			int decimalesCuota = hVCSeriesRescate.get(r.IdSerieFondo).DecimalesCuota.intValue();
			r.NumeroCuotas = r.NumeroCuotas.setScale(decimalesCuota ,RoundingMode.HALF_DOWN);
			Set<String> hashSolicitudesGrupos = new HashSet<>();
			BigDecimal totalNumeroCuotasRescates = BigDecimal.ZERO;
			BigDecimal diferenciaNumeroCuotas = BigDecimal.ZERO;
			Long mayorIdInterno = 0L;
			Long idAgrupador = 0L;
			
			for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
				Grupo value = entry.getValue();
				
				for(InSolicitudesRescate rescate : value.SolicitudesRescate) {
					if(rescate.IdPortafolio.equals(r.IdPortafolio) && rescate.IdSerieFondo.equals(r.IdSerieFondo) &&  rescate.IdRegimenTributario.equals(r.IdRegimenTributario)) {
						String hashSol = rescate.IdCliente + "_" + rescate.IdPortafolio + "_" + rescate.IdSerieFondo + "_" +  rescate.CalculoPorMontoCuota + "_" 
										+ rescate.Monto + "_" + rescate.NumeroCuotas + "_" + rescate.IdRegimenTributario + "_" + rescate.IdAgrupador;
						
						if (hashSolicitudesGrupos.add(hashSol)) {
							totalNumeroCuotasRescates = totalNumeroCuotasRescates.add(rescate.NumeroCuotas);
							
							if(rescate.IdInterno > mayorIdInterno) {
								mayorIdInterno = rescate.IdInterno;
								idAgrupador = rescate.IdAgrupador;
							}
			            }
					}
				}
			}
			
			if(r.NumeroCuotas.compareTo(totalNumeroCuotasRescates) != 0 && (r.NumeroCuotas.subtract(totalNumeroCuotasRescates).abs()).compareTo(BigDecimal.ONE) < 1) {
				diferenciaNumeroCuotas = r.NumeroCuotas.subtract(totalNumeroCuotasRescates);
				
				for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
	
					Grupo value = entry.getValue();
					for(InSolicitudesRescate rescate : value.SolicitudesRescate) {
						if(mayorIdInterno == rescate.IdInterno || idAgrupador == rescate.IdAgrupador) {
							rescate.NumeroCuotas = rescate.NumeroCuotas.add(diferenciaNumeroCuotas);
						}
					}
				}
			}
 		}
		
		//Para revisar conformacion de nuevos grupos
		revisarGrupos(hGrupos, logger);
		
		// ---------------------------------------------------------------------------------------
		// LLAMADA CrearSolicitudRescateReinversion
		// ---------------------------------------------------------------------------------------		

		CrearSolicitudRescateReinversion servicioCrearSolicitudRescateReinversion = new CrearSolicitudRescateReinversion();		
		EntradaCrearSolicitudRescateReinversion entradaCrearSolicitudRescateReinversion = new EntradaCrearSolicitudRescateReinversion();

		entradaCrearSolicitudRescateReinversion.p_IdEmpresa 			= entrada.p_IdEmpresa;
		entradaCrearSolicitudRescateReinversion.p_IdUsuario 			= entrada.p_IdUsuario;
		entradaCrearSolicitudRescateReinversion.p_IdModulo 				= entrada.p_IdModulo;
		entradaCrearSolicitudRescateReinversion.p_SimularOperacion 		= entrada.p_SimularOperacion;
		entradaCrearSolicitudRescateReinversion.p_UsaReglasNegocio 		= entrada.p_UsaReglasNegocio;
		entradaCrearSolicitudRescateReinversion.p_IdTipoCanalIngreso 	= entrada.p_IdTipoCanalIngreso;		
		
		for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) {
		    Grupo value = entry.getValue();
		    entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.addAll(value.SolicitudesRescate);
		    entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.addAll(value.SolicitudesAporte);
		}
		
		entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.forEach(rescate->{
			if(_c.CalculoPorTotal.equals(rescate.CalculoPorMontoCuota)) {
				rescate.CalculoPorMontoCuota = _c.CalculoPorCuota;
			}
		});
		
		SalidaCrearSolicitudRescateReinversion salidaCrearSolicitudRescateReinversion = servicioCrearSolicitudRescateReinversion.logica(con, entradaCrearSolicitudRescateReinversion);
		
		out.P_OUT_MENSAJES.addAll(salidaCrearSolicitudRescateReinversion.P_OUT_MENSAJES);
		out.P_OUT_RESULTADO.addAll(salidaCrearSolicitudRescateReinversion.P_OUT_RESULTADO);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		logger.info("SALIDA > " + out.toString());

		logger.info("FIN::CrearSolicitudRescateReinversion1N");
		return out;
	}
	
	
	// Para revisar la formacion de los nuevos grupos
	public  static void revisarGrupos(HashMap<String,Grupo> hGrupos, Logger log)
	{		
		for(Map.Entry<String, Grupo> entry : hGrupos.entrySet()) 
		{
			//System.out.println("KEY > " + entry.getKey() + "  VALUE > " + entry.getValue());
		    log.info("KEY > " + entry.getKey() + " - VALUE > " + entry.getValue());		    
		}
		
	}
	
	// -----------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -----------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			

		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesRescate\":[{\"IdInterno\":1,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00278AUSD\",\"CalculoPorMontoCuota\":\"C\",\"Monto\":975.26,\"NumeroCuotas\":23.597,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"APV-A\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdAgrupador\":1,\"Porcentaje\":100},{\"IdInterno\":2,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00278AUSD\",\"CalculoPorMontoCuota\":\"C\",\"Monto\":975.26,\"NumeroCuotas\":23.597,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"APV-A\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdAgrupador\":1,\"Porcentaje\":100},{\"IdInterno\":3,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00278AUSD\",\"CalculoPorMontoCuota\":\"C\",\"Monto\":975.26,\"NumeroCuotas\":23.597,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"APV-A\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdAgrupador\":1,\"Porcentaje\":100},{\"IdInterno\":4,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00278AUSD\",\"CalculoPorMontoCuota\":\"C\",\"Monto\":975.26,\"NumeroCuotas\":23.597,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"APV-A\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdAgrupador\":1,\"Porcentaje\":100},{\"IdInterno\":5,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00278AUSD\",\"CalculoPorMontoCuota\":\"C\",\"Monto\":975.26,\"NumeroCuotas\":23.597,\"IdMoneda\":\"USD\",\"IdRegimenTributario\":\"APV-A\",\"IdMedioDePago\":\"CIN\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"Glosa\":\"REINVERSION 1 A N\",\"IdAutorizadoOrigen\":0,\"FolioContrato\":0,\"IdAgrupador\":1,\"Porcentaje\":100}],\"p_SolicitudesAporte\":[{\"IdInterno\":1,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00363AUSD\",\"IdEmisor\":\"MST\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":146.29,\"NumeroCuotas\":2.128,\"IndicadorMLT\":\"N\",\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":15,\"Porcentaje\":15,\"FolioContrato\":0,\"IdAgrupador\":1},{\"IdInterno\":2,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00066CCLP\",\"IdEmisor\":\"SURA\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":176522,\"NumeroCuotas\":41.3188,\"IndicadorMLT\":\"N\",\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":20,\"Porcentaje\":20,\"FolioContrato\":0,\"IdAgrupador\":1},{\"IdInterno\":3,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00354AUSD\",\"IdEmisor\":\"MST\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":243.82,\"NumeroCuotas\":8.928,\"IndicadorMLT\":\"N\",\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":25,\"Porcentaje\":25,\"FolioContrato\":0,\"IdAgrupador\":1},{\"IdInterno\":4,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00290AUSD\",\"IdEmisor\":\"JPM\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":195.05,\"NumeroCuotas\":3.914,\"IndicadorMLT\":\"N\",\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":20,\"Porcentaje\":20,\"FolioContrato\":0,\"IdAgrupador\":1},{\"IdInterno\":5,\"IdCliente\":77814,\"IdPortafolio\":459978,\"IdSerieFondo\":\"00132AUSD\",\"IdEmisor\":\"BGF\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":195.05,\"NumeroCuotas\":8.234,\"IndicadorMLT\":\"N\",\"IdMoneda\":\"USD\",\"IdMedioDePago\":\"CIN\",\"IdRegimenTributario\":\"APV-A\",\"Glosa\":20,\"Porcentaje\":20,\"FolioContrato\":0,\"IdAgrupador\":1}]}";
		
		EntradaCrearSolicitudRescateReinversion1N Objin = gson.fromJson(json, EntradaCrearSolicitudRescateReinversion1N.class);
		CrearSolicitudRescateReinversion1N demo = new CrearSolicitudRescateReinversion1N();				
		try {
			SalidaCrearSolicitudRescateReinversion1N ObjOut=demo.logica(con, Objin);
			con.commit();
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
		con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
