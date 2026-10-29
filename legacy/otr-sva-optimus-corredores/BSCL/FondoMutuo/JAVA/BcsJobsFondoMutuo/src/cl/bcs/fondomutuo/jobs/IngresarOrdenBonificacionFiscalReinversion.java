package cl.bcs.fondomutuo.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.APV.ProcesarDistribucion;
import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.EntradaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesRescate;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.SalidaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ReinversionSerieFondoDestino;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.bat.AgregarLogPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
//import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngresarOrdenBonificacionFiscalReinversion extends BcsJob implements JavaCall{
	//private final Log log = new Log("APV.IngresarOrdenFMBonificacionFiscal");
	static final Logger logger = LogManager.getLogger(ProcesarDistribucion.class);
	
	private final String IdTipoPrograma = "INGRESAR_BONIFICACION_FISCAL_REINVERSION";
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_FFMM_BF = loadResourceToString("cl/bcs/fondomutuo/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectFFMMBF.sql");
	private final String SQL_SELECT_PARAMETROS = loadResourceToString("cl/bcs/fondomutuo/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectParametros.sql");
	private final String SQL_SELECT_REINVERSION_DESTINO	= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectReinversionSerieFondoDestino.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/jobs/sql/IngresarOrdenFMBonificacionFiscal_SelectSerieFondo.sql");
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
	}

	@Override
	public void onError(Throwable t) {
		logger.info("****** ERROR  *******");
        //log.write("****** ERROR  *******");
		logger.info(t);
        //log.write(t);
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
		public Long IdSolicitudAporteRescate;
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
		public Long IdPortafolioOrigen;
		public BigDecimal PorcentajeCalculado;
		
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
		Long contadorFFMM = 1L;
		Long contadorFF = 1L;
		Long idAgrupador = 1L;

		HashMap<String,Object> ParamsSelectAporteRescate = new HashMap<String,Object>();
		ArrayList<Long> IdsAporteRescateABonificar = new ArrayList<Long>();
        if (debug) {
        	IdPrograma = 1438514L;
            IdEmpresa = 1L;
            IdsAporteRescateABonificar.add(17405965L);
            IdsAporteRescateABonificar.add(17405964L);
            IdsAporteRescateABonificar.add(17405966L);
            IdUsuario = 0L;
        } else {
			IdPrograma = this.getJobId();
			HashMap<String,Object> ParamsSelectParametrosPrograma = new HashMap<String,Object>();
			ParamsSelectParametrosPrograma.put("IdPrograma", IdPrograma);
			List<ParametrosPrograma> _ParametrosPrograma = sql.query(con, SQL_SELECT_PARAMETROS, ParamsSelectParametrosPrograma, ParametrosPrograma.class);
			for(ParametrosPrograma parametro : _ParametrosPrograma){
				if(parametro.NombreParametro.equals("IdEmpresa")) IdEmpresa = Long.parseLong(parametro.Valor);
				if(parametro.NombreParametro.equals("IdUsuario")) IdUsuario = Long.parseLong(parametro.Valor);
				if(parametro.NombreParametro.equals("IdSolicitudAporteRescate")) {
					String[] idsSolicitudAporteRescate = parametro.Valor.split(";");
					for (String id : idsSolicitudAporteRescate) {
						IdsAporteRescateABonificar.add(Long.parseLong(id));
			        }
				}
			}
        }
        ParamsSelectAporteRescate.put("IdsAporteRescate", IdsAporteRescateABonificar);
		List<ApoResFFMM> _AporteRescate = sql.query(con, SQL_SELECT_FFMM_BF, ParamsSelectAporteRescate, ApoResFFMM.class);
		
		if (_AporteRescate.size()>0){
			
			Entrada_ConsultaCustodiaPortafolio Entrada_ConsultaCustodiaPortafolio = new Entrada_ConsultaCustodiaPortafolio();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Entrada_ConsultaCustodiaPortafolio.p_IdEmpresa = IdEmpresa;
			Entrada_ConsultaCustodiaPortafolio.p_IdUsuario = IdUsuario;
			Entrada_ConsultaCustodiaPortafolio.p_IdModulo = "FMM";
			Entrada_ConsultaCustodiaPortafolio.p_AgrupaPorBoveda = "S";
			Entrada_ConsultaCustodiaPortafolio.p_IdCliente = _AporteRescate.get(0).IdCliente;
			Entrada_ConsultaCustodiaPortafolio.p_IdPortafolio = null;
			Entrada_ConsultaCustodiaPortafolio.p_IdProductoAgrupador = null;
			Entrada_ConsultaCustodiaPortafolio.p_IdInstrumento = null;
			Entrada_ConsultaCustodiaPortafolio.p_FechaConsulta = fechaSistema;
			
			EntradaCrearSolicitudRescateReinversion entradaCrearSolicitudRescateReinversion = new EntradaCrearSolicitudRescateReinversion();
			entradaCrearSolicitudRescateReinversion.p_IdEmpresa = IdEmpresa;
			entradaCrearSolicitudRescateReinversion.p_IdUsuario = IdUsuario;
			entradaCrearSolicitudRescateReinversion.p_IdModulo = "FMM";
			entradaCrearSolicitudRescateReinversion.p_SimularOperacion = "N";
			entradaCrearSolicitudRescateReinversion.p_UsaReglasNegocio = "S";
			
			ConsultarCustodiaPortafolio ConsultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
			Salida_ConsultaCustodiaPortafolio salida = ConsultarCustodiaPortafolio.logica(con, Entrada_ConsultaCustodiaPortafolio);
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdSolicitudAporteRescate", IdsAporteRescateABonificar);
			
			logger.debug("INICIO::SQL_SELECT_REINVERSION_DESTINO");
			List<ReinversionSerieFondoDestino> reinversionSerieFondoDestino = sql.query(con, SQL_SELECT_REINVERSION_DESTINO, params, ReinversionSerieFondoDestino.class);
			logger.debug("FIN::SQL_SELECT_REINVERSION_DESTINO");
			
			List<String> series = _AporteRescate.stream()
												.map(
													s -> s.IdSerieFondo)
												.distinct()
												.collect(Collectors.toList());
			
			params.clear();
			params.put("IdEmpresa", IdEmpresa);
			params.put("IdSerieFondo", series);
			logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
			List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
			logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
			HashMap<String,Object> hSeries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
			Set<String> hashSolicitudes = new HashSet<>();
					
			for (ApoResFFMM sol : _AporteRescate) {
				if (sol.Porcentaje.compareTo(BigDecimal.ZERO) == 0) {
					for(Tabla_Salida outOrigen : salida.P_OUT_TBL) {
						if(outOrigen.IdPortafolio.compareTo(sol.IdPortafolioOrigen)==0 && outOrigen.Nemotecnico.equals(sol.IdSerieFondo)) {
							sol.PorcentajeCalculado = sol.NumeroCuotas.multiply(BigDecimal.valueOf(100)).divide(outOrigen.CantidadCustodia,2, RoundingMode.HALF_UP);
						}
					}
				}
			}
			
			for(Tabla_Salida out : salida.P_OUT_TBL) {
				
				BigDecimal totalNumeroCuotasRescates = BigDecimal.ZERO;
				
				for (ApoResFFMM sol : _AporteRescate) {				
					if(out.Nemotecnico.equals(sol.IdSerieFondo)&& (out.IdPortafolio.compareTo(sol.IdPortafolio))==0) {
						
						SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hSeries.get(sol.IdSerieFondo);
						int decimalesCuota = serie.DecimalesCuota.intValue();
						
						List<ReinversionSerieFondoDestino> reinversionSerieFondoDestinoFiltrada = reinversionSerieFondoDestino.stream()
																															  .filter(
																																s -> s.IdSolicitudAporteRescate.compareTo(sol.IdSolicitudAporteRescate) == 0) 
																															  .collect(Collectors.toList());
						
						for(ReinversionSerieFondoDestino apor : reinversionSerieFondoDestinoFiltrada){
							
							//Rescate 
							
							InSolicitudesRescate AporteRescateFFMM = new InSolicitudesRescate();
							AporteRescateFFMM.IdCliente = sol.IdCliente;
							AporteRescateFFMM.IdPortafolio = sol.IdPortafolio;
							AporteRescateFFMM.IdSerieFondo = sol.IdSerieFondo;
							AporteRescateFFMM.IdInterno = contadorFFMM;
							AporteRescateFFMM.IdAgrupador = idAgrupador;
							
							if(_c.TipoSolicitudAporte.equals(sol.TipoSolicitud)) {
								AporteRescateFFMM.CalculoPorMontoCuota=_c.CalculoPorMonto;
								AporteRescateFFMM.Monto=(out.MontoMercadoCustodia.multiply(sol.Porcentaje).divide(BigDecimal.valueOf(100)));
								AporteRescateFFMM.NumeroCuotas=out.CantidadCustodia;
							}else if (_c.TipoSolicitudRescate.equals(sol.TipoSolicitud)){
								AporteRescateFFMM.CalculoPorMontoCuota=_c.CalculoPorCuota;
								
								if(sol.Porcentaje.compareTo(BigDecimal.ZERO)==0) {
									
									AporteRescateFFMM.Monto=(out.MontoMercadoCustodia.multiply(sol.PorcentajeCalculado).divide(BigDecimal.valueOf(100)));
									AporteRescateFFMM.NumeroCuotas=(out.CantidadCustodia.multiply(sol.PorcentajeCalculado).divide(BigDecimal.valueOf(100)));
								
								}else {
									AporteRescateFFMM.Monto=(out.MontoMercadoCustodia.multiply(sol.Porcentaje).divide(BigDecimal.valueOf(100)));
									AporteRescateFFMM.NumeroCuotas=(out.CantidadCustodia.multiply(sol.Porcentaje).divide(BigDecimal.valueOf(100)));
									
								}
								
								AporteRescateFFMM.NumeroCuotas=AporteRescateFFMM.NumeroCuotas.setScale(decimalesCuota ,RoundingMode.HALF_DOWN);
								String hashSol = sol.IdCliente + "_" + sol.IdPortafolio + "_" + sol.IdSerieFondo + "_" +  sol.CalculoPorMontoCuota + "_" 
										+ AporteRescateFFMM.Monto + "_" + AporteRescateFFMM.NumeroCuotas + "_" + idAgrupador;
						
								if (hashSolicitudes.add(hashSol)) {
									totalNumeroCuotasRescates = totalNumeroCuotasRescates.add(AporteRescateFFMM.NumeroCuotas);
									
									if (out.CantidadCustodia.compareTo(totalNumeroCuotasRescates) < 0 && (out.CantidadCustodia.subtract(totalNumeroCuotasRescates).abs()).compareTo(BigDecimal.ONE) < 1) {
										AporteRescateFFMM.NumeroCuotas=AporteRescateFFMM.NumeroCuotas.add(out.CantidadCustodia.subtract(totalNumeroCuotasRescates));
									}
					            }
								
							}
							
							AporteRescateFFMM.IdMoneda=sol.IdMoneda;
							AporteRescateFFMM.IdMedioDePago=_c.CuentaInversion;
							AporteRescateFFMM.SolicitudBatch=sol.SolicitudBatch;
							AporteRescateFFMM.OrigenOperacion=sol.OrigenOperacion;
							AporteRescateFFMM.IdRegimenTributario="BF";
							AporteRescateFFMM.Glosa="";
							AporteRescateFFMM.IdAutorizadoOrigen=0L;
							AporteRescateFFMM.FolioContrato=0L;
							AporteRescateFFMM.Porcentaje=sol.Porcentaje;
							AporteRescateFFMM.IngresoAutomatico = _c.SI;
								
							//Aporte 
							InSolicitudesAporte Aporte = new InSolicitudesAporte();
							Aporte.IdInterno = contadorFF;
							Aporte.IdCliente=sol.IdCliente;
							Aporte.IdPortafolio=sol.IdPortafolio;
							Aporte.IdEmisor=sol.IdEmisorDestino;
							Aporte.IdMoneda=sol.IdMoneda;
							Aporte.IdMedioDePago=_c.CuentaInversion;
							Aporte.IdRegimenTributario="BF";
							Aporte.Glosa="";
							Aporte.IndicadorMLT="N";
						    Aporte.IdSerieFondo= apor.IdSerieFondoDestino;
							Aporte.Porcentaje= apor.Porcentaje;
							Aporte.Monto=(out.MontoMercadoCustodia.multiply(apor.Porcentaje).divide(BigDecimal.valueOf(100)));
							Aporte.NumeroCuotas=(out.CantidadCustodia.multiply(apor.Porcentaje).divide(BigDecimal.valueOf(100)));
							Aporte.FolioContrato=0L;
							Aporte.IdAgrupador = idAgrupador;
							entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.add(AporteRescateFFMM);
							entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.add(Aporte);
							
							contadorFF++;
							contadorFFMM++;
						}
					}

					idAgrupador++;
				}
			}
			
			
			CrearSolicitudRescateReinversion CrearSolicitudRescateReinversion = new CrearSolicitudRescateReinversion();
			
			SalidaCrearSolicitudRescateReinversion rpt = CrearSolicitudRescateReinversion.logica(con, entradaCrearSolicitudRescateReinversion);
			
			logger.info("FIN APORTE");

			//RESULTADOFFM
			List<ResultadoSolicitud> resultadoFFMM = rpt.P_OUT_RESULTADO;

			for(ResultadoSolicitud filaResultadoFFMM : resultadoFFMM){
				logger.info("RESULTADO FFMM : "+filaResultadoFFMM.IdSolicitudAporteRescate);
			}
			
			for(MensajeSalida mje : rpt.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mje.tipo)) {
					//ADVERTENCIASFFM
					AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma,"ADVERTENCIAS FFMM : " + mje.descripcion,true);
				}else if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
					//ERRORESFFM
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
            Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
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
            	IngresarOrdenBonificacionFiscalReinversion demo=new IngresarOrdenBonificacionFiscalReinversion();
            	demo.logica(con,true);
               // con.commit();
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
