package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Salida;
import cl.bcs.facturacion.AnularMovimientoComprobante.Movimiento_Entrada;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.NotificarEstadoSpot.EntradaNotificarEstadoSpot;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularOperacionSpot implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(AnularOperacionSpot.class);
	
	private final String SQL_SELECT_OPERACIONES_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpot.sql");
	private final String SQL_SELECT_REQUISITOS_ANULAR_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarRequisitosAnularOperacionSpot.sql");
	private final String SQL_UPDATE_OPERACIONES_SPOT_ESTADO = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_UpdateOperacionesSpotEstado.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_FACTURACION = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosFacturacion.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_TESORERIA = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosTesoreria.sql");
	private final String SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_CUENTA_INVERSION = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_ConsultarOperacionesSpotAnularMovimientosCtaInversion.sql");

	private final String SQL_SELECT_ROLES = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarRolesUsuario.sql");
	private final String SQL_UPDATE_OPERACIONES_ORDEN_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularOperacionSpot_UpdateOrdenGeneraSpot.sql");
	
	public static class AnularOperacionSpotEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdCanalIngreso;
		public List<OperacionSpot> p_DatosOperacion;
		
		public AnularOperacionSpotEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionSpot>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnularOperacionSpotSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public AnularOperacionSpotSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class RequisitosAnularOperacionSpot{
		public Long NumeroOperacion;
		public Long IdOperacionSpot;
		public String FechaOperacion;
		public Long IdMesa;
		public String IdEstadoMesa;
		public String IdEstadoModulo;
		public String TipoEstadoModulo;
		public Long IdUsuario;
		public Long IdUsuarioSupervisor;
		public Long IdUsuarioReemplaza;
		public Integer PuedeModificarAnular;
		public String CodigoModificarAnular;
		public String Estado;
        public String TipoSolicitud;
        public Long IdSolicitudAporteRescate;
        public String NombreEstadoSpot;

		public Integer PuedeAnularTesoreria;
		public String CodigoAnularTesoreria;
		

		public String IdTipoCanalIngresoSolApoRes; // Canal ingreso de la solicitud aporte rescate(si es que la hay)
		public String IdTipoCanalIngreso;
		
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosOperacionSpot {
		
		public String NumeroOperacion;
		public Long IdOperacionSpot;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdMovimiento;
		public String IdTipoOperacion;
		public String Nemotecnico;
		public BigDecimal MontoMonedaSecundaria;
		public String IdMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public String Estado;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class Roles {
	
		public String EstadoRol;
		public String NombreRol;
		
		
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AnularOperacionSpotEntrada in = gson.fromJson(jin.toString(), AnularOperacionSpotEntrada.class);
		AnularOperacionSpotSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public AnularOperacionSpotSalida logica(Connection conn, AnularOperacionSpotEntrada in) throws Exception {
		SQL _sql = new SQL();
		AnularOperacionSpotSalida out = new AnularOperacionSpotSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		logger.info("INICIO :: SPOT.AnularOperacionSpot");
		
		try {
			if (in.p_DatosOperacion.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_HAY_OPERACIONES_SPOT_PARA_ANULAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long cantOpeIn = 0L;
			Long cantOpeErr = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			List<OperacionSpot> operacionesInErrValidaciones = new ArrayList<>();
			
			List<String> numeroOperacion = new ArrayList<String>();
			for (OperacionSpot ope: in.p_DatosOperacion){
				numeroOperacion.add(ope.NumeroOperacion);
				cantOpeIn++;
			}
			
			
			//Obtiene datos de las operaciones Spot de entrada
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			List<OperacionSpot> selectOpeSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT, params, OperacionSpot.class);
			
			if(selectOpeSpot.size() == 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_HAY_OPERACIONES_SPOT_PARA_ANULAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			
			/***** VALIDACIONES *****/
			if(in.p_UsaReglasNegocio.equals("S")){
				params.put("IdUsuario", in.p_IdUsuario);
				List<RequisitosAnularOperacionSpot> requisitosAnularOperacionSpot = _sql.query(conn, SQL_SELECT_REQUISITOS_ANULAR_OPERACION_SPOT, params, RequisitosAnularOperacionSpot.class);
				
				for(RequisitosAnularOperacionSpot req: requisitosAnularOperacionSpot){
					OperacionSpot ope = new OperacionSpot();
					boolean entraIF = false;
					
					if(!req.IdEstadoMesa.equals("A")){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,"MESA_SPOT_CERRADA",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					if(!(req.IdEstadoModulo.equals("ACT") && req.TipoEstadoModulo.equals("A"))){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_CERRADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					

					boolean permiteAnular = false;
					boolean tieneRolDistribucion = false;
					params = new HashMap<>();
					params.put("IdUsuario", in.p_IdUsuario);
					params.put("IdEmpresa", in.p_IdEmpresa);
					List <Roles> rolesUsuario = _sql.query(conn, SQL_SELECT_ROLES, params, Roles.class);
					for(Roles element : rolesUsuario ) {
						if(element.NombreRol.equalsIgnoreCase("full") || element.NombreRol.equalsIgnoreCase("interbancario")) {
							permiteAnular= true;
						}
						if(element.NombreRol.equalsIgnoreCase("distribucion")) {
							tieneRolDistribucion = true;
						}
					}

					//El rol distribucion solo puede anular operaciones de las que es dueño
					boolean permiteAnularDistribucion = tieneRolDistribucion && req.IdUsuario != null
							&& in.p_IdUsuario.longValue() == req.IdUsuario.longValue();

					String p_final_canal_ingreso_anulacion = in.p_IdCanalIngreso != null ? in.p_IdCanalIngreso : "";
					String p_final_canal_ingreso_solicitud = req.IdTipoCanalIngreso != null ? req.IdTipoCanalIngreso : "";

					//Esta validacion solo aplica si el canal no es WEB ya que por web el usuario es generico
					if (!req.IdTipoCanalIngresoSolApoRes.equalsIgnoreCase("WEB") &&
							!( in.p_IdUsuario.longValue() == req.IdUsuarioSupervisor.longValue()
								|| in.p_IdUsuario.longValue() == req.IdUsuarioReemplaza.longValue()
								|| in.p_IdModulo.equals("TES")
								|| permiteAnular == true
								|| permiteAnularDistribucion == true
							)
							|| (p_final_canal_ingreso_anulacion.equalsIgnoreCase("CLI") && !p_final_canal_ingreso_solicitud.equalsIgnoreCase("CLI"))
						)
					{
						if (p_final_canal_ingreso_anulacion.equalsIgnoreCase("CLI")) {
							mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
							String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CANAL_NO_AUTORIZADO_A_ANULAR_OPERACION").msg();
							out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,descripcion,mapMsg,conn,in.p_IdEmpresa));
							Id++;
							entraIF = true;
						}else {
							mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,"USUARIO_NO_AUTORIZADO_A_ANULAR_OPERACION",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							entraIF = true;
						}
						
					}
					if(req.PuedeModificarAnular != 1){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,req.CodigoModificarAnular,mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}

					if(req.PuedeAnularTesoreria != 1){
						mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,req.CodigoAnularTesoreria,mapMsg,conn,in.p_IdEmpresa));
						Id++;
						entraIF = true;
					}
					
					if(in.p_IdModulo.equals("MSP")) {
						if(req.Estado != null && !req.Estado.equals("ANU")){
							mapMsg.put("NumeroOperacion", req.NumeroOperacion.toString());
							mapMsg.put("TipoSolicitud", req.TipoSolicitud.toString());
							mapMsg.put("NombreEstadoSpot", req.NombreEstadoSpot.toString());
							mapMsg.put("IdSolicitudAporteRescate", req.IdSolicitudAporteRescate.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(req.NumeroOperacion,MensajeSalida.MENSAJE_ERROR,"OPERACION_ANULADA_ERROR_FFMM",mapMsg,conn,in.p_IdEmpresa));		
							Id++;
							entraIF = true;
							System.out.println(out.P_OUT_MENSAJES.get(0).descripcion);
						}
					}
					
					if(entraIF){
						ope.NumeroOperacion	= req.NumeroOperacion.toString();
						ope.Estado			= "ERR";
						operacionesInErrValidaciones.add(ope);
					}
				}
			}
			
			List<String> operacionExistente = new ArrayList<String>();
			//Setea estado de las operaciones de entrada para continuar solo con las operaciones sin error
			for(OperacionSpot ope: selectOpeSpot) {
				operacionExistente.add(ope.NumeroOperacion);
				for(OperacionSpot opeErr: operacionesInErrValidaciones) {
					if(ope.NumeroOperacion.equals(opeErr.NumeroOperacion)) {
						ope.Estado = "ERR";
						cantOpeErr++;
					}
				}
			}
			
			//Retorna si existen errores en todas las operaciones de entrada
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			logger.info("SPT.AnularOperacionSpot :: cantidad de errores :: "+errCont);
			
			if (cantOpeErr.longValue() != 0L && cantOpeErr.longValue() == cantOpeIn.longValue()){
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) + out.P_OUT_ERRORESCONTADOR;
				return out;
			}
			
			//Continua solo con las operaciones sin Error
			selectOpeSpot.removeIf(ope -> ("ERR".equals(ope.Estado)));
			
			for(String numOperacion: numeroOperacion){
				if(! operacionExistente.contains(numOperacion)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(numOperacion), MensajeSalida.MENSAJE_ERROR, "OPERACION_SPOT_NO_EXISTE",  "OPERACION FOLIO " + Long.parseLong(numOperacion) + " NO EXISTE"));
				}
			}

			
			numeroOperacion = new ArrayList<String>();
			for (OperacionSpot ope: selectOpeSpot) {
				if("VIG".equals(ope.Estado) || "PEN".equals(ope.Estado)) {
					numeroOperacion.add(ope.NumeroOperacion);
				}
			}
			
			
			/***** ESTADO DE LAS OPERACIONES SPOT QUEDA ANU *****/
			
			for(OperacionSpot opSpot: selectOpeSpot) {
				if("DAT".equals(opSpot.IdTipoCanalIngreso)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_OK, "OPERACION_SPOT_ANULADA", "OPERACION SPOT SE ANULO CORRECTAMENTE"));
					
				}
				
				if("ANU".equals(opSpot.Estado)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(opSpot.IdOperacionSpot,MensajeSalida.MENSAJE_ERROR, "OPERACION_SPOT_YA_SE_ENCUENTRA_ANULADA", "OPERACION FOLIO " + opSpot.IdOperacionSpot + " YA SE ENCUENTRA ANULADA" ));
				}

			}
			
			// Eliminamos las operaciones que ya se encuentren anuladas antes de enviar el listado completo
			selectOpeSpot.removeIf(ope -> ("ANU".equals(ope.Estado)));

			_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_ESTADO, selectOpeSpot, OperacionSpot.class);

			

			
			/***** ANULAR MOVIMIENTO COMPROBANTE DE FACTURACION *****/
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("NumeroOperacion", numeroOperacion);
			
			List<OperacionSpot> selectOpeSpotAnular = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_FACTURACION, params, OperacionSpot.class);
			
			if(selectOpeSpotAnular.size() > 0){
				logger.info("MESA SPOT :: Llamada AnularMovimientoComprobante");
				AnularMovimientoComprobante anuSpt = new AnularMovimientoComprobante();
				AnularMovimientoComprobante_Entrada anuSptIn = new AnularMovimientoComprobante_Entrada();
				anuSptIn.p_IdEmpresa = in.p_IdEmpresa;
				anuSptIn.p_IdUsuario = in.p_IdUsuario;
				anuSptIn.p_IdModulo = in.p_IdModulo;
				anuSptIn.p_simularOperacion = "N";
				anuSptIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				anuSptIn.p_EsIngresoAutomatico = in.p_UsaReglasNegocio;
				anuSptIn.p_Glosa = "ANULACION_SPOT";
				anuSptIn.p_MovimientoAAnular = selectOpeSpotAnular.parallelStream().map(operacion->{
					Movimiento_Entrada operacionAnulacion = new Movimiento_Entrada();
					operacionAnulacion.IdMovimientoComprobante = operacion.IdComprobanteFacturacion;
					return operacionAnulacion;
				}).collect(Collectors.toList());
				AnularMovimientoComprobante_Salida anuSptOut = anuSpt.logica(conn, anuSptIn);
				
				//Procesa salida AnularMovimientoComprobante				
				for(MensajeSalida mensaje : anuSptOut.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ADVERTENCIASCONTADOR++;
						Id++;
					}
					if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ERRORESCONTADOR++;
						Id++;
					}
				}
				
				
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) + out.P_OUT_ERRORESCONTADOR;
			
			/***** ANULAR MOVIMIENTO DE TESORERIA *****/

			List<MovimientosOperacionSpot> selectOpeSpotAnularTesoreria = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_TESORERIA, params, MovimientosOperacionSpot.class);
			if(selectOpeSpotAnularTesoreria.size() > 0){
				logger.info("MESA SPOT :: Llamada AnularMovimientoComprobanteTesoreria");
				AnularRegistros anuSptTes = new AnularRegistros();
				AnularRegistros.AnularRegistros_Entrada anuSptTesIn = new AnularRegistros.AnularRegistros_Entrada();
				anuSptTesIn.p_IdEmpresa = in.p_IdEmpresa;
				anuSptTesIn.p_IdUsuario = in.p_IdUsuario;
				anuSptTesIn.p_IdModulo = in.p_IdModulo;
				anuSptTesIn.p_simularOperacion = "N";
				anuSptTesIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;				
				anuSptTesIn.p_FechaMovimiento = fechaSistema;
				anuSptTesIn.p_RegistrosAProcesar = selectOpeSpotAnularTesoreria.parallelStream().map(operacion->{
					Registro_Entrada operacionAnulacion = new Registro_Entrada();
					operacionAnulacion.Id = operacion.IdMovimiento;
					operacionAnulacion.Entidad = "TES.IngresoEgreso";
					return operacionAnulacion;
				}).collect(Collectors.toList());
				AnularRegistros.AnularRegistros_Salida anuSptTesOut = anuSptTes.logica(conn, anuSptTesIn);
				
				for(MensajeSalida mensaje : anuSptTesOut.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ADVERTENCIASCONTADOR++;
						Id++;
					}
					if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ERRORESCONTADOR++;
						Id++;
					}
					if(MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_AUTORIZACION, mensaje.codigo, mensaje.descripcion));
						Id++;
					}
				}
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) + out.P_OUT_ERRORESCONTADOR;
			
			/***** ANULAR MOVIMIENTO CARGO ABONO *****/
			List<MovimientosOperacionSpot> selectOpeSpotAnularCtaInversion = _sql.query(conn, SQL_SELECT_OPERACIONES_SPOT_ANULAR_MOVIMIENTOS_CUENTA_INVERSION, params, MovimientosOperacionSpot.class);

			if(selectOpeSpotAnularCtaInversion.size() > 0){
				logger.info("MESA SPOT :: Llamada AnularMovimientoComprobanteCuentaInversion");
				AnularCargoAbono anuSptCtaInv = new AnularCargoAbono();
				AnularCargoAbono.AnularCargoAbono_Entrada anuSptCtaInvIn = new AnularCargoAbono.AnularCargoAbono_Entrada();
				anuSptCtaInvIn.p_IdEmpresa = in.p_IdEmpresa;
				anuSptCtaInvIn.p_IdUsuario = in.p_IdUsuario;
				anuSptCtaInvIn.p_IdModulo = in.p_IdModulo;
				anuSptCtaInvIn.p_simularOperacion = "N";
				anuSptCtaInvIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				anuSptCtaInvIn.p_FechaMovimiento = fechaSistema;
				anuSptCtaInvIn.p_CargoAbonoAAnular = selectOpeSpotAnularCtaInversion.parallelStream().map(operacion->{
					AnularCargoAbono.CargoAbono_Entrada operacionAnulacion = new AnularCargoAbono.CargoAbono_Entrada();
					operacionAnulacion.IdCargoAbonoMovimiento = operacion.IdMovimiento;
					return operacionAnulacion;
				}).collect(Collectors.toList());
				AnularCargoAbono.AnularCargoAbono_Salida anuSptCtaInvOut = anuSptCtaInv.logica(conn, anuSptCtaInvIn);
				
				for(MensajeSalida mensaje : anuSptCtaInvOut.P_OUT_MENSAJES) {
					if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ADVERTENCIASCONTADOR++;
						Id++;
					}
					if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, mensaje.codigo, mensaje.descripcion));
						out.P_OUT_ERRORESCONTADOR++;
						Id++;
					}
					if(MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_AUTORIZACION, mensaje.codigo, mensaje.descripcion));
						Id++;
					}
				}
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) + out.P_OUT_ERRORESCONTADOR;
			
			/***** AGREGAR LOG *****/
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (OperacionSpot opSp: selectOpeSpot) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = opSp.IdOperacionSpot;
				log.IdTipoLog = "INFORMATIVO";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = opSp.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = opSp.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "ANULACION";
				log.EstadoRegistro = opSp.Estado;
				log.IdTipoOperacion = opSp.IdTipoOperacion;
				log.Folio = opSp.IdOperacionSpotOrigen;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = opSp.MontoMonedaSecundaria;
				log.IdMonedaOrigen = opSp.IdMonedaPrincipal;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = opSp.TipoCambioCierre.toString();
				log.Nemo = opSp.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = opSp.Estado;
				log.NombreProceso = "AnularOperacionSpot";						
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
			
			/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ACTSPOT", "Refresco Movimientos Spot");
			
			
			/***** NOTIFICACIONES *****/
			EntradaNotificarEstadoSpot entradaNotificarEstadoSpot = new EntradaNotificarEstadoSpot();
			for(OperacionSpot opSp : selectOpeSpot){
				NotificarEstadoSpot.OperacionSpot operacionSpot = new NotificarEstadoSpot.OperacionSpot();
				operacionSpot.IdOperacionSpot = opSp.IdOperacionSpot;
				entradaNotificarEstadoSpot.p_IdOperacionesSpot.add(operacionSpot);
			}
			
			if(entradaNotificarEstadoSpot.p_IdOperacionesSpot.size() > 0){
				entradaNotificarEstadoSpot.p_IdEmpresa = in.p_IdEmpresa;
				entradaNotificarEstadoSpot.p_IdUsuario = in.p_IdUsuario;
				entradaNotificarEstadoSpot.p_IdModulo = in.p_IdModulo;
				NotificarEstadoSpot notificarEstadoSpot = new NotificarEstadoSpot();
				notificarEstadoSpot.logica(conn, entradaNotificarEstadoSpot);
			}
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa","C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76810,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":753219}]}";
			//String jsonMaBa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MSP\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":6705}]}";
			AnularOperacionSpotEntrada Objin = gson.fromJson(jsonDesa, AnularOperacionSpotEntrada.class);
			
			AnularOperacionSpot demo = new AnularOperacionSpot();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				AnularOperacionSpotSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}
}