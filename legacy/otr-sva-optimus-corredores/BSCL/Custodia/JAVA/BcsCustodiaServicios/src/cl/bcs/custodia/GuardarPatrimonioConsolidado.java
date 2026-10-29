package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.GuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja;
import cl.bcs.custodia.GuardarPatrimonioCustodia.EntradaGuardarPatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.SalidaGuardarPatrimonioCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class GuardarPatrimonioConsolidado implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(GuardarPatrimonioConsolidado.class);
	
	public static class EntradaGuardarPatrimonioConsolidado{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGuardarPatrimonioConsolidado{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarPatrimonioConsolidado() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGuardarPatrimonioConsolidado jin = gson.fromJson(in.toString(), EntradaGuardarPatrimonioConsolidado.class);
		SalidaGuardarPatrimonioConsolidado out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGuardarPatrimonioConsolidado logica(Connection con, EntradaGuardarPatrimonioConsolidado entrada) throws Exception {
		logger.info("INICIO::EntradaGuardarPatrimonioConsolidado");
		SalidaGuardarPatrimonioConsolidado out = new SalidaGuardarPatrimonioConsolidado();
		
		{
			logger.info("Comienza patrimonio custodia");
			EntradaGuardarPatrimonioCustodia entradaGuardarPatrimonioCustodia = new EntradaGuardarPatrimonioCustodia();
			entradaGuardarPatrimonioCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaGuardarPatrimonioCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaGuardarPatrimonioCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaGuardarPatrimonioCustodia.p_Fecha = entrada.p_Fecha;
			GuardarPatrimonioCustodia guardarPatrimonioCustodia = new GuardarPatrimonioCustodia();
			SalidaGuardarPatrimonioCustodia salidaGuardarPatrimonioConsolidado = guardarPatrimonioCustodia.logica(con, entradaGuardarPatrimonioCustodia);
			out.P_OUT_ERRORESCONTADOR += salidaGuardarPatrimonioConsolidado.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaGuardarPatrimonioConsolidado.P_OUT_MENSAJES);
			logger.info("Finaliza patrimonio custodia");
			
		}
		
		{
			logger.info("Comienza patrimonio caja");
			EntradaGuardarPatrimonioCaja entradaGuardarPatrimonioCaja = new EntradaGuardarPatrimonioCaja();
			entradaGuardarPatrimonioCaja.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaGuardarPatrimonioCaja.p_IdUsuario = entrada.p_IdUsuario;
			entradaGuardarPatrimonioCaja.p_IdModulo = entrada.p_IdModulo;
			entradaGuardarPatrimonioCaja.p_Fecha = entrada.p_Fecha;
			GuardarPatrimonioCaja guardarPatrimonioCaja = new GuardarPatrimonioCaja();
			SalidaGuardarPatrimonioCaja salidaGuardarPatrimonioCaja = guardarPatrimonioCaja.logica(con, entradaGuardarPatrimonioCaja);
			out.P_OUT_ERRORESCONTADOR += salidaGuardarPatrimonioCaja.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaGuardarPatrimonioCaja.P_OUT_MENSAJES);
			logger.info("Finaliza patrimonio caja");
		}
		
		ReplicaUtils r = new ReplicaUtils();
		r.guardarEnvioPostPauta(con, "CuentaInversion_PatrimonioCajaFechaLiquidacion", entrada.p_Fecha, entrada.p_Fecha);
		r.guardarEnvioPostPauta(con, "CuentaInversion_PatrimonioCajaFechaOperacion", entrada.p_Fecha, entrada.p_Fecha);
		r.guardarEnvioPostPauta(con, "Custodia_PatrimonioConsolidadoFechaLiquidacion", entrada.p_Fecha, entrada.p_Fecha);
		r.guardarEnvioPostPauta(con, "Custodia_PatrimonioConsolidadoFechaOperacion", entrada.p_Fecha, entrada.p_Fecha);
		r.guardarEnvioPostPauta(con, "Custodia_PatrimonioCustodiaFechaLiquidacion", entrada.p_Fecha, entrada.p_Fecha);
		r.guardarEnvioPostPauta(con, "Custodia_PatrimonioCustodiaFechaOperacion", entrada.p_Fecha, entrada.p_Fecha);
		
		logger.info("FIN::EntradaGuardarPatrimonioConsolidado");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
			EntradaGuardarPatrimonioConsolidado Objin = new EntradaGuardarPatrimonioConsolidado();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "CUS";
		Objin.p_IdUsuario = 21L;
		Objin.p_Fecha = "2019-07-31";
		GuardarPatrimonioConsolidado  demo=new GuardarPatrimonioConsolidado();				
		try {
			SalidaGuardarPatrimonioConsolidado ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
