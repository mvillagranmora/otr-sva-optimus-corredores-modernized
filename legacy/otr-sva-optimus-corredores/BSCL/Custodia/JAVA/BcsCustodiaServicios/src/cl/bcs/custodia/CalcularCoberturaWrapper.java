package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjERRORESDETALLE;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;*/
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CalcularCoberturaWrapper implements JavaCall{
	private final Log log = new Log("CrearCargoAbono");
	
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
	
	public static class ObjCalcularCobertura {
		public Long					IdReferencial;
		public Long					IdInstrumento;
		public BigDecimal			Cantidad;
		public BigDecimal			PrecioMercado;
		public BigDecimal			Monto;
		public BigDecimal			MontoCoberturaSimular;
		public ObjCalcularCobertura(){
			this.MontoCoberturaSimular = BigDecimal.ZERO;
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjTableCoberturaSimulacion {
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
	};
	
	public static class ObjTableCoberturaPortafolio {
		public BigDecimal			MontoCoberturaEntidad;
		public BigDecimal			MontoCoberturaCorredora;
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjInformacionDetalle {
		public Long			Obj; 
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion; 
	}
	
	public static class ObjdatosSimulacion {
		public Long			IdReferencial; 
		public Long			IdInstrumento; 
		public BigDecimal	Cantidad;
		public BigDecimal	PrecioMercado;
		public BigDecimal	Monto;
		public BigDecimal MontoCoberturaSimular;
	}
	
	public static class CalcularCoberturaWrapperEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_UsaReglasNegocio;
		public Long 						p_IdPortafolio;
		public Long 						p_IdEntidadDestinoGarantia;
		public String 						p_IdTipoInstrumentoExigeGarantia;
		public Long 						p_IdInstrumentoExigeGarantia;
		public List<ObjCalcularCobertura>	p_Simulacion;
		
		public CalcularCoberturaWrapperEntrada (){			
			p_Simulacion	= new ArrayList<ObjCalcularCobertura>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class CalcularCoberturaWrapperSalida{
		public Long									P_OUT_ERRORESCONTADOR;
		public Long									P_OUT_ADVERTENCIASCONTADOR;
		public List<ObjTableCoberturaSimulacion>	p_out_TableCoberturaSimulacion;
		public List<ObjTableCoberturaPortafolio>	p_out_TableCoberturaPortafolio;
		public List<ObjERRORESDETALLES>				p_out_ErroresDetalle;
		public List<ObjADVERTENCIASDETALLES>		p_out_AdvertenciasDetalle;

		public CalcularCoberturaWrapperSalida (){

			p_out_TableCoberturaSimulacion	= new ArrayList<ObjTableCoberturaSimulacion>();
			p_out_TableCoberturaPortafolio	= new ArrayList<ObjTableCoberturaPortafolio>();
			p_out_ErroresDetalle			= new ArrayList<ObjERRORESDETALLES>();
			p_out_AdvertenciasDetalle		= new ArrayList<ObjADVERTENCIASDETALLES>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public CalcularCoberturaWrapperSalida logica(Connection con, CalcularCoberturaWrapperEntrada in) throws Exception {
		CalcularCoberturaWrapperSalida out = new CalcularCoberturaWrapperSalida();
		SQL sql = new SQL();
		
		try {
			
			// ------------------------------
			// Crea las tablas temporales
			SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #CalcularCobertura_P_SIMULACION (\"IdReferencial\" BIGINT,\"IdInstrumento\" BIGINT, \"Cantidad\" DECIMAL(26,8), \"PrecioMercado\" DECIMAL(26,8), \"Monto\" DECIMAL(26,8), \"MontoCoberturaSimular\" DECIMAL(26,8))");
			// ------------------------------
			// Inserta tablas temporales globales
			sql.batch(con, "INSERT INTO #CalcularCobertura_P_SIMULACION (\"IdReferencial\",\"IdInstrumento\", \"Cantidad\", \"PrecioMercado\", \"Monto\", \"MontoCoberturaSimular\") VALUES (:IdReferencial,:IdInstrumento, :Cantidad, :PrecioMercado, :Monto,:MontoCoberturaSimular)",in.p_Simulacion, ObjCalcularCobertura.class);
			
			List<ObjdatosSimulacion> dato_p_Simulacion = new ArrayList<ObjdatosSimulacion>();
			List<ObjdatosSimulacion> datosSimula = new ArrayList<ObjdatosSimulacion>();
			
			
			Utils.copyPojo( in.p_Simulacion, datosSimula);
			dato_p_Simulacion.addAll(datosSimula);
			
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIn.put("p_IdPortafolio", in.p_IdPortafolio);
			parmsIn.put("p_IdEntidadDestinoGarantia", in.p_IdEntidadDestinoGarantia);
			parmsIn.put("p_IdTipoInstrumentoExigeGarantia", in.p_IdTipoInstrumentoExigeGarantia);
			parmsIn.put("p_IdInstrumentoExigeGarantia", in.p_IdInstrumentoExigeGarantia);
			
			//Escalares de salida:
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_TableCoberturaSimulacion", ObjTableCoberturaSimulacion.class);
			parmsOut.put("p_out_TableCoberturaPortafolio", ObjTableCoberturaPortafolio.class);
			parmsOut.put("p_out_AdvertenciasContador", Long.class);
			parmsOut.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLES.class);
			parmsOut.put("p_out_ErroresContador", Long.class);
			parmsOut.put("p_out_ErroresDetalle", ObjERRORESDETALLES.class);
			
			//Realiza el call
			log.write(in.p_Simulacion);
			log.write(parmsIn);
			
			// ------------------------------
			// Ejecuta el SP
			
			String queryCall = "call \"BSCL.Plataforma.BD.Servicios::CalcularCobertura\"(p_IdEmpresa=>:p_IdEmpresa,p_IdUsuario=>:p_IdUsuario,p_IdModulo=>:p_IdModulo,p_UsaReglasNegocio=>'S',p_IdPortafolio=>:p_IdPortafolio,p_IdEntidadDestinoGarantia=>:p_IdEntidadDestinoGarantia,p_IdTipoInstrumentoExigeGarantia=>:p_IdTipoInstrumentoExigeGarantia,p_IdInstrumentoExigeGarantia=>:p_IdInstrumentoExigeGarantia,p_Simulacion=>#CalcularCobertura_P_SIMULACION,p_out_TableCoberturaSimulacion=>$p_out_TableCoberturaSimulacion,p_out_TableCoberturaPortafolio=>$p_out_TableCoberturaPortafolio, p_out_AdvertenciasContador=>@p_out_AdvertenciasContador, p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle, p_out_ErroresContador=>@p_out_ErroresContador,p_out_ErroresDetalle=>$p_out_ErroresDetalle)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIn, parmsOut);
			
			log.write(rpt);

			
			out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

			// ------------------------------

			out.p_out_TableCoberturaSimulacion = (List<ObjTableCoberturaSimulacion>) rpt.get("p_out_TableCoberturaSimulacion");
			out.p_out_TableCoberturaPortafolio = (List<ObjTableCoberturaPortafolio>) rpt.get("p_out_TableCoberturaPortafolio");
			
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
		CalcularCoberturaWrapperEntrada jin=gson.fromJson(in.toString() ,CalcularCoberturaWrapperEntrada.class);
		CalcularCoberturaWrapperSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
};