package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjEstadoOperacionOUT;
import cl.bcs.mesa.MRF.AnularOperacionMesa.EntidadTesoreria_Proceso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReversarAnticipoOperacionMesaRF implements JavaCall {

	private final Log log = new Log("MRF.ReversarAnticipoOperacionMesaRF");

	private final String SQL_SELECT_DETALLE_TESORERIA = loadResourceToString("cl/bcs/mesa/sql/ReversarAnticipoOperacionMesaRF_SelectDetalleTesoreria.sql");
	private final String SQL_SELECT_NUEVO_ESTADO = loadResourceToString("cl/bcs/mesa/sql/ReversarAnticipoOperacionMesaRF_SelectNuevoEstado.sql");
	private final String SQL_SELECT_CARGO_ABONO = loadResourceToString("cl/bcs/mesa/sql/ReversarAnticipoOperacionMesaRF_SelectCargoAbonoAnular.sql");
	private final String SQL_INSERT_IDS_OPERACION = loadResourceToString("cl/bcs/mesa/sql/ReversarAnticipoOperacionMesaRF_InsertIdsOperacion.sql");
	private final String SQL_SELECT_DETALLE_OPERACION = loadResourceToString("cl/bcs/mesa/sql/ReversarAnticipoOperacionMesaRF_SelectDetalleOperacion.sql");
	
	public static class DetalleOperacion {
		public Long 				IdOperacion;
		public Long 				FolioOperacion;
		public java.sql.Timestamp 	FechaOperacion;
		public String 				IdTipoProducto;
		public String 				IdTipoOperacion;
		public String 				NombreOperacion;
		public Long 				IdOperacionRelacionada;
		public String 				Intermesa;
		public Long 				IdMesaOrigen;
		public String 				NombreMesaOrigen;
		public Long 				IdMesaDestino;
		public String 				NombreMesaDestino;
		public Long 				IdCliente;
		public Long 				IdPortafolio;
		public String 				CodigoPortafolio;
		public Long 				IdClienteCorredor;
		public Long 				IdPortafolioCorredor;
		public String 				RazonSocial;
		public BigDecimal 			MontoOperacion;
		public BigDecimal 			MontoOperacionInicial;
		public BigDecimal 			MontoOperacionFinal;
		public String 				IdTipoReajuste;
		public String 				IdMoneda;
		public BigDecimal 			Tasa;
		public BigDecimal 			MontoOperacionMoneda;
		public BigDecimal 			MontoOperacionInicialMoneda;
		public BigDecimal 			MontoOperacionFinalMoneda;
		public String 				IdMonedaLiquidacion;
		public Long 				PlazoEnDias;
		public java.sql.Timestamp 	FechaVencimiento;
		public java.sql.Timestamp 	FechaLiquidacion;
		public String 				IdCondicionLiquidacion;
		public java.sql.Timestamp 	FechaCondicionLiquidacion;
		public Long 				IdAgente;
		public BigDecimal 			TasaAgente;
		public String 				IdEstado;
		public String 				NombreEstado;
		public java.sql.Timestamp 	FechaCreacion;
		public Long 				IdUsuarioCreacion;
		public String 				CodigoUsuario;
		public String 				Glosa;
		public Long 				IdEmpresa;
		public String 				DocumentoIdentidad;
		public String 				IdTipoCanalIngreso;
		public String 				AfectaCustodia;
		public BigDecimal 			MontoOperacionAnticipo;
		public BigDecimal 			MontoOperacionInicialAnticipo;
		public BigDecimal 			MontoOperacionFinalAnticipo;
		public BigDecimal 			TasaAnticipo;
		public BigDecimal 			MontoOperacionMonedaAnticipo;
		public BigDecimal 			MontoOperacionInicialMonedaAnticipo;
		public BigDecimal 			MontoOperacionFinalMonedaAnticipo;
		public String 				IdMonedaLiquidacionAnticipo;
		public String 				IdCondicionLiquidacionAnticipo;
		public Date 				FechaCondicionLiquidacionAnticipo;
		public String 				FechaVencimientoUI;
		public String 				TipoDocumento;
		public String 				ConfirmaAdemdum;
		public Long 				IdUsuarioConfirma;
		public String 				IdTipoInstrumento;
		public Long 				IdOperacionOrigenReasignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjReversarAnticipoOperacionMesaRFIn {
		public Long IdOperacion;
		public String IdEstadoSiguiente;
	}

	public static class ReversarAnticipoOperacionMesaRFIn {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public Long p_IdPais;
		public List<ObjReversarAnticipoOperacionMesaRFIn> p_Operaciones;
	}

	public static class ReversarAnticipoOperacionMesaRFOut {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ObjEstadoOperacionOUT> P_OUT_OPERACIONES;

		public ReversarAnticipoOperacionMesaRFOut() {
			this.P_OUT_OPERACIONES = new ArrayList<ObjEstadoOperacionOUT>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TempOperacion {
		public Long IdOperacion;

		TempOperacion(Long IdOperacion) {
			this.IdOperacion = IdOperacion;
		}
	}

	public ReversarAnticipoOperacionMesaRFOut logica(Connection conn, ReversarAnticipoOperacionMesaRFIn in) throws Exception {
		log.write("INICIO::MRF.ReversarAnticipoOperacionMesaRF");
		SQL _sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		HashMap<String, String> mapMsg = new HashMap<>();
		ReversarAnticipoOperacionMesaRFOut out = new ReversarAnticipoOperacionMesaRFOut();
		Long Id = 0L;
		
		List<TempOperacion> IdsOperaciones = in.p_Operaciones.stream().map(o -> new TempOperacion(o.IdOperacion)).collect(Collectors.toList());
		_sql.batch(conn, SQL_INSERT_IDS_OPERACION, IdsOperaciones, TempOperacion.class);
		
		List<DetalleOperacion> listaDetalleOperacion = _sql.query(conn, SQL_SELECT_DETALLE_OPERACION, new HashMap<>(1), DetalleOperacion.class);
		
		for (DetalleOperacion elem : listaDetalleOperacion) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date fechaSistemaDate = dateFormat.parse(fechaSistema);
			java.sql.Timestamp fechaSistemaTimestamp = new java.sql.Timestamp(fechaSistemaDate.getTime());
			java.sql.Timestamp fechaVencimientoTimestamp = elem.FechaVencimiento;
			
			if (fechaVencimientoTimestamp.before(fechaSistemaTimestamp) || fechaVencimientoTimestamp.equals(fechaSistemaTimestamp)) {
				mapMsg.put("Id", Long.toString(elem.IdOperacion));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_OPERACION_VENCIDA", mapMsg, conn, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
			}
			
			String fechaLiquidacionS = elem.FechaLiquidacion.toString().substring(0, 10);
			
			if (!fechaLiquidacionS.equals(fechaSistema)) {
				mapMsg.put("Id", Long.toString(elem.IdOperacion));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_FECHA_ANTICIPADA_NO_VALIDA", mapMsg, conn, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
			}
		}
		
		List<EntidadTesoreria_Proceso> entidadesTesoreria = _sql.query(conn, SQL_SELECT_DETALLE_TESORERIA, new HashMap<>(1), EntidadTesoreria_Proceso.class);

		for (EntidadTesoreria_Proceso ent : entidadesTesoreria) {
			if (ent.IdEstado.equalsIgnoreCase("LIQ") || "LIP".equals(ent.IdEstado)) {
				mapMsg.put("Id", Long.toString(ent.Id));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "COMPROBANTE_TESORERIA_LIQUIDADO", mapMsg, conn, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
			}
			if ("EMI".equals(ent.IdEstado)) {
				mapMsg.put("Id", Long.toString(ent.Id));
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "MOVIMIENTOS_TESORERIA_EMITIDOS", mapMsg, conn, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = 1L;
			}
		}

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		if (entidadesTesoreria.size() > 0) {
			AnularRegistros_Entrada entradaAnularTesoreria = new AnularRegistros_Entrada();
			entradaAnularTesoreria.p_EjecutaAutorizacion = "N";
			entradaAnularTesoreria.p_FechaMovimiento = fechaSistema;
			entradaAnularTesoreria.p_IdEmpresa = in.p_IdEmpresa;
			entradaAnularTesoreria.p_IdModulo = in.p_IdModulo;
			entradaAnularTesoreria.p_IdUsuario = in.p_IdUsuario;
			List<Registro_Entrada> lista = new ArrayList<>();
			for (EntidadTesoreria_Proceso ent : entidadesTesoreria) {
				Registro_Entrada arg0 = new Registro_Entrada();
				arg0.Entidad = ent.Entidad;
				arg0.Id = ent.Id;
				lista.add(arg0);
			}
			entradaAnularTesoreria.p_RegistrosAProcesar = lista;
			entradaAnularTesoreria.p_simularOperacion = "N";
			entradaAnularTesoreria.p_UsaReglasNegocio = "S";

			AnularRegistros anularTesoreriaService = new AnularRegistros();
			AnularRegistros_Salida salidaAnularTesoreria = anularTesoreriaService.logica(conn, entradaAnularTesoreria);

			out.P_OUT_MENSAJES.addAll(salidaAnularTesoreria.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
		}
		
		List<CargoAbono_Entrada> cargoAbono = _sql.query(conn, SQL_SELECT_CARGO_ABONO, new HashMap<>(1), CargoAbono_Entrada.class);

		if (cargoAbono.size() > 0) {
			AnularCargoAbono_Entrada anularCargoAbono_Entrada = new AnularCargoAbono_Entrada();
			anularCargoAbono_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			anularCargoAbono_Entrada.p_IdUsuario = in.p_IdUsuario;
			anularCargoAbono_Entrada.p_IdModulo = in.p_IdModulo;
			anularCargoAbono_Entrada.p_simularOperacion = "N";
			anularCargoAbono_Entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			anularCargoAbono_Entrada.p_FechaMovimiento = fechaSistema;
			anularCargoAbono_Entrada.p_CargoAbonoAAnular = cargoAbono;

			AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
			AnularCargoAbono_Salida anularCargoAbonoSalida = anularCargoAbono.logica(conn, anularCargoAbono_Entrada);
			out.P_OUT_MENSAJES.addAll(anularCargoAbonoSalida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR += anularCargoAbonoSalida.P_OUT_ERRORESCONTADOR;
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
		}

		CambiarEstadoOperacionEntrada cambiarEstadoOperacionEntrada = new CambiarEstadoOperacionEntrada(
					in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_simularOperacion, in.p_UsaReglasNegocio,
					in.p_IdPais);
		
		cambiarEstadoOperacionEntrada.p_DataCambiarEstado = new ArrayList<>(_sql.query(conn, SQL_SELECT_NUEVO_ESTADO, new HashMap<>(1), ObjCambiarEstadoOperacionIN.class));
		
		CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
		CambiarEstadoOperacionSalida cambiarEstadoOperacionSalida = cambiarEstadoOperacion.logica(conn, cambiarEstadoOperacionEntrada);
		out.P_OUT_MENSAJES.addAll(cambiarEstadoOperacionSalida.P_OUT_MENSAJES);
		out.P_OUT_OPERACIONES.addAll(cambiarEstadoOperacionSalida.P_OUT_OPERACION);
		out.P_OUT_ERRORESCONTADOR += cambiarEstadoOperacionSalida.P_OUT_ERRORESCONTADOR;
		
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		ReversarAnticipoOperacionMesaRFIn jin = gson.fromJson(in.toString(), ReversarAnticipoOperacionMesaRFIn.class);
		ReversarAnticipoOperacionMesaRFOut out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_Operaciones\":[{\"IdOperacion\":150632,\"IdEstadoSiguiente\":\"VIG\"}]}";
			ReversarAnticipoOperacionMesaRFIn datos = gson.fromJson(json, ReversarAnticipoOperacionMesaRFIn.class);
			ReversarAnticipoOperacionMesaRF prueba = new ReversarAnticipoOperacionMesaRF();
			ReversarAnticipoOperacionMesaRFOut resp = prueba.logica(conn, datos);
			System.out.println(resp);
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");
	}

}
