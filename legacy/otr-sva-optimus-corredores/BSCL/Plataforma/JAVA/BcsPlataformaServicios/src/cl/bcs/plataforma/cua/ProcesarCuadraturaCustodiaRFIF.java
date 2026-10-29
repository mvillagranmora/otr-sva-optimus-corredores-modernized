package cl.bcs.plataforma.cua;

import java.math.BigDecimal;
import java.sql.Connection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCuadraturaCustodiaRFIF implements JavaCall {
	static final Logger logger = LogManager.getLogger(ProcesarCuadraturaCustodiaRFIF.class);
	private final String LIMPIA_TABLA_SALDOS = "DELETE FROM \"BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF\";";	
	private final String LIMPIA_TABLA_MOVIMIENTOS = "DELETE FROM \"BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF\";";
	
	private final String CARGA_INICIAL = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaInicialCuadraturaRFIF.sql");
	private final String CARGA_SFINAL = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaSaldoFinalCuadraturaRFIF.sql");
	private final String CARGA_MOVMESAPACTO = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosMesaPacto.sql");
	private final String CARGA_MOVMANUALES = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosManuales.sql");
	private final String CARGA_MOVFACTURACION = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosFacturacion.sql");
	//private final String CARGA_MOVEVECAP = this.loadResourceToString("cl/bcs/plataforma/cua/sql/ProcesarCuadraturaCustodia_CargaMovimientosEventosCapital.sql");

	private final String CARGA_SFINALIF = "UPDATE \"BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF\" scuad  "
			+"SET scuad.\"SaldoFinalSistema\" = ( "
			+"SELECT SUM(CASE WHEN cart.\"IdClasificacionTipoInstrumento\"='IF' THEN cart.\"INGValorRescate\"  "
			+"ELSE cart.\"INGCantidad\" END) AS INGCantidad FROM \"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraUI\" cart "
			+"WHERE cart.\"IdClasificacionTipoInstrumento\" in ('IF') AND cart.\"IdEstado\"  NOT IN ('ANU','RPA') AND cart.\"IdTipoInstrumento\" NOT IN ('SIM','PRT','EMP','PAC', 'FWD','RRV','RRF') "
			+"AND (cart.\"FechaOperacion\" <= :p_FechaFin AND cart.\"EGRCantidad\" = 0 AND cart.\"IdEstado\" = 'VIG') "
			+"AND (cart.\"DocumentoIdentidad\"=scuad.\"DocumentoIdentidad\" AND scuad.\"IdPortafolio\"=cart.\"IdPortafolio\" AND scuad.\"IdInstrumento\"=cart.\"IdInstrumento\" AND scuad.\"CodigoPortafolio\"=cart.\"CodigoPortafolio\" AND scuad.\"Nemotecnico\"=cart.\"NemoSVS\") "
			+"GROUP BY cart.\"DocumentoIdentidad\",cart.\"IdPortafolio\",cart.\"IdInstrumento\" "
			+")  WHERE scuad.\"IdClasificacionTipoInstrumento\"='IF'; ";
	
	private final String CARGA_SFINALIFN = "UPDATE \"BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF\" scuad "
			+"SET scuad.\"SaldoFinalSistema\" = 0 "
			+"WHERE scuad.\"SaldoFinalSistema\" IS NULL ";
	
	private final String MOD_SALDO_CALCULADO = "UPDATE \"BSCL.Plataforma.BD::CUA.SaldosCuadraturaRF\" scua "
	+"SET scua.\"SaldoFinalCalculado\" = CASE WHEN (SELECT scua.\"SaldoInicial\" - sum(mcua.\"MontoOperacionFinal\") FROM BSCL.\"BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF\" mcua "
	+"WHERE mcua.\"DocumentoIdentidad\"=scua.\"DocumentoIdentidad\" AND mcua.\"IdPortafolio\"=scua.\"IdPortafolio\" AND mcua.\"IdInstrumento\"=scua.\"IdInstrumento\" "
	+"GROUP BY \"DocumentoIdentidad\",\"IdPortafolio\",\"IdInstrumento\") IS NULL then scua.\"SaldoInicial\" ELSE (SELECT scua.\"SaldoInicial\" - sum(mcua.\"MontoOperacionFinal\") FROM BSCL.\"BSCL.Plataforma.BD::CUA.MovimientosCustodiaRF\" mcua "
	+"WHERE mcua.\"DocumentoIdentidad\"=scua.\"DocumentoIdentidad\" AND mcua.\"IdPortafolio\"=scua.\"IdPortafolio\" AND mcua.\"IdInstrumento\"=scua.\"IdInstrumento\" "
	+"GROUP BY \"DocumentoIdentidad\",\"IdPortafolio\",\"IdInstrumento\") END ";
	

		
	public static class FechaOperacion {
		public String p_FechaInicio;
		public String p_FechaFin;
	}
	
	public static class SaldoCalculado {
		public String p_FechaInicio;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal Saldo;
		public BigDecimal Garantia;
		public BigDecimal VentaPlazo;
		public BigDecimal CompraPlazo;					
	}
	

	public static class EntradaProcesarCuadraturaCustodiaRFIF {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaFin;
		
	}


	public static class SalidaProcesarCuadraturaCustodia {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaProcesarCuadraturaCustodia() {
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MensajesErrores
	{
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaProcesarCuadraturaCustodiaRFIF in = gson.fromJson(jin.toString(), EntradaProcesarCuadraturaCustodiaRFIF.class);
		SalidaProcesarCuadraturaCustodia out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	public SalidaProcesarCuadraturaCustodia logica(Connection con, cl.bcs.plataforma.cua.ProcesarCuadraturaCustodiaRFIF.EntradaProcesarCuadraturaCustodiaRFIF e) throws Exception {
		logger.info("Inicio servicio ProcesarCuadraturaCustodiaRFIF");
		SalidaProcesarCuadraturaCustodia s = new SalidaProcesarCuadraturaCustodia();
		s.P_OUT_ERRORESCONTADOR=0L;
		SQL sql = new SQL();
		List<FechaOperacion> paramIn = new ArrayList<>();
		
		FechaOperacion fo = new FechaOperacion();
		fo.p_FechaInicio = e.p_FechaInicio;
		fo.p_FechaFin = e.p_FechaFin;
		paramIn.add(fo);
		
		HashMap<String,String> mapMsg = new HashMap<>();
		
		java.util.Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(e.p_FechaInicio); 			    
		java.util.Date date2=new SimpleDateFormat("yyyy-MM-dd").parse(e.p_FechaFin);
		
		if(date1.after(date2) || date1.equals(date2)) {
			s.P_OUT_ERRORESCONTADOR=1L;
			s.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FECHA_INICIAL_DEBE_SER_MENOR_FECHA_FINAL",mapMsg,con,e.p_IdEmpresa));

		}else {
			SQL.exec(con,LIMPIA_TABLA_SALDOS);
			SQL.exec(con,LIMPIA_TABLA_MOVIMIENTOS);			
			sql.batch(con, CARGA_INICIAL, paramIn, FechaOperacion.class);					
			sql.batch(con, CARGA_SFINAL, paramIn, FechaOperacion.class);
			sql.batch(con, CARGA_SFINALIF, paramIn, FechaOperacion.class);
			sql.batch(con, CARGA_SFINALIFN, paramIn, FechaOperacion.class);
			sql.batch(con, CARGA_MOVMESAPACTO, paramIn, FechaOperacion.class);
			sql.batch(con, CARGA_MOVMANUALES, paramIn, FechaOperacion.class);
			sql.batch(con, CARGA_MOVFACTURACION, paramIn, FechaOperacion.class);
			//sql.batch(con, CARGA_MOVEVECAP, paramIn, FechaOperacion.class);
			SQL.exec(con,MOD_SALDO_CALCULADO);	
		}
		return s;
	}


	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = 	"{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"CUS\",\"p_FechaInicio\":\"2022-06-03\",\"p_FechaFin\":\"2022-06-13\"}";
			
			EntradaProcesarCuadraturaCustodiaRFIF entrada = gson.fromJson(json, EntradaProcesarCuadraturaCustodiaRFIF.class);
		
			ProcesarCuadraturaCustodiaRFIF c = new ProcesarCuadraturaCustodiaRFIF();
		    SalidaProcesarCuadraturaCustodia ObjOut=c.logica(con, entrada);
			System.out.println(ObjOut);		
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
