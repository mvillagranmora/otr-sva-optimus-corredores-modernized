package cl.bcs.tesoreria.nor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularNominasTransferencias implements JavaCall{
	
	private Logger log = LogManager.getLogger(AnularNominasTransferencias.class);
	
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #AnularNominasTransferencias_p_RegistrosAProcesar (\"Id\" BIGINT,\"Entidad\" NVARCHAR(300))";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #AnularNominasTransferencias_p_RegistrosAProcesar (\"Id\",\"Entidad\") VALUES (:Id,:Entidad)";
	private static final String DROP_TEMPORARY_TABLE = "DROP TABLE #AnularNominasTransferencias_p_RegistrosAProcesar";
	private final String SQL_UPDATE_ANULAR_COMPROBANTE_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobanteFormaPago.sql");
	private final String SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantePartidaEgreso.sql");
	private final String SQL_UPDATE_ANULAR_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantes.sql");
	private final String SQL_UPDATE_ANULAR_NOMINA_DETALLES = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularNominaDetalles.sql");
	private final String SQL_UPDATE_ANULAR_NOMINA = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularNomina.sql");
	private final String SQL_UPDATE_ANULAR_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobanteDetalle.sql");
	private final String SQL_SELECT_ANULAR_CARGO_ABONO = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_SelectAnularCargosAbono.sql"); 
	private final String SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantePartidaEgresoDetalle.sql");
	private final String SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO_FORMAPAGO = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_AnularComprobantePartidaEgresoFormaPago.sql");
	private final String SQL_UPDATE_LIBERAR_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_UpdateLiberarMovimientosOriginales.sql");
	private final String SQL_UPDATE_LIBERAR_FORMA_PAGO_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_UpdateLiberarFormaDePagoMovimientosOriginales.sql");

	//SQL MIGRADOS	
	private final String SQL_SELECT_REQUISITOS_ANULAR_NOMINAS_TRANSFERENCIAS = loadResourceToString("cl/bcs/tesoreria/sql/AnularNominasTransferencias_SelectRequisitos.sql");;
	
	public static class RegistrosAnularNominasTransferencias extends BcsPojo {
	    public Long Id;
	    public String Entidad;

	    public RegistrosAnularNominasTransferencias(Long Id, String Entidad) {
	        this.Id = Id;
	        this.Entidad = Entidad;
	    }
	}

	
	public static class EntradaAnularNominasTransferencias{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public List<RegistrosAnularNominasTransferencias> p_RegistrosAAnular;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ResultadoAnularNominasTransferencias{
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
	}
	
	public static class SalidaAnularNominasTransferencias extends SalidaServicios{
		public List<ResultadoAnularNominasTransferencias> p_out_ResultadoProceso;
		public SalidaAnularNominasTransferencias() {
			super();
			this.p_out_ResultadoProceso = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TablaRequisitosAnularNominasTransferencias{
		public Integer Prioridad;
		public Integer Cumple;
		public String CodigoError;
		public String CodigoDato;
		public String ValorDato;
		public String Entidad;
		public Long Id;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Trace
	public SalidaAnularNominasTransferencias logica(Connection conn, EntradaAnularNominasTransferencias in) {
		log.info("INICIO SERVICIO");
		SalidaAnularNominasTransferencias out = new SalidaAnularNominasTransferencias();
		List<MensajeSalida> mensajesSalida = new ArrayList<>();
		SQL sql = new SQL();
		try {
			if(in.p_RegistrosAAnular.isEmpty()) {
				mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS", "No vienen movimientos"));
				out.parseMensajeSalida(mensajesSalida);
				return out;
			}
			
			SQL.exec(conn, CREATE_TEMPORARY_TABLE);
			sql.batch(conn, SQL_INSERT_REGISTROS, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);

				Set<Long> movimientosProcesar = new HashSet<>();
				in.p_RegistrosAAnular.forEach(reg -> {
					movimientosProcesar.add(reg.Id);
				});
				if(in.p_UsaReglasNegocio.equals("S")) {
					RevisarRequisitosBasicos requisitosBasicos = new RevisarRequisitosBasicos();
					
						//revisar requisitos basicos
						RevisarRequisitosBasicosSalida salidaRequisitosBasicos = requisitosBasicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
						mensajesSalida.addAll(salidaRequisitosBasicos.p_out_Mensajes);
						mensajesSalida.stream().filter(msj -> msj.tipo.equals(MensajeSalida.MENSAJE_ERROR)).forEach(msj -> {
							out.addError(msj.codigo, msj.descripcion);
						});
						
						//revisar requisitos anular nomina transferencia
						HashMap<String, Object> params = new HashMap<>();
						List<TablaRequisitosAnularNominasTransferencias> requisitos = sql.query(conn, SQL_SELECT_REQUISITOS_ANULAR_NOMINAS_TRANSFERENCIAS, params, TablaRequisitosAnularNominasTransferencias.class);
						
					
				}
				
				if(!movimientosProcesar.isEmpty()) {
					log.debug("HAY MOVIMIENTOS, CONTINUAR");
					
				    sql.batch(conn, SQL_UPDATE_ANULAR_NOMINA, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
				    sql.batch(conn, SQL_UPDATE_ANULAR_NOMINA_DETALLES, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO_DETALLE, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE_PARTIDA_EGRESO_FORMAPAGO, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTES, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE_DETALLE, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_ANULAR_COMPROBANTE_FORMA_PAGO, in.p_RegistrosAAnular, RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_LIBERAR_MOVIMIENTOS, in.p_RegistrosAAnular,  RegistrosAnularNominasTransferencias.class);
					sql.batch(conn, SQL_UPDATE_LIBERAR_FORMA_PAGO_MOVIMIENTOS, in.p_RegistrosAAnular,  RegistrosAnularNominasTransferencias.class);
					
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
						out.P_OUT_ERRORESCONTADOR+= salidaAnularCargoAbono.P_OUT_ERRORESCONTADOR;
					}

				}else {
					log.debug("NO HAY MOVIMIENTOS, TERMINAR");
				}	


			SQL.exec(conn, DROP_TEMPORARY_TABLE);
			System.out.println(out);
		} catch (Exception e) {
			log.debug(e);
			e.printStackTrace();
		}
		log.info("TERMINO SERVICIO");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAnularNominasTransferencias in = gson.fromJson(jin.toString(), EntradaAnularNominasTransferencias.class);
		SalidaAnularNominasTransferencias out = logica(conn, in);
		return new JsonParser().parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			AnularNominasTransferencias s = new AnularNominasTransferencias();
			Gson gson = Utils.getGsonBuilder();
			String jsonComp = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2024-12-26\",\"p_RegistrosAAnular\":[{\"Id\":39,\"Entidad\":\"TES.NominaTransferencia\"}]}";
			EntradaAnularNominasTransferencias Objin = gson.fromJson(jsonComp, EntradaAnularNominasTransferencias.class);
			SalidaAnularNominasTransferencias salida = s.logica(conn, Objin );
			System.out.println(salida);
			conn.rollback();
			//conn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
