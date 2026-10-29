package cl.bcs.tesoreria;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEvento;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEtapa;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioEntrada;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioSalida;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjIDEVENTO;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceEntrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceSalida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.nor.AnularNominasTransferencias;
import cl.bcs.tesoreria.nor.AnularNominasTransferencias.EntradaAnularNominasTransferencias;
import cl.bcs.tesoreria.nor.AnularNominasTransferencias.RegistrosAnularNominasTransferencias;
import cl.bcs.tesoreria.nor.AnularNominasTransferencias.SalidaAnularNominasTransferencias;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularRegistros implements JavaCall{
	private final Logger log = LogManager.getLogger(AnularRegistros.class);
	
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #AnularRegistros_p_RegistrosAProcesar (\"Id\" BIGINT,\"Entidad\" NVARCHAR(300));";
	
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #AnularRegistros_p_RegistrosAProcesar (\"Id\",\"Entidad\") VALUES (:Id,:Entidad);";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #AnularRegistros_p_RegistrosAProcesar WHERE 1=1;";

	//SQL MIGRADOS
	private final String SQL_SELECT_VALIDAR_MOVIMIENTOS= loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectValidarMovimientos.sql");
	private final String SQL_SELECT_VALIDAR_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectValidarComprobante.sql");
	//private final String SQL_SELECT_EVENTOS_AUTORIZACION = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectEventosAutorizar.sql");
	private final String SQL_SELECT_EVENTOS_AUTORIZACION_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectEventosAutorizarMovimientos.sql");
	private final String SQL_SELECT_EVENTOS_AUTORIZACION_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectEventosAutorizarComprobantes.sql");	
	private final String SQL_UPDATE_COMPROBANTE_EVENTOS_AUTORIZACION = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateComprobanteEventosAutorizar.sql");
	private final String SQL_UPDATE_MOVIMIENTO_EVENTOS_AUTORIZACION = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateMovimientoEventosAutorizar.sql");	
	private final String SQL_UPDATE_ANULAR_MOV_INGRESO_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnulaMovimientosIngresoEgreso.sql");
	private final String SQL_UPDATE_ANULAR_FORMA_PAGO_MOVIMIENTO = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnulaFormaDePagoMovimientos.sql");
	private final String SQL_SELECT_LIQUIDACION_PENDIENTE_AUTORIZACION = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectComprobanteLiquidacionPendienteDeAutorizacion.sql");

	private final String SQL_CREATE_TEMP_TABLE_COMPROBANTES = "CREATE LOCAL TEMPORARY TABLE #AnularRegistros_ComprobantesAnular (\"IdComprobante\" BIGINT, \"IdIngresoEgresoOrigen\" BIGINT, \"IdComprobanteOrigen\"  BIGINT);";
	private final String SQL_INSERT_COMPROBANTES_ANULAR = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectComprobantesAnular.sql");
	private final String SQL_UPDATE_ANULAR_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnularComprobante.sql");
	private final String SQL_UPDATE_ANULAR_FORMA_PAGO_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnularFormasDePagoComprobante.sql");
	private final String SQL_UPDATE_ANULAR_DETALLE_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnularDetalleComprobate.sql");
	
	private final String SQL_UPDATE_LIBERAR_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateLiberarMovimientosOriginales.sql");
	private final String SQL_UPDATE_LIBERAR_FORMA_PAGO_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateLiberarFormaDePagoMovimientosOriginales.sql");
	
	private final String SQL_UPDATE_LIBERAR_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateLiberarComprobantesOriginales.sql");
	private final String SQL_UPDATE_LIBERAR_FORMA_PAGO_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateLiberarFormaDePagoComprobantesOriginales.sql");
	
	private final String SQL_UPDATE_ANULACION_DEPOSITOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_UpdateAnulacionDepositos.sql"); 
	
	private final String SQL_SELECT_NOMINAS_TRANSFERENCIA_ANULAR = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectNominasTransferenciaAnular.sql");  
	
	private final String SQL_SELECT_ANULAR_CARGO_ABONO = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_SelectAnularCargosAbono.sql"); 
	
	private final String SQL_SELECT_OBTENER_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularRegistros_ObtenerMovimientos.sql");  
	
	public static class AnularRegistros_ValidarIngresoEgreso{
		public Long IdIngresoEgreso;
		public String IdEstado;
		public String IdModulo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Registro_Entrada {
		public Long Id;
		public String Entidad;
		public Long getId() {
			return Id;
		}
		public String getEntidad() {
			return Entidad;
		}		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AnularRegistros_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion = "N";
		public String						p_UsaReglasNegocio;
		public String						p_EjecutaAutorizacion = "S";
		public String						p_FechaMovimiento;
		public List<Registro_Entrada>		p_RegistrosAProcesar;
				
		public AnularRegistros_Entrada (){			
			p_RegistrosAProcesar	= new ArrayList<Registro_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ResultadoProceso {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		public ResultadoProceso() {
			
		}
		public ResultadoProceso(String campo, Long id, String servicio, String resultado) {
			this.Campo = campo;
			this.Id = id;
			this.Servicio = servicio;
			this.Resultado = resultado;
		}
	}
	
	public static class AnularRegistros_Salida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public List<ResultadoProceso>		P_OUT_RESULTADOPROCESO;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public AnularRegistros_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<>();
			P_OUT_RESULTADOPROCESO		= new ArrayList<>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class AnularRegistros_TablaRequisitos{
		public Integer Prioridad;
		public Integer Cumple;
		public String CodigoError;
		public String CodigoDato;
		public String ValorDato;
		public String Entidad;
		public Long Id;
		public String RequiereAutorizacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Eventos {
		public Long		IdEvento;
		public Long		IdEtapa;
	};

	@Trace
	public AnularRegistros_Salida logica(Connection conn, AnularRegistros_Entrada in) throws Exception {
		log.info("INICIO ANULAR REGISTROS");
		AnularRegistros_Salida out = new AnularRegistros_Salida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql = new SQL();
	
		SQL.exec(conn, CREATE_TEMPORARY_TABLE);
		sql.batch(conn, SQL_INSERT_REGISTROS, in.p_RegistrosAProcesar, Registro_Entrada.class);
		
		Set<Long> registrosAProcesar = new HashSet<>();
		registrosAProcesar.addAll(in.p_RegistrosAProcesar.stream().map(Registro_Entrada::getId).collect(Collectors.toList()));
		
		//INICIO MIGRACION
		//Evaluar si usa reglas de negocios
		if(in.p_UsaReglasNegocio.equals("S")) {
			//validaciones datos de entrada.
			RevisarRequisitosBasicos revisarRequisitosBasicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaRequisitosBasicos = revisarRequisitosBasicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if(!salidaRequisitosBasicos.p_out_Mensajes.isEmpty()) {
				out.P_OUT_ERRORESCONTADOR = (long) salidaRequisitosBasicos.p_out_Mensajes.size();
				salidaRequisitosBasicos.p_out_Mensajes.forEach(msj -> {
					out.P_OUT_MENSAJES.add(new MensajeSalida(msj.id,msj.tipo,msj.codigo,msj.descripcion));
				});
				return out;
			}
			//validar si cumple requisitos para anular registros.
			List<MensajeSalida> salidaRequisitosAnularRegistros = cumpleRequisitosAnularRegistros(conn, in);
			if(!salidaRequisitosAnularRegistros.isEmpty()) {
				out.P_OUT_MENSAJES.addAll(salidaRequisitosAnularRegistros);
				Long errorContador = MensajeSalida.getNumeroErrores(salidaRequisitosAnularRegistros);
				if(errorContador > 0) {
					out.P_OUT_ERRORESCONTADOR = errorContador;
					out.P_OUT_MENSAJES = salidaRequisitosAnularRegistros.stream().filter(err -> err.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					return out;
				}
			}
			//eliminar registros con requisitos por cumplir.
			salidaRequisitosAnularRegistros.forEach(req -> {
				registrosAProcesar.remove(req.id);
			});
			
			/**
			 * CREAR EVENTOS AUTORIZACION MOVIMIENTOS
			 */			
			List<Long> registrosPorAutorizar = out.P_OUT_MENSAJES.stream().filter(msj -> msj.tipo.equals(MensajeSalida.MENSAJE_AUTORIZACION)).map(msj -> msj.id).collect(Collectors.toList());
			List<Long> registrosPorAutorizarMovimientos = in.p_RegistrosAProcesar.stream().filter(ent -> ent.Entidad.equals("TES.IngresoEgreso") && registrosPorAutorizar.contains(ent.Id)).map(ent -> ent.Id).collect(Collectors.toList());
			if(!registrosPorAutorizarMovimientos.isEmpty()) {
				CrearEventoAutorizacionMasivo eventoAutMasivo = new CrearEventoAutorizacionMasivo();
				ServiceEntrada entradaAutMasiva = new ServiceEntrada();
				entradaAutMasiva.P_IDEMPRESA = in.p_IdEmpresa;
				entradaAutMasiva.P_IDMODULO = in.p_IdModulo;
				entradaAutMasiva.P_SIMULAROPERACION = in.p_simularOperacion;
				entradaAutMasiva.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
				
				HashMap<String, Object> params = new HashMap<>();
				params.put("p_IdModulo", in.p_IdModulo);
				params.put("p_IdUsuario", in.p_IdUsuario);
				params.put("p_FechaMovimiento", in.p_FechaMovimiento);
				params.put("Id", registrosPorAutorizarMovimientos);
				
				entradaAutMasiva.P_TABLAEVENTOS = sql.query(conn, SQL_SELECT_EVENTOS_AUTORIZACION_MOVIMIENTOS, params, ObjTABLAEVENTOS.class);
				if(!entradaAutMasiva.P_TABLAEVENTOS.isEmpty()) {
					ServiceSalida salidaAutMasivo = eventoAutMasivo.logica(conn, entradaAutMasiva);
					if(salidaAutMasivo.P_OUT_ERRORESCONTADOR > 0) {
						out.P_OUT_ERRORESCONTADOR += salidaAutMasivo.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = obtenerMensajeErroresAutorizaciones(salidaAutMasivo.P_OUT_ERRORESDETALLE);
						return out;
					}
					sql.batch(conn, SQL_UPDATE_MOVIMIENTO_EVENTOS_AUTORIZACION, salidaAutMasivo.P_OUT_IDEVENTO, ObjIDEVENTO.class);
				}
			}
			/**
			 * CREAR EVENTOS AUTORIZACION COMPROBANTES
			 */
			List<Long> registrosPorAutorizarComprobantes = in.p_RegistrosAProcesar.stream().filter(ent -> ent.Entidad.equals("TES.Comprobante") && registrosPorAutorizar.contains(ent.Id)).map(ent -> ent.Id).collect(Collectors.toList());
			if(!registrosPorAutorizarComprobantes.isEmpty()) {
				CrearEventoAutorizacionMasivo eventoAutMasivo = new CrearEventoAutorizacionMasivo();
				ServiceEntrada entradaAutMasiva = new ServiceEntrada();
				entradaAutMasiva.P_IDEMPRESA = in.p_IdEmpresa;
				entradaAutMasiva.P_IDMODULO = in.p_IdModulo;
				entradaAutMasiva.P_SIMULAROPERACION = in.p_simularOperacion;
				entradaAutMasiva.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
				
				HashMap<String, Object> params = new HashMap<>();
				params.put("p_IdModulo", in.p_IdModulo);
				params.put("p_IdUsuario", in.p_IdUsuario);
				params.put("p_FechaMovimiento", in.p_FechaMovimiento);
				params.put("Id", registrosPorAutorizarComprobantes);
				
				entradaAutMasiva.P_TABLAEVENTOS = sql.query(conn, SQL_SELECT_EVENTOS_AUTORIZACION_COMPROBANTES, params, ObjTABLAEVENTOS.class);
				if(!entradaAutMasiva.P_TABLAEVENTOS.isEmpty()) {
					ServiceSalida salidaAutMasivo = eventoAutMasivo.logica(conn, entradaAutMasiva);
					if(salidaAutMasivo.P_OUT_ERRORESCONTADOR > 0) {
						out.P_OUT_ERRORESCONTADOR += salidaAutMasivo.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = obtenerMensajeErroresAutorizaciones(salidaAutMasivo.P_OUT_ERRORESDETALLE);
						return out;						
					}
					sql.batch(conn, SQL_UPDATE_COMPROBANTE_EVENTOS_AUTORIZACION, salidaAutMasivo.P_OUT_IDEVENTO, ObjIDEVENTO.class);
				}
			}
			SQL.exec(conn, SQL_DELETE_REGISTROS);
			sql.batch(conn, SQL_INSERT_REGISTROS, in.p_RegistrosAProcesar.stream().filter(reg-> registrosAProcesar.contains(reg.Id)).collect(Collectors.toList()), Registro_Entrada.class);
			
		}
		
		/**
		 * LOGICA
		 */
		MensajeSalida msj = null;
		if(!registrosAProcesar.isEmpty()) {
			log.debug("REGISTROS CUMPLEN REQUSITOS, ANULAR");
		
			/**
			* RECHAZA AUTORIZACIONES DE COPROBANTES DE LIQUIDACIONES EN ESTADO PDA
			*/
			HashMap<String, Object> IdsComprobante = new HashMap<String, Object>();
			List<Long> idComprobante = new ArrayList<Long>();
			for (Registro_Entrada registros : in.p_RegistrosAProcesar) {
				idComprobante.add(registros.Id);
			}
			IdsComprobante.put("IdsComprobante",new ArrayList<>(idComprobante));
			List<Eventos> _eventos = sql.query(conn, SQL_SELECT_LIQUIDACION_PENDIENTE_AUTORIZACION, IdsComprobante , Eventos.class);
			RechazarAutorizaciones(conn,_eventos, in);
			
			/**
			 * ANULACION DE COMPROBANTES
			 */
			SQL.exec(conn, SQL_CREATE_TEMP_TABLE_COMPROBANTES);
			sql.batch(conn, SQL_INSERT_COMPROBANTES_ANULAR, Arrays.asList(in),  AnularRegistros_Entrada.class);
			//Anula Comprobantes
			sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE,  Arrays.asList(in),  AnularRegistros_Entrada.class);
			// Anular las formas de pago de los comprobantes(si es que tiene)
			sql.batch(conn, SQL_UPDATE_ANULAR_FORMA_PAGO_COMPROBANTE, Arrays.asList(in),  AnularRegistros_Entrada.class);
			// Anular detalle de los comprobantes
			sql.batch(conn, SQL_UPDATE_ANULAR_DETALLE_COMPROBANTE, Arrays.asList(in), AnularRegistros_Entrada.class);
			if (!in.p_IdModulo.equals("TES")) {
				HashMap<String, Object> idComprobantes = new HashMap<String, Object>();
				for (Registro_Entrada registros : in.p_RegistrosAProcesar) {
					idComprobante.add(registros.Id);
				}
				idComprobantes.put("p_comprobante", idComprobante);
				List<Registro_Entrada> movimientos = sql.query(conn, SQL_SELECT_OBTENER_MOVIMIENTOS, idComprobantes, Registro_Entrada.class);
				
				System.out.println(movimientos);
				sql.batch(conn, SQL_INSERT_REGISTROS, movimientos, Registro_Entrada.class);
				
				for (Registro_Entrada movimientosEntrada : movimientos) {
					msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,"INGRESO_EGRESO_ANULADO", "Ingreso/Egreso " + movimientosEntrada.Id + " anulado");	

				}
			}else {
				//Liberar los movimientos originales(para comprobantes que se generaron de movimientos)
				sql.batch(conn, SQL_UPDATE_LIBERAR_MOVIMIENTOS, Arrays.asList(in), AnularRegistros_Entrada.class);
				//Liberar Las formas de Pago de los movimientos originales
				sql.batch(conn, SQL_UPDATE_LIBERAR_FORMA_PAGO_MOVIMIENTOS, Arrays.asList(in), AnularRegistros_Entrada.class);
			}
			
			//Libera los comprobantes originales(para comprobantes que se generaron de comprobantes)
			sql.batch(conn, SQL_UPDATE_LIBERAR_COMPROBANTES, Arrays.asList(in), AnularRegistros_Entrada.class);
			//Libera las formas de pago de los comprobantes originales
			sql.batch(conn, SQL_UPDATE_LIBERAR_FORMA_PAGO_COMPROBANTES, Arrays.asList(in), AnularRegistros_Entrada.class);
			
			/**
			 * ANULACION DE MOVIMIENTOS
			 */
			//Anula Movimientos de Ingreso o Egreso
			sql.batch(conn, SQL_UPDATE_ANULAR_MOV_INGRESO_EGRESO, Arrays.asList(in), AnularRegistros_Entrada.class);
			
			//Anula la Forma de Pago de los movimientos anulados
			sql.batch(conn, SQL_UPDATE_ANULAR_FORMA_PAGO_MOVIMIENTO, Arrays.asList(in), AnularRegistros_Entrada.class);
			
			
			/**
			 * ANULALACION DE DEPOSITOS (si es que existen) 
			 */
			sql.batch(conn, SQL_UPDATE_ANULACION_DEPOSITOS, Arrays.asList(in), AnularRegistros_Entrada.class);
			
			List<AnularRegistros_Entrada> nominasTransferenciaAnular = sql.query(conn, SQL_SELECT_NOMINAS_TRANSFERENCIA_ANULAR, new HashMap<String,Object>(), AnularRegistros_Entrada.class);
			if(!nominasTransferenciaAnular.isEmpty()) {
				AnularNominasTransferencias anularNominasTransferencia = new AnularNominasTransferencias();
				EntradaAnularNominasTransferencias entradaAnularNominaTransferencia = new EntradaAnularNominasTransferencias();
				entradaAnularNominaTransferencia.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularNominaTransferencia.p_IdModulo = in.p_IdModulo;
				entradaAnularNominaTransferencia.p_IdUsuario = in.p_IdUsuario;
				entradaAnularNominaTransferencia.p_FechaMovimiento = in.p_FechaMovimiento;
				entradaAnularNominaTransferencia.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				entradaAnularNominaTransferencia.p_RegistrosAAnular = in.p_RegistrosAProcesar.stream().map(reg -> {
					return new RegistrosAnularNominasTransferencias(reg.Id, reg.Entidad);
				}).collect(Collectors.toList());
				SalidaAnularNominasTransferencias salidaAnularNominaTransferencia = anularNominasTransferencia.logica(conn, entradaAnularNominaTransferencia);
				out.P_OUT_RESULTADOPROCESO.addAll(salidaAnularNominaTransferencia.p_out_ResultadoProceso.stream().map(res -> {
					return new ResultadoProceso(res.Campo, res.Id, res.Servicio, res.Resultado);
				}).collect(Collectors.toList()));
			}
			
			/**
			 * REVERSA CUENTA INVERSION
			 */
			//Anulación de movimiento de cargo/Abono cuenta inversión
			List<CargoAbono_Entrada> cargosAbonoAnular = sql.query(conn, SQL_SELECT_ANULAR_CARGO_ABONO, new HashMap<>(), CargoAbono_Entrada.class);
			if(!cargosAbonoAnular.isEmpty()) {
				AnularCargoAbono_Entrada entradaAnularCargoAbono = new AnularCargoAbono_Entrada();
				entradaAnularCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaAnularCargoAbono.p_IdModulo = in.p_IdModulo;
				entradaAnularCargoAbono.p_FechaMovimiento = in.p_FechaMovimiento;
				entradaAnularCargoAbono.p_simularOperacion = "N";
				entradaAnularCargoAbono.p_UsaReglasNegocio = "S";
				entradaAnularCargoAbono.p_EjecutaAutorizacion = "N";
				entradaAnularCargoAbono.p_OmiteRegistroSinAutorizar = "N";
				entradaAnularCargoAbono.p_EsAnulacionManual = "N";
				entradaAnularCargoAbono.p_CargoAbonoAAnular = cargosAbonoAnular;
				AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
				AnularCargoAbono_Salida salidaAnularCargoAbono = anularCargoAbono.logica(conn, entradaAnularCargoAbono);
				out.P_OUT_MENSAJES.addAll(salidaAnularCargoAbono.P_OUT_MENSAJES);
				out.P_OUT_ERRORESCONTADOR+= salidaAnularCargoAbono.P_OUT_ERRORESCONTADOR;
			}
			
		}
		
		//construir salida
		HashMap<String, Object> registros = Utils.List2Hash(in.p_RegistrosAProcesar, "Id");
		for(Long id : registrosAProcesar) {
			Registro_Entrada reg = (Registro_Entrada) registros.get(id);
			if(reg!=null) {
				ResultadoProceso res = new ResultadoProceso();
				res.Campo = reg.Entidad.equals("TES.IngresoEgreso") ? "IdIngresoEgreso" : "IdComprobante";
				res.Id = id;
				res.Servicio = "BSCL.Tesoreria.BD.Servicios::AnularRegistros";
				res.Resultado = "ANU";
				out.P_OUT_RESULTADOPROCESO.add(res);
			}
		}

		//Construccion mensajes de salida.
		for (ResultadoProceso res : out.P_OUT_RESULTADOPROCESO) {
			

			if (res.Resultado.equalsIgnoreCase("ANU") && res.Campo.equalsIgnoreCase("IdComprobante")) {
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,"COMPROBANTE_ANULADO", "Comprobante " + res.Id + " anulado");	
			}
			else if (res.Resultado.equalsIgnoreCase("RECH") && res.Campo.equalsIgnoreCase("IdComprobante")) {
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"COMPROBANTE_RECHAZADO", "Comprobante " + res.Id + " rechazado");	
			}
			else if (res.Resultado.equalsIgnoreCase("ANU") && res.Campo.equalsIgnoreCase("IdIngresoEgreso")) {
				String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_ANULADO")
						 .add("IdMovimientoComprobante", res.Id)
						 .msg();
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,"UI_MOVIMIENTO_COMPROBANTE_ANULADO", descripcion);	
			}
			else if (res.Resultado.equalsIgnoreCase("RECH") && res.Campo.equalsIgnoreCase("IdIngresoEgreso")) {
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"INGRESO_EGRESO_RECHAZADO", "Ingreso/Egreso " + res.Id + " rechazado");	
			}
			else if (res.Resultado.equalsIgnoreCase("ANU") && res.Campo.equalsIgnoreCase("IdNominaTransferencia")) {
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK,"NOMINA_TRANSFERENCIA_ANULADO", "Nomina Transferencia " + res.Id + " anulado");	
			}
			else if (res.Resultado.equalsIgnoreCase("RECH") && res.Campo.equalsIgnoreCase("IdNominaTransferencia")) {
				msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NOMINA_TRANSFERENCIA_RECHAZADO", "Nomina Transferencia " + res.Id + " rechazado");	
			}
			
			out.P_OUT_MENSAJES.add(msj);
		}
		
		try {
			SQL.exec(conn, "DROP TABLE #AnularRegistros_p_RegistrosAProcesar");
			SQL.exec(conn, "DROP TABLE #AnularRegistros_ComprobantesAnular");
		} catch (Exception er) {
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		log.info("FIN ANULAR REGISTROS");
		return out;
	}
	

	private List<MensajeSalida> cumpleRequisitosAnularRegistros(Connection conn, AnularRegistros_Entrada in) throws Exception{
		List<MensajeSalida> out = new ArrayList<>();
		SQL sql = new SQL();
		/**
		 * VALIDACIONES BASICAS (MOVIMIENTOS Y COMPROBANTES)
		 */
		//validar que la fecha de sistema sea valida.
		try {
			LocalDate.parse(in.p_FechaMovimiento, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(DateTimeParseException ex) {
			log.debug(ex);
			out.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_SISTEMA_INVALIDA", new HashMap<String,String>(), conn, in.p_IdEmpresa));
		}
		//valida que venga id de movimiento comprobante
		if(in.p_RegistrosAProcesar.isEmpty() ) {
			out.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS", new HashMap<String,String>(), conn, in.p_IdEmpresa));
		}
		//valida que el movimiento o comprobante sea valido
		in.p_RegistrosAProcesar.stream().filter(reg -> Optional.ofNullable(reg.Id).orElse(0L).equals(0L)).forEach(reg->{
			String codigoError = (reg.Entidad.equals("TES.IngresoEgreso")) ? "MOVIMIENTO_TESORERIA_INVALIDO" : "COMPROBANTE_TESORERIA_IVALIDO";
			String codigoDato =  (reg.Entidad.equals("TES.IngresoEgreso")) ? "IdIngresoEgreso": "IdComprobante";
			
			HashMap<String, String> param = new HashMap<>();
			param.put(codigoDato, String.valueOf(reg.Id));
			try {
				out.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoError, param, conn, in.p_IdEmpresa));
			} catch (Exception e) {
				log.debug(e);
			}
		});
		List<String> entidadesValidas = Arrays.asList("TES.IngresoEgreso", "TES.Comprobante");
		//valida que la entidad ingresada sea valida
		in.p_RegistrosAProcesar.stream().filter(reg -> !entidadesValidas.contains(reg.Entidad) ).forEach(reg->{
			try {
				out.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_TESORERIA_INVALIDA",  new HashMap<String,String>(), conn, in.p_IdEmpresa));
			} catch (Exception e) {
				log.debug(e);
			}
		});
		
		/**
		 * VALIDACIONES DE NEGOCIO (PARA MOVIMIENTOS)
		 */
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdModulo", in.p_IdModulo);
		List<AnularRegistros_TablaRequisitos> requisitosMovimientos = sql.query(conn, SQL_SELECT_VALIDAR_MOVIMIENTOS, params, AnularRegistros_TablaRequisitos.class);
		requisitosMovimientos.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			String tipo = (req.RequiereAutorizacion.equals("S")) ? MensajeSalida.MENSAJE_AUTORIZACION: MensajeSalida.MENSAJE_ERROR;
			HashMap<String, String> param = new HashMap<>();
			param.put(req.CodigoDato, req.ValorDato);
			try {
				out.add(new MensajeSalida(req.Id,tipo, req.CodigoError,param,conn,in.p_IdEmpresa));
			} catch (Exception e) {
				log.debug(e);
			}
		});
		
		/**
		 * VALIDACIONES DE NEGOCIO (PARA COMPROBANTES)
		 */
		params.clear();
	
		params.put("p_IdModulo", in.p_IdModulo);
		List<AnularRegistros_TablaRequisitos> requisitosComprobantes= sql.query(conn, SQL_SELECT_VALIDAR_COMPROBANTE, params, AnularRegistros_TablaRequisitos.class);
		requisitosComprobantes.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			String tipo = (req.RequiereAutorizacion.equals("S")) ? MensajeSalida.MENSAJE_AUTORIZACION: MensajeSalida.MENSAJE_ERROR;
			HashMap<String, String> param = new HashMap<>();
			param.put(req.CodigoDato, req.ValorDato);
			try {
				out.add(new MensajeSalida(req.Id,tipo, req.CodigoError,param,conn,in.p_IdEmpresa));
			} catch (Exception e) {
				log.debug(e);
			}
		});

		return out;
	}
	
	private ServicioSalida RechazarAutorizaciones(Connection conn,List<Eventos> ids, AnularRegistros_Entrada in){
		ServicioEntrada entrada = new ServicioEntrada();
		entrada.p_IdEmpresa = in.p_IdEmpresa;
		entrada.p_IdModulo = "AUT";
		entrada.p_IdUsuario = in.p_IdUsuario;
		entrada.p_SimularOperacion = "N";
		entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		entrada.p_IdEstado = "RZD";
		entrada.p_EventoEtapa = "ETAPA";
		entrada.p_Observaciones = " ";
		
		List<ServicioEntradaEtapa> etapas = new ArrayList<>();
		ids.forEach(e -> {
			ServicioEntradaEtapa etapa = new ServicioEntradaEtapa();
			etapa.IdEvento = e.IdEvento;
			etapa.IdEtapa = e.IdEtapa;
			etapas.add(etapa);
		});
		entrada.p_Etapas = etapas;
		
		List<ServicioEntradaEvento> eventos = new ArrayList<>();
		ServicioEntradaEvento e = new ServicioEntradaEvento();
		e.IdEvento = 0L;
		eventos.add(e);
		entrada.p_Eventos = eventos;
		
		ModificarEstadoEventoAutorizacionMasivo autorizacionMasivo = new ModificarEstadoEventoAutorizacionMasivo();
		ServicioSalida salida = new ServicioSalida();
		try {
			salida = autorizacionMasivo.logica(conn,entrada);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return salida;
	}

	private List<MensajeSalida> obtenerMensajeErroresAutorizaciones(List<ObjERRORESDETALLE> errores){
		log.trace("INICIO::AnularRegistro.obtenerMensajeErroresAutorizaciones");
		List<MensajeSalida> mensajes = errores.parallelStream().map(error->{
			MensajeSalida mje = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion);
			return mje;
		}).collect(Collectors.toList());
		log.trace("FIN::AnularRegistro.obtenerMensajeErroresAutorizaciones");
		return mensajes;
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
		Gson gson = getGsonBuilder();		
		AnularRegistros_Entrada jin=gson.fromJson(in.toString() ,AnularRegistros_Entrada.class);
		AnularRegistros_Salida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AnularRegistros s = new AnularRegistros();
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-08-19\",\"p_RegistrosAProcesar\":[{\"Id\":18971,\"Entidad\":\"TES.IngresoEgreso\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1644,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2021-04-29\",\"p_RegistrosAProcesar\":[{\"Id\":35,\"Entidad\":\"TES.IngresoEgreso\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2019-12-18\",\"p_RegistrosAProcesar\":[{\"Id\":5528,\"Entidad\":\"TES.IngresoEgreso\"}]}";
			//String jsonComp = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":54,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2019-06-04\",\"p_RegistrosAProcesar\":[{\"Id\":5925,\"Entidad\":\"TES.Comprobante\"}]}";
			AnularRegistros_Entrada Objin = gson.fromJson(json, AnularRegistros_Entrada.class);
			AnularRegistros_Salida salida = s.logica(conn, Objin );
			System.out.println(salida);
			conn.rollback();
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};
};