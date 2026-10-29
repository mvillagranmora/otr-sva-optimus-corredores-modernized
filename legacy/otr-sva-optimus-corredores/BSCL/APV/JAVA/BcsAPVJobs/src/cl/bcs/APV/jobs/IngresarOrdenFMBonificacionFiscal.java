package cl.bcs.APV.jobs;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.APV.ProcesarDistribucion;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
//import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngresarOrdenFMBonificacionFiscal extends BcsJob implements JavaCall{
	//private final Log log = new Log("APV.IngresarOrdenFMBonificacionFiscal");
	static final Logger logger = LogManager.getLogger(ProcesarDistribucion.class);
	
	private final String IdTipoPrograma = "INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL";
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_FFMM_BF = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectFFMMBF.sql");
	private final String SQL_SELECT_PARAMETROS = loadResourceToString("cl/bcs/APV/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectParametros.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");


	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
	}

	@Override
	public void onError(Throwable t) {
		logger.info("****** ERROR  *******");
		logger.info(t);
        t.printStackTrace();

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	public static class ParametrosPrograma{
		public Long IdSecuencia;
		public String NombreParametro;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ApoResFFMM{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String TipoSolicitud;
		public String IdSerieFondo;
		public String CalculoPorMontoCuota;
		public String EsPorMontoLiquido;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String IdMoneda;
		public String IdMedioDePago;
		public Long IdPreferenciaPagoPortafolio;
		public String Visado;
		public String SolicitudBatch;
		public String OrigenOperacion;
		public String FechaOperacion;
		public String IdRegimenTributario;
		public String Glosa;
		public String IndicadorMLT;
		public String IdSerieFondoDestino;
		public String IdEmisorDestino;
		public Long IdAutorizadoOrigen;
		public BigDecimal Porcentaje;
		public Long FolioContrato;
		public String IdMonedaPago;
		public String IdTipoMovimientoFondoMutuo;
		public String TipoContratoOperacion;
		public String FolioContratoOperacion;
		public Long IdSolicitudAporteRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	//log.write("INICIO JOB");
    	logger.info("INICIO JOB");
		SQL sql=new SQL();
        
        Long IdEmpresa = 0L;
        Long IdPrograma = 0L;
        //Long IdAporteRescateABonificar = 0L;
        Long IdUsuario = 0L;
        String IdModulo = "";
		Long contadorFFMM = 1L;
		Long IdPais = 0L;

		HashMap<String,Object> 	ParamsSelectAporteRescate 	= new HashMap<String,Object>();
		ArrayList<Long> 		IdsAporteRescateABonificar	= new ArrayList<Long>();
		ConsultarEmpresa 		consultarEmpresaService 	= new ConsultarEmpresa();

        
		if (debug) {
        	IdPrograma = 1585540L;
            IdEmpresa = 1L;
            IdsAporteRescateABonificar.add(10971866L);
            IdModulo = "APV";            
            IdPais = 44L;     
            IdUsuario = 15L;
            
        } else{
			IdPrograma 	= this.getJobId();
            IdModulo 	= "APV";           
			
			HashMap<String,Object> ParamsSelectParametrosPrograma = new HashMap<String,Object>();
			ParamsSelectParametrosPrograma.put("IdPrograma", IdPrograma);
			
			List<ParametrosPrograma> _ParametrosPrograma = sql.query(con, SQL_SELECT_PARAMETROS, ParamsSelectParametrosPrograma, ParametrosPrograma.class);
			
			for(ParametrosPrograma parametro : _ParametrosPrograma){
				if(parametro.NombreParametro.equals("IdEmpresa")) IdEmpresa = Long.parseLong(parametro.Valor);
				if(parametro.NombreParametro.equals("IdUsuario")) IdUsuario = Long.parseLong(parametro.Valor);
				if(parametro.NombreParametro.equals("IdSolicitudAporteRescate")) IdsAporteRescateABonificar.add(Long.parseLong(parametro.Valor));
			}
			
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, IdEmpresa);
			
			IdPais = empresa.IdPais;			
        }
        ParamsSelectAporteRescate.put("IdsAporteRescate", IdsAporteRescateABonificar);
        
        HashMap<String,Object> params = new HashMap<String,Object>();
                
        
		Long 	PorcentajeBonificacionFiscalString 	= ConfiguracionSistema.getParametroConfiguracionLong(con,IdEmpresa, "APV","APV.PorcentajeBonificacionFiscal");
		String 	fechaSistema 						= ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<ApoResFFMM> _AporteRescate = sql.query(con, SQL_SELECT_FFMM_BF, ParamsSelectAporteRescate, ApoResFFMM.class);
		
		EntradaCrearSolicitudAporteRescateNormal 	entradaCrearSolicitudAporteRescateNormal 	= new EntradaCrearSolicitudAporteRescateNormal();
		SalidaConsultarFlujoCustodiaFondoMutuo 		salidaFlujo 								= new SalidaConsultarFlujoCustodiaFondoMutuo();
		
		//LLAMAMOS FLUJO CUSTODIA
		logger.info("flujo custodia");
		ConsultarFlujoCustodiaFondoMutuo 		flujo 			= new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo 	= new EntradaConsultarFlujoCustodiaFondoMutuo();

		entradaFlujo.p_IdEmpresa 		= IdEmpresa;
		entradaFlujo.p_IdUsuario 		= IdUsuario;
		entradaFlujo.p_IdModulo 		= IdModulo;
		entradaFlujo.p_IdPais 			= IdPais;
		entradaFlujo.p_FechaSaldo 		= fechaSistema;
		entradaFlujo.p_DiasProyeccion 	= 0;
		
		for (ApoResFFMM apor : _AporteRescate) {
			ConsultarFlujoCustodiaFondoMutuo.InCarteras 	cartera 	= new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
			ConsultarFlujoCustodiaFondoMutuo.InInstrumento 	instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();

			cartera.IdCliente 		= apor.IdCliente;
			cartera.IdPortafolio 	= apor.IdPortafolio;
			
			entradaFlujo.p_Carteras.add(cartera);
			
			//Obtenemos seriesSolicitudes
			params = new HashMap<String,Object>();
			params.put("IdEmpresa", IdEmpresa);
			params.put("IdSerieFondo", apor.IdSerieFondo);
			
			List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);

			for (SerieFondoSolicitudes serie : seriesSolicitudes) {
				instrumento.IdInstrumento 		= serie.IdInstrumento;
				instrumento.IdRegimenFondoMutuo = "BF";
				
				entradaFlujo.p_Instrumentos.add(instrumento);
			}
		}
		
		salidaFlujo = flujo.logica(con, entradaFlujo);		
		
		for (ApoResFFMM apor : _AporteRescate) {
			InSolicitudesAporteRescate AporteRescateFFMM = new InSolicitudesAporteRescate();

			AporteRescateFFMM.IdInterno = contadorFFMM;
			AporteRescateFFMM.IdCliente=apor.IdCliente;
			AporteRescateFFMM.IdPortafolio=apor.IdPortafolio;
			AporteRescateFFMM.TipoSolicitud=apor.TipoSolicitud;
			AporteRescateFFMM.IdSerieFondo=apor.IdSerieFondo;
			AporteRescateFFMM.CalculoPorMontoCuota=apor.CalculoPorMontoCuota;
			AporteRescateFFMM.EsPorMontoLiquido=apor.EsPorMontoLiquido;
			
			
			
			if(_c.CalculoPorMonto.equals(AporteRescateFFMM.CalculoPorMontoCuota)) {
				AporteRescateFFMM.Monto 		= (apor.Monto.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100)));
				AporteRescateFFMM.NumeroCuotas	= BigDecimal.ZERO;
				
			}else if (_c.CalculoPorCuota.equals(AporteRescateFFMM.CalculoPorMontoCuota)){
				AporteRescateFFMM.Monto			= BigDecimal.ZERO;
				AporteRescateFFMM.NumeroCuotas	=(apor.NumeroCuotas.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100)));
			
			}else {
				AporteRescateFFMM.Monto			=(apor.Monto.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100)));
				AporteRescateFFMM.NumeroCuotas	=(apor.NumeroCuotas.multiply(BigDecimal.valueOf(PorcentajeBonificacionFiscalString)).divide(BigDecimal.valueOf(100)));
			}
			
			if("RESNORM".equalsIgnoreCase(apor.IdTipoMovimientoFondoMutuo)) {
				List<ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado> flujoCustodia = salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO.parallelStream().filter(custodia->apor.IdPortafolio.equals(custodia.IdPortafolio) && (apor.IdSerieFondo.equals(custodia.Nemotecnico))).collect(Collectors.toList());	
				
				for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoC : flujoCustodia) {
					
					if(flujoC.CantidadDisponible.compareTo(AporteRescateFFMM.NumeroCuotas) == -1) {
						AporteRescateFFMM.NumeroCuotas 			= flujoC.CantidadDisponible;
						AporteRescateFFMM.CalculoPorMontoCuota 	= _c.CalculoPorCuota;
					}else if ("T".equals(AporteRescateFFMM.CalculoPorMontoCuota)) {
						AporteRescateFFMM.NumeroCuotas 			= flujoC.CantidadDisponible;
						AporteRescateFFMM.CalculoPorMontoCuota 	= "T";
					}
				}
				
				AporteRescateFFMM.IdMonedaPago  = apor.IdMonedaPago;
				AporteRescateFFMM.IdMedioDePago = apor.IdMedioDePago;
			} else {
				AporteRescateFFMM.IdMedioDePago=_c.CuentaInversion;
			}
			AporteRescateFFMM.IdPreferenciaPagoPortafolio	= 0L;
			AporteRescateFFMM.IdMoneda						= apor.IdMoneda;
			AporteRescateFFMM.Visado						= apor.Visado;
			AporteRescateFFMM.SolicitudBatch				= apor.SolicitudBatch;
			AporteRescateFFMM.OrigenOperacion				= apor.OrigenOperacion;
			AporteRescateFFMM.FechaOperacion				= "0001-01-01";
			AporteRescateFFMM.IdRegimenTributario			= "BF";
			AporteRescateFFMM.IndicadorMLT					= "N";
			AporteRescateFFMM.IdSerieFondoDestino			= "";
			AporteRescateFFMM.IdEmisorDestino				= "";
			AporteRescateFFMM.IdAutorizadoOrigen			= 0L;
			AporteRescateFFMM.Porcentaje					= BigDecimal.valueOf(0);
			AporteRescateFFMM.FolioContrato					= apor.FolioContrato;
			AporteRescateFFMM.EsPorMontoLiquido 			= "N";
			AporteRescateFFMM.IngresoAutomatico 			= _c.SI;
			AporteRescateFFMM.TipoContratoOperacion 		= apor.TipoContratoOperacion;
			AporteRescateFFMM.FolioContratoOperacion		= apor.FolioContratoOperacion;
			AporteRescateFFMM.IdSolicitudAporteRescateRelacionado = apor.IdSolicitudAporteRescate;
			AporteRescateFFMM.Glosa 						= apor.Glosa;
			contadorFFMM++;
			
			logger.info("DISTRIBUCION PARA EL FONDO : "+apor.IdSerieFondo);
			
			entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(AporteRescateFFMM);
		}
		
		entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa 	= IdEmpresa;
		entradaCrearSolicitudAporteRescateNormal.p_IdUsuario 	= IdUsuario;
		entradaCrearSolicitudAporteRescateNormal.p_IdModulo 	= "APV";
		entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = "N";
		entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = "S";
		
		CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
		
		if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0) 
		{
			SalidaCrearSolicitudAporteRescateNormal rpt = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
			
			logger.info("FIN APORTE");
	
			List<ResultadoSolicitud> resultadoFFMM = rpt.P_OUT_RESULTADO;
	
			for(ResultadoSolicitud filaResultadoFFMM : resultadoFFMM){
				logger.info("RESULTADO FFMM : "+filaResultadoFFMM.IdSolicitudAporteRescate);
			}
			
			for(MensajeSalida mje : rpt.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mje.tipo)) {
					AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS FFMM : " + mje.descripcion,true);
					
				}else if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
					AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ERRORES FFMM : "+ mje.descripcion,true);
				}
			}
		}else{
			AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"NO CUENTA CON PORTAFOLIO BF",true);
		}
		
		logger.info("FIN JOB");
		
		con.commit(); 
		return true;
	}
	
	//DEBUG
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
/*
    			List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
    			AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
    			paramIdEmpresa.NombreParametro 	= "IdEmpresa";
    			paramIdEmpresa.Valor			= "1";
    			paramsParametros.add(paramIdEmpresa);
    			
    			String FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
    			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
    			paramsIn.p_IdEmpresa		= 1L;
    			paramsIn.p_IdModulo			= "APV";
    			paramsIn.p_IdUsuario		= 23L;
    			paramsIn.p_IdTipoPrograma	= "INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL";
    			paramsIn.p_Segundos			= 0L;
    			paramsIn.p_FechaSistema		= FechaSistema; 
    			paramsIn.p_Parametros		= paramsParametros;
    			AgregarPrograma AP = new AgregarPrograma();
    			AgregarPrograma.OutAgregarPrograma result = AP.logica(con, paramsIn);
            	
            	*/
            	IngresarOrdenFMBonificacionFiscal demo=new IngresarOrdenFMBonificacionFiscal();
            	demo.logica(con,true);
                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
