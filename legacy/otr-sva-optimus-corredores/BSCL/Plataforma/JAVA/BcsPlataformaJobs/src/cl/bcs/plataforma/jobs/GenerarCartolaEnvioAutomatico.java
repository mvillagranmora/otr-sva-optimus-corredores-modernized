package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.normativa.cartola.GestionarNomina;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaEntrada;
import cl.bcs.normativa.cartola.GestionarNomina.GestionarNominaSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarCartolaEnvioAutomatico implements BcsProcesoPauta {
	private final Log log = new Log("PLA.GenerarCartolaEnvioAutomatico");
	
	FileLoader loader = new FileLoader();
	
    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
    	log.write("***** Inicio GenerarCartolaEnvioAutomatico *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
	        String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			ObtenerDiaSiguiente dsig = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.InObtenerDiaSiguiente dsigin = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
			dsigin.p_soloCaeDiaHabil = "S";
			dsigin.p_diaHabil = "S";
			dsigin.p_idPais = empresa.IdPais;
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			String fechaGenerarCartola = "";
			String DiaGenera = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"NORMATIVA","INDICADIAMESAGENERARCARTOLA");
			
			// Obtiene dia a generar cartola y periodo a generar
			String Periodo = "";
			ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn itemFec = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
			if (DiaGenera.equals("DECIMO")) {
				itemFec.fechaInicial = confFechaSistema.substring(0,8) + "01";
				itemFec.numeroDias = 10L;
				dsigin.p_fechas.add(itemFec);
				ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, dsigin);
				fechaGenerarCartola = dsigout.p_out_fechas.get(0).fechaCalculada;
				
				Periodo = ObtenerPeriodoAnterior(confFechaSistema);
			}
			else if (DiaGenera.equals("QUINTO")) {
				itemFec.fechaInicial = confFechaSistema.substring(0,8) + "01";
				itemFec.numeroDias = 5L;
				dsigin.p_fechas.add(itemFec);
				ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, dsigin);
				fechaGenerarCartola = dsigout.p_out_fechas.get(0).fechaCalculada;
				
				Periodo = ObtenerPeriodoAnterior(confFechaSistema);
			}
			else if (DiaGenera.equals("ULTIMO")) {
				Calendar cal = Calendar.getInstance();
				String fechaInicial = confFechaSistema.substring(0,8) + "01";
				cal.setTime(Fecha.toDate(fechaInicial));
				
				int ultimoDia = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				boolean EsDiaHabil = false;
				while (!EsDiaHabil) {
					ultimoDia--;
					itemFec.fechaInicial = fechaInicial;
					itemFec.numeroDias = (long) ultimoDia;
					dsigin.p_fechas.add(itemFec);
					ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, dsigin);
					fechaGenerarCartola = dsigout.p_out_fechas.get(0).fechaCalculada;
					
					cal.setTime(Fecha.toDate(fechaInicial));
					int mesRef = cal.get(Calendar.MONTH);
					
					cal.setTime(Fecha.toDate(fechaGenerarCartola));
					int mesRes = cal.get(Calendar.MONTH);
					
					EsDiaHabil = !(mesRef < mesRes);
				}
				
				Periodo = confFechaSistema.substring(0,7);
			}
			else if (DiaGenera.equals("PRIMERO")) {  
				Calendar cal = Calendar.getInstance();
				String fechaInicial = confFechaSistema.substring(0,8) + "01";
				cal.setTime(Fecha.substractDays(Fecha.toDate(fechaInicial), 1));
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String fechaAnterior = dateFormat.format(cal.getTime());
				
				itemFec.fechaInicial = fechaAnterior;
				itemFec.numeroDias = 1L;
				dsigin.p_fechas.add(itemFec);
				ObtenerDiaSiguiente.OutObtenerDiaSiguiente dsigout = dsig.logica(con, dsigin);
				fechaGenerarCartola = dsigout.p_out_fechas.get(0).fechaCalculada;
				
				Periodo = fechaAnterior.substring(0,7);
			}
			else {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJE = "Ha ocurrido un error con el valor del parametro INDICADIAMESAGENERARCARTOLA, Valor actual: "+DiaGenera;
				log.write("***** Error GenerarCartolaEnvioAutomatico *****");
				return salida;
			}
			
			// Generar Cartola
			GestionarNominaEntrada nomEnt = new GestionarNominaEntrada();
			GestionarNominaSalida nomSal = new GestionarNominaSalida();
			if (fechaGenerarCartola.equals(confFechaSistema)) {
				nomEnt.p_IdEmpresa = empresa.IdEmpresa;
				nomEnt.p_IdUsuario = 0L;
				nomEnt.p_IdModulo = "CAR";
				nomEnt.p_AccionNomina = "INGRESAR";
				nomEnt.p_TipoProceso = "NORMAL";
				nomEnt.p_Periodo = Periodo;
				nomEnt.p_FechaDesde = null;
				nomEnt.p_FechaHasta = null;
				nomEnt.p_SegundosEsperaProceso = 0L;
				nomEnt.p_Comentario = "";
				nomEnt.p_IncluirPortafoliosConSaldo = "S";
				nomEnt.p_IncluirPortafoliosConCustodia = "S";
				nomEnt.p_IncluirPortafoliosConMovimientoCuentaInversion = "S";
				nomEnt.p_IncluirCategoriaInversionista = "N";
				nomEnt.p_CategoriaInversionista = "";
				nomEnt.p_IncluirTipoCliente = "N";
				nomEnt.p_TipoCliente = "";
				nomEnt.p_IncluirTipoPersona = "N";
				nomEnt.p_TipoPersona = "";
				nomEnt.p_IncluirPortafoliosExcluidos = "N";
				nomEnt.p_EnvioAutomatico = "S";
				nomEnt.p_ExcluirPortafoliosIncluidosOtraNomina = "N";
				nomEnt.p_ExcluirOperadoresDirectos = "N";
				nomEnt.p_OpcionProceso = "NORMATIVO";
				nomEnt.p_CartolaConsolidada = "n";
				nomEnt.p_DiaEjecucion = fechaGenerarCartola;
				
				GestionarNomina ingNom = new GestionarNomina();
				nomSal = ingNom.logica(con,nomEnt);
				
				if (nomSal.P_OUT_ERRORESCONTADOR>0) {
					salida.P_OUT_ERRORESCONTADOR = nomSal.P_OUT_ERRORESCONTADOR;
					salida.P_OUT_MENSAJES = nomSal.P_OUT_MENSAJES;
					salida.P_OUT_MENSAJE = "Ha ocurrido un error al ingresar nomina";
					
					log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
					log.write("Salida: ");
					log.write(salida.P_OUT_MENSAJE);
					log.write("Detalles: ");
					log.write(salida.P_OUT_MENSAJES);
				} else {
					salida.P_OUT_MENSAJE = "Proceso de generacion de cartola realizado con exito.";
				}
			} else {
				salida.P_OUT_ERRORESCONTADOR = 0L;
				salida.P_OUT_MENSAJE = " ";
			}
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error GenerarCartolaEnvioAutomatico *****");
    		throw e;
    	}
    	log.write("***** Fin GenerarCartolaEnvioAutomatico *****");
		return salida;
    }
    
    private String ObtenerPeriodoAnterior (String fecha) {
		Calendar cal = Calendar.getInstance();
		String fechaInicial = fecha.substring(0,8) + "01";
		cal.setTime(Fecha.substractDays(Fecha.toDate(fechaInicial), 1));
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaAnterior = dateFormat.format(cal.getTime());
		
    	return fechaAnterior.substring(0,7);
    }
    
    public static void main(String[] args) throws Exception {
    	try (Connection con = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")){
    		HashMap<String,Object> params = new HashMap<String,Object>();
    		params.put("IdEmpresa", 1L);
    		GenerarCartolaEnvioAutomatico gencar = new GenerarCartolaEnvioAutomatico();
    		
    		try {
				SalidaProcesoPauta out = gencar.transaction(con,params);
				con.rollback();
			} catch (Throwable e) {
				e.printStackTrace();
				con.rollback();
			}
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}