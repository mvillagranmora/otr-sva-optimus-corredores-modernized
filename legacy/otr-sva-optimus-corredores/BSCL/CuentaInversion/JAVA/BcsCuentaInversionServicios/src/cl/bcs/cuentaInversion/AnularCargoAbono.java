package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoEntrada;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoSalida;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.CuentaInversion;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjIDEVENTO;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceEntrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

/**
 * Migracion del servicioAnularCargoAbono.sql
 * @author chernandez
 * 
 */
public class AnularCargoAbono implements JavaCall{

	private final Logger log = LogManager.getLogger(AnularCargoAbono.class);

	private final String SQL_INSERT_CUMPLE_REQUISITOS = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_CumpleRequisito_Insert.sql");
	private final String SQL_SELECT_CUMPLE_REQUISITOS = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_CumpleRequisito_Select.sql");
	private final String SQL_SELECT_EVENTO_AUTORIZACION = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_EventoAutorizacion.sql");
	private String SQL_UPDATE_EVENTO_AUTORIZACION = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_EventoCargoAbono.sql");
	private final String SQL_DELETE_MOVIMIENTO_ANULAR = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Delete_CargoAbono.sql");
	
	private final String SQL_SELECT_CONF_CTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_ConfiguracionCuentaInversion.sql");
	private final String SQL_UPDATE_ESTADO_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_EstadoMovimiento.sql");
	private final String SQL_UPDATE_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_Movimiento.sql");
	private final String SQL_UPDATE_LIBRO_INVENTARIO_SALDOS= loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Update_LibroInventarioSaldos.sql");
	private final String SQL_SELECT_RESULTADO_ANULAR_CARGO_ABONO = "SELECT \"IdCargoAbonoMovimiento\",'ANU' as \"Resultado\", \"IdTipoMovimiento\" FROM \"BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular\";";
	
	private final String SQL_SELECT_FECHA_MINIMA = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_FechaMinima.sql"); 
	private final String SQL_SELECT_SRV_MOVIMIENTOS_ANULAR = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_SrvMovimientoAnular.sql");
	
	private final String SQL_SELECT_OBTENER_PLAZO_MASIVO = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_ObtenerPlazoMasivo.sql");
	
	private final String SQL_SELECT_ENTRADA_CREAR_CARGO_ABONO = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_EntradaCrearCargoAbono.sql");
	
	private final String SQL_Insert_MovimientosAnular = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_Select_Insert_CargoAbono.sql");
	
	private final String SQL_Insert_CargoAbonoAnular = loadResourceToString("cl/bcs/cuentaInversion/sql/AnularCargoAbono_InsertCargoAbono.sql");
	
	private final String SQL_DELETE_IN_CARGO_ABONO = "DELETE FROM \"BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_IN_CargoAbonoAAnular\"";
	
	


	private final String SQL_DELETE_SRV_MOVIMIENTOS_ANULAR = "DELETE FROM \"BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular\"";
	 

	
		
		
	public static class CargoAbono_Entrada {
		public Long IdCargoAbonoMovimiento;
		public String GlosaAnulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnularCargoAbono_PermiteAnular{
		public String PermiteAnularMovimiento;
	}
	
	public static class AnularCargoAbono_ActualizarSaldo{
		public Date FechaSistema;
		public Long IdEjecucion;
		public AnularCargoAbono_ActualizarSaldo() {}
		
		public AnularCargoAbono_ActualizarSaldo(Long idEjecucion) {
			super();
			IdEjecucion = idEjecucion;
		}
	}
	
	public static class AnularCargoAbono_Entrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_EjecutaAutorizacion = "S";
		public String						p_OmiteRegistroSinAutorizar	= "S";
		public String						p_FechaMovimiento;
		public String						p_EsAnulacionManual	= "N";
		public List<CargoAbono_Entrada>		p_CargoAbonoAAnular;
				
		public AnularCargoAbono_Entrada (){			
			p_CargoAbonoAAnular	= new ArrayList<CargoAbono_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class AnularCargoAbono_Salida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public AnularCargoAbono_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	
	public static class resultadoParametrosMantenedorClass{
		
		public String IdConcepto;
		public String RequiereAutorizacion;
	
	}
	
	public static class ResultadoCumpleRequisitosCargoAgono{
		public Long IdInterno;
		public String Codigo;
		public String Resultado;
		public String IdTipoMovimiento;
		public String CodigoDato1;
		public String ValorDato1;
		public String CodigoDato2;
		public String ValorDato2;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoAnularCargoAbono{
		public Long IdCargoAbonoMovimiento;
		public String Resultado;
		public String IdTipoMovimiento;
		public String IdTipoOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		
		public ResultadoAnularCargoAbono() {
			
		}
		public ResultadoAnularCargoAbono(Long numeroCargoAbono, 
				String resultado, String IdTipoMovimiento,String IdTipoOperacion
				,Long IdCliente, Long IdPortafolio, String IdMoneda, BigDecimal Monto) {
			
			this.IdCargoAbonoMovimiento = numeroCargoAbono;
			this.Resultado = resultado;
			this.IdTipoMovimiento = IdTipoMovimiento;
			this.IdTipoOperacion = IdTipoOperacion;
			this.IdCliente = IdCliente;
			this.IdPortafolio = IdPortafolio;
			this.IdMoneda = IdMoneda;
			this.Monto = Monto;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// ---------------------------------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------------------------------
	@Trace
	public AnularCargoAbono_Salida logica(Connection conn, AnularCargoAbono_Entrada in) throws Exception {
		log.info("INICIO ANULAR CARGO ABONO: " + in.toString());
		AnularCargoAbono_Salida out = new AnularCargoAbono_Salida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql = new SQL();
		
	
		//Se limpian tablas globales
		sql.exec(conn, SQL_DELETE_IN_CARGO_ABONO);
		sql.exec(conn, SQL_DELETE_SRV_MOVIMIENTOS_ANULAR);
		
		
		sql.batch(conn, SQL_Insert_CargoAbonoAnular, in.p_CargoAbonoAAnular, CargoAbono_Entrada.class);
		
		List<ResultadoAnularCargoAbono> resultados = new ArrayList<>();
		
		sql.batch(conn, SQL_Insert_MovimientosAnular, Arrays.asList(in), AnularCargoAbono_Entrada.class);
		
		//se agregan parametros para validar si el abono o cargo necesita autorizacion	
//		
//		HashMap<String, Object> paramABonoCargo = new HashMap<String, Object>();
//		paramABonoCargo.put("RequiereAutorizacion", in.);
		
		/**
		 * VALIDACIONES
		 **/
		if(in.p_UsaReglasNegocio.equals("S")) {
			//validar requisitos basicos.
			RevisarRequisitosBasicos requisitosBasicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaRequisitosBasicos = requisitosBasicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, "TES");
			if(!salidaRequisitosBasicos.p_out_Mensajes.isEmpty()) {
				out.P_OUT_ERRORESCONTADOR = (long) salidaRequisitosBasicos.p_out_Mensajes.size();
				salidaRequisitosBasicos.p_out_Mensajes.forEach(msj -> {
					out.P_OUT_MENSAJES.add(new MensajeSalida(msj.id,msj.tipo,msj.codigo,msj.descripcion));
				});
				return out;
			}
			//validar cargos abono.
			if(in.p_CargoAbonoAAnular.stream().filter(cargoAbono -> cargoAbono.IdCargoAbonoMovimiento > 0).collect(Collectors.toList()).size()==0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "NO_VIENEN_MOVIMIENTOS", "NO_VIENEN_MOVIMIENTOS"));
				return out;
			}
			//Validar Cumple Requisitos Anular Cargo Abono
			//Transformar movimientos a tabla entrada
			SQL.exec(conn, SQL_INSERT_CUMPLE_REQUISITOS);

			//Realizar la validacion
			List<ResultadoCumpleRequisitosCargoAgono> resultadoCumpleRequisitosCargoAbono = sql.query(conn, SQL_SELECT_CUMPLE_REQUISITOS, new HashMap<String,Object>(), ResultadoCumpleRequisitosCargoAgono.class);
			
			ValicacionSegunMantenedorGeneral(conn, sql, resultadoCumpleRequisitosCargoAbono); 
			
			if(!resultadoCumpleRequisitosCargoAbono.isEmpty() && in.p_simularOperacion.equals("N") && in.p_EjecutaAutorizacion.equals("S")) {
				
				//verificar si hay movimientos por autorizar
				List<Long> movimientosPorAutorizar = resultadoCumpleRequisitosCargoAbono.stream().filter(res -> res.Resultado.equals("RAUT")).map(res -> res.IdInterno).collect(Collectors.toList());
				if (!movimientosPorAutorizar.isEmpty()) {
					// solicita crear evento de autotizacion.
					CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
					HashMap<String, Object> params = new HashMap<>();
					params.put("p_IdModulo", in.p_IdModulo);
					params.put("p_IdUsuario", in.p_IdUsuario);
					params.put("IdInterno", movimientosPorAutorizar);
					ServiceEntrada entradaCrearEventoAutorizacion = new ServiceEntrada();
					entradaCrearEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
					entradaCrearEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
					entradaCrearEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
					entradaCrearEventoAutorizacion.P_SIMULAROPERACION = in.p_simularOperacion;
					entradaCrearEventoAutorizacion.P_TABLAEVENTOS = sql.query(conn, SQL_SELECT_EVENTO_AUTORIZACION, params, ObjTABLAEVENTOS.class);
					entradaCrearEventoAutorizacion.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
					ServiceSalida salidaCrearEventoAutorizacion = crearEventoAutorizacionMasivo.logica(conn, entradaCrearEventoAutorizacion);

					// actualizar movimientos cargo abono con idevento autorizacion.
					SQL_UPDATE_EVENTO_AUTORIZACION = SQL_UPDATE_EVENTO_AUTORIZACION.replaceAll(":p_IdEmpresa", in.p_IdEmpresa.toString());
					sql.batch(conn, SQL_UPDATE_EVENTO_AUTORIZACION, salidaCrearEventoAutorizacion.P_OUT_IDEVENTO, ObjIDEVENTO.class);

				}
				
				//Quitar de los elementos a procesar los que necesitan autorizacion o fueron rechazados.
				 try(PreparedStatement ps = conn.prepareStatement(SQL_DELETE_MOVIMIENTO_ANULAR)){
					 resultadoCumpleRequisitosCargoAbono.forEach(res ->{
						 try {
							ps.setLong(1, res.IdInterno);
							ps.setString(2, res.Resultado);
							ps.setString(3, in.p_EjecutaAutorizacion);
							ps.setString(4, in.p_OmiteRegistroSinAutorizar);
							ps.setString(5, in.p_EjecutaAutorizacion);
							ps.setString(6, in.p_OmiteRegistroSinAutorizar);
							ps.addBatch();
						} catch (SQLException e) {
							log.debug("ERROR AL PREPARAR ELIMINACION MOVIMIENTOS CON AUTORIZACION O RECHAZADOS", e);
						} 
					 });
					 ps.executeBatch();
				 }
				 
				//revisar si hay eventos por autorizar o rechazados.
				resultadoCumpleRequisitosCargoAbono.stream().filter(res -> res.Resultado.equals("RECH") || res.Resultado.equals("RAUT")).forEach(res -> {
					String tipo = (res.Resultado.equals("RECH") ? MensajeSalida.MENSAJE_ERROR : MensajeSalida.MENSAJE_AUTORIZACION);
					HashMap<String, String> param = new HashMap<>();
					param.put("IdCargoAbonoMovimiento", String.valueOf(res.IdInterno));
					param.put("Id", String.valueOf(res.IdInterno));
					try {
						out.P_OUT_MENSAJES.add(new MensajeSalida(res.IdInterno, tipo, res.Codigo, param, conn, in.p_IdEmpresa));
					} catch (Exception e) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(res.IdInterno, tipo, res.Codigo, res.Codigo));
					}
				});
			}
			
			//evaluar si en la tabla quedan eventos que procesar si no, terminar el programa
			Optional<Long> count = Utils.contarRegistros(conn, "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular");
			if(!count.isPresent() || count.get().equals(0L)) {
				return out;
			}
		}

		//inicio logica migrada
		//Determinar si se permite anular el movimiento cuenta inversion.
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<AnularCargoAbono_PermiteAnular> parmiteAnular = sql.query(conn, SQL_SELECT_CONF_CTA_INVERSION, params, AnularCargoAbono_PermiteAnular.class);
		if(!parmiteAnular.isEmpty() && parmiteAnular.get(0).PermiteAnularMovimiento.equals("S")) {
			params.clear();
			//actualizar movimiento
			sql.batch(conn, SQL_UPDATE_ESTADO_MOVIMIENTO, Arrays.asList(in), AnularCargoAbono_Entrada.class);
			sql.batch(conn, SQL_UPDATE_MOVIMIENTO, Arrays.asList(in), AnularCargoAbono_Entrada.class);
			//actualizar libro inventario
			sql.batch(conn, SQL_UPDATE_LIBRO_INVENTARIO_SALDOS, Arrays.asList(in), AnularCargoAbono_Entrada.class);
			
			//actualizar saldo cargo abono
			List<AnularCargoAbono_ActualizarSaldo> actSaldo = sql.query(conn, SQL_SELECT_FECHA_MINIMA, params, AnularCargoAbono_ActualizarSaldo.class);
			String fechaSistemaMovimiento = (!actSaldo.isEmpty()) ? Fecha.stringToSqlDate(actSaldo.get(0).FechaSistema) : "";
			if(fechaSistemaMovimiento.length()  > 0) {
				List<CuentaInversion> listaCuentas = new ArrayList<CuentaInversion>();

				//obtener los cargos/abonos a actualizar en la tabla temporal
				listaCuentas = sql.query(conn, SQL_SELECT_SRV_MOVIMIENTOS_ANULAR, new HashMap<>(), CuentaInversion.class);
				//llamar al servicio de actualizacion
				ActualizarSaldoCargoAbono actualizarSaldoCargoAbono = new ActualizarSaldoCargoAbono();
				ActualizarSaldoCargoAbonoEntrada entradaActualizarCagoAbono = new ActualizarSaldoCargoAbonoEntrada();
				entradaActualizarCagoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaActualizarCagoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaActualizarCagoAbono.p_FechaMovimiento = fechaSistemaMovimiento;
				entradaActualizarCagoAbono.p_CuentaInversion.addAll(listaCuentas);
				ActualizarSaldoCargoAbonoSalida salidaActCargoAbono = actualizarSaldoCargoAbono.logica(conn, entradaActualizarCagoAbono);
				log.trace(salidaActCargoAbono);
				//que passa si esto tiene errores???, no se revisan las tablas en el SP
			}
			
			resultados = sql.query(conn, SQL_SELECT_RESULTADO_ANULAR_CARGO_ABONO, params, ResultadoAnularCargoAbono.class);
			
		}else {
			params.clear();
			//consultar empresa para el idPais
			ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
			InformacionEmpresa infoEmpresa = consultarEmpresa.logica(conn, in.p_IdEmpresa);
			//Obtener plazo masivo
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FechaMovimiento", in.p_FechaMovimiento);
			
			InObtenerPlazo inObtenerPlazo = new InObtenerPlazo();
			inObtenerPlazo.p_IdPais = infoEmpresa.IdPais;
			inObtenerPlazo.p_Fechas = sql.query(conn, SQL_SELECT_OBTENER_PLAZO_MASIVO, params, FechasIn.class);
			inObtenerPlazo.p_EnTablaTemporal = "S";
			ObtenerPlazo obtenerPlazo = new ObtenerPlazo();
			obtenerPlazo.logica(conn, inObtenerPlazo);
			//Crear movimiento de contrapartida
			CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			entradaCrearCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdModulo = in.p_IdModulo;
			entradaCrearCargoAbono.p_IdUsuario = in.p_IdUsuario;
			entradaCrearCargoAbono.p_simularOperacion = "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio = "N";
			entradaCrearCargoAbono.p_FechaSistema = in.p_FechaMovimiento;
			entradaCrearCargoAbono.p_EsIngresoManual = in.p_UsaReglasNegocio;
			entradaCrearCargoAbono.p_CargoAbonoAGenerar = sql.query(conn, SQL_SELECT_ENTRADA_CREAR_CARGO_ABONO, params, ObjCargoAbonoAGenerar.class);
			//llamar al servicio crearcargoabono
			CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(conn, entradaCrearCargoAbono);
			for(ObjCargoAbonoGenerado cargo : salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO) {
				resultados.add(new ResultadoAnularCargoAbono(cargo.IdCargoAbonoMovimiento,"CRT", cargo.IdTipoMovimiento, null, null, null, null, null));	
			}
		}
		//fin logica migrada
		
		//Construir salida y agregar al log
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog entradaLog = new InAgregarLog();
		HashMap<String,String> valores = new HashMap<>();
		for(ResultadoAnularCargoAbono res : resultados) {
			valores.clear();
			String codigo = "";
			if(res.IdTipoMovimiento.equals("MOVCAR")){
				codigo = (res.Resultado.equals("ANU")) ? "UI_CARGO_ANULADO" : "UI_CARGO_INGRESADO";
			}else if(res.IdTipoMovimiento.equals("MOVABN")){
				codigo = (res.Resultado.equals("ANU")) ? "UI_ABONO_ANULADO" : "UI_ABONO_INGRESADO";
			}
			
			String tipo = "";
			if(res.Resultado.equals("OK") || res.Resultado.equals("ANU")) {
				tipo = MensajeSalida.MENSAJE_INFORMACION;
			}else if(res.Resultado.equals("ADVE")) {
				tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
			}else if(res.Resultado.equals("RAUT")) {
				tipo = MensajeSalida.MENSAJE_AUTORIZACION;
			}else if(res.Resultado.equals("RECH")) {
				tipo = MensajeSalida.MENSAJE_ERROR;
			}
			valores.put("IdCargoAbonoMovimiento", String.valueOf(res.IdCargoAbonoMovimiento));
			out.P_OUT_MENSAJES.add(new MensajeSalida(res.IdCargoAbonoMovimiento, tipo, codigo, valores, conn, in.p_IdEmpresa));
			
			//Construir entradas log
			InAgregarLog.Log headLog = new InAgregarLog.Log();
			headLog.IdEmpresa = in.p_IdEmpresa;
			headLog.IdModulo = in.p_IdModulo;
			headLog.IdUsuario = in.p_IdUsuario;
			headLog.IdTipoLog = res.Resultado.equals("ANU")? "ANULACION": "EDICION"; //si viene ANU es anulacion sino dejamos como estaba
			headLog.IdCabeceraLog = res.IdCargoAbonoMovimiento;
			headLog.FechaSistemaLog = in.p_FechaMovimiento;
			headLog.IdUsuarioAtendido = in.p_IdUsuario;
			headLog.IdTipoRegistro = res.IdTipoMovimiento != null ? res.IdTipoMovimiento : "CargoAbonoMovimiento";
			headLog.Folio = res.IdCargoAbonoMovimiento;
			headLog.IdCliente = res.IdCliente != null ? res.IdCliente : 0L;
			headLog.IdPortafolio= res.IdPortafolio != null ? res.IdPortafolio : 0L;
			headLog.Monto =res.Monto != null ? res.Monto : BigDecimal.ZERO;
			headLog.IdTipoOperacion=res.IdTipoOperacion != null ? res.IdTipoOperacion : "";
			
			entradaLog.p_Log.add(headLog);
	
			
			InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = res.IdCargoAbonoMovimiento;
			detLog.SubRegistro = res.IdTipoMovimiento != null ? res.IdTipoMovimiento : "CargoAbonoMovimiento";
			detLog.CampoRegistro = "IdEstado";
			detLog.ValorAntiguo = "CRT";
			detLog.ValorNuevo = "ANU";
			entradaLog.p_LogDetalle.add(detLog);
		}
		//guardar log.
		agregarLog.logica(conn, entradaLog);
		sql.exec(conn, SQL_DELETE_IN_CARGO_ABONO);
		log.info("FIN");
		return out;
	}

	private void ValicacionSegunMantenedorGeneral(Connection conn, SQL sql, List<ResultadoCumpleRequisitosCargoAgono> resultadoCumpleRequisitosCargoAbono) throws SQLException {
		List<resultadoParametrosMantenedorClass>resultadoParametrosMantenedor =  sql.query(conn, "SELECT RA.\"IdConcepto\", RA.\"RequiereAutorizacion\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.RequiereAutorizacion\" AS RA ",  new HashMap<String,Object>(), resultadoParametrosMantenedorClass.class);

		HashMap<String, Object> paramABonoCargo = new HashMap<String, Object>();
		resultadoParametrosMantenedor.forEach(element ->{
				if(element.IdConcepto.equals("ANUCAR")) {
					paramABonoCargo.put("ANUCAR",element.RequiereAutorizacion );
	
				}if(element.IdConcepto.equals("ANUABO")) {
					paramABonoCargo.put("ANUABO",element.RequiereAutorizacion );
	
				}

		});
		
		resultadoCumpleRequisitosCargoAbono.forEach(element ->{
			if("ANULACION_REQUIERE_AUTORIZACION".equals(element.Codigo)) {
				String valor ="";
				if("MOVCAR".equals(element.IdTipoMovimiento)){
					//consultar parametro de cargo
					 //si esta prendido se deja validacion de sql
						try {
							if(paramABonoCargo.containsKey("ANUCAR")) {
								valor = (String) paramABonoCargo.get("ANUCAR");
							}
							
					   }catch(Exception e) {
						    valor = "S";
					   }
					  //si esta apagado cambiamos RAUT a OK

					  if(!valor.equals("S")){
					  	element.Resultado = "OK";
					  } 
					  
				}else {
					//se asume que es abono -> misma logica
					try {
						if(paramABonoCargo.containsKey("ANUABO")) {
							valor = (String) paramABonoCargo.get("ANUABO");
						}
						
				   }catch(Exception e) {
					    valor = "S";
				   }
				  //si esta apagado cambiamos RAUT a OK

				  if(!valor.equals("S")){
				  	element.Resultado = "OK";  
				}
	
			}
			}
		});
	}
	
	public static class ObjInformacionDetalle {
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjAdvertenciasDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjErroresDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AnularCargoAbono_Entrada jin=gson.fromJson(in.toString() ,AnularCargoAbono_Entrada.class);
		AnularCargoAbono_Salida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return new JsonParser().parse(gson.toJson(out)).getAsJsonObject();
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AnularCargoAbono s = new AnularCargoAbono();
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CTA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2018-12-26\",\"p_CargoAbonoAAnular\":[{\"IdCargoAbonoMovimiento\":12679}],\"p_EsAnulacionManual\":\"S\"}";
			AnularCargoAbono_Entrada Objin = gson.fromJson(json, AnularCargoAbono_Entrada.class);
			AnularCargoAbono_Salida salida = s.logica(conn, Objin );
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			System.out.println(salida);
			conn.rollback();
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};
};