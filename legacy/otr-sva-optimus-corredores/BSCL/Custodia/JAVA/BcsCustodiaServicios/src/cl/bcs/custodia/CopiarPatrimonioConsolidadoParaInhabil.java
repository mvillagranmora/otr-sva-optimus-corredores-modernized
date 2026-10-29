package cl.bcs.custodia;

/*
 * Objetivo: Copiar patrimonio consolidado de un dia habil a un dia inabil (para feriados y fines de semana)
 *           Este proceso debe inscribirse en la pauta desspues de PAUTA_PATRIMONIO y antes de CAMBIO_FECHA_SISTEMA
 * Fecha:    18/05/2020
 * */
import java.sql.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.utils.SQL;

import cl.bcs.cuentaInversion.GuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja;
import cl.bcs.custodia.GuardarPatrimonioCustodia.EntradaGuardarPatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.PatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.SalidaGuardarPatrimonioCustodia;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;




//jvillagran 2 

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.Feriados;

public class CopiarPatrimonioConsolidadoParaInhabil implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CopiarPatrimonioConsolidadoParaInhabil.class);
	
	private final String SQL_DELETE_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CopiarPatrimonioConsolidadoParaInhabil_DeletePatrimonioCustodia.sql");
	private final String SQL_INSERT_PATRIMONIO_CUSTODIA = loadResourceToString("cl/bcs/custodia/sql/CopiarPatrimonioConsolidadoParaInhabil_InsertPatrimonioCustodia.sql");	
	private final String SQL_DELETE_PATRIMONIO_CAJA 	= loadResourceToString("cl/bcs/cuentaInversion/sql/CopiarPatrimonioConsolidadoParaInhabil_DeletePatrimonioCaja.sql");		
	private final String SQL_INSERT_PATRIMONIO_CAJA 	= loadResourceToString("cl/bcs/cuentaInversion/sql/CopiarPatrimonioConsolidadoParaInhabil_InsertPatrimonioCaja.sql");

	
	public static class EntradaCopiarPatrimonioConsolidadoParaInhabil{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCopiarPatrimonioConsolidadoParaInhabil{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCopiarPatrimonioConsolidadoParaInhabil() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class FechaProceso{
		public String 	FechaOrigen;
		public String 	FechaDestino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//-----------------------------------------------------------
	// CALL
	//-----------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
	
		EntradaCopiarPatrimonioConsolidadoParaInhabil jin = gson.fromJson(in.toString(), EntradaCopiarPatrimonioConsolidadoParaInhabil.class);
		SalidaCopiarPatrimonioConsolidadoParaInhabil out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	

	//-----------------------------------------------------------
	// LOGICA
	//-----------------------------------------------------------
	
	public SalidaCopiarPatrimonioConsolidadoParaInhabil logica(Connection con, EntradaCopiarPatrimonioConsolidadoParaInhabil entrada) throws Exception {
		logger.info("INICIO::CopiarPatrimonioConsolidadoParaInhabil");
		SalidaCopiarPatrimonioConsolidadoParaInhabil out = new SalidaCopiarPatrimonioConsolidadoParaInhabil();

		SQL sql = new SQL();
		ReplicaUtils r = new ReplicaUtils();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		String FechaDestino = entrada.p_Fecha;
		Boolean ContinuarCopiaPatrimonio = true;
		
		while (ContinuarCopiaPatrimonio)
		{
			//-------------------------------------------------------------
			//  OBTENER DIA SIGUIENTE
			//-------------------------------------------------------------
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente  inObtenerDiaSiguiente = new InObtenerDiaSiguiente();

			
			List<FechasIn> lstFechas = new ArrayList<FechasIn>();
			FechasIn inFechas = new FechasIn();
			inFechas.fechaInicial 	= FechaDestino;
			inFechas.numeroDias 	= 1L;
			inFechas.pais 			= idPaisEmpresa;
			lstFechas.add(inFechas);
			
			inObtenerDiaSiguiente.p_idPais 			= idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil 		= "N";  //al sumar dias, considera solo dias habiles
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "N";  //el resultado debe ser un dia habil
			inObtenerDiaSiguiente.p_fechas 			= lstFechas;
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
				
			if (outObtenerDiaSiguiente.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0)
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_EN_OBTENER_DIA_SIGUIENTE",null,con,entrada.p_IdEmpresa));
				return out;
			}

			FechaDestino = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
			logger.debug("FechaDestino > --- " + FechaDestino + "---");
			
			
			//-------------------------------------------------------------			
			// EVALUA SI DIA SIGUIENTE ES INHABIL
			//-------------------------------------------------------------
			//Evalua si el dia es inhabil
			Boolean esDiaHabil = Feriados.esDiaHabil(con, FechaDestino, idPaisEmpresa);
			
			logger.debug("EsDiaHabil > " + esDiaHabil + (esDiaHabil ? " > Entonces Termina proceso" : " > Entonces Copiar Patrimonio"));
			if (esDiaHabil)
			{
				ContinuarCopiaPatrimonio = false; //si dia siguiente es habil, deja de copiar			
				break;
			}
			
			
			//-------------------------------------------------------------			
			// ELIMINA ACTUAL PATRIMONIO CUSTODIA Y CAJA (Del dia inhabi)
			//-------------------------------------------------------------
			{
				List<FechaProceso> lstPatrimonioEliminar = new ArrayList<FechaProceso>();
				FechaProceso objPatrimonioEliminar = new FechaProceso();
				objPatrimonioEliminar.FechaDestino = FechaDestino;
				lstPatrimonioEliminar.add(objPatrimonioEliminar);
				
				sql.batch(con, SQL_DELETE_PATRIMONIO_CUSTODIA, lstPatrimonioEliminar, FechaProceso.class);				
				sql.batch(con, SQL_DELETE_PATRIMONIO_CAJA, lstPatrimonioEliminar, FechaProceso.class);
				logger.debug("Limpia Patrimonio de > " + FechaDestino);
			}
			
			
			//-------------------------------------------------------------			
			// COPIAR PATRIMONIO CUSTODIA Y CJA DE UN DIA HABIL A UN DIA INHABIL
			//-------------------------------------------------------------
			{
				List<FechaProceso> lstFechaCopia = new ArrayList<FechaProceso>();
				FechaProceso objFechaCopia = new FechaProceso();			
				objFechaCopia.FechaOrigen = entrada.p_Fecha;
				objFechaCopia.FechaDestino = FechaDestino;			
				lstFechaCopia.add(objFechaCopia);	
				
				sql.batch(con, SQL_INSERT_PATRIMONIO_CUSTODIA, lstFechaCopia, FechaProceso.class);
				sql.batch(con, SQL_INSERT_PATRIMONIO_CAJA, lstFechaCopia, FechaProceso.class);				
				logger.debug("Copia Patrimonio del  > " + entrada.p_Fecha +" a "+ FechaDestino);
				
				r.guardarEnvioPostPauta(con, "CuentaInversion_PatrimonioCajaFechaLiquidacion", entrada.p_Fecha, FechaDestino);
				r.guardarEnvioPostPauta(con, "CuentaInversion_PatrimonioCajaFechaOperacion", entrada.p_Fecha, FechaDestino);
				r.guardarEnvioPostPauta(con, "Custodia_PatrimonioConsolidadoFechaLiquidacion", entrada.p_Fecha, FechaDestino);
				r.guardarEnvioPostPauta(con, "Custodia_PatrimonioConsolidadoFechaOperacion", entrada.p_Fecha, FechaDestino);
				r.guardarEnvioPostPauta(con, "Custodia_PatrimonioCustodiaFechaLiquidacion", entrada.p_Fecha, FechaDestino);
				r.guardarEnvioPostPauta(con, "Custodia_PatrimonioCustodiaFechaOperacion", entrada.p_Fecha, FechaDestino);

			}
			
		}  // while (ContinuarCopiaPatrimonio)
		

		logger.info("FIN::CopiarPatrimonioConsolidadoParaInhabil");
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"RESPALDO_PATRIMONIO_DIA_INHABIL_OK",null,con,entrada.p_IdEmpresa));
		return out;
	}

	
	
	//-----------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-----------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getMBIConnection()) {			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"CUS\",\"p_Fecha\":\"2022-02-22\"}";
			
			EntradaCopiarPatrimonioConsolidadoParaInhabil Objin = gson.fromJson(json, EntradaCopiarPatrimonioConsolidadoParaInhabil.class);
			
			CopiarPatrimonioConsolidadoParaInhabil demo = new CopiarPatrimonioConsolidadoParaInhabil();				
			try {
				SalidaCopiarPatrimonioConsolidadoParaInhabil ObjOut = demo.logica(con, Objin);
				con.commit();
				//con.rollback();
				System.out.println("SALIDA > " + ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		
			con.rollback(); 			
    	}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

	
	
}
