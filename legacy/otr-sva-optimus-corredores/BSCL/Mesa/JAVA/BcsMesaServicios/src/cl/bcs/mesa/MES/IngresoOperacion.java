package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.MarcarOperacion.MarcarOperacionObjIn;
import cl.bcs.mesa.MES.MarcarOperacion.MarcarOperacion_IN;
import cl.bcs.mesa.MES.MarcarOperacion.MarcarOperacion_OUT;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class IngresoOperacion implements JavaCall{
	private static final Logger logger = LogManager.getLogger(IngresoOperacion.class);
	
	
	private final String SQL_SELECT_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_ConsultarOperaciones.sql");
	private final String SQL_INSERT_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_InsertOperaciones.sql");
	private final String SQL_UPDATE_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_UpdateOperaciones.sql");
	private final String SQL_SELECT_ESTADO_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_ConsultarEstadosOperacionMesa.sql");
	
	public static class OperacionObjIn extends BcsPojo{
		
		public BigDecimal 	TasaAnualizada;
		public BigDecimal 	CostoFondo;
		public BigDecimal 	UtilidadPerdida;
		public BigDecimal 	UtilidadFinal;
		
		public String Accion;//						NVARCHAR(10)-- CREAR, MODIFICAR, ELIMINAR/ANULAR, CERRAR
		public Long IdInternoOperacion;//				BIGINT
		public Long IdOperacion;//					BIGINT  	-- Id de la operacion
		public Long FolioOperacion;//				BIGINT  	-- Folio operacion
		public Long FolioCustodia;//	--Folio Custodia, para el caso de pactos, Depositos y desembolso
		public String FechaOperacion;//			TIMESTAMP  	-- debe venir en formato AAAA-MM-DD	
		public String IdTipoProducto;//				NVARCHAR(4)  
		public String IdTipoOperacion;//			NVARCHAR(6)  
		public Long IdOperacionRelacionada;// BIGINT  	-- Id de la operacion rel intermesas			   
		// Campos Forward----------------------------
		public Long IdInstrumento;//	BIGINT		
		public String IdMonedaPrincipal;//			NVARCHAR(3)	
		public String IdMonedaSecundaria;//			NVARCHAR(3)	
		public Long IdModalidadLiquidacion;//		BIGINT		
		public Long IdReferenciaMercado;//   		BIGINT		
		public Long IdPlaza;//						BIGINT		
		//----------------------------------------------
		public String Intermesa;//					NVARCHAR(1) 
		public Long IdMesaOrigen;//				BIGINT 
		public Long IdMesaDestino;//			BIGINT 
		public Long IdCliente;//					BIGINT 
		public Long IdPortafolio;//				BIGINT 
		public Long IdClienteCorredor;//			BIGINT 
		public Long IdPortafolioCorredor;//		BIGINT 
		//-- Campo Forward------------------------------
		public String IdTipoReajuste;//				NVARCHAR(3)
		public BigDecimal MontoOperacion;//				DECIMAL(26,8)
		public BigDecimal MontoOperacionInicial;//		DECIMAL(26,8)  
		public BigDecimal MontoOperacionFinal;//			DECIMAL(26,8)  
		public String IdMoneda;//					NVARCHAR(3)
		public BigDecimal Tasa;//					DECIMAL(26,8)
		// Campos de conversion moneda ----------------
		public BigDecimal MontoOperacionMoneda;// 		DECIMAL(26,8)
		public BigDecimal MontoOperacionInicialMoneda;//	DECIMAL(26,8)
		public BigDecimal MontoOperacionFinalMoneda;//	DECIMAL(26,8)
		public String IdMonedaLiquidacion;//			NVARCHAR(3)	
		// Campos Forward------------------------------
		public BigDecimal TCCierre;//					DECIMAL(26,8)
		public BigDecimal ParidadCierre;//				DECIMAL(26,8)
		public BigDecimal TCCosto;//						DECIMAL(26,8)
		public String PuntosTasasForward;//			NVARCHAR(1)	
		public BigDecimal PuntosForward;//				DECIMAL(26,8)
		public BigDecimal TCForward;//					DECIMAL(26,8)
		public BigDecimal TasasMonedaPrincipal;//		DECIMAL(26,8)
		public BigDecimal TasasMonedaSecundaria;//		DECIMAL(26,8)
		public BigDecimal MontoDiferido;//				DECIMAL(26,8)
		public BigDecimal MontoMargen;//					DECIMAL(26,8)
		public Long IdFixing;//					BIGINT		
		public Date FechaFixing;//					DATETIME		
		public String HoraFixing;//					NVARCHAR(10)
		public Long IdSpotAsociado;//				BIGINT		
		public Long IdClasificacionCartera;//		BIGINT		

		//-----------------------------------------------
		public String AfectaCustodia;//				NVARCHAR(6)
		public Long PlazoEnDias;//					BIGINT 
		public Date FechaVencimiento;//			TIMESTAMP
		public Date FechaLiquidacion;//			TIMESTAMP
		public String IdCondicionLiquidacion;//		NVARCHAR(2)  
		public Date FechaCondicionLiquidacion;//	TIMESTAMP
		public Long IdAgente;//					BIGINT 
		public BigDecimal TasaAgente;//					DECIMAL(26,8)			   
		public String IdEstado;//					NVARCHAR(3)  
		//,FechaCreacion				TIMESTAMP
		public Long IdUsuarioCreacion;//			BIGINT 
		public String IdTipoCanalIngreso;//			NVARCHAR(3)
		public String Glosa;//						NVARCHAR(500) 
		//-----------------------------------------------			   
		public Long IdEmpresa;//					BIGINT 
		public String AccionEdicion;//				CHAR(1)
		public Long UsuarioEdicion;//				BIGINT
		public java.sql.Timestamp FechaCreacion;
		public String PagaConCtaInv;	//			NVARCHAR(1) 'N' o 'S' DEFAULT 'N'
		//Nuevo deposito desembolso
		public String			IdEmisor;
		
		public Long IdCarga = 0L; 
		public Long IdContacto;
		public String TipoDocumento;
		public String RequiereAutorizacion = "N";
		public String FechaSuscripcion = "";
		public String HoraSuscripcion = "";		
		public String ValorizacionManual;
		public String IdMonedaCompensacion;
		public Long IdEmpleadoDigitador = 0L;
		
		public OperacionObjIn () {
			this.ValorizacionManual="N";
			this.IdMonedaCompensacion = "";
		}
		
		
	}
	
	public static class IngresoOperacion_IN extends BcsPojo{
		public Long p_IdEmpresa;// 				BIGINT,
	    public Long p_IdUsuario;// 				BIGINT,
	    public String p_IdModulo;// 				NVARCHAR(3),
	    public String p_simularOperacion;//		nvarchar(1),	-- S/N se usa para simular operacion (algunos casos) 
		public String p_UsaReglasNegocio;//	 	nvarchar(1),	-- S/N se usa en caso de servicios anidados.
		public Long p_IdPais;// 				BIGINT
		public String PagaConCtaInv;	//			NVARCHAR(1) 'N' o 'S' DEFAULT 'N'
		public List<OperacionObjIn> p_Operaciones;
		
		
	}
	public static class OperacionObjOut extends BcsPojo {
		public Long IdAplicacion;//			BIGINT
		public Long IdOperacion;//			BIGINT  		-- Id de la operacion
		public Long FolioOperacion;//		BIGINT  		-- Folio operacion
		public Long IdOperacionRelacionada; //BIGINT  		-- Id de la operacion Relacionada
		public String IdEstado;//				NVARCHAR(3)
		public String NombreEstado;
		
		
	}
	public static class IngresoOperacion_OUT extends BcsPojo {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<OperacionObjOut>		P_OUT_OPERACION;
		public IngresoOperacion_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<OperacionObjOut>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		
	}
	
	public static class Estado extends BcsPojo{
		public String IdEstado;
		public String NombreEstado;
		public String IdEstadoSiguiente;
		public String NombreEstadoSiguiente;
		public String IdTipoProducto;
		public String NombreEntidad;
		
		
	}

	public IngresoOperacion_OUT logica (Connection conn, IngresoOperacion_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		
		SQL _sql = new SQL();
		IngresoOperacion_OUT out = new IngresoOperacion_OUT();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		logger.info("Inicio servicio MES.IngresoOperacion");
		

			Long errTot = 0L;
			List<OperacionObjIn> v_TblOpConIdOperacion = new ArrayList<>();//usar para modificar
			List<Long> v_TblOpConId = new ArrayList<>();
			List<OperacionObjIn> v_TblOpSinIdOperacion = new ArrayList<>();//usar para ingreso
			//String v_FechaOperacionSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);	
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
	        Fecha.setLocalOffSet(segundosZonaHoraria);
			String v_FechaOperacionSecondate = Fecha.toStringDatetime(Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa)));
			
			Long countTblOpConIdOperacion = 0L;
			Long countTblOpSinIdOperacion = 0L;
			Long countTblOpConIdFolioOperacion = 0L;
			Long countTblOpSinIdFolioOperacion = 0L;
			HashMap<String, Object> parEstado= new HashMap<>();
			
			if (in.p_Operaciones.size() <= 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_ORDEN",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			List<Object> par = new ArrayList<>();
			List<OperacionObjIn> operacionesValorizacionManualLog = new ArrayList<OperacionObjIn>();
			for (OperacionObjIn op: in.p_Operaciones){
				if (op.IdOperacion.compareTo(0L) > 0 ){
					v_TblOpConIdOperacion.add(op);
					v_TblOpConId.add(op.IdOperacion);
					if (op.Accion.equalsIgnoreCase("CERRAR")){
						countTblOpConIdFolioOperacion++;						
						if("S".equals(op.ValorizacionManual)) {
							operacionesValorizacionManualLog.add(op);
						} 
					}
					countTblOpConIdOperacion++;
				}
				else{
					op.PagaConCtaInv = in.PagaConCtaInv;
					v_TblOpSinIdOperacion.add(op);
					if (op.Accion.equalsIgnoreCase("CERRAR")){
						countTblOpSinIdFolioOperacion++;
						if("S".equals(op.ValorizacionManual)) {
							operacionesValorizacionManualLog.add(op);
						}
					}
					countTblOpSinIdOperacion++;
				}
				par.add(op.IdTipoProducto);
			}
			parEstado.put("IdTipoProducto", par);
			par = new ArrayList<>();
			par.add("PEN");
			par.add("VIG");
			par.add("PDA");
			parEstado.put("IdEstado", par);	
			parEstado.put("NombreEntidad", "INGRESO");	
			//Consulto estados de las operaciones

			logger.debug("[IngresoOperacion] Inicio SQL_SELECT_ESTADO_OPERACIONES");
			startTime = System.nanoTime();					
			List<Estado> estados = _sql.query(conn, SQL_SELECT_ESTADO_OPERACIONES, parEstado, Estado.class);
			logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_ESTADO_OPERACIONES");
			
			HashMap<String, Object> estadosHash = Utils.List2Hash(estados, "IdEstado");
			//consulto las operaciones si existen
			HashMap<String, Object> paramsOp = new HashMap<>();
			paramsOp.put("IdOperaciones", v_TblOpConId);
			
			logger.debug("[IngresoOperacion] Inicio SQL_SELECT_OPERACIONES");
			startTime = System.nanoTime();					
			List<OperacionObjIn> OpExistente = _sql.query(conn, SQL_SELECT_OPERACIONES, paramsOp, OperacionObjIn.class);
			logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_OPERACIONES");

			HashMap<Object, Object> OpExistenteHash = Utils.List2Hash(OpExistente,"IdOperacion");
			//:: validaciones
			for (OperacionObjIn op :v_TblOpConIdOperacion){
				OperacionObjIn dat = (OperacionObjIn) OpExistenteHash.get(op.IdOperacion);
				if (dat == null){					
					mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"ORDEN_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				else if (op.Accion.equals("CREAR")){
					mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CREAR_ORDEN_EXISTENTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				else if (!op.Accion.equals("MODIFICAR") && !op.Accion.equals("CERRAR")){
					mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_ACCION_ORDEN",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				} 
			}
			for (OperacionObjIn op :v_TblOpSinIdOperacion){
				if (op.Accion.equals("MODIFICAR")){
					mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MODIFICAR_ORDEN_INEXISTENTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				else if (!op.Accion.equals("CREAR") && !op.Accion.equals("CERRAR")){
					mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_ACCION_ORDEN",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				} 
			}
			errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
			
			//Bloqueo las operaciones
			MarcarOperacion_IN inBloquear = new MarcarOperacion_IN();
			inBloquear.p_IdEmpresa = in.p_IdEmpresa;
			inBloquear.p_IdModulo = in.p_IdModulo;
			inBloquear.p_IdUsuario = in.p_IdUsuario;
			inBloquear.p_Operaciones = new ArrayList<MarcarOperacionObjIn>();
			
			for (OperacionObjIn op : v_TblOpConIdOperacion){
				MarcarOperacionObjIn obj = new MarcarOperacionObjIn();
				obj.Accion 		= op.Accion.equals("CERRAR")? "L":"B";
				obj.IdOperacion = op.IdOperacion;
				inBloquear.p_Operaciones.add(obj);
			}
			if (inBloquear.p_Operaciones.size() > 0){
				MarcarOperacion bloquear = new MarcarOperacion();
				
				logger.debug("[IngresoOperacion] Inicio MarcarOperacion bloquear");
				startTime = System.nanoTime();					
				MarcarOperacion_OUT resultBloquear = bloquear.logica(conn, inBloquear);
				logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin MarcarOperacion bloquear");

				out.P_OUT_MENSAJES.addAll(resultBloquear.P_OUT_MENSAJES);
				
				errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				if (!errTot.equals(0L) ){
					return out;
				}
			}
			
			//Ingreso la operaciones
			List<Long> secuenciaIdOperacion = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::Operacion", countTblOpSinIdOperacion);//idOperaciones
			List<Long> secuenciaFolioOperacion = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::OperacionMesa", countTblOpSinIdFolioOperacion);//FoliosOperaciones
			
			int j=0;
			for (int i=0; i < v_TblOpSinIdOperacion.size(); i++){
				OperacionObjIn op = v_TblOpSinIdOperacion.get(i);
				op.IdOperacion = secuenciaIdOperacion.get(i);
				if (op.Accion.equalsIgnoreCase("CERRAR")){
					op.FolioOperacion = secuenciaFolioOperacion.get(j);
					j++;
				}
				if (op.Accion.equalsIgnoreCase("CREAR")){
					op.IdEstado = "PEN";
				}
				else if( "S".equals(op.RequiereAutorizacion)) {
					op.IdEstado = "PDA";
				}
				else {
					op.IdEstado = "VIG";
				}
																
				op.FechaCreacion = Fecha.toDateTime(v_FechaOperacionSecondate);
				
				if(!StringUtils.isNullOrBlank(op.FechaSuscripcion) && !StringUtils.isNullOrBlank(op.HoraSuscripcion)) {
						op.FechaOperacion = op.FechaSuscripcion + " " + op.HoraSuscripcion;
				}
				
				mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"OPERACION_INGRESADA",mapMsg,conn,in.p_IdEmpresa));
				
				Id++;
			}
			
				
			logger.debug("[IngresoOperacion] SQL_INSERT_OPERACIONES");
			startTime = System.nanoTime();					
			_sql.batch(conn, SQL_INSERT_OPERACIONES, v_TblOpSinIdOperacion, OperacionObjIn.class);
			logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_INSERT_OPERACIONES");


			logger.debug("[IngresoOperacion] Actualizo operaciones");
			startTime = System.nanoTime();
			
			//Actualizo operaciones
			secuenciaFolioOperacion = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::OperacionMesa", countTblOpConIdFolioOperacion);//FoliosOperaciones
			j=0;
			for (int i=0; i < v_TblOpConIdOperacion.size(); i++){
				OperacionObjIn op = v_TblOpConIdOperacion.get(i);
				
				if (op.Accion.equalsIgnoreCase("CERRAR")){
					op.FolioOperacion = secuenciaFolioOperacion.get(j);
					j++;
				}
				if (op.Accion.equalsIgnoreCase("MODIFICAR")){
					op.IdEstado = "PEN";
				}
				else if( "S".equals(op.RequiereAutorizacion)) {
					op.IdEstado = "PDA";
				}
				else {
					op.IdEstado = "VIG";
				}
				op.FechaCreacion = Fecha.toDateTime(v_FechaOperacionSecondate);
				mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"OPERACION_ACTUALIZADA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin Actualizo operaciones");


			logger.debug("[IngresoOperacion] Inicio SQL_UPDATE_OPERACIONES");
			startTime = System.nanoTime();
			_sql.batch(conn, SQL_UPDATE_OPERACIONES, v_TblOpConIdOperacion, OperacionObjIn.class);
			logger.debug("[IngresoOperacion] (" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_UPDATE_OPERACIONES");

			
			//tabla salida
			out.P_OUT_OPERACION = new ArrayList<OperacionObjOut>();
			for (OperacionObjIn op: v_TblOpSinIdOperacion){
				OperacionObjOut salida = new OperacionObjOut();
				salida.IdAplicacion = op.IdInternoOperacion;
				salida.IdOperacion = op.IdOperacion;
				salida.FolioOperacion = op.FolioOperacion;
				salida.IdOperacionRelacionada = op.IdOperacionRelacionada;
				salida.IdEstado = op.IdEstado;
				salida.NombreEstado = ((Estado)estadosHash.get(op.IdEstado)).NombreEstado;
				out.P_OUT_OPERACION.add(salida);
			}
			for (OperacionObjIn op: v_TblOpConIdOperacion){
				OperacionObjOut salida = new OperacionObjOut();
				salida.IdAplicacion = op.IdInternoOperacion;
				salida.IdOperacion = op.IdOperacion;
				salida.FolioOperacion = op.FolioOperacion;
				salida.IdOperacionRelacionada = op.IdOperacionRelacionada;
				salida.IdEstado = op.IdEstado;
				salida.NombreEstado = ((Estado)estadosHash.get(op.IdEstado)).NombreEstado;
				out.P_OUT_OPERACION.add(salida);
			}
			
			this.agregarLog(conn, in, operacionesValorizacionManualLog);

		logger.info("Termino del servicio MES.IngresoOperacion, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
			
		return out;
	};
	
	
	private void agregarLog(Connection con, IngresoOperacion_IN entrada, List<OperacionObjIn> operacionesValorizacionManual) throws Exception {
		logger.trace("INICIO::CargarValorCuota.agregarLog");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa= entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdModulo = "MRF";
		entradaAgregarLog.p_IdUsuario= entrada.p_IdUsuario;		
		entradaAgregarLog.p_UsaReglasNegocio= "N";
		entradaAgregarLog.p_IdEstadoAutorizacion="VIG";
		
		entradaAgregarLog.p_Log = operacionesValorizacionManual.parallelStream().map(operacion->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = operacion.FolioOperacion;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = operacion.FechaOperacion;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = operacion.IdCliente; 
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = ""; 
			log.IdPortafolio = operacion.IdPortafolio; 
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "VALORIZACIONMANUAL";  
			log.EstadoRegistro = "";
			log.IdTipoOperacion = operacion.IdTipoOperacion;
			log.Folio = operacion.IdOperacion;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = operacion.Tasa;
			log.Monto = operacion.MontoOperacion;
			log.IdMonedaOrigen = operacion.IdTipoReajuste;
			log.IdMonedaLiquidacion =operacion.IdMonedaLiquidacion ;
			log.TipoCambio = "";
			log.Nemo = operacion.IdTipoOperacion;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";						
			entradaAgregarLog.p_Log.add(log);
			
			
			InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = operacion.FolioOperacion;
			detLog.IdTrabajo=0L;
			detLog.SubRegistro = "VALORIZACION MANUAL";
			detLog.CampoRegistro = "ValorizacionManual";
			detLog.ValorAntiguo = operacion.ValorizacionManual;
			detLog.ValorNuevo = "";
			entradaAgregarLog.p_LogDetalle.add(detLog);
			
			return log;
		}).collect(Collectors.toList());
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		logger.trace("FIN::CargarValorCuota.agregarLog");
	}
	
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		IngresoOperacion_IN jin = gson.fromJson(in.toString(), IngresoOperacion_IN.class);
		IngresoOperacion_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAPEConnection();
			//datos entrada
			String json = "";
			IngresoOperacion_IN objIn = gson.fromJson(json,IngresoOperacion_IN.class);
			//mientras pruebo hare el rollback
			IngresoOperacion demo = new IngresoOperacion();
			
			IngresoOperacion_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
