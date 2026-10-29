package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ins.CargaArchivoPorcentajeDescuentoRF;
import cl.bcs.plataforma.ins.CargaArchivoPorcentajeDescuentoRF.CargaPorcentajeDescuentoRF;
import cl.bcs.plataforma.ins.CargaArchivoPorcentajeDescuentoRF.InCargaPorcentajeDescuentoRF;
import cl.bcs.plataforma.ins.CargaArchivoPorcentajeDescuentoRF.OutCargaPorcentajeDescuentoRF;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.CargaValorRiesgoRV;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.InCargaValorRiesgoRV;
import cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV.OutCargaValorRiesgoRV;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
 
public class ProcesarCargaMasivaPlataforma implements JavaCall {

	private final Log log = new Log("PLA.CargaMasiva");
	private final String SQL_ACTUALIZA_ESTADO_CARGA_MASIVA			= loadResourceToString("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_ActualizaEstadoCargaMasiva.sql");
	private final String SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE	= loadResourceToString("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_ActualizaEstadoCargaMasivaDetalle.sql");
	//private final String SQL_SELECT_CARGAMASIVADETALLE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaCustodia_SelectCargaMasivaDetalle.sql");
	private final String SQL_SELECT_CARGAMASIVADETALLEPERU			= loadResourceToString("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_SelectCargaMasivaDetallePeru.sql");
	private final String SQL_INSERT_CARGA_VALOR_RIESGO_RV = loadResourceToString("cl/bcs/plataforma/ins/sql/CargaArchivoValorRiesgoRV_InsertValorRiesgo.sql");
	private final String SQL_SELECT_INSTRUMENTOS_RV = "SELECT 1 AS \"IdEmpresa\", INS.\"Nemotecnico\",IVC.\"IdInstrumento\",IVC.\"IdEntidadClasificadoraRiesgo\" FROM \"BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo\" AS IVC INNER JOIN \"BSCL.Plataforma.BD::INS.Instrumento\" AS INS ON INS.\"IdInstrumento\" = IVC.\"IdInstrumento\" WHERE INS.\"Nemotecnico\" = :Nemotecnico AND IVC.\"IdEntidadClasificadoraRiesgo\" = :IdEntidadClasificadoraRiesgo";
	private final String SQL_SELECT_INSTRUMENTOS = "SELECT \"IdInstrumento\" FROM \"BSCL.Plataforma.BD::INS.Instrumento\" WHERE \"Nemotecnico\" = :Nemotecnico";
	
	public static class DetalleCarga {
		public Long IdTipoCargaMasiva;
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalle;
		public String Nemotecnico;
		public String RV_IdEntidadClasificadoraRiesgo;
		public String RV_Valor;
		public BigDecimal RF_PorcentajeDescuento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosActualizacion {
		public Long IdCargaMasiva;
		public String Fecha;  
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosActualizacionDeta {
		public Long IdCargaMasiva;  
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	public static class ConsultaTipOpeInstrumento{
		public Long IdInstrumento;
		public String Nemotecnico;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorRiesgoRV{
		public Long 	IdInstrumento;
		public String	Entidad;
		public String	Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentosRV{
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdEntidadClasificadoraRiesgo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class IdInstrumentoClass{
		public Long IdInstrumento;		
	}
	
	//TODO: Entrada
	public static class ProcesarCargaMasivaPlataformaEntrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public String 	p_FechaProceso;
		public Long 	p_CargaMasivaProcesar;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO :Salida
	public static class CuadraturaDiariaSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public CuadraturaDiariaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarCargaMasivaPlataformaEntrada in = gson.fromJson(jin.toString(), ProcesarCargaMasivaPlataformaEntrada.class);
		CuadraturaDiariaSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public CuadraturaDiariaSalida logica(Connection conn, ProcesarCargaMasivaPlataformaEntrada in) throws Exception {
		SQL _sql = new SQL();
		CuadraturaDiariaSalida out = new CuadraturaDiariaSalida();
		String monedaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		log.write("INICIO");
		try {
			//TODO : PRINCIPAL
			HashMap<String,Object> regDetalleCarga = new HashMap<>();
			regDetalleCarga.put("IdEmpresa", in.p_IdEmpresa);
			regDetalleCarga.put("IdCargaMasiva", in.p_CargaMasivaProcesar);
			List<DetalleCarga> rsDetalleComun = new ArrayList<DetalleCarga>();
			List<DetalleCarga> rsDetalleComun2 = new ArrayList<DetalleCarga>();

			switch (localizacionSistema) {
			case "PE":
				List<DetalleCarga> rsDetallePe = _sql.query(conn, SQL_SELECT_CARGAMASIVADETALLEPERU, regDetalleCarga, DetalleCarga.class);
				rsDetalleComun.addAll(rsDetallePe);
				break;

			default:
//				List<DetalleCarga> rsDetalle = _sql.query(conn, SQL_SELECT_CARGAMASIVADETALLE, regDetalleCarga, DetalleCarga.class);
//				rsDetalleComun.addAll(rsDetalle);
				break;
			}
			
			InCargaValorRiesgoRV InCargaRV = new InCargaValorRiesgoRV();
			List<CargaValorRiesgoRV> listValorRiesgoRV = new ArrayList<>();
			
			InCargaPorcentajeDescuentoRF InCargaRF = new InCargaPorcentajeDescuentoRF();
			List<CargaPorcentajeDescuentoRF> listValorRiesgoRF = new ArrayList<>();
			
			for (DetalleCarga recDetalle: rsDetalleComun){
				
				Integer iTipo = (int) (long) recDetalle.IdTipoCargaMasiva;
				switch (iTipo) {
				case 1: //PORCENTAJE_DESCUENTO_RF
					
					CargaPorcentajeDescuentoRF valorRiesgoRF = new CargaPorcentajeDescuentoRF();
					
					valorRiesgoRF.Nemotecnico = recDetalle.Nemotecnico;
					valorRiesgoRF.PorcentajeDescuento = recDetalle.RF_PorcentajeDescuento;

					listValorRiesgoRF.add(valorRiesgoRF);
					
					InCargaRF.p_IdEmpresa = in.p_IdEmpresa;
					InCargaRF.p_IdModulo = in.p_IdModulo;
					InCargaRF.p_IdUsuario = in.p_IdUsuario;
					InCargaRF.p_ListaCarga = listValorRiesgoRF;
					
					CargaArchivoPorcentajeDescuentoRF cargaArchivoPorcentajeDescuento = new CargaArchivoPorcentajeDescuentoRF();
					
					OutCargaPorcentajeDescuentoRF salidaCargaPorcentajeDescuento = cargaArchivoPorcentajeDescuento.logica(conn, InCargaRF);
					
					if ( salidaCargaPorcentajeDescuento.p_out_ErroresContador == 0 ){
						List<ParametrosActualizacion> paramUpdate = new ArrayList<>();
						ParametrosActualizacion ii = new ParametrosActualizacion();
						ii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
						ii.Fecha			= in.p_FechaProceso;
						paramUpdate.add(ii);
						_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA, paramUpdate, ParametrosActualizacion.class);
						
						List<ParametrosActualizacionDeta> paramUpdateDet = new ArrayList<>();
						ParametrosActualizacionDeta iii = new ParametrosActualizacionDeta();
						iii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
						paramUpdateDet.add(iii);
						_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE, paramUpdateDet, ParametrosActualizacionDeta.class);				
					}
										
					break;
				case 2: //VALOR_RIESGO
					
					CargaValorRiesgoRV valorRiesgoRV = new CargaValorRiesgoRV();
					
					valorRiesgoRV.Nemotecnico = recDetalle.Nemotecnico;
					valorRiesgoRV.Entidad = recDetalle.RV_IdEntidadClasificadoraRiesgo;
					valorRiesgoRV.Valor = recDetalle.RV_Valor;

					listValorRiesgoRV.add(valorRiesgoRV);
					
					InCargaRV.p_IdEmpresa = in.p_IdEmpresa;
					InCargaRV.p_IdModulo = in.p_IdModulo;
					InCargaRV.p_IdUsuario = in.p_IdUsuario;
					InCargaRV.p_ListaCarga = listValorRiesgoRV;
					
					CargaArchivoValorRiesgoRV cargaArchivoValorRiesgo = new CargaArchivoValorRiesgoRV();
					
					OutCargaValorRiesgoRV salidaCargaValorRiesgo = cargaArchivoValorRiesgo.logica(conn, InCargaRV);
					
					if ( salidaCargaValorRiesgo.p_out_ErroresContador == 0 ){
						List<ParametrosActualizacion> paramUpdate = new ArrayList<>();
						ParametrosActualizacion ii = new ParametrosActualizacion();
						ii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
						ii.Fecha			= in.p_FechaProceso;
						paramUpdate.add(ii);
						_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA, paramUpdate, ParametrosActualizacion.class);
						
						List<ParametrosActualizacionDeta> paramUpdateDet = new ArrayList<>();
						ParametrosActualizacionDeta iii = new ParametrosActualizacionDeta();
						iii.IdCargaMasiva 	= in.p_CargaMasivaProcesar;
						paramUpdateDet.add(iii);
						_sql.batch(conn, SQL_ACTUALIZA_ESTADO_CARGA_MASIVA_DETALLE, paramUpdateDet, ParametrosActualizacionDeta.class);				
					}
					
					break;					
				default: 
					break;
				}
			}
			
			switch (localizacionSistema) {
			case "PE":	
				List<ValorRiesgoRV> valores = new ArrayList<>();
				HashMap<String,Object> params = new HashMap<>();
				for (DetalleCarga recDetalle2: rsDetalleComun){
					ValorRiesgoRV valorRiesgoRV = new ValorRiesgoRV();
					if(recDetalle2.IdTipoCargaMasiva == 2){
						params = new HashMap<>();
						params.put("Nemotecnico", recDetalle2.Nemotecnico);
						List<IdInstrumentoClass> _IdInstrumento = _sql.query(conn, SQL_SELECT_INSTRUMENTOS, params, IdInstrumentoClass.class);
				
						params = new HashMap<>();
						params.put("Nemotecnico", recDetalle2.Nemotecnico);
						params.put("IdEntidadClasificadoraRiesgo", recDetalle2.RV_IdEntidadClasificadoraRiesgo);
						
						List<InstrumentosRV> Instrumento = _sql.query(conn, SQL_SELECT_INSTRUMENTOS_RV, params, InstrumentosRV.class);
						
						if(Instrumento.size() == 0){
							valorRiesgoRV.IdInstrumento = _IdInstrumento.get(0).IdInstrumento;
							valorRiesgoRV.Entidad = recDetalle2.RV_IdEntidadClasificadoraRiesgo;
							valorRiesgoRV.Valor = recDetalle2.RV_Valor;
							valores.add(valorRiesgoRV);
						}
					}
				}
				if(valores.size() > 0){
					_sql.batch(conn, SQL_INSERT_CARGA_VALOR_RIESGO_RV, valores, ValorRiesgoRV.class);
				}
				
			default:
				break;
			}
		}
		catch(Exception e) {
			log.write(e);
			throw e;
		}
		finally {
			//conn.close();
		}
		log.write("TERMINO");
		return out;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
//		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
//			String json = "{\"p_IdUsuario\":69,\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_FechaProceso\":\"2018-03-19\",\"p_CargaMasivaProcesar\":11,\"p_UsaCarterasSistema\":\"N\"}";
//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2018-03-19\",\"p_CargaMasivaProcesar\":13}";
	
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2017-07-26\",\"p_CargaMasivaProcesar\":271}"; 
			ProcesarCargaMasivaPlataformaEntrada Objin = gson.fromJson(json, ProcesarCargaMasivaPlataformaEntrada.class);
			
			ProcesarCargaMasivaPlataforma demo=new ProcesarCargaMasivaPlataforma();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CuadraturaDiariaSalida ObjOut=demo.logica(con, Objin);
// 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
	    	}
		System.out.println("== END ==");
	}
}