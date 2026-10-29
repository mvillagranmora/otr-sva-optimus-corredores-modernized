package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchSalida;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PrepararOrdenesPostrerasJob implements JavaCall,JavaCallMetadata{

	private static final Logger logger = LogManager.getLogger(PrepararOrdenesPostrerasJob.class);
	private final String SQL_InsertTablaPaso = loadResourceToString("cl/bcs/ordenes/sql/PrepararOrdenesPostrerasJob_InsertTablaPaso.sql");
	private final String SQL_UpdateEstadoTransacciones = loadResourceToString("cl/bcs/ordenes/sql/PrepararOrdenesPostrerasJob_UpdateEstadoTransacciones.sql");
	
	
	public static class ProcesarOrdenesPostrerasBatch_Entrada {
		public Long								p_IdEmpresa;
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public String							p_OrigenPost = "";
		public String							p_simularOperacion;
		public String							p_UsaReglasNegocio;
		public List<CobroModificados_Entrada>	p_CobrosModificados;
		public List<Cobro_Fijo>  				p_CobrosFijos;
		public List<Orden_Entrada>		p_Ordenes;
		public String							p_AplicarComision;
		public String							p_EnviarFacturarAutomatico;
		public String							p_EsAsignacionMasivaPrecioPromedio;
		public String							p_UsaLogDetallado;
		public ProcesarOrdenesPostrerasBatch_Entrada() {
			p_simularOperacion = "";
			p_UsaReglasNegocio = "";
			p_IdModulo = "ORD";
			p_AplicarComision = "S";
			p_EnviarFacturarAutomatico = "N";
			p_EsAsignacionMasivaPrecioPromedio = "N";
			p_UsaLogDetallado = "N";
			p_CobrosModificados = new ArrayList<CobroModificados_Entrada>();
			p_Ordenes = new ArrayList<Orden_Entrada>();
			p_CobrosFijos = new ArrayList<Cobro_Fijo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Fijo {
		public Long IdInterno; 
		public Long IdInternoCobro;
		public Long IdPortafolio; 
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro; 
		public String FormaCobro; 
		public BigDecimal ValorTopeMinimoOrden; 
		public String IdMoneda; 
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CobroModificados_Entrada {
		public Long			IdInterno;
		public String		IdCobro;
		public BigDecimal	ValorCobroAplicado;
	}

	public static class Orden_Entrada extends OrdenPostrera_Entrada{
		public Long			IdTrabajo;
		
		public Orden_Entrada() {
			FlujoOrigen = "INGRESOORDEN";
			IdTrabajo = 0L;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}			
	}
	
	public static class ProcesarOrdenesPostrerasBatch_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_IDPROGRAMA;
		
		
		public ProcesarOrdenesPostrerasBatch_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_IDPROGRAMA = 0L;
			
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class TablaPaso_Eliminar {
		public Long IdTrabajo;
		public TablaPaso_Eliminar(){
			IdTrabajo = 0L;
		}
	}
	
	public static class ActualizarEstadoTransaccion{
		public String IdEstado;
		public Long IdTrabajo;
		ActualizarEstadoTransaccion(){
			IdEstado = "VIG";
			IdTrabajo = 0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ProcesarOrdenesPostrerasBatch_Salida logica (Connection con,ProcesarOrdenesPostrerasBatch_Entrada in ) throws Exception {
		ProcesarOrdenesPostrerasBatch_Salida out = new ProcesarOrdenesPostrerasBatch_Salida();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		for(Orden_Entrada orden : in.p_Ordenes) {
			orden.IdTrabajo = idTrabajo;
		}
		SQL sql=new SQL();
		if(in.p_Ordenes.size() > 0) {
			try {
				logger.debug("INICIO::SQL_InsertTablaPaso");
				
				sql.batch(con,SQL_InsertTablaPaso, in.p_Ordenes, Orden_Entrada.class);
				
				logger.debug("FIN::SQL_InsertTablaPaso");
			}
			catch(Exception e) {
				logger.debug("ERROR::SQL_InsertTablaPaso "+e);
				out.P_OUT_ERRORESCONTADOR++;
				String codigo = "ERROR";
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,codigo,"Error al preparar datos..."));
				return out;
			}
			
			
			
			
			InscribirProcesoBatch procesoBatch = new InscribirProcesoBatch();
			InscribirProcesoBatchEntrada entradaProcesoBatch = new InscribirProcesoBatchEntrada();
			InscribirProcesoBatchSalida salidaProcesoBatch = new InscribirProcesoBatchSalida();
			List<Parametro> parametros = new ArrayList<Parametro>();
			entradaProcesoBatch.p_Parametro = new ArrayList<Parametro>();
			entradaProcesoBatch.p_TipoPrograma = new ArrayList<InTipoPrograma>();
			InTipoPrograma inTipoPrograma = new InTipoPrograma();
			inTipoPrograma.IdInterno = 0L;
			inTipoPrograma.IdTipoPrograma = "CREAR_ORDENES_POSTRERAS_JOB";
			inTipoPrograma.SegundosDesfase = 0L;
			entradaProcesoBatch.p_TipoPrograma.add(inTipoPrograma);
			 entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdTrabajo",idTrabajo.toString(), null));
		    entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdEmpresa",in.p_IdEmpresa.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdUsuario",in.p_IdUsuario.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdModulo",in.p_IdModulo.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"OrigenPost",in.p_OrigenPost.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"simularOperacion",in.p_simularOperacion.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"UsaReglasNegocio",in.p_UsaReglasNegocio.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"AplicarComision",in.p_AplicarComision.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"EnviarFacturarAutomatico",in.p_EnviarFacturarAutomatico.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"EsAsignacionMasivaPrecioPromedio",in.p_EsAsignacionMasivaPrecioPromedio.toString(), null));
			entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"UsaLogDetallado",in.p_UsaLogDetallado.toString(), null));
			
			
			entradaProcesoBatch.p_IdEmpresa = in.p_IdEmpresa;
			entradaProcesoBatch.p_IdUsuario = in.p_IdUsuario;
			entradaProcesoBatch.p_IdModulo = in.p_IdModulo;
			logger.info(entradaProcesoBatch);
			logger.debug("INICIO::InscribirProcesoBatch");
			salidaProcesoBatch= procesoBatch.logica(con, entradaProcesoBatch);
			logger.debug("FIN::InscribirProcesoBatch");
			
			if(salidaProcesoBatch.P_OUT_ERRORESCONTADOR > 0){
				out.P_OUT_MENSAJES.addAll(salidaProcesoBatch.P_OUT_MENSAJES);
				return out;
			}
			
			if(salidaProcesoBatch.P_OUT_PROGRAMA.size() > 0) {
				out.P_OUT_IDPROGRAMA = salidaProcesoBatch.P_OUT_PROGRAMA.get(0).IdPrograma;
				List<ActualizarEstadoTransaccion> listActualizarEstadoTransaccion = new ArrayList<ActualizarEstadoTransaccion>();
				ActualizarEstadoTransaccion estadoTransaccion = new ActualizarEstadoTransaccion();
				estadoTransaccion.IdEstado = "EPR";
				estadoTransaccion.IdTrabajo = idTrabajo;
				listActualizarEstadoTransaccion.add(estadoTransaccion);
				try {
					logger.debug("INICIO::SQL_UpdateEstadoTransacciones");
					sql.batch(con,SQL_UpdateEstadoTransacciones , listActualizarEstadoTransaccion,ActualizarEstadoTransaccion.class);
					logger.debug("FIN::SQL_UpdateEstadoTransacciones");
				}
				catch(Exception e) {
					logger.debug("ERROR::SQL_UpdateEstadoTransacciones "+e);
				}
			}
			
		}
		
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ProcesarOrdenesPostrerasBatch_Entrada in = gson.fromJson(jin.toString(), ProcesarOrdenesPostrerasBatch_Entrada.class);
		ProcesarOrdenesPostrerasBatch_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		setMetadata(in,out);
		return dataout;
	}

	@Override
	public void setMetadata(Object in, Object out) {
		ProcesarOrdenesPostrerasBatch_Salida s = (ProcesarOrdenesPostrerasBatch_Salida)out;
		metadata[0] = s.P_OUT_IDPROGRAMA+"";
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_Ordenes\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"FechaRecepcion\": \"2026-01-05 18:05:04\",\r\n"
					+ "            \"Glosa\": \"\",\r\n"
					+ "            \"IdBoveda\": \"DCV\",\r\n"
					+ "            \"IdFormaCustodia\": \"CUS\",\r\n"
					+ "            \"EsFacturaInmediata\": \"N\",\r\n"
					+ "            \"IdTransaccion\": 2884915,\r\n"
					+ "            \"FolioTransaccion\": \"78946\",\r\n"
					+ "            \"IdPortafolio\": 1285,\r\n"
					+ "            \"IdGrupo\": null,\r\n"
					+ "            \"FolioCartera\": 0,\r\n"
					+ "            \"TipoCambio\": 0,\r\n"
					+ "            \"IdMonedaLiquidacionOrden\": \"CLP\",\r\n"
					+ "            \"TasaVentaCorta\": 0,\r\n"
					+ "            \"PlazoVentaCorta\": 0,\r\n"
					+ "            \"PagoCobroCuentaInversion\": \"S\",\r\n"
					+ "            \"IdPreferenciaPagoPortafolio\": 1243183,\r\n"
					+ "            \"CodigoIFRS\": \"\",\r\n"
					+ "            \"IngresoDesde\": \"COR\",\r\n"
					+ "            \"Cantidad\": 50,\r\n"
					+ "            \"ValorRescate\": 0\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 1,\r\n"
					+ "            \"FechaRecepcion\": \"2026-01-05 18:05:04\",\r\n"
					+ "            \"Glosa\": \"\",\r\n"
					+ "            \"IdBoveda\": \"DCV\",\r\n"
					+ "            \"IdFormaCustodia\": \"CUS\",\r\n"
					+ "            \"EsFacturaInmediata\": \"N\",\r\n"
					+ "            \"IdTransaccion\": 2884915,\r\n"
					+ "            \"FolioTransaccion\": \"78946\",\r\n"
					+ "            \"IdPortafolio\": 1127,\r\n"
					+ "            \"IdGrupo\": null,\r\n"
					+ "            \"FolioCartera\": 0,\r\n"
					+ "            \"TipoCambio\": 0,\r\n"
					+ "            \"IdMonedaLiquidacionOrden\": \"CLP\",\r\n"
					+ "            \"TasaVentaCorta\": 0,\r\n"
					+ "            \"PlazoVentaCorta\": 0,\r\n"
					+ "            \"PagoCobroCuentaInversion\": \"S\",\r\n"
					+ "            \"IdPreferenciaPagoPortafolio\": 2298769,\r\n"
					+ "            \"CodigoIFRS\": \"\",\r\n"
					+ "            \"IngresoDesde\": \"COR\",\r\n"
					+ "            \"Cantidad\": 50,\r\n"
					+ "            \"ValorRescate\": 0\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"p_CobrosModificados\": [],\r\n"
					+ "    \"p_AplicarComision\": \"S\",\r\n"
					+ "    \"p_EnviarFacturarAutomatico\": \"N\",\r\n"
					+ "    \"p_EsAsignacionMasivaPrecioPromedio\": \"S\",\r\n"
					+ "    \"p_UsaLogDetallado\": \"S\"\r\n"
					+ "}";
			ProcesarOrdenesPostrerasBatch_Entrada Objin = gson.fromJson(json, ProcesarOrdenesPostrerasBatch_Entrada.class);
			
			PrepararOrdenesPostrerasJob demo=new PrepararOrdenesPostrerasJob();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
 			try {
 				ProcesarOrdenesPostrerasBatch_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
// 				con.commit();
 				con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
// 			con.commit();
 			con.rollback();
	    	}
		 	
		System.out.println("== END ==");
	}

}
