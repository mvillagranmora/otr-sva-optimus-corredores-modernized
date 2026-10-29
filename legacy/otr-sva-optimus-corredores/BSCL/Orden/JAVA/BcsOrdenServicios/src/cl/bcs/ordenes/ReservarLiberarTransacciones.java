package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ReservarLiberarTransacciones implements JavaCall {
	
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ReservarLiberarTransacciones_SelectTransacciones.sql");

	private final String SQL_SELECT_GRUPO_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ReservarLiberarTransacciones_SelectGrupoTransacciones.sql");
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ReservarLiberarTransacciones_UpdateTransacciones.sql");
	

	private final String SQL_SELECT_USUARIO = "SELECT \"CodigoUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = :IdUsuario";
	
	
	//-----------------------------------------------------------------------
	// CALL
	//-----------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ReservarLiberarTransacciones_Entrada in=gson.fromJson(jin.toString() ,ReservarLiberarTransacciones_Entrada.class);
		ReservarLiberarTransacciones_Salida out=logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
	

		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	//-----------------------------------------------------------------------
	// LOGICA
	//-----------------------------------------------------------------------
	public ReservarLiberarTransacciones_Salida logica(Connection conn,ReservarLiberarTransacciones_Entrada entrada) throws Exception {
		SQL SQL = new SQL();
		ReservarLiberarTransacciones_Salida salida = new ReservarLiberarTransacciones_Salida();
		

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String PermitePalosNegociacionReservado = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
		
		
		List<Long> idsTransaccion = new ArrayList<>();
		for (Transaccion_Entrada tran :entrada.p_Transacciones) {
			if (!idsTransaccion.contains(tran.IdTransaccion))
				idsTransaccion.add(tran.IdTransaccion);
		}
		
		HashMap<Long,Transaccion> transacciones = new HashMap<>();
		{
			HashMap<String,Object> params = new HashMap<>();

			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdsTransaccion", idsTransaccion);

			List<Transaccion> _transacciones = SQL.query(conn, SQL_SELECT_TRANSACCIONES, params, Transaccion.class);
			for (Transaccion tran : _transacciones) {
				transacciones.put(tran.IdTransaccion, tran);
			}
			
		}
		
		List<Transaccion> transaccionesOK = new ArrayList<>();
		for (Transaccion_Entrada tran : entrada.p_Transacciones) {
			
			Transaccion t = transacciones.get(tran.IdTransaccion);

			

			if (t == null) {
				String codigo = "TRANSACCION_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			}
			else if (t.Bloqueado.equalsIgnoreCase("S")) {
				String codigo = "TRANSACCION_BLOQUEADA";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			}
			else if (t.Reservado.equalsIgnoreCase("S") && entrada.p_Accion.equalsIgnoreCase("R")) {

				HashMap<String, String> value = new HashMap<String, String>();
				value.put("FolioTransaccion", t.Folio);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_INFORMACION,"TRANSACCION_YA_RESERVADA", value ,conn,entrada.p_IdEmpresa));
				
				
			}
			else if (t.Reservado.equalsIgnoreCase("N") && entrada.p_Accion.equalsIgnoreCase("L")) {

				HashMap<String, String> value = new HashMap<String, String>();
				value.put("FolioTransaccion", t.Folio);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_INFORMACION,"TRANSACCION_NO_RESERVADA", value ,conn,entrada.p_IdEmpresa));
				
			}
			else if (t.Reservado.equalsIgnoreCase("N") && entrada.p_Accion.equalsIgnoreCase("R")) {
				Transaccion tx = new Transaccion();
				tx.IdTransaccion = t.IdTransaccion;
				tx.Reservado = "S";
				if("S".equalsIgnoreCase(PermitePalosNegociacionReservado)) {
					tx.IdUsuarioReserva = (t.IdUsuarioReserva != null )? t.IdUsuarioReserva:entrada.p_IdUsuario;
				}else{
					tx.IdUsuarioReserva = entrada.p_IdUsuario;
				}
				tx.MotivoReserva = entrada.p_Motivo;
				tx.Folio = t.Folio;
				tx.IdTipoOperacion = t.IdTipoOperacion;
				tx.Nemotecnico = t.Nemotecnico;
				tx.Cantidad = t.Cantidad;
				tx.Monto = t.Monto;
				

				//IdGrupoReserva es un identificador de transaciones agrupadas por refacturacion
				tx.IdGrupoReserva = (entrada.p_Origen.equalsIgnoreCase("REFACTURACION")) ? generarIdGrupoReserva(t, fechaSistema, entrada.p_IdUsuario,entrada.p_IdEmpresa,SQL,conn) : "";
				

				
				transaccionesOK.add(tx);
				


				HashMap<String, String> value = new HashMap<String, String>();
				value.put("FolioTransaccion", t.Folio);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_INFORMACION,"TRANSACCION_RESERVADA", value ,conn,entrada.p_IdEmpresa));
				
				
			}
			else if (t.Reservado.equalsIgnoreCase("S") && entrada.p_Accion.equalsIgnoreCase("L")) {
				Transaccion tx = new Transaccion();
				tx.IdTransaccion = t.IdTransaccion;
				tx.Reservado = "N";
				tx.IdUsuarioReserva = null;
				tx.MotivoReserva = "";
				tx.Folio = t.Folio;
				tx.IdTipoOperacion = t.IdTipoOperacion;
				tx.Nemotecnico = t.Nemotecnico;
				tx.Cantidad = t.Cantidad;
				tx.Monto = t.Monto;
				

				tx.IdGrupoReserva = "";
				
				
				transaccionesOK.add(tx);
				

				HashMap<String, String> value = new HashMap<String, String>();
				value.put("FolioTransaccion", t.Folio);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_INFORMACION,"TRANSACCION_LIBERADA", value ,conn,entrada.p_IdEmpresa));
				
				
			}

		}


		
		//AGREGAR LOG
		{
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;
			for (Transaccion tran : transaccionesOK) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo = 1L;
				log.IdCabeceraLog = tran.IdTransaccion;
				log.IdTipoLog = "INFORMATIVO";
				log.IdModulo = entrada.p_IdModulo;
				log.IdUsuario = entrada.p_IdUsuario;
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				if("S".equalsIgnoreCase(PermitePalosNegociacionReservado)) {
					log.IdUsuarioAtendido = (tran.IdUsuarioReserva != null )? tran.IdUsuarioReserva:entrada.p_IdUsuario;
				}else{
					log.IdUsuarioAtendido = entrada.p_IdUsuario;
				}
				log.IdCliente = null;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = null;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "TRANSACCION";
				log.EstadoRegistro = entrada.p_Accion;
				log.IdTipoOperacion = tran.IdTipoOperacion;
				log.Folio =Long.parseLong(tran.Folio);
				log.Cantidad = tran.Cantidad;
				log.PrecioTasa = null;
				log.Monto = tran.Monto;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = tran.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.IdRegistro = tran.IdTransaccion != null ?  tran.IdTransaccion.toString() : "";
				if("R".equalsIgnoreCase(entrada.p_Accion)) {
					log.NombreProceso = entrada.p_Motivo;						
					
				}else {
					log.NombreProceso = "LIBERACION TRANSACCION";
				}
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			//System.out.println("Se agrego al log");
		}
		
		if (transaccionesOK.size() > 0)
			SQL.batch(conn, SQL_UPDATE_TRANSACCIONES, transaccionesOK, Transaccion.class);
		
		return salida;
	}
	
	

	//Genera un identificador de grupo(codigo) de reserva  Ej.: USUARIO1-BCS-C-CN-BSANTANDER-25/07/2012-0000001
	public String generarIdGrupoReserva(Transaccion tran, String fechaSistema, Long idUsuario,Long idEmpresa, SQL sql, Connection conn) throws Exception
	{  		
		String idGrupoReserva;
		String operacion, codigoUsuario;
		HashMap<String,Object> params = new HashMap<>();
		
		// Obtiene datos del usuario		
		params.put("IdUsuario", idUsuario);
		List<Usuario> usuario = sql.query(conn, SQL_SELECT_USUARIO, params, Usuario.class);
		
		codigoUsuario 	= (!usuario.isEmpty()) ? usuario.get(0).CodigoUsuario : "";
		operacion 		= (tran.AccionOrden.equalsIgnoreCase("COMPRA")) ? "C" : ((tran.AccionOrden.equalsIgnoreCase("VENTA")) ? "V" : "");
		
		idGrupoReserva = "";
		idGrupoReserva = idGrupoReserva + codigoUsuario + "-";      // Usuario que reservo la transaccion		
		idGrupoReserva = idGrupoReserva + tran.IdBolsa + "-";       // Bolsa de valores donde se realizo la transaccion
		idGrupoReserva = idGrupoReserva + operacion + "-";          // Tipo operacion, C si es de compra y V si es de venta		
		idGrupoReserva = idGrupoReserva + tran.Nemotecnico + "-";   // Nemotecnico del instrumento de la transaccion		
		idGrupoReserva = idGrupoReserva + fechaSistema.replace('-', '/') + "-";   // Fecha de seleccion
		
		//Genera un correlativo de grupo
		params.clear();
		params.put("IdEmpresa", idEmpresa);

		params.put("IdsGrupoReserva", idGrupoReserva);
		List<Transaccion> _transacciones = sql.query(conn, SQL_SELECT_GRUPO_TRANSACCIONES, params, Transaccion.class);
		String correlativo = String.format("%07d" , _transacciones.size() + 1); // Aumenta en 1 si hay grupos con las mismas caracteristicas

		idGrupoReserva = idGrupoReserva + correlativo;          // Correlativo, es un numero de siete digitos que distingue los grupos que han sido creados y que tienen todos los elementos anteriores iguales

		
		return idGrupoReserva;
	}
	
	//-----------------------------------------------------------------------
	//ENTRADAS
	//-----------------------------------------------------------------------
	public static class Transaccion_Entrada {
		public Long IdTransaccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ReservarLiberarTransacciones_Entrada {
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_Accion;  // R:Reservar          L:liberar
		public String p_Motivo;		
		
		public String p_Origen = "";  //Origen de la Reversa  (TRANSACCION  |  REFACTURACION)  dejar en blanco si no viene de refacturacion
		
		public List<Transaccion_Entrada> p_Transacciones;
		
		public ReservarLiberarTransacciones_Entrada() {
			this.p_Transacciones = new ArrayList<>();
		}		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-----------------------------------------------------------------------
	//SALIDAS
	//-----------------------------------------------------------------------
	public static class ReservarLiberarTransacciones_Salida {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ReservarLiberarTransacciones_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Usuario{
		public String CodigoUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-----------------------------------------------------------------------
	// PARA PRUEBAS LOCALES
	//-----------------------------------------------------------------------
	public static void main(String[]args) throws Exception{
		System.out.print("== INICIO ==");
		try (Connection con = PlataformaUtils.getDESAConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Accion\":\"L\",\"p_Motivo\":\"TEST\",\"p_Transacciones\":[{\"IdTransaccion\":30736},{\"IdTransaccion\":30737}]}";
			ReservarLiberarTransacciones_Entrada Objin = gson.fromJson(json, ReservarLiberarTransacciones_Entrada.class);
			ReservarLiberarTransacciones res = new ReservarLiberarTransacciones();
			System.out.println(res.logica(con, Objin).toString());
			con.rollback();
			//con.commit();
			
			System.out.print("== END ==");

		}	
	}
}
	