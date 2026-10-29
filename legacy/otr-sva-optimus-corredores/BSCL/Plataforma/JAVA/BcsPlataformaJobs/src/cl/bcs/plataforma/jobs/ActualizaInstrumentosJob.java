package cl.bcs.plataforma.jobs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.jobs.ActualizaInstrumentosJob.VectorPreciosWebService.Cabecera.ConsultaVectorPrecioPTResult;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
/**
 * Actualizar instrumentos ruebas -> optimus
 * IdPrograma: ACTUALIZAR_INSTRUMENTOS
 * @author chernandez
 *
 */
public class ActualizaInstrumentosJob extends BcsJob implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ActualizaInstrumentosJob.class);
	//Futuros
	private final String SQL_SELECT_TBFUMI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBFUMI.sql");
	private final String SQL_INSERT_FUTUROS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertFuturos.sql");
	private final String SQL_DELETE_FUTUROS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteFuturos.sql");
	private final String SQL_CALL_FUTUROS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallFuturos.sql");
	//Acciones
	private final String SQL_SELECT_TBACMI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBACMI.sql");
	private final String SQL_INSERT_ACCIONES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertAcciones.sql");
	private final String SQL_DELETE_ACCIONES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteAcciones.sql");
	private final String SQL_CALL_ACCIONES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallAcciones.sql");
	//Cuotas fondo
	private final String SQL_SELECT_TBCFMI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBCFMI.sql");
	private final String SQL_INSERT_CUOTAS_FONDO = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertCuotasFondo.sql");
	private final String SQL_DELETE_CUOTAS_FONDO = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteCuotasFondo.sql");
	private final String SQL_CALL_CUOTAS_FONDO = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallCuotasFondo.sql");
	//Monetarios
	private final String SQL_SELECT_TBMOMI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBMOMI.sql");
	private final String SQL_INSERT_MONETARIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertMonetarios.sql");
	private final String SQL_DELETE_MONETARIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteMonetarios.sql");
	private final String SQL_CALL_MONETARIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallMonetarios.sql");
	//Renta fija
	private final String SQL_SELECT_TBRFMI= loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBRFMI.sql");
	private final String SQL_INSERT_RENTA_FIJA= loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertRentaFija.sql");
	private final String SQL_DELETE_RENTA_FIJA = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteRentaFija.sql");
	private final String SQL_CALL_RENTA_FIJA = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallRentaFija.sql");
	//Emisores
	private final String SQL_SELECT_TBUTEMI = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectTBUTEMI.sql");
	private final String SQL_INSERT_EMISORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertEmisores.sql");
	private final String SQL_DELETE_EMISORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteEmisores.sql");
	private final String SQL_CALL_EMISORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallEmisores.sql");
	//Emisores
	private final String SQL_SELECT_VECTOR_PRECIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_SelectVectorPrecios.sql");
	private final String SQL_INSERT_VECTOR_PRECIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_InsertVectorPrecios.sql");
	private final String SQL_DELETE_VECTOR_PRECIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_DeleteVectorPrecios.sql");
	private final String SQL_CALL_VECTOR_PRECIOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarInstrumentosJob_CallVectorPrecios.sql");
			
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection optimusConn,boolean debug) throws InterruptedException, Throwable {
		logger.info("Inicio job");
		Long idEmpresa = (debug) ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
		
		Properties prop = new Properties();
        try(InputStream input = new FileInputStream("ruebas.properties");){
        	prop.load(input);	
        }

        String DBdriver=prop.getProperty("DBdriver");
        String DBuser=prop.getProperty("DBuser");
        String DBpass=prop.getProperty("DBpass");
        String DBurl=prop.getProperty("DBurl");
        String DBprop=prop.getProperty("DBprop");
        
            
        BasicDataSource dsTarget = new BasicDataSource();
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            logger.debug("Conectado a RUEBAS");
            
            try {
            	logger.debug("Procesando Futuros");
        		procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBFUMI, SQL_DELETE_FUTUROS ,SQL_INSERT_FUTUROS, SQL_CALL_FUTUROS, RegistroTBFUMI.class, idEmpresa);
        		optimusConn.commit();
            }
            catch(Exception e) {
            	optimusConn.rollback();
            }
            
            try {
            	logger.debug("Procesando Acciones");
            	procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBACMI, SQL_DELETE_ACCIONES, SQL_INSERT_ACCIONES, SQL_CALL_ACCIONES, RegistroTBACMI.class, idEmpresa);
            	optimusConn.commit();
            }
            catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	try {
        		logger.debug("Procesando Cuotas fondo");
        		procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBCFMI, SQL_DELETE_CUOTAS_FONDO, SQL_INSERT_CUOTAS_FONDO, SQL_CALL_CUOTAS_FONDO, RegistroTBCFMI.class, idEmpresa);
        		optimusConn.commit();
        	}
        	catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	try {
        		logger.debug("Procesando Monetarios");
        		procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBMOMI, SQL_DELETE_MONETARIOS, SQL_INSERT_MONETARIOS, SQL_CALL_MONETARIOS, RegistroTBMOMI.class, idEmpresa);
        		optimusConn.commit();
        	}
        	catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	try {
        		logger.debug("Procesando Renta fija");
        		procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBRFMI, SQL_DELETE_RENTA_FIJA, SQL_INSERT_RENTA_FIJA, SQL_CALL_RENTA_FIJA, RegistroTBRFMI.class,idEmpresa);
        		optimusConn.commit();
        	}
        	catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	try {
        		logger.debug("Procesando Emisores");
        		procesarETLInstrumento(dsTarget.getConnection(), optimusConn, SQL_SELECT_TBUTEMI, SQL_DELETE_EMISORES, SQL_INSERT_EMISORES, SQL_CALL_EMISORES, RegistroTBUTEMI.class, idEmpresa);
        		optimusConn.commit();
        	}
        	catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	try {
        		logger.debug("Procesando Vector Precios");
        		procesarETLInstrumento(null, optimusConn, SQL_SELECT_VECTOR_PRECIOS, SQL_DELETE_VECTOR_PRECIOS, SQL_INSERT_VECTOR_PRECIOS, SQL_CALL_VECTOR_PRECIOS, RegistroVectorPrecios.class, idEmpresa);
        		optimusConn.commit();
        	}
        	catch(Exception e) {
            	optimusConn.rollback();
            }
        	
        	
    	}catch(Exception e){
        	logger.error(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }finally {
			if(dsTarget!=null && !dsTarget.isClosed()) {
				dsTarget.close();
			}
		}
    	logger.info("Fin job");
        return true;
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void procesarETLInstrumento(Connection ruebasConn, Connection optimusConn, String selectRuebas,String deleteOptimus, String insertOptimus, String callOptimus, Class claseResultadoOptimus, Long idEmpresa) throws Exception {
		SQL sql = new SQL();
		List<?> resultadosQueryRuebas = new ArrayList();
		if (ruebasConn != null) {
			//traer data desde ruebas
			resultadosQueryRuebas = sql.query(ruebasConn, selectRuebas, new HashMap<>(), claseResultadoOptimus);
		}else {
			
			resultadosQueryRuebas = obtenerVector(optimusConn, idEmpresa);
		}
    	//limpiar tabla en optimus
    	sql.batch(optimusConn, deleteOptimus , new ArrayList<>(), claseResultadoOptimus);
    	//poblar tabla en optimus
    	sql.batch(optimusConn, insertOptimus , resultadosQueryRuebas, claseResultadoOptimus);
    	HashMap<String, Object> paramsIn = new HashMap<String, Object>();
    	paramsIn.put("P_LIMPIATABLAS", "S");
    	HashMap<String, Class> paramsOut = new HashMap<String, Class>();
    	paramsOut.put("p_out_Resultado",String.class);
    	paramsOut.put("p_out_Mensaje",String.class);
    	//llamar al sp
    	HashMap<String, Object> salida = sql.call(optimusConn, callOptimus, paramsIn, paramsOut);
    	logger.debug(salida.get("p_out_Mensaje"));
    	logger.debug(salida.get("p_out_Resultado"));
	}
	
	public List<RegistroVectorPrecios> obtenerVector(Connection optimusConn, Long idEmpresa) throws Exception{
		List<RegistroVectorPrecios> listVectorPrecios = new ArrayList<RegistroVectorPrecios>();
		//obtener configuraciones
			HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(optimusConn, idEmpresa);
			String servidor =  configuraciones.get("PLATAFORMA").get("MER.ServidorTec");
			String urlWebservice = configuraciones.get("PLATAFORMA").get("MER.UrlServicioPrecios");	
			String user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTec");
			String passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTec");   
			boolean auth = true;
			
			JsonArray entradaWebservice = new JsonArray();
			JsonObject entradaWS = new JsonObject();
			entradaWS.addProperty("nemo", "");        			
			entradaWebservice.add(entradaWS);		
			
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, false);
			String salidaWebservice = call.callRestPost(entradaWebservice.toString());
			List<VectorPreciosWebService> vectorPreciosWebservice = Arrays.asList(Utils.getGsonBuilder().fromJson(salidaWebservice, VectorPreciosWebService[].class));
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(optimusConn,idEmpresa);
			Timestamp vFechaHoraSistema = Fecha.toTimeStamp(fechaHoraSistema);
			
			List<ConsultaVectorPrecioPTResult> listaWS = vectorPreciosWebservice.get(0).getCabecera().getConsultaVectorPrecioPTResult();
			
			if (listaWS != null && !listaWS.isEmpty()) {
			List<RegistroVectorPrecios> listaPrecios = vectorPreciosWebservice.get(0).getCabecera().getConsultaVectorPrecioPTResult().parallelStream().map(elementoLista -> {				
                RegistroVectorPrecios registroVectorPrecios = new RegistroVectorPrecios();
                    registroVectorPrecios.FECHA = vFechaHoraSistema;
                    registroVectorPrecios.NEMO  = elementoLista.getNemo();
                    registroVectorPrecios.SEC_LISTA = elementoLista.getSecLista();
                    registroVectorPrecios.TIPO= elementoLista.getTipo();
                    registroVectorPrecios.VALOR = elementoLista.valor;
                    registroVectorPrecios.PRECIO_BASE = elementoLista.precioBase;
                    registroVectorPrecios.VALOR_01 = elementoLista.valor01;
                    registroVectorPrecios.VALOR_02 = elementoLista.valor02;
                    registroVectorPrecios.VALOR_03 = elementoLista.valor03;
                    registroVectorPrecios.VALOR_04 = elementoLista.valor04;
                    registroVectorPrecios.VALOR_05 = elementoLista.valor05;
                    registroVectorPrecios.VALOR_95 = elementoLista.valor95;
                    registroVectorPrecios.VALOR_98 = elementoLista.valor98;
                    return registroVectorPrecios;
            }).collect(Collectors.toList());
			listVectorPrecios.addAll(listaPrecios);
			}
			
		return listVectorPrecios;
		
		
		}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String [] args) throws InterruptedException, Throwable {
		
		Connection c = PlataformaUtils.getDESAConnection();
		ActualizaInstrumentosJob a = new ActualizaInstrumentosJob();
		a.logica(c, true);
		c.rollback();
		
	}
	
	public static class RegistroTBFUMI {
		public String NEMO;
		public BigDecimal LOTE_PADRON;
		public String COD_OFI;
		public String COND_COD_DI_AC_001;
		public String COND_COD_DI_AC_002;
		public String COND_COD_DI_AC_003;
		public String COND_COD_DI_AC_004;
		public String COND_COD_DI_AC_005;
		public String COND_COD_DI_AC_006;
		public String COND_COD_DI_AC_007;
		public String COND_COD_DI_AC_008;
		public String COND_COD_DI_AC_009;
		public String COND_COD_DI_AC_010;
		public String COND_COD_OFI_001;
		public String COND_COD_OFI_002;
		public String COND_COD_OFI_003;
		public String COND_COD_OFI_004;
		public String COND_COD_OFI_005;
		public String COND_COD_OFI_006;
		public String COND_COD_OFI_007;
		public String COND_COD_OFI_008;
		public String COND_COD_OFI_009;
		public String COND_COD_OFI_010;
		public String COND_DIS_DI_AN;
		public String COND_DIS_DI_AC;
		public String COND_DIS_OFI;
		public BigDecimal DIAS_AL_VCTO;
		public String ESTADO;
		public String FEC_FIJ_CIE;
		public String FEC_INI_VIG;
		public String FEC_VCTO;
		public String HORA_FIJ_CIE;
		public BigDecimal INT_ABI;
		public BigDecimal MAR_INI;
		public BigDecimal MONTO_ACU_10;
		public BigDecimal MONTO;
		public String RAZON_SOCIAL;
		public BigDecimal NUM_DEC_PRE;
		public BigDecimal NUM_CON;
		public BigDecimal NUM_NEG;
		public BigDecimal NUM_REDON;
		public BigDecimal PTJE_REST_PRE;
		public BigDecimal PRE_CIE_DI_AC;
		public BigDecimal PRE_CIE_DI_AN;
		public BigDecimal PRE_CIE_OFI;
		public BigDecimal PRE_MAY;
		public BigDecimal PRE_MED;
		public BigDecimal PRE_MEN;
		public BigDecimal PRE_PRO_10;
		public BigDecimal TASA_NOMI;
		public String TENDEN_DI_AC;
		public String TIPO_INSTRU;
		public BigDecimal PRE_PEN_DI_AC;
		public BigDecimal PRE_ULT_DI_AC;
		public BigDecimal VAL_ACT;
		public BigDecimal VAR_PRE_CTDO;
		public BigDecimal VAL_CONTRATO_BCS;
		public String CATEGORIA;
		public BigDecimal PRE_APE_DI_AC;
		public String ULT_DIA_NEG;
		public BigDecimal VAL_CONTRATO_UNI;	
	}
	
	public static class RegistroTBACMI{
		public String RV_MERCADO;
		public String RV_NEMO;
		public BigDecimal RV_PRE_PULT_TR;
		public BigDecimal RV_PRE_ULT_TR;
		public BigDecimal RV_CATEGORIA;
		public String RV_IND_CARTERA;
		public String RV_IND_EXCLUIDA;

		public String RV_AUT_VT_CORTO;
		public String RV_TENDEN_VT_CORTO;
		public String RV_IND_LIQ_DCV;
		public String RV_BOLSA = "BCS";
		public BigDecimal RV_MONEDA;
		public String RV_AUT_OD_BLOQUE;
		public String RV_COD_RUBRO;

		public BigDecimal RV_PRE_APE_DI_AC;
		public BigDecimal RV_PRE_MAY_52SEM;
		public BigDecimal RV_PRE_MEN_52SEM;
		public BigDecimal RV_PRE_CIE_7DIAS;
		public BigDecimal RV_PRE_CIE_30DIAS;
		public BigDecimal RV_PRE_CIE_52SEM;

		public BigDecimal RV_RENT_7DIAS;
		public BigDecimal RV_RENT_30DIAS;
		public BigDecimal RV_RENT_52SEM;
		public BigDecimal RV_DIVIDEND_YIELD;
		public BigDecimal RV_BETA_90DIAS;
		public String RV_CODIGO_ISIN;
		public BigDecimal RV_P_IPSA;

		public BigDecimal RV_P_IGPA;
		public BigDecimal RV_P_INTER_10;
		public BigDecimal RV_ACCIONES_EQ;
		public BigDecimal RV_VALOR_EQ;
		public BigDecimal RV_NON_FFLOAT;
		public String RV_SECTOR;
		public BigDecimal RV_PRE_CIE_TEORICO;

		public String RV_BOLSA_EX;
		public String RV_MONEDA_EX;
		public String RV_PAIS_EMISOR;
		public String RV_TIPO_INSTRU;
		public BigDecimal RV_VOL_52SEM;
		public String RV_LISTA;

		public BigDecimal AC_ACC_INSCRIP;
		public String AC_AUT_OPE_SIMUL;
		public String AC_AUT_TRAN_PLAZO;
		public BigDecimal AC_LOTE_PADRON;
		public String AC_CESA_PAGO;
		public String AC_CLASE;
		public String AC_COD_FONOB;

		public String AC_COD_IGPA;
		public String AC_COD_OFI;
		public String AC_COD_OFI_SASA;
		public BigDecimal AC_COLUMNA;
		public String AC_COND_COD_DI_AC_001;
		public String AC_COND_COD_DI_AC_002;
		public String AC_COND_COD_DI_AC_003;

		public String AC_COND_COD_DI_AC_004;
		public String AC_COND_COD_DI_AC_005;
		public String AC_COND_COD_DI_AC_006;
		public String AC_COND_COD_DI_AC_007;
		public String AC_COND_COD_DI_AC_008;

		public String AC_COND_COD_DI_AC_009;
		public String AC_COND_COD_DI_AC_010;
		public String AC_COND_COD_OFI_001;
		public String AC_COND_COD_OFI_002;
		public String AC_COND_COD_OFI_003;
		public String AC_COND_COD_OFI_004;

		public String AC_COND_COD_OFI_005;
		public String AC_COND_COD_OFI_006;
		public String AC_COND_COD_OFI_007;
		public String AC_COND_COD_OFI_008;
		public String AC_COND_COD_OFI_009;
		public String AC_COND_COD_OFI_010;

		public String AC_COND_DIS_AN_AN;
		public String AC_COND_DIS_DI_AN;
		public String AC_COND_DIS_DI_AC;
		public String AC_COND_DIS_ME_AN;
		public String AC_COND_DIS_OFI;
		public String AC_COND_DIS_SE_AN;

		public String AC_COND_DIS_SN_AN;
		public BigDecimal AC_DIAS_TR_ME_AC;
		public BigDecimal AC_DIAS_TR_SE_AC;
		public BigDecimal AC_DIAS_TR_AN_AC;
		public BigDecimal AC_DIAS_TR_TR_AC;
		public String AC_DER_ESP;
		public BigDecimal AC_DIVIDENDOS;

		public String AC_ESTADO;
		public String AC_ESTADTRI;
		public BigDecimal AC_FAC_IGPA;
		public BigDecimal AC_FAC_IGPA_FBASE;
		public BigDecimal AC_FAC_IPSA;
		public String AC_FEC_EST_SIT;
		public String AC_FEC_FIJ_CIE;
		public String AC_FEC_INSCRIP;

		public String AC_FEC_ULT_TR;
		public BigDecimal AC_FILA;
		public String AC_IND_AJU_VC_ULT_TR;
		public String AC_IND_PIZ_ELEC;
		public String AC_IND_SIT_ESP;
		public BigDecimal AC_INVER_ME;
		public String AC_HORA_FIJ_CIE;

		public BigDecimal AC_INVER_SE;
		public BigDecimal AC_INVER_AN;
		public String AC_LIQUIDA;
		public BigDecimal AC_MONTO_AN_AC;
		public BigDecimal AC_MONTO_AN_AN;
		public BigDecimal AC_MONTO_DI_AC;
		public BigDecimal AC_MONTO_DI_AN;
		public BigDecimal AC_MONTO_ME_AC;

		public BigDecimal AC_MONTO_ME_AN;
		public BigDecimal AC_MONTO_PMED_DI_AC;
		public BigDecimal AC_MONTO_PMED_SN_AC;
		public BigDecimal AC_MONTO_PMED_ME_AC;
		public BigDecimal AC_MONTO_PMED_SE_AC;
		public BigDecimal AC_MONTO_PMED_AN_AC;

		public BigDecimal AC_MONTO_PROM_UF_3M;
		public BigDecimal AC_MONTO_SE_AC;
		public BigDecimal AC_MONTO_SE_AN;
		public BigDecimal AC_MONTO_SN_AC;
		public BigDecimal AC_MONTO_SN_AN;
		public BigDecimal AC_NAA;
		public String AC_NEMO_SASA;

		public String AC_RAZON_SOCIAL;
		public BigDecimal AC_NUM_DEC_PRE;
		public BigDecimal AC_NUM_ACC_CIR;
		public BigDecimal AC_NUM_ACC_31DIC;
		public String AC_NUM_MEN_FONOB;
		public BigDecimal AC_NUM_MES_UTI;
		public BigDecimal AC_NUM_NEG_AN_AC;

		public BigDecimal AC_NUM_NEG_AN_AN;
		public BigDecimal AC_NUM_NEG_DI_AC;
		public BigDecimal AC_NUM_NEG_DI_AN;
		public BigDecimal AC_NUM_NEG_ME_AC;
		public BigDecimal AC_NUM_NEG_ME_AN;
		public BigDecimal AC_NUM_NEG_SE_AC;
		public BigDecimal AC_NUM_NEG_SE_AN;

		public BigDecimal AC_NUM_NEG_SN_AC;
		public BigDecimal AC_NUM_NEG_SN_AN;
		public BigDecimal AC_NUM_REDON;
		public BigDecimal AC_PATRI_AJU;
		public BigDecimal AC_PATRI_CON;
		public BigDecimal AC_PATRI_AN_AC;
		public BigDecimal AC_PATRI_AN_AN;
		public BigDecimal AC_PATRI_BASE;

		public BigDecimal AC_PATRI_ME_AC;
		public BigDecimal AC_PATRI_ME_AN;
		public String AC_PATRI_NEG;
		public BigDecimal AC_PATRI_SE_AC;
		public BigDecimal AC_PATRI_SE_AN;
		public BigDecimal AC_PRESEN_AN_AC;
		public BigDecimal AC_PRESEN_AN_AN;
		public BigDecimal AC_PRESEN_ME_AC;

		public BigDecimal AC_PRESEN_ME_AN;
		public BigDecimal AC_PRESEN_SE_AC;
		public BigDecimal AC_PRESEN_SE_AN;
		public BigDecimal AC_PRESEN_PRO_12_M;
		public BigDecimal AC_PRESEN_PRO_1_T;
		public BigDecimal AC_PRESEN_PRO_2_T;
		public BigDecimal AC_PRESEN_PRO_3_T;

		public BigDecimal AC_PRESEN_PRO_4_T;
		public BigDecimal AC_PRE_CIE_AN_AN;
		public BigDecimal AC_PRE_CIE_DI_AC;
		public BigDecimal AC_PRE_CIE_DI_AN;
		public BigDecimal AC_PRE_CIE_ME_AN;
		public BigDecimal AC_PRE_CIE_OFI;
		public BigDecimal AC_PRE_CIE_PEN;

		public BigDecimal AC_PRE_CIE_SE_AN;
		public BigDecimal AC_PRE_CIE_SN_AN;
		public BigDecimal AC_PRE_CIE_BASE;
		public BigDecimal AC_PRE_MAY_AN_AC;
		public BigDecimal AC_PRE_MAY_AN_AN;
		public BigDecimal AC_PRE_MAY_DI_AC;

		public BigDecimal AC_PRE_MAY_DI_AN;
		public BigDecimal AC_PRE_MAY_ME_AC;
		public BigDecimal AC_PRE_MAY_ME_AN;
		public BigDecimal AC_PRE_MAY_SE_AC;
		public BigDecimal AC_PRE_MAY_SE_AN;
		public BigDecimal AC_PRE_MAY_SN_AC;
		public BigDecimal AC_PRE_MAY_SN_AN;

		public BigDecimal AC_PRE_MED_AN_AC;
		public BigDecimal AC_PRE_MED_AN_AN;
		public BigDecimal AC_PRE_MED_DI_AC;
		public BigDecimal AC_PRE_MED_DI_AN;
		public BigDecimal AC_PRE_MED_ME_AC;
		public BigDecimal AC_PRE_MED_ME_AN;
		public BigDecimal AC_PRE_MED_SE_AC;

		public BigDecimal AC_PRE_MED_SE_AN;
		public BigDecimal AC_PRE_MED_SN_AC;
		public BigDecimal AC_PRE_MED_SN_AN;
		public BigDecimal AC_PRE_MED_ULT_TR;
		public BigDecimal AC_PRE_MEN_AN_AC;
		public BigDecimal AC_PRE_MEN_AN_AN;
		public BigDecimal AC_PRE_MEN_DI_AC;

		public BigDecimal AC_PRE_MEN_DI_AN;
		public BigDecimal AC_PRE_MEN_ME_AC;
		public BigDecimal AC_PRE_MEN_ME_AN;
		public BigDecimal AC_PRE_MEN_SE_AC;
		public BigDecimal AC_PRE_MEN_SE_AN;
		public BigDecimal AC_PRE_MEN_SN_AC;
		public BigDecimal AC_PRE_MEN_SN_AN;

		public BigDecimal AC_REL_BOL_LIB;
		public BigDecimal AC_REL_PRE_DIV;
		public BigDecimal AC_REL_PRE_UTI;
		public BigDecimal AC_RENT_AN_AC;
		public BigDecimal AC_RENT_AN_AN;
		public BigDecimal AC_RENT_ME_AC;
		public BigDecimal AC_RENT_ME_AN;
		public BigDecimal AC_RENT_SE_AC;

		public BigDecimal AC_RENT_SE_AN;
		public String AC_TENDEN_DI_AC;
		public String AC_TIPO_ENT;
		public BigDecimal AC_PRE_ULT_DI_AC;
		public BigDecimal AC_UNI_PMED_DI_AC;
		public BigDecimal AC_UNI_PMED_SN_AC;
		public BigDecimal AC_UNI_PMED_ME_AC;

		public BigDecimal AC_UNI_PMED_SE_AC;
		public BigDecimal AC_UNI_PMED_AN_AC;
		public BigDecimal AC_UNI_TRA_AN_AC;
		public BigDecimal AC_UNI_TRA_AN_AN;
		public BigDecimal AC_UNI_TRA_DI_AC;
		public BigDecimal AC_UNI_TRA_DI_AN;
		public BigDecimal AC_UNI_TRA_ME_AC;

		public BigDecimal AC_UNI_TRA_ME_AN;
		public BigDecimal AC_UNI_TRA_SE_AC;
		public BigDecimal AC_UNI_TRA_SE_AN;
		public BigDecimal AC_UNI_TRA_SN_AC;
		public BigDecimal AC_UNI_TRA_SN_AN;
		public BigDecimal AC_UNI_TRA_ULT_TR;
		public BigDecimal AC_UTI_12_M;

		public BigDecimal AC_UTI_NUM_MES;
		public BigDecimal AC_VAR_PRE;

		public String SOC_RUT;

		public String EMI_EMISOR;
		public String EMI_COD_EMI;
		public String EMI_COD_SVS;
		public String EMI_IND_IIF;
		public String EMI_IND_IRF;
		public String EMI_RAZON_SOCIAL;

		public String EMI_TIP_ENT;
		public String EMI_RUT;
		public String EMI_CLA_RIE_IIF;
		public String EMI_AUTORIZA_PACTO;
		public String EMI_IND_FUNG;
		
		public BigDecimal AC_PRESEN_AJU;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RegistroTBCFMI{
		public String RV_MERCADO;
		public String RV_NEMO;
		public BigDecimal RV_PRE_PULT_TR;
		public BigDecimal RV_PRE_ULT_TR;
		public BigDecimal RV_CATEGORIA;
		public String RV_IND_CARTERA;
		public String RV_IND_EXCLUIDA;

		public String RV_AUT_VT_CORTO;
		public String RV_TENDEN_VT_CORTO;
		public String RV_IND_LIQ_DCV;
		public String RV_BOLSA = "BCS";
		public BigDecimal RV_MONEDA;
		public String RV_AUT_OD_BLOQUE;
		public String RV_COD_RUBRO;

		public BigDecimal RV_PRE_APE_DI_AC;
		public BigDecimal RV_PRE_MAY_52SEM;
		public BigDecimal RV_PRE_MEN_52SEM;
		public BigDecimal RV_PRE_CIE_7DIAS;
		public BigDecimal RV_PRE_CIE_30DIAS;
		public BigDecimal RV_PRE_CIE_52SEM;

		public BigDecimal RV_RENT_7DIAS;
		public BigDecimal RV_RENT_30DIAS;
		public BigDecimal RV_RENT_52SEM;
		public BigDecimal RV_DIVIDEND_YIELD;
		public BigDecimal RV_BETA_90DIAS;
		public String RV_CODIGO_ISIN;
		public BigDecimal RV_P_IPSA;

		public BigDecimal RV_P_IGPA;
		public BigDecimal RV_P_INTER_10;
		public BigDecimal RV_ACCIONES_EQ;
		public BigDecimal RV_VALOR_EQ;
		public BigDecimal RV_NON_FFLOAT;
		public String RV_SECTOR;
		public BigDecimal RV_PRE_CIE_TEORICO;

		public String RV_BOLSA_EX;
		public String RV_MONEDA_EX;
		public String RV_PAIS_EMISOR;
		public String RV_TIPO_INSTRU;
		public BigDecimal RV_VOL_52SEM;
		public String RV_LISTA;


		public String CF_NEMO;
		public BigDecimal CF_LOTE_PADRON;
		public String CF_CESA_PAGO;
		public String CF_COD_OFI;
		public String CF_COD_OFI_SASA;
		public String CF_COND_COD_DI_AC_001;
		public String CF_COND_COD_DI_AC_002;

		public String CF_COND_COD_DI_AC_003;
		public String CF_COND_COD_DI_AC_004;
		public String CF_COND_COD_DI_AC_005;
		public String CF_COND_COD_DI_AC_006;
		public String CF_COND_COD_DI_AC_007;

		public String CF_COND_COD_DI_AC_008;
		public String CF_COND_COD_DI_AC_009;
		public String CF_COND_COD_DI_AC_010;
		public String CF_COND_COD_OFI_001;
		public String CF_COND_COD_OFI_002;

		public String CF_COND_COD_OFI_003;
		public String CF_COND_COD_OFI_004;
		public String CF_COND_COD_OFI_005;
		public String CF_COND_COD_OFI_006;
		public String CF_COND_COD_OFI_007;

		public String CF_COND_COD_OFI_008;
		public String CF_COND_COD_OFI_009;
		public String CF_COND_COD_OFI_010;
		public String CF_COND_DIS_AN_AN;
		public String CF_COND_DIS_DI_AN;

		public String CF_COND_DIS_DI_AC;
		public String CF_COND_DIS_ME_AN;
		public String CF_COND_DIS_OFI;
		public String CF_COND_DIS_SE_AN;
		public String CF_COND_DIS_SN_AN;

		public BigDecimal CF_CUO_INSCRIP;
		public BigDecimal CF_DIVIDENDOS;
		public BigDecimal CF_DIAS_TR_ME_AC;
		public BigDecimal CF_DIAS_TR_SE_AC;
		public BigDecimal CF_DIAS_TR_AN_AC;
		public BigDecimal CF_DIAS_TR_TR_AC;

		public String CF_EMISOR;
		public String CF_ESTADO;
		public String CF_FEC_EST_SIT;
		public String CF_FEC_FIJ_CIE;
		public String CF_FEC_INSCRIP;
		public String CF_FEC_ULT_TR;
		public String CF_HORA_FIJ_CIE;

		public String CF_IND_AJU_VC_ULT_TR;
		public String CF_IND_SIT_ESP;
		public String CF_IND_HISBUR;
		public String CF_IND_INSTRU;
		public BigDecimal CF_INVER_ME;
		public BigDecimal CF_INVER_SE;
		public BigDecimal CF_INVER_AN;

		public String CF_LIQUIDA;
		public BigDecimal CF_MONTO_AN_AC;
		public BigDecimal CF_MONTO_AN_AN;
		public BigDecimal CF_MONTO_DI_AC;
		public BigDecimal CF_MONTO_DI_AN;
		public BigDecimal CF_MONTO_ME_AC;
		public BigDecimal CF_MONTO_ME_AN;

		public BigDecimal CF_MONTO_PMED_DI_AC;
		public BigDecimal CF_MONTO_PMED_SN_AC;
		public BigDecimal CF_MONTO_PMED_ME_AC;
		public BigDecimal CF_MONTO_PMED_SE_AC;
		public BigDecimal CF_MONTO_PMED_AN_AC;
		public BigDecimal CF_MONTO_PROM_UF_3M;

		public BigDecimal CF_MONTO_SE_AC;
		public BigDecimal CF_MONTO_SE_AN;
		public BigDecimal CF_MONTO_SN_AC;
		public BigDecimal CF_MONTO_SN_AN;
		public BigDecimal CF_NAA;
		public String CF_NEMO_SASA;
		public String CF_RAZON_SOCIAL;
		public BigDecimal CF_NUM_DEC_PRE;

		public BigDecimal CF_CUO_CIR_ME_AC;
		public BigDecimal CF_CUO_31DIC;
		public BigDecimal CF_NUM_MES_UTI;
		public BigDecimal CF_NUM_NEG_AN_AC;
		public BigDecimal CF_NUM_NEG_AN_AN;
		public BigDecimal CF_NUM_NEG_DI_AC;
		public BigDecimal CF_NUM_NEG_DI_AN;

		public BigDecimal CF_NUM_NEG_ME_AC;
		public BigDecimal CF_NUM_NEG_ME_AN;
		public BigDecimal CF_NUM_NEG_SE_AC;
		public BigDecimal CF_NUM_NEG_SE_AN;
		public BigDecimal CF_NUM_NEG_SN_AC;
		public BigDecimal CF_NUM_NEG_SN_AN;
		public BigDecimal CF_NUM_REDON;

		public BigDecimal CF_PATRI_AJU;
		public BigDecimal CF_PATRI_CON;
		public BigDecimal CF_PATRI_AN_AC;
		public BigDecimal CF_PATRI_AN_AN;
		public BigDecimal CF_PATRI_ME_AC;
		public BigDecimal CF_PATRI_ME_AN;
		public String CF_PATRI_NEG;

		public BigDecimal CF_PATRI_SE_AC;
		public BigDecimal CF_PATRI_SE_AN;
		public BigDecimal CF_PRESEN_AN_AC;
		public BigDecimal CF_PRESEN_AN_AN;
		public BigDecimal CF_PRESEN_DIN;
		public BigDecimal CF_PRESEN_ME_AC;
		public BigDecimal CF_PRESEN_ME_AN;

		public BigDecimal CF_PRESEN_SE_AC;
		public BigDecimal CF_PRESEN_SE_AN;
		public BigDecimal CF_PRESEN_PRO_12_M;
		public BigDecimal CF_PRESEN_PRO_1_T;
		public BigDecimal CF_PRESEN_PRO_2_T;
		public BigDecimal CF_PRESEN_PRO_3_T;
		public BigDecimal CF_PRESEN_PRO_4_T;
		public BigDecimal CF_PRE_CIE_AN_AN;

		public BigDecimal CF_PRE_CIE_DI_AC;
		public BigDecimal CF_PRE_CIE_DI_AN;
		public BigDecimal CF_PRE_CIE_ME_AN;
		public BigDecimal CF_PRE_CIE_OFI;
		public BigDecimal CF_PRE_CIE_PEN;
		public BigDecimal CF_PRE_CIE_SE_AN;
		public BigDecimal CF_PRE_CIE_SN_AN;

		public BigDecimal CF_PRE_MAY_AN_AC;
		public BigDecimal CF_PRE_MAY_AN_AN;
		public BigDecimal CF_PRE_MAY_DI_AC;
		public BigDecimal CF_PRE_MAY_DI_AN;
		public BigDecimal CF_PRE_MAY_ME_AC;
		public BigDecimal CF_PRE_MAY_ME_AN;
		public BigDecimal CF_PRE_MAY_SE_AC;

		public BigDecimal CF_PRE_MAY_SE_AN;
		public BigDecimal CF_PRE_MAY_SN_AC;
		public BigDecimal CF_PRE_MAY_SN_AN;
		public BigDecimal CF_PRE_MED_AN_AC;
		public BigDecimal CF_PRE_MED_AN_AN;
		public BigDecimal CF_PRE_MED_DI_AC;

		public BigDecimal CF_PRE_MED_DI_AN;
		public BigDecimal CF_PRE_MED_ME_AC;
		public BigDecimal CF_PRE_MED_ME_AN;
		public BigDecimal CF_PRE_MED_ULT_TR;
		public BigDecimal CF_PRE_MED_SE_AC;

		public BigDecimal CF_PRE_MED_SE_AN;
		public BigDecimal CF_PRE_MED_SN_AC;
		public BigDecimal CF_PRE_MED_SN_AN;
		public BigDecimal CF_PRE_MEN_AN_AC;
		public BigDecimal CF_PRE_MEN_AN_AN;
		public BigDecimal CF_PRE_MEN_DI_AC;

		public BigDecimal CF_PRE_MEN_DI_AN;
		public BigDecimal CF_PRE_MEN_ME_AC;
		public BigDecimal CF_PRE_MEN_ME_AN;
		public BigDecimal CF_PRE_MEN_SE_AC;
		public BigDecimal CF_PRE_MEN_SE_AN;

		public BigDecimal CF_PRE_MEN_SN_AC;
		public BigDecimal CF_PRE_MEN_SN_AN;
		public BigDecimal CF_REL_BOL_LIB;
		public BigDecimal CF_REL_PRE_DIV;
		public BigDecimal CF_REL_PRE_UTI;
		public BigDecimal CF_RENT_AN_AC;

		public BigDecimal CF_RENT_AN_AN;
		public BigDecimal CF_RENT_ME_AC;
		public BigDecimal CF_RENT_ME_AN;
		public BigDecimal CF_RENT_SE_AC;
		public BigDecimal CF_RENT_SE_AN;
		public String CF_TENDEN_DI_AC;

		public String CF_TIPO_ENT;
		public BigDecimal CF_PRE_ULT_DI_AC;
		public BigDecimal CF_UNI_PMED_DI_AC;
		public BigDecimal CF_UNI_PMED_SN_AC;
		public BigDecimal CF_UNI_PMED_ME_AC;
		public BigDecimal CF_UNI_PMED_SE_AC;

		public BigDecimal CF_UNI_PMED_AN_AC;
		public BigDecimal CF_UNI_TRA_AN_AC;
		public BigDecimal CF_UNI_TRA_AN_AN;
		public BigDecimal CF_UNI_TRA_DI_AC;
		public BigDecimal CF_UNI_TRA_DI_AN;
		public BigDecimal CF_UNI_TRA_ME_AC;

		public BigDecimal CF_UNI_TRA_ME_AN;
		public BigDecimal CF_UNI_TRA_ULT_TR;
		public BigDecimal CF_UNI_TRA_SE_AC;
		public BigDecimal CF_UNI_TRA_SE_AN;
		public BigDecimal CF_UNI_TRA_SN_AC;
		public BigDecimal CF_UNI_TRA_SN_AN;

		public BigDecimal CF_UTI_12_M;
		public BigDecimal CF_UTI_NUM_MES;
		public String CF_TIPO_FONDO;
		public String CF_AUT_OPE_SIMUL;
		public String CF_NEMO_DCV;


		public String SOC_RUT_ADMIN;
		public String SOC_NOM_ADMIN;
		
		public BigDecimal CF_PRESEN_AJU;
		public String SOC_RUT;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class RegistroTBMOMI{
		public String NEMO;
		public BigDecimal LOTE_PADRON;
		public String COD_OFI;
		public String COND_COD_DI_AC_001;
		public String COND_COD_DI_AC_002;
		public String COND_COD_DI_AC_003;
		public String COND_COD_DI_AC_004;
		public String COND_COD_DI_AC_005;
		public String COND_COD_DI_AC_006;
		public String COND_COD_DI_AC_007;
		public String COND_COD_DI_AC_008;
		public String COND_COD_DI_AC_009;
		public String COND_COD_DI_AC_010;
		
		public String COND_COD_OFI_001;
		public String COND_COD_OFI_002;
		public String COND_COD_OFI_003;
		public String COND_COD_OFI_004;
		public String COND_COD_OFI_005;
		public String COND_COD_OFI_006;
		public String COND_COD_OFI_007;
		public String COND_COD_OFI_008;
		public String COND_COD_OFI_009;
		public String COND_COD_OFI_010;
		public String COND_DIS_DI_AC;
		public String COND_DIS_DI_AN;
		public String COND_DIS_OFI;
		public String ESTADO;
		
		public String FEC_FIJ_CIE;
		public String FIJA_CIE;
		public String HORA_FIJ_CIE;
		public BigDecimal MONTO;
		public BigDecimal MONTO_SN_AC;
		public BigDecimal MONTO_ME_AC;
		public String RAZON_SOCIAL;
		public BigDecimal NUM_DEC_PRE;
		public BigDecimal NUM_NEG;
		public BigDecimal NUM_REDON;
		public BigDecimal PRE_CIE_DI_AC;
		public BigDecimal PRE_CIE_DI_AN;
		public BigDecimal PRE_CIE_OFI;
		public BigDecimal PRE_MAY;
		public BigDecimal PRE_MED;
		public BigDecimal PRE_MEN;
		public String TENDEN_DI_AC;
		public String TIPO_INSTRU;
		public BigDecimal PRE_ULT_DI_AC;
		public BigDecimal RENT_AN_AC;
		
		public BigDecimal RENT_AN_AN;
		public BigDecimal UNI_TRA;
		public BigDecimal UNI_TRA_SN_AC;
		public BigDecimal UNI_TRA_ME_AC;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}

	public static class RegistroTBRFMI{
		public String RF_NEMO;
		public String RF_COD_OFI;
		public String RF_COND_COD_OFI_001;
		public String RF_COND_COD_OFI_002;
		public String RF_COND_COD_OFI_003;
		public String RF_COND_COD_OFI_004;
		public String RF_COND_COD_OFI_005;
		public String RF_COND_COD_OFI_006;
		public String RF_COND_COD_OFI_007;
		public String RF_COND_COD_OFI_008;
		public String RF_COND_COD_OFI_009;
		public String RF_COND_COD_OFI_010;
		public String RF_COND_DIS_OFI;
		public BigDecimal RF_DIAS_TR_SN_AC;
		public BigDecimal RF_DIAS_TR_ME_AC;
		public String RF_EMI_PROV;
		public String RF_ESTADO;
		public BigDecimal RF_FAC_FRACC_OFE;
		public String RF_FEC_EMI;
		public String RF_FEC_FIJ_CIE;
		public String RF_FEC_VCTO;
		public String RF_IND_BOLSON;
		public String RF_HORA_FIJ_CIE;
		public BigDecimal RF_MONTO;
		public BigDecimal RF_MONTO_SN_AC;
		public BigDecimal RF_MONTO_ME_AC;
		public BigDecimal RF_MONTO_MED_DI_AC;
		public BigDecimal RF_MONTO_MED_SN_AC;
		public BigDecimal RF_MONTO_X_PMED_DI;
		public BigDecimal RF_MONTO_X_PMED_SN;
		public BigDecimal RF_MONTO_X_TIR;
		public String RF_RAZON_SOCIAL;
		public BigDecimal RF_NUM_NEG;
		public BigDecimal RF_PRE_CIE_AN_AN;
		public BigDecimal RF_PRE_CIE_DI_AC;
		public BigDecimal RF_PRE_CIE_ME_AN;
		public BigDecimal RF_PRE_CIE_OFI;
		public BigDecimal RF_PRE_CIE_SN_AN;
		public BigDecimal RF_PRE_MAY;
		public BigDecimal RF_PRE_MAY_SN_AC;
		public BigDecimal RF_PRE_MED;
		public BigDecimal RF_PRE_MED_SN_AC;
		public BigDecimal RF_PRE_MEN;
		public BigDecimal RF_PRE_MEN_SN_AC;
		public String RF_TENDEN;
		public String RF_TIPO_CALCULO;
		public BigDecimal RF_TIR_MED;
		public BigDecimal RF_UNI_TRA;
		public BigDecimal RF_UNI_TRA_SN_AC;
		public BigDecimal RF_UNI_TRA_ME_AC;
		public String RF_COD_SERIE;
		public String RF_FAMILIA;
		public String RF_GENERICO;
		public String RF_MONEDA;
		public BigDecimal RF_TIR_MAY;
		public BigDecimal RF_TIR_MEN;
		public BigDecimal RF_DURATION_MED;
		public BigDecimal RF_MONTO_DURATION;
		public String RF_CODIGO_ISIN;
		public String SE_COD_SERIE;
		public String SE_COD_BCO;
		public String SE_COD_OFI;
		public BigDecimal SE_CORTE_MIN;
		public String SE_CLASIF_RIESGO;
		public String SE_IND_DIVISI;
		public String SE_EMISOR;
		public BigDecimal SE_FAC_FRACC_OFE;
		public String SE_FAMILIA;
		public String SE_FEC_COM_PPAG;
		public String SE_FEC_PRI_EMI;
		public String SE_FEC_VCTO;
		public String SE_IND_BOLSON;
		public String SE_IRF_INSCRIP;
		public BigDecimal SE_MONTO_EMI;
		public BigDecimal SE_NUM_AMORT;
		public BigDecimal SE_NUM_CAR_VAL;
		public BigDecimal SE_NUM_CUPON;
		public BigDecimal SE_ORDEN;
		public BigDecimal SE_PERI_PAGO_CUPON;
		public BigDecimal SE_PLAZO;
		public String SE_RANGO_PLAZO;
		public String SE_RANGO_TASA;
		public BigDecimal SE_TASA_NOMI_EMI;
		public BigDecimal SE_TERA;
		public String SE_TIPO_AMORT;
		public String SE_TIPO_REAJ;
		public String SE_TIPO_INSTRU;
		public String SE_TIPO_INTERES;
		public String SE_TIPO_NOMINAL;
		public String SE_COMENTARIOS;
		public String SE_FEC_INGRESO;
		public String SE_MONEDA;
		public BigDecimal SE_ORDEN_CLASIF_RIESGO;
		public String SE_TIPO_MONEDA_PAGO;
		public BigDecimal SE_TASA_FISCAL;
		public String SE_IND_VENTA_CORTA;
		public String SE_IND_ART104;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class RegistroTBUTEMI{
		public String EMISOR;
		public String COD_EMI;
		public String COD_SVS;
		public String IND_IIF;
		public String IND_IRF;
		public String RAZON_SOCIAL;
		public String TIP_ENT;
		public String RUT;
		public String CLA_RIE_IIF;
		public String AUTORIZA_PACTO;
		public String IND_FUNG;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistroVectorPrecios{
		public Timestamp FECHA;
		public String NEMO;
		public Integer SEC_LISTA;
		public String TIPO;
		public BigDecimal VALOR;
		public BigDecimal VALOR_95;
		public BigDecimal VALOR_98;
		public BigDecimal VALOR_01;
		public BigDecimal VALOR_02;
		public BigDecimal VALOR_03;
		public BigDecimal VALOR_04;
		public BigDecimal VALOR_05;
		public BigDecimal PRECIO_BASE;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public  class VectorPreciosWebService{
		public class Cabecera{
			public class ConsultaVectorPrecioPTResult{
				public String nemo;
				public BigDecimal precioBase;
				public Integer 	  secLista;
				public String 	  tipo;
				public BigDecimal valor;
				public BigDecimal valor01;
				public BigDecimal valor02;
				public BigDecimal valor03;
				public BigDecimal valor04;
				public BigDecimal valor05;
				public BigDecimal valor95;
				public BigDecimal valor98;
				
				@Override
				public String toString() {
					return Utils.printJson(this, false);
				}
				public String getNemo() {
					return nemo;
				}
				public BigDecimal getPrecioBase() {
					return precioBase;
				}
				public Integer getSecLista() {
					return secLista;
				}
				public String getTipo() {
					return tipo;
				}
				public BigDecimal getValor() {
					return valor;
				}
				public BigDecimal getValor01() {
					return valor01;
				}
				public BigDecimal getValor02() {
					return valor02;
				}
				public BigDecimal getValor03() {
					return valor03;
				}
				public BigDecimal getValor04() {
					return valor04;
				}
				public BigDecimal getValor05() {
					return valor05;
				}
				public BigDecimal getValor95() {
					return valor95;
				}
				public BigDecimal getValor98() {
					return valor98;
				}
			}
			List<ConsultaVectorPrecioPTResult> consultaVectorPrecioPTResult;
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
			public List<ConsultaVectorPrecioPTResult> getConsultaVectorPrecioPTResult() {
				return consultaVectorPrecioPTResult;
			}
		}
		public Cabecera cabecera;
		public Long id;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Cabecera getCabecera() {
			return cabecera;
		}
		public Long getId() {
			return id;
		}
	}
	
	

}
