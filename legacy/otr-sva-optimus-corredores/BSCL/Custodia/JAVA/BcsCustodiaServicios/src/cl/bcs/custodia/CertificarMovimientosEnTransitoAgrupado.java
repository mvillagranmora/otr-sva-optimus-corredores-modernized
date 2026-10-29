package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.sql.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada;
import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida;
import cl.bcs.custodia.CertificarMovimientosEnTransito.MovimientoEnTransito;
import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados;
import cl.bcs.custodia.CertificarMovimientosEnTransito.ObjMovimientos;


import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CertificarMovimientosEnTransitoAgrupado implements JavaCall {
		
	private static final Logger logger = LogManager.getLogger(CertificarMovimientosEnTransitoAgrupado.class);

	private final String SQL_CONSULTAR_REGISTROS = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransitoAgrupados_SELECT_RegistrosAgrupados.sql");
	private final String SQL_SELECT_MOVIMIENTO_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_SelectMovimientoEnTransito.sql");
	private final String SQL_TABLA_REGISTROS_AGRUPADOS = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransitoAgrupados_TablaRegistros.sql");
	private final String SQL_TABLA_REGISTROS_AGRUPADOS_INSERT = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransitoAgrupados_INSERT_TablaRegistros.sql");
	private final String SQL_TABLA_REGISTROS_AGRUPADOS_DELETE = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransitoAgrupados_DELETE_TablaRegistros.sql");

	public static class EntradaMovimientosEnTransitoAgrupados extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaProc;
		public List<MovEnTransitoAG> p_MovEnTransitoAG;

		public EntradaMovimientosEnTransitoAgrupados() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CUS";
			this.p_UsaReglasNegocio = "N";
			this.p_FechaProc = "0001-01-01";
			this.p_MovEnTransitoAG = new ArrayList<>();
		}
	}
	
	public static class RegistrosDes extends BcsPojo{
		public Long IdMovimientoCarteraEnTransito;
		public String IdTipoOperacion;
		public String IdConcepto;
		public String IdClasificacionTipoInstrumento;
		public String Glosa;
		
		public RegistrosDes() {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdTipoOperacion = "";
			this.IdConcepto = "";
			this.IdClasificacionTipoInstrumento="";
			this.Glosa = "";
			
		}

	}
	
	public static class MovEnTransitoAG extends BcsPojo {
			public Long IdCliente;
			public String DocumentoIdentidad;
			public String IdTipoOperacion;
			public String IdClasificacionTipoInstrumento;
			public Date FechaOperacion;
			public Date FechaLiquidacion;
			public String IdCondicionLiquidacion;
			public String IdConcepto;
			public Long IdInstrumento;
			public String Nemotecnico;
			public String IdMonedaInstrumento;
			public String IdBoveda;
			public String EsNominal;			
	}
	
	
	public static class ObjMovEnTransitoaCertificar {
		public Long IdMovimientoCarteraEnTransito;
		public String IdTipoOperacion;
		public String MovimientoGarantia;
	}
	


	public static class SalidaMovimientosEnTransitoAgrupados extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaMovimientosEnTransitoAgrupados() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}

	public SalidaMovimientosEnTransitoAgrupados logica(Connection conn, EntradaMovimientosEnTransitoAgrupados in) throws SQLException, Exception {
		logger.info("INICIO::CertificarMovimientosEnTransitoAgrupado");
		long inicio = System.currentTimeMillis();
		SalidaMovimientosEnTransitoAgrupados Salida = new SalidaMovimientosEnTransitoAgrupados();
		SQL sql=new SQL();
		
		if (!in.p_MovEnTransitoAG.isEmpty()) {
			
			//CREAMOS TABLA TEMPORAL
			SQL.exec(conn, SQL_TABLA_REGISTROS_AGRUPADOS);
			
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			List<MovEnTransitoAG> insertRegistros = new ArrayList<MovEnTransitoAG>();
			
			
			
			for (MovEnTransitoAG reg : in.p_MovEnTransitoAG) {
				
				MovEnTransitoAG registro = new MovEnTransitoAG();
				
				registro.IdCliente = reg.IdCliente;
				registro.DocumentoIdentidad = reg.DocumentoIdentidad;
				registro.IdTipoOperacion = reg.IdTipoOperacion;
				registro.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
				registro.FechaOperacion = reg.FechaOperacion;
				registro.FechaLiquidacion=reg.FechaLiquidacion;
				registro.IdCondicionLiquidacion=reg.IdCondicionLiquidacion;
				registro.IdConcepto = reg.IdConcepto;
				registro.Nemotecnico = reg.Nemotecnico;
				registro.IdInstrumento = reg.IdInstrumento;
				registro.IdMonedaInstrumento = reg.IdMonedaInstrumento;
				registro.IdBoveda = reg.IdBoveda;
				registro.EsNominal = reg.EsNominal;
				insertRegistros.add(registro);
				
				
			}
			
			/// INSERTAMOS LA DATA EN TABLA TEMPORAL
			
			sql.batch(conn, SQL_TABLA_REGISTROS_AGRUPADOS_INSERT, insertRegistros, MovEnTransitoAG.class);
			
			List <RegistrosDes> registros_des= sql.query(conn, SQL_CONSULTAR_REGISTROS, parametros, RegistrosDes.class);
			
			if(!registros_des.isEmpty()) {
				List <ObjMovimientos> p_movCertificar = new ArrayList<>();
				CertificarMovimientosEnTransitoEntrada entrada = new CertificarMovimientosEnTransitoEntrada();
				Set <Long> idMovEntrada = new HashSet<>();
				
				for (RegistrosDes regC : registros_des) {
					
					ObjMovimientos movACertificar = new ObjMovimientos();
					
					movACertificar.IdMovimientoCarteraEnTransito = regC.IdMovimientoCarteraEnTransito;
					movACertificar.IdTipoOperacion = regC.IdTipoOperacion;
					movACertificar.MovimientoGarantia = validarMovimientoGarantia(regC);
					idMovEntrada.add(regC.IdMovimientoCarteraEnTransito);
					p_movCertificar.add(movACertificar);
					
				}								 
				
				entrada.p_IdEmpresa = in.p_IdEmpresa;
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = in.p_IdModulo;
				entrada.p_FechaOperacion = in.p_FechaProc;
				entrada.P_MOVIMIENTOSCERTIFICAR = p_movCertificar;
				
				// GUARDAMOS LA CANTIDAD DE REGISTROS INICIALES
				Integer cantidadRegistrosIn = p_movCertificar.size();
				
				CertificarMovimientosEnTransito certificarMovAGR = new CertificarMovimientosEnTransito();
				
				CertificarMovimientosEnTransitoSalida salidaCer = certificarMovAGR.logica(conn, entrada);
				
				
				logger.info("Movimientos Procesados: " + salidaCer.P_OUT_MOVIMIENTOSPROCESADOS.size());
				

				// CONTROL DE SALIDA MASIVA

				if (!(salidaCer.P_OUT_MOVIMIENTOSPROCESADOS.isEmpty())) { 
					Integer regUnicos = 0;					
					Set <Long> idMovCarTraUnicos = new HashSet<>();
					List<ObjMovimientosProcesados> certNom = new ArrayList<>();
					
					for (ObjMovimientosProcesados pro : salidaCer.P_OUT_MOVIMIENTOSPROCESADOS) {
						if(pro.IdMovimientoCarteraEnTransito != null && idMovCarTraUnicos.add(pro.IdMovimientoCarteraEnTransito) && idMovEntrada.contains(pro.IdMovimientoCarteraEnTransito)) {
							regUnicos++;
						}
					}
					
					for (MensajeSalida nom : salidaCer.P_OUT_MENSAJES) { // PARA CAPTURAR NOMINALES Y LOG
						if(nom.id != null && idMovCarTraUnicos.add(nom.id) && "INFORMACION".equals(nom.tipo) && idMovEntrada.contains(nom.id)) {
							ObjMovimientosProcesados nominalReg = new ObjMovimientosProcesados();
							nominalReg.IdMovimientoCarteraEnTransito = nom.id;
							certNom.add(nominalReg);
							regUnicos++;
						}
					}
					
						if (regUnicos > 0) {
							logger.info("Se certifican " + regUnicos + " registros");
							
							HashMap <String,String> param =  new HashMap<>();
							param.put("Cantidad", Integer.toString(regUnicos));
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_CANTIDAD_CERTIFICADA", param, conn, in.p_IdEmpresa));
							param.clear();
						
							Integer dif = cantidadRegistrosIn - regUnicos;
							
							
							if (dif > 0) {
								param.put("Cantidad", Integer.toString(dif));
								Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANTIDAD_NO_CERTIFICADA", param, conn, in.p_IdEmpresa));
							}
							
							if(!certNom.isEmpty()) { // SI EXISTEN NOMINALES CERTIFICADOS SE GENERAN LOGS
								agregarLog(conn, in, certNom);
							}
							
							conn.commit();
						}
						
						else {
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_MOVIMIENTOS_CERTIFICADOS", new HashMap<>(), conn, in.p_IdEmpresa));
						}
						
				}
				else {
					
					if (!salidaCer.P_OUT_MENSAJES.isEmpty()) {
						Integer regUnicosNom = 0;
						Set <Long> idMovCarTraUnicosNom = new HashSet<>();
						List<ObjMovimientosProcesados> certNom = new ArrayList<>();
						
						for (MensajeSalida nom : salidaCer.P_OUT_MENSAJES) { // PARA CAPTURAR NOMINALES
							if(nom.id != null && idMovCarTraUnicosNom.add(nom.id) && "INFORMACION".equals(nom.tipo) && idMovEntrada.contains(nom.id)) {
								ObjMovimientosProcesados nominalReg = new ObjMovimientosProcesados();
								nominalReg.IdMovimientoCarteraEnTransito = nom.id;
								certNom.add(nominalReg);
								regUnicosNom++;
							}
						}
						
						Integer difnom = cantidadRegistrosIn - regUnicosNom;
						HashMap <String,String> param =  new HashMap<>();
						
						if(regUnicosNom > 0) {
							logger.info("Se certifican " + regUnicosNom + " registros");
							param.put("Cantidad", Integer.toString(regUnicosNom));
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_CANTIDAD_CERTIFICADA", param, conn, in.p_IdEmpresa));
							param.clear();
							
							if (difnom > 0) {
								param.put("Cantidad", Integer.toString(difnom));
								Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANTIDAD_NO_CERTIFICADA", param, conn, in.p_IdEmpresa));
								
							}
							
							if(!certNom.isEmpty()) { // SI EXISTEN NOMINALES CERTIFICADOS SE GENERAN LOGS
								agregarLog(conn, in, certNom);
							}
							
							conn.commit();
						}
						else {
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_MOVIMIENTOS_CERTIFICADOS", new HashMap<>(), conn, in.p_IdEmpresa));
						}
						
					}
					else {
						if(salidaCer.P_OUT_ERRORESCONTADOR.equals(0L)) {
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_OPERACION_EJECUTADA", new HashMap<>(), conn, in.p_IdEmpresa));
						}
						else {
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_MOVIMIENTOS_A_CERTIFICAR", new HashMap<>(), conn, in.p_IdEmpresa));
						}
					}
					
				}
								
			}
			else {
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_MOVIMIENTOS_A_CERTIFICAR", new HashMap<>(), conn, in.p_IdEmpresa));
			}
			// ELIMANIMOS TABLA TEMPORAL
			SQL.exec(conn, SQL_TABLA_REGISTROS_AGRUPADOS_DELETE);
		}
		
		Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CertificarMovimientosEnTransitoAgrupado.", fin - inicio);
		
		return Salida;
	}
	
public String validarMovimientoGarantia(RegistrosDes in) {
		
		String EsGarantia = "N";
		
		if ("VCDTIT".equals(in.IdConcepto) && "RETTIT".equals(in.IdTipoOperacion) && "IF".equals(in.IdClasificacionTipoInstrumento) && "VENCIMIENTO GARANTIA".equals(in.Glosa)) {
			
			EsGarantia = "S";
		}	
		return EsGarantia;
	}

private void agregarLog(Connection con, EntradaMovimientosEnTransitoAgrupados in, List<ObjMovimientosProcesados> listaMovimientosProcesados) {
	try {
		logger.debug("INICIO::CertificarMovimientosEnTransito.agregarLog");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	
		List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
		List<Long> idsMovimientosCarteraEnTransito = new ArrayList<Long>();
		
		for (ObjMovimientosProcesados movimiento : listaMovimientosProcesados) {
			idsMovimientosCarteraEnTransito.add(movimiento.IdMovimientoCarteraEnTransito);
		}
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdMovimientoCarteraEnTransito", idsMovimientosCarteraEnTransito);
		List<MovimientoEnTransito> listaDatosMovimiento = _sql.query(con, SQL_SELECT_MOVIMIENTO_EN_TRANSITO, params, MovimientoEnTransito.class);
		HashMap<String, Object> hDatosMovimiento = Utils.List2Hash(listaDatosMovimiento, "IdMovimientoCarteraEnTransito");
		
		for (ObjMovimientosProcesados movimiento : listaMovimientosProcesados) {
			//CABECERA
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			MovimientoEnTransito datosMovimiento = (MovimientoEnTransito) hDatosMovimiento.get(movimiento.IdMovimientoCarteraEnTransito);
			
			log.IdTrabajo = 1L; 
			log.IdCabeceraLog = datosMovimiento.IdMovimientoCarteraEnTransito; //
			log.IdTipoLog = "CERTIFICACION";
			log.IdModulo = in.p_IdModulo;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdCliente = datosMovimiento.IdCliente; //
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = datosMovimiento.IdPortafolio;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CERTIFICARMOVIMIENTOENTRANSITO";
			log.IdRegistro = datosMovimiento.IdMovimientoCarteraEnTransito.toString();
			log.EstadoRegistro = datosMovimiento.IdEstado;
			log.IdTipoOperacion = datosMovimiento.IdTipoOperacion;
			log.Folio = 0L;
			log.Cantidad = datosMovimiento.Cantidad;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = datosMovimiento.Nemotecnico; //
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "cl.bcs.custodia.CertificarMovimientosEnTransito";
					
			listaLog.add(log);
		}
		
		//REGISTRO
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;		
		entradaAgregarLog.p_Log = listaLog;	
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CertificarMovimientosEnTransito.agregarLog:" , fin - inicio);
	} catch (Exception e) {
		e.printStackTrace();
	}	
}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaMovimientosEnTransitoAgrupados jin=gson.fromJson(in.toString() , EntradaMovimientosEnTransitoAgrupados.class);
		SalidaMovimientosEnTransitoAgrupados out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\": 1, \"p_IdUsuario\": 97, \"p_IdModulo\": \"CUS\", \"p_UsaReglasNegocio\": \"S\", \"p_FechaProc\": \"2025-06-20\", \"p_MovEnTransitoAG\": [{ \"IdCliente\": 3050, \"DocumentoIdentidad\": \"9251272-K\", \"IdTipoOperacion\": \"INGTIT\", \"IdClasificacionTipoInstrumento\": \"RV\", \"FechaOperacion\": \"2025-06-05\", \"FechaLiquidacion\": \"2025-06-05\", \"IdCondicionLiquidacion\": \"T0\", \"IdConcepto\": \"MOVREG\", \"IdInstrumento\": 174, \"Nemotecnico\": \"BESALCO\", \"IdMonedaInstrumento\": \"CLP\", \"IdBoveda\": \"DCV\", \"EsNominal\": \"N\" }]}";
			EntradaMovimientosEnTransitoAgrupados Objin = gson.fromJson(json, EntradaMovimientosEnTransitoAgrupados.class);
			CertificarMovimientosEnTransitoAgrupado demo = new CertificarMovimientosEnTransitoAgrupado();
			try {
				SalidaMovimientosEnTransitoAgrupados salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
				//con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}