package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjERRORESDETALLE;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularMovFacturarAsignacionMasivaWrapper implements JavaCall{
	private final Log log = new Log("FAC.AnularMovFacturarAsignacionMasiva");
	
	public static class ObjERRORESDETALLES {
		public String	Codigo;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjADVERTENCIASDETALLES {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjAsignaciones {
		public Long					IdAsignacion;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	/*public static class ObjTableCoberturaSimulacion {
		public Long					IdReferencial;
		public Long					IdInstrumento;
		public BigDecimal			Cantidad;
		public BigDecimal			PrecioMercado;
		public BigDecimal			Monto; 
		public BigDecimal			PorcentajeCoberturaEntidad;
		public BigDecimal			MontoCoberturaEntidad;
		public BigDecimal			PorcentajeCoberturaCorredora;
		public BigDecimal			MontoCoberturaCorredora;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};*/
	
	/*public static class ObjTableCoberturaPortafolio {
		public BigDecimal			MontoCoberturaEntidad;
		public BigDecimal			MontoCoberturaCorredora;
		public String toString() {
			return Utils.printJson(this, false);
		};
	};*/
	
	public static class ObjSalidaResumen {
		public Long			IdInterno; 
		public Long			IdOperacion; 
		public String		Resultado; 
		public Long  		OK;
		public Long         ADVERTENCIA;
		public Long         AUTORIZACION;
		public Long         RECHAZO; 
	}
	
		
	public static class ObjdatosAsignacion {
		public Long			IdAsignacion; 
		
	}
	
	public static class AnularMovFacturarAsignacionMasivaWrapperEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		//public String						p_UsaReglasNegocio;
		//public Long 						p_IdPortafolio;
		//public Long 						p_IdEntidadDestinoGarantia;
		//public String 						p_IdTipoInstrumentoExigeGarantia;
		//public Long 						p_IdInstrumentoExigeGarantia;
		public List<ObjAsignaciones>	    p_Asignaciones;
		
		public AnularMovFacturarAsignacionMasivaWrapperEntrada (){			
			p_Asignaciones	= new ArrayList<ObjAsignaciones>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class AnularMovFacturarAsignacionMasivaWrapperSalida{
		public Long									P_OUT_ERRORESCONTADOR;
		public Long									P_OUT_ADVERTENCIASCONTADOR;
		public List<ObjSalidaResumen>	            p_out_SalidaResumen;
		//public List<ObjTableCoberturaPortafolio>	p_out_TableCoberturaPortafolio;
		public List<ObjERRORESDETALLES>				p_out_ErroresDetalle;
		public List<ObjADVERTENCIASDETALLES>		p_out_AdvertenciasDetalle;

		public AnularMovFacturarAsignacionMasivaWrapperSalida (){

			//p_out_TableCoberturaSimulacion	= new ArrayList<ObjTableCoberturaSimulacion>();
			p_out_SalidaResumen            	= new ArrayList<ObjSalidaResumen>();
			p_out_ErroresDetalle			= new ArrayList<ObjERRORESDETALLES>();
			p_out_AdvertenciasDetalle		= new ArrayList<ObjADVERTENCIASDETALLES>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public AnularMovFacturarAsignacionMasivaWrapperSalida logica(Connection con, AnularMovFacturarAsignacionMasivaWrapperEntrada in) throws Exception {
		AnularMovFacturarAsignacionMasivaWrapperSalida out = new AnularMovFacturarAsignacionMasivaWrapperSalida();
		SQL sql = new SQL();
		
		try {
			
			// ------------------------------
			// Crea las tablas temporales
			SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #AnularMovFacturarAsignacionMasiva_p_asignaciones (\"IdAsignacion\" BIGINT)");
			// ------------------------------
			// Inserta tablas temporales globales
			sql.batch(con, "INSERT INTO #AnularMovFacturarAsignacionMasiva_p_asignaciones (\"IdAsignacion\") VALUES (:IdAsignacion)",in.p_Asignaciones, ObjAsignaciones.class);
			
			List<ObjdatosAsignacion> dato_p_Asignacion = new ArrayList<ObjdatosAsignacion>();
			List<ObjdatosAsignacion> datosAsigna = new ArrayList<ObjdatosAsignacion>();
			
			
			Utils.copyPojo( in.p_Asignaciones, datosAsigna);
			dato_p_Asignacion.addAll(datosAsigna);
			
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			//parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			//parmsIn.put("p_IdPortafolio", in.p_IdPortafolio);
			//parmsIn.put("p_IdEntidadDestinoGarantia", in.p_IdEntidadDestinoGarantia);
			//parmsIn.put("p_IdTipoInstrumentoExigeGarantia", in.p_IdTipoInstrumentoExigeGarantia);
			parmsIn.put("p_asignaciones", in.p_Asignaciones);
			
			//Escalares de salida:
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_SalidaResumen", ObjSalidaResumen.class);
			//parmsOut.put("p_out_TableCoberturaPortafolio", ObjTableCoberturaPortafolio.class);
			parmsOut.put("p_out_AdvertenciasContador", Long.class);
			parmsOut.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLES.class);
			parmsOut.put("p_out_ErroresContador", Long.class);
			parmsOut.put("p_out_ErroresDetalle", ObjERRORESDETALLES.class);
			
			//Realiza el call
			log.write(in.p_Asignaciones);
			log.write(parmsIn);
			
			// ------------------------------
			// Ejecuta el SP
			
			String queryCall = "call \"BSCL.Orden.BD.Servicios::AnularMovFacturarAsignacionMasiva\"(p_IdEmpresa=>:p_IdEmpresa,p_IdUsuario=>:p_IdUsuario,p_IdModulo=>:p_IdModulo,p_asignaciones=>#    ,p_out_SalidaResumen=>$p_out_SalidaResumen,p_out_AdvertenciasContador=>@p_out_AdvertenciasContador, p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle, p_out_ErroresContador=>@p_out_ErroresContador,p_out_ErroresDetalle=>$p_out_ErroresDetalle)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIn, parmsOut);
			
			log.write(rpt);

			
			out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

			// ------------------------------

			out.p_out_SalidaResumen = (List<ObjSalidaResumen>) rpt.get("p_out_SalidaResumen");
			//out.p_out_TableCoberturaPortafolio = (List<ObjTableCoberturaPortafolio>) rpt.get("p_out_TableCoberturaPortafolio");
			
			out.p_out_ErroresDetalle = (List<ObjERRORESDETALLES>) rpt.get("p_out_ErroresDetalle");
			out.p_out_AdvertenciasDetalle = (List<ObjADVERTENCIASDETALLES>) rpt.get("p_out_AdvertenciasDetalle");

		}
		finally {
			SQL.exec(con, "DROP TABLE #CalcularCobertura_P_SIMULACION");
		}
		log.write(out);
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AnularMovFacturarAsignacionMasivaWrapperEntrada jin=gson.fromJson(in.toString() ,AnularMovFacturarAsignacionMasivaWrapperEntrada.class);
		AnularMovFacturarAsignacionMasivaWrapperSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
};