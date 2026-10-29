package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.normativa.interfaces.ClienteConfigFactory;
import cl.bcs.normativa.interfaces.GenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.EntradaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazICU.SalidaGenerarInterfazICU;
import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero;
import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero.EntradaGenerarInterfazLavadodeDinero;
import cl.bcs.normativa.interfaces.GenerarInterfazLavadodeDinero.SalidaGenerarInterfazLavadodeDinero;
import cl.bcs.normativa.interfaces.GenerarInterfazP40.DatoRUT;
import cl.bcs.normativa.interfaces.GenerarInterfazP40;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora.EntradaGenerarInterfazP40Aseguradora;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Aseguradora.SalidaGenerarInterfazP40Aseguradora;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.EntradaGenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40Filial.SalidaGenerarInterfazP40Filial;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazSIG;
import cl.bcs.normativa.interfaces.GenerarInterfazSIG.EntradaGenerarInterfazSIG;
import cl.bcs.normativa.interfaces.GenerarInterfazSIG.SalidaGenerarInterfazSIG;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;

public class BcsJobPautaGenerarInterfaces extends BcsJob implements BcsProcesoPauta {

	static final Logger logger = LogManager.getLogger(BcsJobPautaGenerarInterfaces.class); 
	private final String SQL_CONSULTA_PARAMETRO_P40_CORREDORA = "SELECT \"Valor\" FROM \"BSCL.Normativa.BD::NOR.Configuracion\" WHERE \"CodigoParametro\" = 'GENERAR_P40_ASEGURADORA_PAUTA'";
	
	public static class GenerarInterfaces_Entrada{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String		p_FechaEjecucion = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GenerarInterfaces_Salida {
		public List<MensajeSalida>	P_OUT_MENSAJES;	//DETALLES
		public String				P_OUT_MENSAJE;	//RESUMEN
		public Long					P_OUT_ERRORESCONTADOR = 0L;

		public GenerarInterfaces_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	public GenerarInterfaces_Salida GenerarInterfaces(Connection con, GenerarInterfaces_Entrada entradaGeneral) throws InterruptedException, Throwable {
		logger.info("Inicia Generacion Interfaces");
		GenerarInterfaces_Salida salida = new GenerarInterfaces_Salida();
		SQL sql 						= new SQL();
		try {	
			String fechaSistema = entradaGeneral.p_FechaEjecucion;
			if(fechaSistema.isEmpty()) {
				fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,
						entradaGeneral.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			}
			
			String sufijo = Utils.getConfiguracion(con, Utils.getSchema(), entradaGeneral.p_IdEmpresa, "PLA.SufijoArchivos");
			
			
			//////////////////////INTERFACES C40//////////////////////
			logger.debug("Inicia Generacion Interfaces C40");
			GenerarInterfazICU generarInterfazICUC40 = new  GenerarInterfazICU();
			EntradaGenerarInterfazICU entrada = new EntradaGenerarInterfazICU();
			SalidaGenerarInterfazICU   salidac40  	 = new SalidaGenerarInterfazICU();
			
			entrada.p_IdEmpresa			= entradaGeneral.p_IdEmpresa;
			entrada.p_Fecha 			= fechaSistema;
			entrada.p_Ruta 				= Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseInterfaces");
			entrada.p_IdModulo 			= "NOR";
			entrada.p_IdUsuario			= entradaGeneral.p_IdUsuario;
			entrada.p_EsTodos 			= true;
			entrada.p_Llamado 			= "C40-TODOS";
			salidac40 = generarInterfazICUC40.logica(con, entrada);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidac40",salidac40.p_out_ErroresContador.toString(),salidac40.p_out_Comentario));
			
			String zipc40 = "C40"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidac40.p_out_Archivos, entrada.p_Ruta + zipc40);
			
		

			
			//////////////////////INTERFACES C08//////////////////////
			logger.debug("Inicia Generacion Interfaces C08");
			GenerarInterfazICU generarInterfazICUC08 = new  GenerarInterfazICU();
			EntradaGenerarInterfazICU  entradac08    = new EntradaGenerarInterfazICU();
			SalidaGenerarInterfazICU   salidac08  	 = new SalidaGenerarInterfazICU();  
			
			entradac08.p_Llamado 		= "C08-TODOS";
			entradac08.p_EsTodos 		= true;
			entradac08.p_Fecha 			= fechaSistema;
			entradac08.p_Ruta  			= Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseInterfaces");
			entradac08.p_IdEmpresa 		= entradaGeneral.p_IdEmpresa;
			entradac08.p_IdUsuario		= entradaGeneral.p_IdUsuario;
			entradac08.p_IdModulo 		= "NOR";
			salidac08 = generarInterfazICUC08.logica(con, entradac08);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidac08",salidac08.p_out_ErroresContador.toString(),salidac08.p_out_Comentario));
			
			String zipc08 = "CA54"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidac08.p_out_Archivos, entradac08.p_Ruta + zipc08);

			
			
			//////////////////////INTERFAZ DE LAVADO DE DINERO//////////////////////
			logger.debug("Inicia Generacion Interfaces DE LAVADO DE DINERO");
			GenerarInterfazLavadodeDinero lavado = new  GenerarInterfazLavadodeDinero();
			EntradaGenerarInterfazLavadodeDinero entradalavado = new EntradaGenerarInterfazLavadodeDinero();
			SalidaGenerarInterfazLavadodeDinero salidalavado = new SalidaGenerarInterfazLavadodeDinero();
			
			entradalavado.p_Fecha     = fechaSistema;
			entradalavado.p_Ruta      = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseInterfaces");
			entradalavado.p_IdEmpresa = entradaGeneral.p_IdEmpresa;
			salidalavado = lavado.logica(con, entradalavado);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidalavado",salidalavado.p_out_ErroresContador.toString(),salidalavado.p_out_Comentario));
			
			String zipcLavado = "FINDNA"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidalavado.p_out_Archivos, entradalavado.p_Ruta + zipcLavado);
			
			
			
			//////////////////////INTERFAZ P40 SBIF//////////////////////
			logger.debug("Inicia Generacion Interfaces P40 SBIF");
			GenerarInterfazP40SBIF p40SBIF = new  GenerarInterfazP40SBIF();
			EntradaGenerarInterfazP40SBIF entradaP40SBIF = new EntradaGenerarInterfazP40SBIF();
			SalidaGenerarInterfazP40SBIF salidaP40SBIF = new SalidaGenerarInterfazP40SBIF();
			
			ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(GenerarInterfazP40.CODIGO_BANCO);
			EntradaGenerarInterfazP40SBIF entradap40 = new EntradaGenerarInterfazP40SBIF();
			clienteConfig.configurarParametros(entradap40,entradaGeneral.p_IdEmpresa.toString(), entradaGeneral.p_IdUsuario.toString(), Utils.getConfiguracion(con,Utils.getSchema(),entradaGeneral.p_IdEmpresa,"PLA.RutaBaseInterfaces") );
			
			entradaP40SBIF.p_Fecha     					= "";	// Interfaz P40 genera la fecha actual
			entradaP40SBIF.p_IdModulo  					= "NOR";
			entradaP40SBIF.p_IdEmpresa 					= entradap40.p_IdEmpresa;
			entradaP40SBIF.p_IdUsuario 					= entradap40.p_IdUsuario;
			entradaP40SBIF.p_Ruta      					= entradap40.p_Ruta ;
			entradaP40SBIF.p_Banco						= GenerarInterfazP40.CODIGO_BANCO;
			entradaP40SBIF.p_SBIF						= entradap40.p_SBIF;
			entradaP40SBIF.p_Mercado					= "";	// (RF/IF/ACN/FM/"": todos) -> ACN: Acciones Nacionales
			entradaP40SBIF.p_TipoCartera				= entradap40.p_TipoCartera;
			entradaP40SBIF.p_UsaNemoSVS					= entradap40.p_UsaNemoSVS;
			entradaP40SBIF.p_ExcluyeGarantia			= entradap40.p_ExcluyeGarantia;
			entradaP40SBIF.p_ExcluyeSimultanea			= entradap40.p_ExcluyeSimultanea;
			//String codigoDefault = "54";
			salidaP40SBIF = p40SBIF.logica(con, entradaP40SBIF);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidaP40SBIF",salidaP40SBIF.P_OUT_ERRORESCONTADOR.toString(),salidaP40SBIF.P_OUT_COMENTARIO));
			
			String zipp40SBIF = "P40"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidaP40SBIF.P_OUT_ARCHIVOS, entradaP40SBIF.p_Ruta + zipp40SBIF);
			
			
			
			//////////////////////INTERFAZ P40 ASEGURADORA//////////////////////
			logger.debug("Inicia Generacion Interfaces P40 ASEGURADORA");
			 GenerarInterfazP40Aseguradora p40Aseguradora = new GenerarInterfazP40Aseguradora();
			 EntradaGenerarInterfazP40Aseguradora entradap40Aseguradora = new  EntradaGenerarInterfazP40Aseguradora();
			 SalidaGenerarInterfazP40Aseguradora salidap40Aseguradora = new SalidaGenerarInterfazP40Aseguradora();
			 
			entradap40Aseguradora.p_Fecha     					= "";	// Interfaz P40 genera la fecha actual
			entradap40Aseguradora.p_IdModulo  					= "NOR";
			entradap40Aseguradora.p_IdEmpresa 					= entradaGeneral.p_IdEmpresa;
			entradap40Aseguradora.p_IdUsuario 					= entradaGeneral.p_IdUsuario;
			entradap40Aseguradora.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),entradaGeneral.p_IdEmpresa,"PLA.RutaBaseInterfaces");
			entradap40Aseguradora.p_Banco						= GenerarInterfazP40.CODIGO_BANCO;;
			entradap40Aseguradora.p_SBIF						="862";
			entradap40Aseguradora.p_TipoMercado					="";	// Todos los mercados (RF/IF/ACN/FM)
			entradap40Aseguradora.p_TipoCartera					="2";
			entradap40Aseguradora.p_UsaNemoSVS					="N";
			entradap40Aseguradora.p_ExcluyeGarantia				="N";
			entradap40Aseguradora.p_ExcluyeSimultanea			="N";
			entradap40Aseguradora.p_UsaCustodia					="N";
			entradap40Aseguradora.p_FamiliaInstrumento			="49";
			salidap40Aseguradora = p40Aseguradora.logica(con, entradap40Aseguradora);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidap40Aseguradora",salidap40Aseguradora.P_OUT_ERRORESCONTADOR.toString(),salidap40Aseguradora.P_OUT_COMENTARIO));
			
			String zipp40Aseguradora = "SP40"+sufijo+".zip";
			
			String valor = "S";
			try (Statement st = con.createStatement()) {
			    try (ResultSet rs = st.executeQuery(SQL_CONSULTA_PARAMETRO_P40_CORREDORA)) {
			        if (rs.next() && rs.getString("Valor") != null) {
			            valor = rs.getString("Valor");
			        }
			    }
			} 
			if ("S".equalsIgnoreCase(valor)) {
				ZipUtil.generateMultipleZip(salidap40Aseguradora.P_OUT_ARCHIVOS, entradap40Aseguradora.p_Ruta + zipp40Aseguradora);
			}
			
			
			
			
			//////////////////////INTERFAZ P40 FILIAL//////////////////////		
			logger.debug("Inicia Generacion Interfaces P40 FILIAL");
			GenerarInterfazP40Filial p40Filial = new  GenerarInterfazP40Filial();
			EntradaGenerarInterfazP40Filial entradaP40Filial = new EntradaGenerarInterfazP40Filial();
			SalidaGenerarInterfazP40Filial salidaP40Filial = new SalidaGenerarInterfazP40Filial();
			
			// Parametros de entrada (el resto se asigna por defecto en el constructor de la clase)
			entradaP40Filial.p_IdEmpresa	= entradaGeneral.p_IdEmpresa;
			entradaP40Filial.p_IdUsuario 	= entradaGeneral.p_IdUsuario;	
			entradaP40Filial.p_Ruta      	= Utils.getConfiguracion(con,Utils.getSchema(),entradaGeneral.p_IdEmpresa,"PLA.RutaBaseInterfaces");
			salidaP40Filial = p40Filial.logica(con, entradaP40Filial);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidaP40Filial",salidaP40Filial.P_OUT_ERRORESCONTADOR.toString(),salidaP40Filial.P_OUT_COMENTARIO));
			
			String zipp40Filial = "SVP40"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidaP40Filial.P_OUT_ARCHIVOS, entradaP40Filial.p_Ruta + zipp40Filial);
			
			
			
			//////////////////////INTERFAZ SIG ///////////////////////////////
			logger.debug("Inicia Generacion Interfaces SIG");
			GenerarInterfazSIG sig = new GenerarInterfazSIG();
			EntradaGenerarInterfazSIG entradaSIG = new EntradaGenerarInterfazSIG();
			SalidaGenerarInterfazSIG salidaSIG = new SalidaGenerarInterfazSIG();
			
			entradaSIG.p_Fecha     					= "";	// Interfaz SIG genera la fecha actual
			entradaSIG.p_IdModulo  					= "NOR";
			entradaSIG.p_IdEmpresa 					= entradaGeneral.p_IdEmpresa;
			entradaSIG.p_IdUsuario 					= entradaGeneral.p_IdUsuario;
			entradaSIG.p_Ruta      					= Utils.getConfiguracion(con,Utils.getSchema(),entradaGeneral.p_IdEmpresa,"PLA.RutaBaseInterfaces");
			salidaSIG = sig.Logica(con, entradaSIG);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,"salidaSIG",salidaSIG.P_OUT_ERRORESCONTADOR.toString(),salidaSIG.P_OUT_COMENTARIO));
			
			String zipSIG = "SIG"+sufijo+".zip";
			ZipUtil.generateMultipleZip(salidaSIG.P_OUT_ARCHIVOS, entradaSIG.p_Ruta + zipSIG);
			
			
			
			// Validaciones 
			long totalErrores = salidac08.p_out_ErroresContador +
                    salidac40.p_out_ErroresContador +
                    salidalavado.p_out_ErroresContador +
                    salidaP40Filial.P_OUT_ERRORESCONTADOR +
                    salidaP40SBIF.P_OUT_ERRORESCONTADOR +
                    salidaSIG.P_OUT_ERRORESCONTADOR;
			if (totalErrores > 0) {
			    salida.P_OUT_MENSAJE = "Error al generar interfaces";
			} else {
			    salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente";
			}
			salida.P_OUT_ERRORESCONTADOR = totalErrores;
			return salida;
		}catch(Exception e){
			logger.error(e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = e.getMessage();
			return salida;
		}
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			Map<String,Object> params = new HashMap<>();
			BcsJobPautaGenerarInterfaces gi = new BcsJobPautaGenerarInterfaces();
			params.put("IdEmpresa", 1L);
			params.put("IdUsuario", 6408L);
			//params.put("FechaEjecucion", "2025-08-11");
			try {
				gi.transaction(con, params);
//				con.commit();
				con.rollback();
			} catch (Throwable e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	

	}
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters)
			throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaGenerarInterfaces *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			GenerarInterfaces_Entrada entrada = new GenerarInterfaces_Entrada();
			entrada.p_IdEmpresa = (Long) parameters.get("IdEmpresa");
			entrada.p_IdUsuario = (Long) parameters.get("IdUsuario");
			GenerarInterfaces_Salida salidaProceso = GenerarInterfaces(con, entrada);
			
			salida.P_OUT_MENSAJE = salidaProceso.P_OUT_MENSAJE;
			salida.P_OUT_MENSAJES = salidaProceso.P_OUT_MENSAJES;
			salida.P_OUT_ERRORESCONTADOR = salidaProceso.P_OUT_ERRORESCONTADOR;
		} catch (Exception e) {
			logger.error("***** Error Pauta Generacion de interfaces *****", e);
			throw e;
		}
		
		logger.info("***** Fin Pauta Generacion de interfaces *****");
		return salida;

	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		try {
			GenerarInterfaces_Entrada entrada = new GenerarInterfaces_Entrada();
			entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
			entrada.p_FechaEjecucion = this.getParam("FechaEjecucion") == null ? "" : this.getParam("FechaEjecucion");

			logger.debug("BcsJobPautaGenerarInterfaces-Param: " + entrada.toString());
			
			GenerarInterfaces_Salida salidaProceso = GenerarInterfaces(con, entrada);
			
			// Validaciones 
			if (salidaProceso.P_OUT_ERRORESCONTADOR > 0) {
				logger.error(salidaProceso.toString());
				return false;
			} else {
				return true;
			}
		}catch(Exception e){
			logger.error(e);
			return false;
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t);
    }

	@Override
	public void pause() {

	}

}